package com.ubx.rfid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ubx.rfid.model.ErrorModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DAO para la tabla Errors.
 * Registra todas las excepciones y errores que ocurren en la app.
 *
 * Uso:
 *   ErrorDao.logError(db, "MiClase.miMetodo", exception);
 *   ErrorDao.logError(db, "MiClase", "Mensaje de error sin excepción");
 */
public class ErrorDao {

    private static final String TAG   = "ErrorDao";
    private static final String TABLE = "Errors";

    private final AppDatabase dbHelper;

    public ErrorDao(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // Métodos estáticos de conveniencia (para usar desde cualquier clase)
    // -------------------------------------------------------------------------

    /**
     * Registra una excepción en la tabla Errors.
     *
     * @param db     instancia de AppDatabase
     * @param source nombre de la clase/método donde ocurrió el error
     * @param e      excepción capturada
     */
    public static void logError(AppDatabase db, String source, Exception e) {
        try {
            String stackTrace = getStackTrace(e);
            insert(db, source, e.getMessage() != null ? e.getMessage() : e.getClass().getName(), stackTrace);
        } catch (Exception ignored) {
            // No propagar errores del logger
            Log.e(TAG, "Error al registrar error: " + ignored.getMessage());
        }
    }

    /**
     * Registra un mensaje de error sin excepción.
     *
     * @param db      instancia de AppDatabase
     * @param source  nombre de la clase/método
     * @param message descripción del error
     */
    public static void logError(AppDatabase db, String source, String message) {
        try {
            insert(db, source, message, null);
        } catch (Exception ignored) {
            Log.e(TAG, "Error al registrar error: " + ignored.getMessage());
        }
    }

    private static void insert(AppDatabase db, String source, String message, String stackTrace) {
        try {
            SQLiteDatabase sqlite = db.getWritableDatabase();
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            ContentValues cv = new ContentValues();
            cv.put("errorDate",  now);
            cv.put("source",     source);
            cv.put("message",    message);
            cv.put("stackTrace", stackTrace);
            sqlite.insert(TABLE, null, cv);
            Log.e(TAG, "[" + source + "] " + message);
        } catch (Exception e) {
            Log.e(TAG, "No se pudo insertar error en BD: " + e.getMessage());
        }
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    /** Retorna los últimos {@code limit} errores registrados. */
    public List<ErrorModel> getRecent(int limit) {
        List<ErrorModel> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"id", "errorDate", "source", "message"},
                    null, null, null, null,
                    "errorDate DESC",
                    String.valueOf(limit));
            while (c.moveToNext()) {
                list.add(new ErrorModel(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3)
                ));
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "getRecent error: " + e.getMessage());
        }
        return list;
    }

    /** Elimina errores más antiguos que {@code days} días. */
    public int deleteOlderThan(int days) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.delete(TABLE,
                    "errorDate < datetime('now', '-" + days + " days')",
                    null);
        } catch (Exception e) {
            Log.e(TAG, "deleteOlderThan error: " + e.getMessage());
            return 0;
        }
    }
}
