package com.ubx.rfid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ubx.rfid.model.InventaryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Inventary.
 * Almacena el catálogo local de TAGs conocidos (TagId + Description).
 */
public class InventaryDao {

    private static final String TABLE = "Inventary";
    private final AppDatabase dbHelper;

    public InventaryDao(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Inserta o reemplaza un ítem del inventario. */
    public long insertOrReplace(String tagId, String description) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("tagId",       tagId);
            cv.put("description", description);
            return db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "InventaryDao.insertOrReplace", e);
            return -1;
        }
    }

    /** Retorna todos los ítems del inventario. */
    public List<InventaryModel> getAll() {
        List<InventaryModel> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"id", "tagId", "description"},
                    null, null, null, null, "tagId ASC");
            while (c.moveToNext()) {
                list.add(new InventaryModel(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2)
                ));
            }
            c.close();
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "InventaryDao.getAll", e);
        }
        return list;
    }

    /** Busca un ítem por TagId. Retorna null si no existe. */
    public InventaryModel findByTagId(String tagId) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE,
                    new String[]{"id", "tagId", "description"},
                    "tagId = ?", new String[]{tagId},
                    null, null, null);
            if (c.moveToFirst()) {
                InventaryModel m = new InventaryModel(c.getLong(0), c.getString(1), c.getString(2));
                c.close();
                return m;
            }
            c.close();
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "InventaryDao.findByTagId", e);
        }
        return null;
    }

    /** Elimina un ítem por id. */
    public int delete(long id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.delete(TABLE, "id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            ErrorDao.logError(dbHelper, "InventaryDao.delete", e);
            return 0;
        }
    }
}
