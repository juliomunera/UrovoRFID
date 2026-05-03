package com.ubx.rfid.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper SQLite de la app RFID.
 *
 * Tablas:
 *  - TagRead   : TAGs leídos por el lector (con estado de sincronización)
 *  - Inventary : Inventario local de TAGs conocidos (TagId + Description)
 *  - Settings  : Configuración de la app (clave/valor)
 *  - Errors    : Log de excepciones y errores de la app
 */
public class AppDatabase extends SQLiteOpenHelper {

    private static final String TAG     = "AppDatabase";
    private static final String DB_NAME = "rfid_app.db";
    private static final int    DB_VER  = 1;

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context ctx) {
        if (instance == null) {
            instance = new AppDatabase(ctx.getApplicationContext());
        }
        return instance;
    }

    private AppDatabase(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    // -------------------------------------------------------------------------
    // Creación de tablas
    // -------------------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {
        // --- TagRead ---
        // synced: 0=pendiente, 1=enviado a la API
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS TagRead (" +
            "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  tagId    TEXT    NOT NULL," +
            "  readDate TEXT    NOT NULL," +   // formato: YYYY-MM-DD HH:mm:ss
            "  synced   INTEGER NOT NULL DEFAULT 0" +
            ");"
        );

        // --- Inventary ---
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Inventary (" +
            "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  tagId       TEXT    NOT NULL UNIQUE," +
            "  description TEXT" +
            ");"
        );

        // --- Settings ---
        // Valores por defecto insertados al crear la tabla
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Settings (" +
            "  key   TEXT PRIMARY KEY," +
            "  value TEXT NOT NULL" +
            ");"
        );
        db.execSQL("INSERT OR IGNORE INTO Settings(key,value) VALUES('retention_days','8');");
        db.execSQL("INSERT OR IGNORE INTO Settings(key,value) VALUES('api_url','https://api.example.com/tags');");
        db.execSQL("INSERT OR IGNORE INTO Settings(key,value) VALUES('device_id','DT50-001');");
        db.execSQL("INSERT OR IGNORE INTO Settings(key,value) VALUES('sync_interval_min','5');");

        // --- Errors ---
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Errors (" +
            "  id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  errorDate TEXT    NOT NULL," +
            "  source    TEXT," +
            "  message   TEXT    NOT NULL," +
            "  stackTrace TEXT" +
            ");"
        );

        Log.d(TAG, "Base de datos creada correctamente");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migraciones futuras aquí
        Log.d(TAG, "onUpgrade: " + oldVersion + " → " + newVersion);
    }
}
