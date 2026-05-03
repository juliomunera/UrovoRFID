package com.ubx.rfid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ubx.rfid.model.TagReadModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DAO para la tabla TagRead.
 *
 * Operaciones:
 *  - insert()          : guarda un TAG leído
 *  - getPending()      : obtiene TAGs no sincronizados
 *  - markSynced()      : marca TAGs como enviados
 *  - deleteOlderThan() : elimina registros más antiguos que N días
 */
public class TagReadDao {

    private static final String TAG   = "TagReadDao";
    private static final String TABLE = "TagRead";

    private final AppDatabase dbHelper;

    public TagReadDao(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    /**
     * Inserta un TAG leído en la base de datos.
     *
     * @param tagId EPC del TAG
     * @return id del registro insertado, o -1 si falló
     */
    public long insert(String tagId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            ContentValues cv = new ContentValues();
            cv.put("tagId",    tagId);
            cv.put("readDate", now);
            cv.put("synced",   0);
            long id = db.insert(TABLE, null, cv);
            Log.d(TAG, "TAG insertado id=" + id + " epc=" + tagId);
            return id;
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.insert", e);
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // QUERY
    // -------------------------------------------------------------------------

    /**
     * Retorna todos los TAGs pendientes de sincronización (synced = 0).
     */
    public List<TagReadModel> getPending() {
        List<TagReadModel> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"id", "tagId", "readDate"},
                    "synced = 0", null, null, null,
                    "readDate ASC");
            while (c.moveToNext()) {
                list.add(new TagReadModel(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2)
                ));
            }
            c.close();
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.getPending", e);
        }
        return list;
    }

    /**
     * Retorna todos los TAGs (para la pantalla de Sincronizar).
     */
    public List<TagReadModel> getAll() {
        List<TagReadModel> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"id", "tagId", "readDate", "synced"},
                    null, null, null, null,
                    "readDate DESC");
            while (c.moveToNext()) {
                TagReadModel m = new TagReadModel(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2)
                );
                m.setSynced(c.getInt(3) == 1);
                list.add(m);
            }
            c.close();
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.getAll", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Marca una lista de IDs como sincronizados (synced = 1).
     *
     * @param ids lista de IDs a marcar
     */
    public void markSynced(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                cv.put("synced", 1);
                for (Long id : ids) {
                    db.update(TABLE, cv, "id = ?", new String[]{String.valueOf(id)});
                }
                db.setTransactionSuccessful();
                Log.d(TAG, "Marcados como sincronizados: " + ids.size() + " registros");
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.markSynced", e);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Elimina registros más antiguos que {@code days} días.
     *
     * @param days número de días de retención
     * @return número de registros eliminados
     */
    public int deleteOlderThan(int days) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // SQLite: date('now', '-N days') devuelve la fecha límite
            int deleted = db.delete(TABLE,
                    "readDate < datetime('now', '-" + days + " days')",
                    null);
            Log.d(TAG, "Eliminados " + deleted + " registros con más de " + days + " días");
            return deleted;
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.deleteOlderThan", e);
            return 0;
        }
    }

    /** Cuenta los TAGs pendientes de sincronización. */
    public int countPending() {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE + " WHERE synced=0", null);
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            return count;
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "TagReadDao.countPending", e);
            return 0;
        }
    }
}
