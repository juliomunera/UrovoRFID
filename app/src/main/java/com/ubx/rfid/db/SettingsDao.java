package com.ubx.rfid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DAO para la tabla Settings.
 * Almacena configuración de la app como pares clave/valor.
 *
 * Claves predefinidas:
 *  - retention_days    : días de retención de TAGs (default: 8)
 *  - api_url           : URL de la API de sincronización
 *  - device_id         : identificador del dispositivo
 *  - sync_interval_min : intervalo de sincronización en minutos (default: 5)
 */
public class SettingsDao {

    private static final String TABLE = "Settings";

    // Claves de configuración
    public static final String KEY_RETENTION_DAYS    = "retention_days";
    public static final String KEY_API_URL           = "api_url";
    public static final String KEY_DEVICE_ID         = "device_id";
    public static final String KEY_SYNC_INTERVAL_MIN = "sync_interval_min";

    private final AppDatabase dbHelper;

    public SettingsDao(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Obtiene el valor de una clave. Retorna {@code defaultValue} si no existe. */
    public String get(String key, String defaultValue) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"value"},
                    "key = ?", new String[]{key},
                    null, null, null);
            if (c.moveToFirst()) {
                String val = c.getString(0);
                c.close();
                return val;
            }
            c.close();
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "SettingsDao.get", e);
        }
        return defaultValue;
    }

    /** Obtiene el valor como entero. */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Guarda o actualiza un valor. */
    public void set(String key, String value) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("key",   key);
            cv.put("value", value);
            db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "SettingsDao.set", e);
        }
    }

    /** Guarda un valor entero. */
    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }
}
