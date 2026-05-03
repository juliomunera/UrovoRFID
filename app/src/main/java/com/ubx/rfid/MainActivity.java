package com.ubx.rfid;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ubx.rfid.p006ui.scan.ScanFragment;
import com.ubx.rfid.util.sharedPreference.PreKey;
import com.ubx.rfid.util.sharedPreference.SPUtils;
import com.ubx.usdk.rfid.RfidManager;

/**
 * Activity principal de la app RFID Urovo DT50.
 *
 * Responsabilidades:
 * - Conectar al servicio RFID del sistema (com.ubx.usdk.rfid)
 * - Gestionar el ciclo de vida del RfidManager
 * - Capturar el trigger físico (keyCode 523) y delegarlo al ScanFragment
 * - Exponer el RfidManager a los Fragments via getter
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /** KeyCode del trigger físico del Urovo DT50 */
    private static final int KEYCODE_TRIGGER = 523;

    private RfidManager mRfidManager;
    private MainViewModel mainViewModel;
    private long lastBackPress = 0;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Inicializar y conectar el RfidManager
        initRfidManager();

        // Cargar el ScanFragment como pantalla principal
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ScanFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRfidManager != null) {
            mRfidManager.release();
        }
    }

    // -------------------------------------------------------------------------
    // RFID Manager
    // -------------------------------------------------------------------------

    private void initRfidManager() {
        mRfidManager = new RfidManager(this);
        mRfidManager.bindService(new RfidManager.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Servicio RFID conectado");
                connectReader();
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Servicio RFID desconectado");
                mainViewModel.setConnected(false);
            }
        });
    }

    private void connectReader() {
        // Intentar conectar al lector físico
        if (mRfidManager.isConnected()) {
            // Ya estaba conectado
            byte id = mRfidManager.getReadId();
            mainViewModel.setReadId(id);
            mainViewModel.setConnected(true);
            Log.d(TAG, "Lector ya conectado, readId=" + (id & 0xFF));
        } else {
            // Conectar por puerto serie (valores por defecto del Urovo DT50)
            String port     = SPUtils.getInstance().getString(PreKey.SERIAL.name(), "/dev/ttyHSL0");
            int    baudrate = SPUtils.getInstance().getInt(PreKey.SERIAL_PORT.name(), 115200);

            boolean ok = mRfidManager.connectCom(port, baudrate);
            Log.d(TAG, "connectCom(" + port + ", " + baudrate + ") = " + ok);

            if (ok) {
                byte id = mRfidManager.getReadId();
                mainViewModel.setReadId(id);
                mainViewModel.setConnected(true);
                Log.d(TAG, "Lector conectado, readId=" + (id & 0xFF));
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, R.string.rfid_connect_failed, Toast.LENGTH_LONG).show()
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // Trigger físico
    // -------------------------------------------------------------------------

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KEYCODE_TRIGGER
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            Log.d(TAG, "Trigger físico presionado");
            ScanFragment scanFragment = getScanFragment();
            if (scanFragment != null) {
                scanFragment.onTriggerKey();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // -------------------------------------------------------------------------
    // Back press (doble toque para salir)
    // -------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBackPress > 2000) {
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
            lastBackPress = now;
        } else {
            finish();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Expone el RfidManager a los Fragments. */
    public RfidManager getRfidManager() {
        return mRfidManager;
    }

    private ScanFragment getScanFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (f instanceof ScanFragment) ? (ScanFragment) f : null;
    }
}
