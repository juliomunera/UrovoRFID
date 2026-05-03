package com.ubx.rfid.p006ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ubx.rfid.R;
import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.ErrorDao;
import com.ubx.rfid.db.SettingsDao;

/**
 * Pantalla de Configuración de la app.
 * Permite al usuario modificar los parámetros almacenados en la tabla Settings.
 */
public class SettingsFragment extends Fragment {

    private EditText etRetentionDays, etApiUrl, etDeviceId, etSyncInterval;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_app, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etRetentionDays = view.findViewById(R.id.et_retention_days);
        etApiUrl        = view.findViewById(R.id.et_api_url);
        etDeviceId      = view.findViewById(R.id.et_device_id);
        etSyncInterval  = view.findViewById(R.id.et_sync_interval);
        Button btnSave  = view.findViewById(R.id.btn_save_settings);

        loadSettings();

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        new Thread(() -> {
            try {
                AppDatabase db  = AppDatabase.getInstance(requireContext());
                SettingsDao dao = new SettingsDao(db);

                String retDays  = dao.get(SettingsDao.KEY_RETENTION_DAYS,    "8");
                String apiUrl   = dao.get(SettingsDao.KEY_API_URL,           "");
                String deviceId = dao.get(SettingsDao.KEY_DEVICE_ID,         "DT50-001");
                String interval = dao.get(SettingsDao.KEY_SYNC_INTERVAL_MIN, "5");

                requireActivity().runOnUiThread(() -> {
                    etRetentionDays.setText(retDays);
                    etApiUrl.setText(apiUrl);
                    etDeviceId.setText(deviceId);
                    etSyncInterval.setText(interval);
                });
            } catch (Exception e) {
                ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                        "SettingsFragment.loadSettings", e);
            }
        }).start();
    }

    private void saveSettings() {
        String retDays  = etRetentionDays.getText().toString().trim();
        String apiUrl   = etApiUrl.getText().toString().trim();
        String deviceId = etDeviceId.getText().toString().trim();
        String interval = etSyncInterval.getText().toString().trim();

        // Validaciones básicas
        try { Integer.parseInt(retDays); } catch (NumberFormatException e) {
            etRetentionDays.setError(getString(R.string.settings_invalid_number));
            return;
        }
        try { Integer.parseInt(interval); } catch (NumberFormatException e) {
            etSyncInterval.setError(getString(R.string.settings_invalid_number));
            return;
        }

        new Thread(() -> {
            try {
                AppDatabase db  = AppDatabase.getInstance(requireContext());
                SettingsDao dao = new SettingsDao(db);
                dao.set(SettingsDao.KEY_RETENTION_DAYS,    retDays);
                dao.set(SettingsDao.KEY_API_URL,           apiUrl);
                dao.set(SettingsDao.KEY_DEVICE_ID,         deviceId);
                dao.set(SettingsDao.KEY_SYNC_INTERVAL_MIN, interval);

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                R.string.settings_saved, Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                ErrorDao.logError(AppDatabase.getInstance(requireContext()),
                        "SettingsFragment.saveSettings", e);
            }
        }).start();
    }
}
