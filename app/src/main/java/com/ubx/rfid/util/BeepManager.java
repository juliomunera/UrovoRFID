package com.ubx.rfid.util;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

/**
 * Gestiona el sonido de beep al detectar un TAG RFID.
 *
 * Usa ToneGenerator (sin archivo de audio externo).
 * El beep solo suena cuando se detecta un TAG nuevo (no en duplicados).
 *
 * Uso:
 *   BeepManager.init();
 *   BeepManager.beep();       // beep corto (TAG nuevo)
 *   BeepManager.release();    // liberar en onDestroyView
 */
public class BeepManager {

    private static final String TAG = "BeepManager";

    /** Duración del beep en milisegundos */
    private static final int BEEP_DURATION_MS = 80;

    /** Volumen del beep (0–100) */
    private static final int BEEP_VOLUME = 80;

    private static ToneGenerator toneGenerator;

    /** Inicializa el ToneGenerator. Llamar en onViewCreated o onResume. */
    public static void init() {
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, BEEP_VOLUME);
                Log.d(TAG, "ToneGenerator inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar ToneGenerator", e);
            toneGenerator = null;
        }
    }

    /**
     * Emite un beep corto.
     * Llamar desde el hilo UI (o cualquier hilo — ToneGenerator es thread-safe).
     */
    public static void beep() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al reproducir beep", e);
        }
    }

    /** Libera el ToneGenerator. Llamar en onDestroyView o onPause. */
    public static void release() {
        try {
            if (toneGenerator != null) {
                toneGenerator.release();
                toneGenerator = null;
                Log.d(TAG, "ToneGenerator liberado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al liberar ToneGenerator", e);
        }
    }
}
