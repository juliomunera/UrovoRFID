package com.ubx.rfid.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ubx.rfid.db.AppDatabase;
import com.ubx.rfid.db.ErrorDao;
import com.ubx.rfid.db.SettingsDao;
import com.ubx.rfid.db.TagReadDao;
import com.ubx.rfid.model.TagReadModel;

import java.util.ArrayList;
import java.util.List;

// import okhttp3.MediaType;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.RequestBody;
// import okhttp3.Response;

/**
 * Worker de sincronización de TAGs leídos hacia la API central.
 *
 * Se ejecuta cada 5 minutos (configurable en Settings.sync_interval_min).
 * Programado desde RFIDApplication al iniciar la app.
 *
 * Flujo:
 *  1. Obtiene TAGs pendientes (synced = 0) de la tabla TagRead
 *  2. Construye el JSON: [{"date":"...","TagId":"...","deviceId":"..."}]
 *  3. [COMENTADO] Envía el JSON a la API via HTTP POST
 *  4. Si el envío es exitoso, marca los TAGs como sincronizados (synced = 1)
 *  5. Elimina registros más antiguos que retention_days
 */
public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker iniciado");
        AppDatabase db         = AppDatabase.getInstance(getApplicationContext());
        TagReadDao  tagReadDao = new TagReadDao(db);
        SettingsDao settingsDao = new SettingsDao(db);

        try {
            // 1. Limpiar registros viejos
            int retentionDays = settingsDao.getInt(SettingsDao.KEY_RETENTION_DAYS, 8);
            tagReadDao.deleteOlderThan(retentionDays);

            // 2. Obtener TAGs pendientes
            List<TagReadModel> pending = tagReadDao.getPending();
            if (pending.isEmpty()) {
                Log.d(TAG, "No hay TAGs pendientes de sincronización");
                return Result.success();
            }

            Log.d(TAG, "TAGs pendientes: " + pending.size());

            // 3. Construir JSON
            String deviceId = settingsDao.get(SettingsDao.KEY_DEVICE_ID, "DT50-001");
            String json     = buildJson(pending, deviceId);
            Log.d(TAG, "JSON a enviar: " + json);

            // 4. Enviar a la API
            // ---------------------------------------------------------------
            // LÍNEA COMENTADA: descomentar cuando la API esté disponible
            // ---------------------------------------------------------------
            // String apiUrl = settingsDao.get(SettingsDao.KEY_API_URL, "");
            // boolean sent  = sendToApi(apiUrl, json);
            // ---------------------------------------------------------------

            // Simulación: asumir envío exitoso (quitar cuando se active la API)
            boolean sent = true; // ← REMOVER cuando se active el envío real

            // 5. Marcar como sincronizados si el envío fue exitoso
            if (sent) {
                List<Long> ids = new ArrayList<>();
                for (TagReadModel t : pending) ids.add(t.getId());
                tagReadDao.markSynced(ids);
                Log.d(TAG, "Sincronizados " + ids.size() + " TAGs");
            }

            return Result.success();

        } catch (Exception e) {
            ErrorDao.logError(db, "SyncWorker.doWork", e);
            return Result.retry();
        }
    }

    // -------------------------------------------------------------------------
    // Construcción del JSON
    // -------------------------------------------------------------------------

    /**
     * Construye el JSON de envío a la API.
     * Formato: [{"date":"YYYY-MM-DD HH:mm:ss","TagId":"xxx","deviceId":"yyy"}]
     */
    private String buildJson(List<TagReadModel> tags, String deviceId) {
        JsonArray array = new JsonArray();
        for (TagReadModel tag : tags) {
            JsonObject obj = new JsonObject();
            obj.addProperty("date",     tag.getReadDate());
            obj.addProperty("TagId",    tag.getTagId());
            obj.addProperty("deviceId", deviceId);
            array.add(obj);
        }
        return new Gson().toJson(array);
    }

    // -------------------------------------------------------------------------
    // Envío HTTP (COMENTADO — activar cuando la API esté disponible)
    // -------------------------------------------------------------------------

    /*
    private static final MediaType JSON_TYPE =
            MediaType.parse("application/json; charset=utf-8");

    private boolean sendToApi(String url, String json) {
        if (url == null || url.isEmpty()) {
            Log.w(TAG, "URL de API no configurada");
            return false;
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            RequestBody body = RequestBody.create(json, JSON_TYPE);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                boolean ok = response.isSuccessful();
                Log.d(TAG, "API response: " + response.code() + " ok=" + ok);
                return ok;
            }
        } catch (Exception e) {
            ErrorDao.logError(AppDatabase.getInstance(getApplicationContext()),
                    "SyncWorker.sendToApi", e);
            return false;
        }
    }
    */
}
