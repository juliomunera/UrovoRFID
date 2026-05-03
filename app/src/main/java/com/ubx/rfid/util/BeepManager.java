package com.ubx.rfid.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Build;
import android.util.Log;

import com.ubx.rfid.R;

/**
 * Gestiona el sonido de beep al detectar un TAG RFID.
 *
 * Estrategia dual:
 *  1. SoundPool con archivo raw/beep.wav  (más confiable en dispositivos industriales)
 *  2. ToneGenerator como fallback         (sin archivo externo)
 *
 * Ambos usan STREAM_MUSIC para evitar que el modo silencio/notificación lo bloquee.
 */
public class BeepManager {

    private static final String TAG = "BeepManager";

    // ToneGenerator
    private static ToneGenerator toneGenerator;
    private static final int BEEP_DURATION_MS = 100;
    private static final int TONE_VOLUME      = 100; // máximo

    // SoundPool
    private static SoundPool soundPool;
    private static int soundId   = -1;
    private static boolean ready = false;

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    /**
     * Inicializa ambos mecanismos de audio.
     * Llamar en onViewCreated() o onResume().
     *
     * @param context contexto de la app (para cargar el recurso raw)
     */
    public static void init(Context context) {
        initToneGenerator();
        initSoundPool(context);
    }

    private static void initToneGenerator() {
        try {
            if (toneGenerator == null) {
                // STREAM_MUSIC: no se ve afectado por modo silencio/notificación
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, TONE_VOLUME);
                Log.d(TAG, "ToneGenerator OK (STREAM_MUSIC)");
            }
        } catch (Exception e) {
            Log.e(TAG, "ToneGenerator init error: " + e.getMessage());
            toneGenerator = null;
        }
    }

    private static void initSoundPool(Context context) {
        if (soundPool != null) return;
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(attrs)
                    .build();

            soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                ready = (status == 0);
                Log.d(TAG, "SoundPool load complete, status=" + status + " ready=" + ready);
            });

            // Cargar el archivo raw/beep.wav
            soundId = soundPool.load(context, R.raw.beep, 1);
            Log.d(TAG, "SoundPool cargando beep.wav, soundId=" + soundId);

        } catch (Exception e) {
            Log.e(TAG, "SoundPool init error: " + e.getMessage());
            soundPool = null;
        }
    }

    // -------------------------------------------------------------------------
    // Beep
    // -------------------------------------------------------------------------

    /**
     * Emite un beep corto.
     * Intenta SoundPool primero; si no está listo, usa ToneGenerator.
     */
    public static void beep() {
        // Intentar SoundPool (más confiable)
        if (soundPool != null && soundId > 0 && ready) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            Log.d(TAG, "beep via SoundPool");
            return;
        }

        // Fallback: ToneGenerator
        if (toneGenerator != null) {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MS);
                Log.d(TAG, "beep via ToneGenerator");
            } catch (Exception e) {
                Log.e(TAG, "ToneGenerator beep error: " + e.getMessage());
                // Reintentar con instancia nueva
                toneGenerator = null;
                initToneGenerator();
            }
        } else {
            Log.w(TAG, "beep() llamado pero no hay generador de audio disponible");
        }
    }

    // -------------------------------------------------------------------------
    // Release
    // -------------------------------------------------------------------------

    /** Libera los recursos de audio. Llamar en onPause() o onDestroyView(). */
    public static void release() {
        try {
            if (toneGenerator != null) {
                toneGenerator.release();
                toneGenerator = null;
            }
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
                soundId = -1;
                ready   = false;
            }
            Log.d(TAG, "BeepManager liberado");
        } catch (Exception e) {
            Log.e(TAG, "release error: " + e.getMessage());
        }
    }
}
