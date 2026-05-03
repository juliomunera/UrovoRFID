package com.ubx.usdk.rfid.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Interfaz AIDL de callbacks del lector RFID Urovo.
 * Extraída por ingeniería inversa de RFID.apk (com.ubx.rfid).
 *
 * Callbacks disponibles:
 *  - onInventoryTag: se llama por cada TAG detectado en inventario
 *  - onInventoryTagEnd: fin de ciclo de inventario
 *  - onOperationTag: resultado de operación (read/write/lock/kill)
 *  - onOperationTagEnd: fin de operación
 *  - refreshSetting: actualización de configuración del lector
 *  - onExeCMDStatus: estado de ejecución de comando
 */
public interface IRfidCallback extends IInterface {

    /** Default no-op implementation */
    class Default implements IRfidCallback {
        @Override public IBinder asBinder() { return null; }
        @Override public void onExeCMDStatus(byte b, byte b2) throws RemoteException {}
        @Override public void onInventoryTag(byte b, String str, String str2, String str3,
                byte b2, String str4, String str5, int i, int i2, String str6) throws RemoteException {}
        @Override public void onInventoryTagEnd(int i, int i2, int i3, int i4, byte b) throws RemoteException {}
        @Override public void onOperationTag(String str, String str2, String str3, String str4,
                int i, byte b, byte b2) throws RemoteException {}
        @Override public void onOperationTagEnd(int i) throws RemoteException {}
        @Override public void refreshSetting(RfidDate rfidDate) throws RemoteException {}
    }

    /**
     * Llamado cuando se detecta un TAG durante inventario.
     *
     * @param b       readId del lector
     * @param str     PC (Protocol Control)
     * @param str2    CRC
     * @param str3    EPC del tag
     * @param b2      antena
     * @param str4    RSSI (valor raw, restar 129 para dBm)
     * @param str5    frecuencia
     * @param i       contador
     * @param i2      total
     * @param str6    datos adicionales
     */
    void onInventoryTag(byte b, String str, String str2, String str3,
            byte b2, String str4, String str5, int i, int i2, String str6) throws RemoteException;

    /**
     * Llamado al finalizar un ciclo de inventario.
     *
     * @param i  total de tags únicos
     * @param i2 total de lecturas
     * @param i3 tiempo en ms
     * @param i4 velocidad (tags/s)
     * @param b  readId
     */
    void onInventoryTagEnd(int i, int i2, int i3, int i4, byte b) throws RemoteException;

    /**
     * Llamado cuando se completa una operación sobre un TAG (read/write/lock/kill).
     *
     * @param str  PC
     * @param str2 CRC
     * @param str3 EPC
     * @param str4 datos leídos/escritos
     * @param i    resultado
     * @param b    código de error
     * @param b2   readId
     */
    void onOperationTag(String str, String str2, String str3, String str4,
            int i, byte b, byte b2) throws RemoteException;

    /** Fin de operación sobre TAG. */
    void onOperationTagEnd(int i) throws RemoteException;

    /** Actualización de configuración del lector. */
    void refreshSetting(RfidDate rfidDate) throws RemoteException;

    /**
     * Estado de ejecución de un comando.
     *
     * @param b  código de comando (ej: 0x72 = getFirmwareVersion, 0x76 = setOutputPower)
     * @param b2 resultado (0x10 = éxito)
     */
    void onExeCMDStatus(byte b, byte b2) throws RemoteException;

    // -------------------------------------------------------------------------
    // Stub (Binder server-side)
    // -------------------------------------------------------------------------
    abstract class Stub extends Binder implements IRfidCallback {
        private static final String DESCRIPTOR = "com.ubx.usdk.rfid.aidl.IRfidCallback";
        static final int TRANSACTION_onInventoryTag    = 1;
        static final int TRANSACTION_onInventoryTagEnd = 2;
        static final int TRANSACTION_onOperationTag    = 3;
        static final int TRANSACTION_onOperationTagEnd = 4;
        static final int TRANSACTION_refreshSetting    = 5;
        static final int TRANSACTION_onExeCMDStatus    = 6;

        @Override
        public IBinder asBinder() { return this; }

        public Stub() { attachInterface(this, DESCRIPTOR); }

        public static IRfidCallback asInterface(IBinder iBinder) {
            if (iBinder == null) return null;
            IInterface local = iBinder.queryLocalInterface(DESCRIPTOR);
            if (local instanceof IRfidCallback) return (IRfidCallback) local;
            return new Proxy(iBinder);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            switch (code) {
                case TRANSACTION_onInventoryTag:
                    data.enforceInterface(DESCRIPTOR);
                    onInventoryTag(data.readByte(), data.readString(), data.readString(),
                            data.readString(), data.readByte(), data.readString(),
                            data.readString(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onInventoryTagEnd:
                    data.enforceInterface(DESCRIPTOR);
                    onInventoryTagEnd(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readByte());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onOperationTag:
                    data.enforceInterface(DESCRIPTOR);
                    onOperationTag(data.readString(), data.readString(), data.readString(),
                            data.readString(), data.readInt(), data.readByte(), data.readByte());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onOperationTagEnd:
                    data.enforceInterface(DESCRIPTOR);
                    onOperationTagEnd(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_refreshSetting:
                    data.enforceInterface(DESCRIPTOR);
                    refreshSetting(data.readInt() != 0 ? RfidDate.CREATOR.createFromParcel(data) : null);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onExeCMDStatus:
                    data.enforceInterface(DESCRIPTOR);
                    onExeCMDStatus(data.readByte(), data.readByte());
                    reply.writeNoException();
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        public static boolean setDefaultImpl(IRfidCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) return false;
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRfidCallback getDefaultImpl() { return Proxy.sDefaultImpl; }

        // Proxy (Binder client-side)
        static class Proxy implements IRfidCallback {
            static IRfidCallback sDefaultImpl;
            private final IBinder mRemote;

            Proxy(IBinder remote) { mRemote = remote; }

            @Override public IBinder asBinder() { return mRemote; }
            public String getInterfaceDescriptor() { return DESCRIPTOR; }

            @Override
            public void onInventoryTag(byte b, String str, String str2, String str3,
                    byte b2, String str4, String str5, int i, int i2, String str6) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByte(b); _data.writeString(str); _data.writeString(str2);
                    _data.writeString(str3); _data.writeByte(b2); _data.writeString(str4);
                    _data.writeString(str5); _data.writeInt(i); _data.writeInt(i2); _data.writeString(str6);
                    if (!mRemote.transact(TRANSACTION_onInventoryTag, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().onInventoryTag(b, str, str2, str3, b2, str4, str5, i, i2, str6);
                        return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override
            public void onInventoryTagEnd(int i, int i2, int i3, int i4, byte b) throws RemoteException {
                Parcel _data = Parcel.obtain(); Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(i); _data.writeInt(i2); _data.writeInt(i3); _data.writeInt(i4); _data.writeByte(b);
                    if (!mRemote.transact(TRANSACTION_onInventoryTagEnd, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().onInventoryTagEnd(i, i2, i3, i4, b); return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override
            public void onOperationTag(String str, String str2, String str3, String str4,
                    int i, byte b, byte b2) throws RemoteException {
                Parcel _data = Parcel.obtain(); Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(str); _data.writeString(str2); _data.writeString(str3);
                    _data.writeString(str4); _data.writeInt(i); _data.writeByte(b); _data.writeByte(b2);
                    if (!mRemote.transact(TRANSACTION_onOperationTag, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().onOperationTag(str, str2, str3, str4, i, b, b2); return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override
            public void onOperationTagEnd(int i) throws RemoteException {
                Parcel _data = Parcel.obtain(); Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR); _data.writeInt(i);
                    if (!mRemote.transact(TRANSACTION_onOperationTagEnd, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().onOperationTagEnd(i); return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override
            public void refreshSetting(RfidDate rfidDate) throws RemoteException {
                Parcel _data = Parcel.obtain(); Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if (rfidDate != null) { _data.writeInt(1); rfidDate.writeToParcel(_data, 0); }
                    else { _data.writeInt(0); }
                    if (!mRemote.transact(TRANSACTION_refreshSetting, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().refreshSetting(rfidDate); return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }

            @Override
            public void onExeCMDStatus(byte b, byte b2) throws RemoteException {
                Parcel _data = Parcel.obtain(); Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(b); _data.writeByte(b2);
                    if (!mRemote.transact(TRANSACTION_onExeCMDStatus, _data, _reply, 0) && getDefaultImpl() != null) {
                        getDefaultImpl().onExeCMDStatus(b, b2); return;
                    }
                    _reply.readException();
                } finally { _reply.recycle(); _data.recycle(); }
            }
        }
    }
}
