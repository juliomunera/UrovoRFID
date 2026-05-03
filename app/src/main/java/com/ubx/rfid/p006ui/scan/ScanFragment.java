package com.ubx.rfid.p006ui.scan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ubx.rfid.MainViewModel;
import com.ubx.rfid.R;
import com.ubx.rfid.adapter.ScanAdapter;
import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.ErrorDao;
import com.ubx.rfid.db.InventaryDao;
import com.ubx.rfid.db.TagReadDao;
import com.ubx.rfid.model.InventaryModel;
import com.ubx.rfid.util.BeepManager;
import com.ubx.rfid.util.sharedPreference.PreKey;
import com.ubx.rfid.util.sharedPreference.SPUtils;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.rfid.aidl.RfidDate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragmento principal de escaneo de TAGs RFID a larga distancia.
 *
 * Flujo:
 * 1. Se conecta al RfidManager (servicio del sistema Urovo)
 * 2. Al pulsar "Iniciar" (o el trigger físico), llama a customizedSessionTargetInventory()
 * 3. Los TAGs detectados llegan por onInventoryTag() y se muestran en la lista
 * 4. Al finalizar cada ciclo (onInventoryTagEnd), relanza el inventario automáticamente
 */
public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment";

    // Mensajes del Handler
    private static final int MSG_NOTIFY_ITEM = 1;
    private static final int MSG_TIMER       = 3;

    // Parámetros de inventario (configurables desde Settings)
    private byte session = 1;   // S1 por defecto
    private byte state   = 0;   // Target A
    private byte repeat  = 1;   // 1 repetición por ciclo

    // Estado
    private boolean inventoryFlag = false;
    private int totalCount = 0;
    private int totalTime  = 0;
    private byte readId    = (byte) -1;

    // Datos
    private final List<ScanModel> mData = new ArrayList<>();
    private final Map<String, Integer> deduplicationMap = new HashMap<>();

    // UI
    private RecyclerView recyclerView;
    private ScanAdapter mAdapter;
    private Button btnInventory;
    private TextView tvLabelCount, tvTotalCount, tvSpeed, tvTime;

    // ViewModels
    private ScanViewModel scanViewModel;
    private MainViewModel mainViewModel;

    // RFID
    private RfidManager mRfidManager;
    private final RFIDCallback mRfidCallback = new RFIDCallback(this);

    // Handler (UI thread)
    private final ScanHandler mHandler = new ScanHandler(this);

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModels
        scanViewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // UI
        recyclerView  = view.findViewById(R.id.recycler_scan);
        btnInventory  = view.findViewById(R.id.btn_inventory);
        tvLabelCount  = view.findViewById(R.id.tv_label_count);
        tvTotalCount  = view.findViewById(R.id.tv_total_count);
        tvSpeed       = view.findViewById(R.id.tv_speed);
        tvTime        = view.findViewById(R.id.tv_time);

        // RecyclerView
        mAdapter = new ScanAdapter(requireContext(), mData, position -> {
            // Selección de item
            for (int i = 0; i < mData.size(); i++) {
                mData.get(i).setSelected(i == position);
            }
            mAdapter.notifyDataSetChanged();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(mAdapter);

        // Botón de inventario
        btnInventory.setOnClickListener(v -> toggleInventory());

        // Observar estado de conexión RFID
        mainViewModel.isConnected().observe(getViewLifecycleOwner(), connected -> {
            if (connected) {
                readId = mainViewModel.getReadId().getValue();
                mRfidManager = ((com.ubx.rfid.MainActivity) requireActivity()).getRfidManager();
                if (mRfidManager != null) {
                    mRfidManager.registerCallback(mRfidCallback);
                }
                Log.d(TAG, "RFID conectado, readId=" + (readId & 0xFF));
            }
        });

        // Observar LiveData del ViewModel
        scanViewModel.getLabelCount().observe(getViewLifecycleOwner(),
                n -> tvLabelCount.setText(String.valueOf(n)));
        scanViewModel.getTotalCount().observe(getViewLifecycleOwner(),
                n -> tvTotalCount.setText(String.valueOf(n)));
        scanViewModel.geScanSpeed().observe(getViewLifecycleOwner(),
                n -> tvSpeed.setText(n + " tags/s"));
        scanViewModel.geTotalTime().observe(getViewLifecycleOwner(),
                n -> tvTime.setText(formatTime(n)));

        // Cargar configuración guardada
        loadSettings();

        // Inicializar el beep
        BeepManager.init(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        BeepManager.init(requireContext());
        if (mRfidManager != null) {
            mRfidManager.registerCallback(mRfidCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopInventory();
        BeepManager.release();
        if (mRfidManager != null) {
            mRfidManager.unregisterCallback(mRfidCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }

    // -------------------------------------------------------------------------
    // Inventario
    // -------------------------------------------------------------------------

    /**
     * Alterna entre iniciar y detener el inventario.
     * También se llama desde el trigger físico (keyCode 523).
     */
    public void toggleInventory() {
        if (inventoryFlag) {
            stopInventory();
        } else {
            startInventory();
        }
    }

    private void startInventory() {
        if (mRfidManager == null || !mRfidManager.isConnected()) {
            Toast.makeText(requireContext(), R.string.rfid_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        inventoryFlag = true;
        totalCount = 0;
        totalTime  = 0;
        mData.clear();
        deduplicationMap.clear();
        mAdapter.notifyDataSetChanged();
        scanViewModel.setLabelCount(0);
        scanViewModel.setTotalCount(0);
        scanViewModel.setTotalTime(0);
        scanViewModel.setScanSpeed(0);

        // Botón → verde (escaneando)
        btnInventory.setText(R.string.scan_stop_inventory);
        btnInventory.setActivated(true);

        // Iniciar timer de 1 segundo
        mHandler.removeMessages(MSG_TIMER);
        mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000L);

        // Lanzar inventario
        mRfidManager.customizedSessionTargetInventory(readId, session, state, repeat);
        Log.d(TAG, "Inventario iniciado: session=" + session + " state=" + state + " repeat=" + repeat);
    }

    private void stopInventory() {
        inventoryFlag = false;
        mHandler.removeMessages(MSG_TIMER);
        // Botón → azul (detenido)
        btnInventory.setText(R.string.scan_start_inventory);
        btnInventory.setActivated(false);
        Log.d(TAG, "Inventario detenido");
    }

    // -------------------------------------------------------------------------
    // Procesamiento de TAGs
    // -------------------------------------------------------------------------

    /**
     * Procesa un TAG recibido del callback y lo agrega a la lista (con deduplicación).
     * Consulta la tabla Inventary para mostrar la descripción si el TAG está catalogado.
     */
    private void processTag(ScanModel model) {
        totalCount++;
        scanViewModel.setTotalCount(totalCount);

        String epc = model.getEpc();
        if (deduplicationMap.containsKey(epc)) {
            // TAG ya conocido: solo actualizar RSSI y contador (sin beep)
            int pos = deduplicationMap.get(epc);
            ScanModel existing = mData.get(pos);
            existing.setRssi(model.getRssi());
            existing.setCount(existing.getCount() + 1);
            mAdapter.notifyItemChanged(pos);
        } else {
            // TAG nuevo: consultar inventario en hilo de fondo, luego actualizar UI
            deduplicationMap.put(epc, mData.size());
            mData.add(model);
            int insertedPos = mData.size() - 1;
            scanViewModel.setLabelCount(mData.size());
            mAdapter.notifyItemInserted(insertedPos);
            BeepManager.beep();

            // Consultar inventario y guardar en BD en hilo de fondo
            new Thread(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext());

                    // 1. Buscar descripción en el inventario local
                    InventaryModel inv = new InventaryDao(db).findByTagId(epc);
                    if (inv != null && inv.getDescription() != null
                            && !inv.getDescription().isEmpty()) {
                        // Actualizar el displayText en el modelo y refrescar el item en UI
                        requireActivity().runOnUiThread(() -> {
                            model.setDisplayText(inv.getDescription());
                            mAdapter.notifyItemChanged(insertedPos);
                        });
                    }

                    // 2. Guardar lectura en TagRead
                    new TagReadDao(db).insert(epc);

                } catch (Exception e) {
                    ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                            "ScanFragment.processTag", e);
                }
            }).start();
        }
    }

    // -------------------------------------------------------------------------
    // Handler (UI thread)
    // -------------------------------------------------------------------------

    private static class ScanHandler extends Handler {
        private final WeakReference<ScanFragment> ref;

        ScanHandler(ScanFragment fragment) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ScanFragment f = ref.get();
            if (f == null) return;

            if (msg.what == MSG_NOTIFY_ITEM) {
                f.processTag((ScanModel) msg.obj);

            } else if (msg.what == MSG_TIMER) {
                f.totalTime++;
                f.scanViewModel.setTotalTime(f.totalTime);
                if (f.totalTime > 0) {
                    f.scanViewModel.setScanSpeed(f.totalCount / f.totalTime);
                }
                f.mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000L);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Callback RFID (hilo Binder → Handler → UI)
    // -------------------------------------------------------------------------

    private static class RFIDCallback extends IRfidCallback.Stub {
        private final WeakReference<ScanFragment> ref;

        RFIDCallback(ScanFragment fragment) {
            ref = new WeakReference<>(fragment);
        }

        @Override
        public void onInventoryTag(byte readId, String pc, String crc, String epc,
                byte ant, String rssiRaw, String freq, int count, int total, String extra)
                throws RemoteException {
            ScanFragment f = ref.get();
            if (f == null || !f.inventoryFlag) return;

            // Convertir RSSI raw a dBm (valor - 129)
            String rssiStr;
            try {
                rssiStr = (Integer.parseInt(rssiRaw) - 129) + "dBm";
            } catch (NumberFormatException e) {
                rssiStr = rssiRaw;
            }

            ScanModel model = new ScanModel(epc, 1, rssiStr, pc, crc, false);
            Message msg = f.mHandler.obtainMessage(MSG_NOTIFY_ITEM, model);
            f.mHandler.sendMessage(msg);
        }

        @Override
        public void onInventoryTagEnd(int total, int reads, int timeMs, int speed, byte readId)
                throws RemoteException {
            ScanFragment f = ref.get();
            if (f == null || !f.inventoryFlag) return;
            // Relanzar inventario automáticamente
            f.mRfidManager.customizedSessionTargetInventory(f.readId, f.session, f.state, f.repeat);
        }

        @Override
        public void onOperationTag(String pc, String crc, String epc, String data,
                int result, byte errorCode, byte readId) throws RemoteException {
            ScanFragment f = ref.get();
            if (f == null || !f.inventoryFlag) return;
            ScanModel model = new ScanModel(epc, 1, "", pc, crc, false);
            Message msg = f.mHandler.obtainMessage(MSG_NOTIFY_ITEM, model);
            f.mHandler.sendMessage(msg);
        }

        @Override
        public void onOperationTagEnd(int result) throws RemoteException {
            ScanFragment f = ref.get();
            if (f == null || !f.inventoryFlag) return;
            f.mRfidManager.customizedSessionTargetInventory(f.readId, f.session, f.state, f.repeat);
        }

        @Override
        public void refreshSetting(RfidDate rfidDate) throws RemoteException {}

        @Override
        public void onExeCMDStatus(byte cmd, byte status) throws RemoteException {
            Log.d(TAG, "onExeCMDStatus cmd=0x" + Integer.toHexString(cmd & 0xFF)
                    + " status=0x" + Integer.toHexString(status & 0xFF));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void loadSettings() {
        session = (byte) SPUtils.getInstance().getInt(PreKey.SESSION.name(), 1);
        state   = (byte) SPUtils.getInstance().getInt(PreKey.STATE.name(), 0);
        repeat  = (byte) SPUtils.getInstance().getInt(PreKey.REPEAT_ONE.name(), 1);
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    /** Llamado desde MainActivity cuando se presiona el trigger físico (keyCode 523). */
    public void onTriggerKey() {
        toggleInventory();
    }
}
