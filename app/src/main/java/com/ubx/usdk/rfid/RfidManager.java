package com.ubx.usdk.rfid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.rfid.aidl.IRfidManager;

/**
 * Gestor del lector RFID Urovo.
 * Se conecta al servicio del sistema "com.ubx.usdk.rfid" via Binder AIDL.
 *
 * Uso básico:
 * <pre>
 *   RfidManager mgr = new RfidManager(context);
 *   mgr.bindService(new RfidManager.ConnectionListener() {
 *       public void onConnected() {
 *           mgr.connectCom("/dev/ttyHSL0", 115200);
 *           mgr.registerCallback(myCallback);
 *           mgr.customizedSessionTargetInventory(mgr.getReadId(), (byte)1, (byte)0, (byte)1);
 *       }
 *       public void onDisconnected() { ... }
 *   });
 * </pre>
 */
public class RfidManager {

    private static final String TAG = "RfidManager";

    /** Acción del servicio del sistema Urovo RFID */
    private static final String SERVICE_ACTION  = "com.ubx.usdk.rfid.RfidService";
    private static final String SERVICE_PACKAGE = "com.ubx.usdk.rfid";

    private final Context mContext;
    private IRfidManager mService;
    private ConnectionListener mListener;
    private boolean mBound = false;

    // -------------------------------------------------------------------------
    // Listener de conexión al servicio
    // -------------------------------------------------------------------------

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = IRfidManager.Stub.asInterface(service);
            mBound = true;
            if (mListener != null) mListener.onConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
            mBound = false;
            if (mListener != null) mListener.onDisconnected();
        }
    };

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public RfidManager(Context context) {
        mContext = context.getApplicationContext();
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida del servicio
    // -------------------------------------------------------------------------

    /**
     * Vincula al servicio RFID del sistema.
     * Llama a esto en onCreate() o onStart() de tu Activity/Fragment.
     */
    public void bindService(ConnectionListener listener) {
        mListener = listener;
        Intent intent = new Intent(SERVICE_ACTION);
        intent.setPackage(SERVICE_PACKAGE);
        boolean ok = mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService result: " + ok);
    }

    /**
     * Desvincula del servicio.
     * Llama a esto en onDestroy() de tu Activity/Fragment.
     */
    public void unbindService() {
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /** Libera recursos y desconecta el lector. */
    public void release() {
        disConnect();
        unbindService();
    }

    // -------------------------------------------------------------------------
    // Conexión al lector físico
    // -------------------------------------------------------------------------

    /**
     * Conecta al lector RFID por puerto serie.
     *
     * @param port     Puerto serie, ej: "/dev/ttyHSL0"
     * @param baudrate Velocidad, ej: 115200
     * @return true si la conexión fue exitosa
     */
    public boolean connectCom(String port, int baudrate) {
        if (mService == null) return false;
        try {
            return mService.connectCom(port, baudrate);
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "connectCom error", e);
            return false;
        }
    }

    /** Desconecta el lector físico. */
    public void disConnect() {
        if (mService == null) return;
        try {
            mService.disConnect();
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "disConnect error", e);
        }
    }

    /** @return true si el lector está conectado */
    public boolean isConnected() {
        if (mService == null) return false;
        try {
            return mService.isConnected();
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /** @return ID del lector (readId), usado en todas las operaciones */
    public byte getReadId() {
        if (mService == null) return (byte) -1;
        try {
            return mService.getReadId();
        } catch (RemoteException | NullPointerException e) {
            return (byte) -1;
        }
    }

    /** @return Identificador único del lector (array de bytes) */
    public byte[] getIdentifier() {
        if (mService == null) return null;
        try {
            return mService.getIdentifier();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Inventario (lectura de TAGs a larga distancia)
    // -------------------------------------------------------------------------

    /**
     * Inventario con sesión y target personalizados.
     * Este es el método principal para lectura de TAGs UHF a larga distancia.
     *
     * @param readId  ID del lector (obtenido con getReadId())
     * @param session Sesión EPC Gen2: 0=S0, 1=S1, 2=S2, 3=S3
     * @param target  Target: 0=A, 1=B
     * @param repeat  Número de repeticiones (1-255)
     * @return 0 si el comando fue enviado correctamente
     */
    public int customizedSessionTargetInventory(byte readId, byte session, byte target, byte repeat) {
        if (mService == null) return -1;
        try {
            return mService.customizedSessionTargetInventory(readId, session, target, repeat);
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "customizedSessionTargetInventory error", e);
            return -1;
        }
    }

    /**
     * Inventario estándar.
     *
     * @param readId ID del lector
     * @param repeat Número de repeticiones
     */
    public int inventory(byte readId, byte repeat) {
        if (mService == null) return -1;
        try {
            return mService.inventory(readId, repeat);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /**
     * Inventario en tiempo real (streaming continuo).
     */
    public int realTimeInventory(byte readId, byte repeat) {
        if (mService == null) return -1;
        try {
            return mService.realTimeInventory(readId, repeat);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre TAGs
    // -------------------------------------------------------------------------

    /**
     * Lee datos de un TAG.
     *
     * @param readId    ID del lector
     * @param memBank   Banco de memoria: 1=EPC, 2=TID, 3=USER
     * @param startAddr Dirección de inicio (en palabras de 16 bits)
     * @param length    Número de palabras a leer
     * @param password  Contraseña de acceso (4 bytes, {0,0,0,0} si no hay)
     */
    public int readTag(byte readId, byte memBank, byte startAddr, byte length, byte[] password) {
        if (mService == null) return -1;
        try {
            return mService.readTag(readId, memBank, startAddr, length, password);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /**
     * Escribe datos en un TAG.
     *
     * @param readId    ID del lector
     * @param password  Contraseña de acceso (4 bytes)
     * @param memBank   Banco de memoria: 1=EPC, 2=TID, 3=USER
     * @param startAddr Dirección de inicio
     * @param length    Número de palabras a escribir
     * @param data      Datos a escribir
     */
    public int writeTag(byte readId, byte[] password, byte memBank, byte startAddr, byte length, byte[] data) {
        if (mService == null) return -1;
        try {
            return mService.writeTag(readId, password, memBank, startAddr, length, data);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Bloquea un TAG. */
    public int lockTag(byte readId, byte[] password, byte memBank, byte lockType) {
        if (mService == null) return -1;
        try {
            return mService.lockTag(readId, password, memBank, lockType);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Destruye (kill) un TAG permanentemente. */
    public int killTag(byte readId, byte[] password) {
        if (mService == null) return -1;
        try {
            return mService.killTag(readId, password);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Configuración de potencia
    // -------------------------------------------------------------------------

    /**
     * Establece la potencia de transmisión.
     *
     * @param readId ID del lector
     * @param power  Potencia en dBm (típico: 26-33 dBm para Urovo DT50)
     */
    public int setOutputPower(byte readId, byte power) {
        if (mService == null) return -1;
        try {
            return mService.setOutputPower(readId, power);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Obtiene la potencia de transmisión actual. */
    public int getOutputPower(byte readId) {
        if (mService == null) return -1;
        try {
            return mService.getOutputPower(readId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Configuración de frecuencia
    // -------------------------------------------------------------------------

    /**
     * Establece la región de frecuencia.
     *
     * @param readId ID del lector
     * @param region Región: 1=FCC (902-928MHz), 2=ETSI (865-868MHz), 3=CHN (920-925MHz)
     * @param start  Canal de inicio
     * @param end    Canal de fin
     */
    public int setFrequencyRegion(byte readId, byte region, byte start, byte end) {
        if (mService == null) return -1;
        try {
            return mService.setFrequencyRegion(readId, region, start, end);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Obtiene la región de frecuencia actual. */
    public int getFrequencyRegion(byte readId) {
        if (mService == null) return -1;
        try {
            return mService.getFrequencyRegion(readId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Información del lector
    // -------------------------------------------------------------------------

    /** Obtiene la versión de firmware del lector. */
    public int getFirmwareVersion(byte readId) {
        if (mService == null) return -1;
        try {
            return mService.getFirmwareVersion(readId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Obtiene la temperatura del lector. */
    public int getReaderTemperature(byte readId) {
        if (mService == null) return -1;
        try {
            return mService.getReaderTemperature(readId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Filtros / Máscara EPC
    // -------------------------------------------------------------------------

    /**
     * Establece un filtro por EPC para operaciones de acceso.
     *
     * @param readId ID del lector
     * @param mode   0=desactivar, 1=activar
     * @param epc    EPC a filtrar (bytes)
     */
    public int setAccessEpcMatch(byte readId, byte mode, byte[] epc) {
        if (mService == null) return -1;
        try {
            return mService.setAccessEpcMatch(readId, mode, epc);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    /** Cancela el filtro EPC activo. */
    public int cancelAccessEpcMatch(byte readId) {
        if (mService == null) return -1;
        try {
            return mService.cancelAccessEpcMatch(readId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Comandos raw
    // -------------------------------------------------------------------------

    /**
     * Envía un comando raw al lector.
     * Útil para funciones avanzadas no expuestas en la API.
     *
     * @param cmd Array de bytes del comando
     * @return 0 si fue enviado correctamente
     */
    public int sendCommand(byte[] cmd) {
        if (mService == null) return -1;
        try {
            return mService.sendCommand(cmd);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // Callbacks
    // -------------------------------------------------------------------------

    /**
     * Registra un callback para recibir eventos del lector.
     * Llama a esto antes de iniciar el inventario.
     */
    public void registerCallback(IRfidCallback callback) {
        if (mService == null) return;
        try {
            mService.registerCallback(callback, callback.hashCode());
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "registerCallback error", e);
        }
    }

    /**
     * Desregistra el callback.
     * Llama a esto en onPause() o cuando dejes de necesitar eventos.
     */
    public void unregisterCallback(IRfidCallback callback) {
        if (mService == null) return;
        try {
            mService.unregisterCallback(callback, callback.hashCode());
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "unregisterCallback error", e);
        }
    }

    /** Envía parámetros de configuración al servicio (formato JSON). */
    public void setParams(String params) {
        if (mService == null) return;
        try {
            mService.setParams(params);
        } catch (RemoteException | NullPointerException e) {
            Log.e(TAG, "setParams error", e);
        }
    }

    /** @return true si el servicio está vinculado */
    public boolean isBound() {
        return mBound;
    }
}
