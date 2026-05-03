package com.ubx.rfid;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.SettingsDao;
import com.ubx.rfid.sync.SyncWorker;
import com.ubx.rfid.util.sharedPreference.SPUtils;

import java.util.concurrent.TimeUnit;

/**
 * Application class.
 * Inicializa la base de datos SQLite y programa el Worker de sincronización.
 */
public class RFIDApplication extends Application {

    private static final String TAG          = "RFIDApplication";
    private static final String SYNC_WORK_ID = "rfid_sync_worker";

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // SharedPreferences
        SPUtils.init(this);

        // Base de datos SQLite (crea las tablas si no existen)
        AppDatabase db = AppDatabase.getInstance(this);
        Log.d(TAG, "Base de datos inicializada: " + db.getDatabaseName());

        // Programar el Worker de sincronización
        scheduleSyncWorker(db);
    }

    /**
     * Programa el Worker de sincronización periódica.
     * El intervalo se lee de la tabla Settings (sync_interval_min, default: 5).
     * WorkManager garantiza que se ejecute aunque la app esté en background.
     */
    private void scheduleSyncWorker(AppDatabase db) {
        try {
            SettingsDao settingsDao   = new SettingsDao(db);
            int         intervalMin   = settingsDao.getInt(SettingsDao.KEY_SYNC_INTERVAL_MIN, 5);
            // WorkManager requiere mínimo 15 minutos para trabajos periódicos en producción.
            // Para desarrollo/pruebas usamos el mínimo permitido (15 min).
            // En producción, usar el valor de Settings.
            long effectiveInterval = Math.max(intervalMin, 15);

            PeriodicWorkRequest syncRequest =
                    new PeriodicWorkRequest.Builder(SyncWorker.class,
                            effectiveInterval, TimeUnit.MINUTES)
                            .build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    SYNC_WORK_ID,
                    ExistingPeriodicWorkPolicy.KEEP,  // no reemplazar si ya existe
                    syncRequest
            );

            Log.d(TAG, "SyncWorker programado cada " + effectiveInterval + " minutos");
        } catch (Exception e) {
            Log.e(TAG, "Error al programar SyncWorker: " + e.getMessage());
        }
    }

    public static Context getContext() {
        return mContext;
    }
}
