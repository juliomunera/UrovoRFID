package com.ubx.usdk.rfid.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * Interfaz AIDL del gestor RFID Urovo.
 * El servicio real corre en com.ubx.usdk.rfid (RfidService).
 */
public interface IRfidManager extends IInterface {

    // --- Conexión ---
    boolean connectCom(String port, int baudrate) throws RemoteException;
    void disConnect() throws RemoteException;
    boolean isConnected() throws RemoteException;
    byte getReadId() throws RemoteException;
    byte[] getIdentifier() throws RemoteException;

    // --- Inventario ---
    int inventory(byte readId, byte repeat) throws RemoteException;
    int realTimeInventory(byte readId, byte repeat) throws RemoteException;
    int customizedSessionTargetInventory(byte readId, byte session, byte target, byte repeat) throws RemoteException;
    int fastSwitchAntInventory(byte readId, byte ant1, byte r1, byte ant2, byte r2,
            byte ant3, byte r3, byte ant4, byte r4, byte interval, byte repeat) throws RemoteException;

    // --- Buffer de inventario ---
    int getInventoryBuffer(byte readId) throws RemoteException;
    int getInventoryBufferTagCount(byte readId) throws RemoteException;
    int getAndResetInventoryBuffer(byte readId) throws RemoteException;
    int resetInventoryBuffer(byte readId) throws RemoteException;

    // --- Operaciones sobre TAG ---
    int readTag(byte readId, byte memBank, byte startAddr, byte length, byte[] password) throws RemoteException;
    int writeTag(byte readId, byte[] password, byte memBank, byte startAddr, byte length, byte[] data) throws RemoteException;
    int blockWriteTag(byte readId, byte[] password, byte memBank, byte startAddr, byte length, byte[] data) throws RemoteException;
    int lockTag(byte readId, byte[] password, byte memBank, byte lockType) throws RemoteException;
    int killTag(byte readId, byte[] password) throws RemoteException;

    // --- Filtro / Máscara ---
    int setAccessEpcMatch(byte readId, byte mode, byte[] epc) throws RemoteException;
    int getAccessEpcMatch(byte readId) throws RemoteException;
    int cancelAccessEpcMatch(byte readId) throws RemoteException;
    int setTagMask(byte readId, byte target, byte action, byte memBank,
            byte startAddr, byte length, byte truncate, byte[] mask) throws RemoteException;
    int getTagMask(byte readId) throws RemoteException;
    int clearTagMask(byte readId, byte target) throws RemoteException;

    // --- Configuración de potencia ---
    int setOutputPower(byte readId, byte power) throws RemoteException;
    int getOutputPower(byte readId) throws RemoteException;
    int setTemporaryOutputPower(byte readId, byte power) throws RemoteException;

    // --- Configuración de frecuencia ---
    int setFrequencyRegion(byte readId, byte region, byte start, byte end) throws RemoteException;
    int getFrequencyRegion(byte readId) throws RemoteException;
    int setUserDefineFrequency(byte readId, byte quantity, byte interval, int startFreq) throws RemoteException;

    // --- Configuración de antena ---
    int setWorkAntenna(byte readId, byte ant) throws RemoteException;
    int getWorkAntenna(byte readId) throws RemoteException;
    int setAntConnectionDetector(byte readId, byte mode) throws RemoteException;
    int getAntConnectionDetector(byte readId) throws RemoteException;
    int getRfPortReturnLoss(byte readId, byte ant) throws RemoteException;

    // --- Perfil RF ---
    int setRfLinkProfile(byte readId, byte profile) throws RemoteException;
    int getRfLinkProfile(byte readId) throws RemoteException;

    // --- Impinj FastTID ---
    int setImpinjFastTid(byte readId, boolean enable, boolean save) throws RemoteException;
    int getImpinjFastTid(byte readId) throws RemoteException;

    // --- Identificador del lector ---
    int setReaderIdentifier(byte readId, byte[] identifier) throws RemoteException;
    int getReaderIdentifier(byte readId) throws RemoteException;
    int setReaderAddress(byte readId, byte newAddress) throws RemoteException;

    // --- Información del lector ---
    int getFirmwareVersion(byte readId) throws RemoteException;
    int getReaderTemperature(byte readId) throws RemoteException;
    int setUartBaudrate(byte readId, byte baudrate) throws RemoteException;

    // --- Control ---
    int reset(byte readId) throws RemoteException;
    int setTrigger(boolean enable) throws RemoteException;
    int sendCommand(byte[] cmd) throws RemoteException;
    void setParams(String params) throws RemoteException;

    // --- ISO 18000-6B ---
    int iso180006BInventory(byte readId) throws RemoteException;
    int iso180006BReadTag(byte readId, byte[] uid, byte startAddr, byte length) throws RemoteException;
    int iso180006BWriteTag(byte readId, byte[] uid, byte startAddr, byte length, byte[] data) throws RemoteException;
    int iso180006BLockTag(byte readId, byte[] uid, byte addr) throws RemoteException;
    int iso180006BQueryLockTag(byte readId, byte[] uid, byte addr) throws RemoteException;

    // --- Callbacks ---
    void registerCallback(IRfidCallback callback, int hashCode) throws RemoteException;
    void unregisterCallback(IRfidCallback callback, int hashCode) throws RemoteException;

    // --- Stub helper ---
    abstract class Stub extends android.os.Binder implements IRfidManager {
        private static final String DESCRIPTOR = "com.ubx.usdk.rfid.aidl.IRfidManager";

        public Stub() { attachInterface(this, DESCRIPTOR); }

        @Override
        public IBinder asBinder() { return this; }

        public static IRfidManager asInterface(IBinder iBinder) {
            if (iBinder == null) return null;
            IInterface local = iBinder.queryLocalInterface(DESCRIPTOR);
            if (local instanceof IRfidManager) return (IRfidManager) local;
            return new Proxy(iBinder);
        }

        // Proxy minimal — el servicio real está en el dispositivo
        static class Proxy implements IRfidManager {
            private final IBinder mRemote;
            Proxy(IBinder remote) { mRemote = remote; }
            @Override public IBinder asBinder() { return mRemote; }

            // Todas las llamadas se delegan al servicio via Binder transact.
            // Los transaction codes coinciden con los del APK original.
            @Override public boolean connectCom(String port, int baud) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(port); _data.writeInt(baud);
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt() != 0;
                } finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public void disConnect() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); mRemote.transact(2, _data, _reply, 0); _reply.readException(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public boolean isConnected() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); mRemote.transact(3, _data, _reply, 0); _reply.readException(); return _reply.readInt() != 0; }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int sendCommand(byte[] cmd) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByteArray(cmd); mRemote.transact(4, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int customizedSessionTargetInventory(byte readId, byte session, byte target, byte repeat) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); _data.writeByte(session); _data.writeByte(target); _data.writeByte(repeat); mRemote.transact(8, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int inventory(byte readId, byte repeat) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); _data.writeByte(repeat); mRemote.transact(25, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int realTimeInventory(byte readId, byte repeat) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); _data.writeByte(repeat); mRemote.transact(34, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int setOutputPower(byte readId, byte power) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); _data.writeByte(power); mRemote.transact(41, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int getOutputPower(byte readId) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); mRemote.transact(18, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public int getFirmwareVersion(byte readId) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeByte(readId); mRemote.transact(13, _data, _reply, 0); _reply.readException(); return _reply.readInt(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public byte getReadId() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); mRemote.transact(54, _data, _reply, 0); _reply.readException(); return _reply.readByte(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public byte[] getIdentifier() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); mRemote.transact(55, _data, _reply, 0); _reply.readException(); return _reply.createByteArray(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public void registerCallback(IRfidCallback cb, int hash) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeStrongBinder(cb != null ? cb.asBinder() : null); _data.writeInt(hash); mRemote.transact(52, _data, _reply, 0); _reply.readException(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public void unregisterCallback(IRfidCallback cb, int hash) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeStrongBinder(cb != null ? cb.asBinder() : null); _data.writeInt(hash); mRemote.transact(53, _data, _reply, 0); _reply.readException(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            @Override public void setParams(String params) throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try { _data.writeInterfaceToken(DESCRIPTOR); _data.writeString(params); mRemote.transact(56, _data, _reply, 0); _reply.readException(); }
                finally { _reply.recycle(); _data.recycle(); }
            }
            // Stubs para métodos restantes (implementación mínima)
            @Override public int readTag(byte r, byte m, byte s, byte l, byte[] p) throws RemoteException { return -1; }
            @Override public int writeTag(byte r, byte[] p, byte m, byte s, byte l, byte[] d) throws RemoteException { return -1; }
            @Override public int blockWriteTag(byte r, byte[] p, byte m, byte s, byte l, byte[] d) throws RemoteException { return -1; }
            @Override public int lockTag(byte r, byte[] p, byte m, byte t) throws RemoteException { return -1; }
            @Override public int killTag(byte r, byte[] p) throws RemoteException { return -1; }
            @Override public int setAccessEpcMatch(byte r, byte m, byte[] e) throws RemoteException { return -1; }
            @Override public int getAccessEpcMatch(byte r) throws RemoteException { return -1; }
            @Override public int cancelAccessEpcMatch(byte r) throws RemoteException { return -1; }
            @Override public int setTagMask(byte r, byte t, byte a, byte m, byte s, byte l, byte tr, byte[] mask) throws RemoteException { return -1; }
            @Override public int getTagMask(byte r) throws RemoteException { return -1; }
            @Override public int clearTagMask(byte r, byte t) throws RemoteException { return -1; }
            @Override public int setTemporaryOutputPower(byte r, byte p) throws RemoteException { return -1; }
            @Override public int setFrequencyRegion(byte r, byte reg, byte s, byte e) throws RemoteException { return -1; }
            @Override public int getFrequencyRegion(byte r) throws RemoteException { return -1; }
            @Override public int setUserDefineFrequency(byte r, byte q, byte i, int f) throws RemoteException { return -1; }
            @Override public int setWorkAntenna(byte r, byte a) throws RemoteException { return -1; }
            @Override public int getWorkAntenna(byte r) throws RemoteException { return -1; }
            @Override public int setAntConnectionDetector(byte r, byte m) throws RemoteException { return -1; }
            @Override public int getAntConnectionDetector(byte r) throws RemoteException { return -1; }
            @Override public int getRfPortReturnLoss(byte r, byte a) throws RemoteException { return -1; }
            @Override public int setRfLinkProfile(byte r, byte p) throws RemoteException { return -1; }
            @Override public int getRfLinkProfile(byte r) throws RemoteException { return -1; }
            @Override public int setImpinjFastTid(byte r, boolean e, boolean s) throws RemoteException { return -1; }
            @Override public int getImpinjFastTid(byte r) throws RemoteException { return -1; }
            @Override public int setReaderIdentifier(byte r, byte[] id) throws RemoteException { return -1; }
            @Override public int getReaderIdentifier(byte r) throws RemoteException { return -1; }
            @Override public int setReaderAddress(byte r, byte a) throws RemoteException { return -1; }
            @Override public int getReaderTemperature(byte r) throws RemoteException { return -1; }
            @Override public int setUartBaudrate(byte r, byte b) throws RemoteException { return -1; }
            @Override public int reset(byte r) throws RemoteException { return -1; }
            @Override public int setTrigger(boolean e) throws RemoteException { return -1; }
            @Override public int getInventoryBuffer(byte r) throws RemoteException { return -1; }
            @Override public int getInventoryBufferTagCount(byte r) throws RemoteException { return -1; }
            @Override public int getAndResetInventoryBuffer(byte r) throws RemoteException { return -1; }
            @Override public int resetInventoryBuffer(byte r) throws RemoteException { return -1; }
            @Override public int fastSwitchAntInventory(byte r, byte a1, byte r1, byte a2, byte r2, byte a3, byte r3, byte a4, byte r4, byte i, byte rep) throws RemoteException { return -1; }
            @Override public int iso180006BInventory(byte r) throws RemoteException { return -1; }
            @Override public int iso180006BReadTag(byte r, byte[] uid, byte s, byte l) throws RemoteException { return -1; }
            @Override public int iso180006BWriteTag(byte r, byte[] uid, byte s, byte l, byte[] d) throws RemoteException { return -1; }
            @Override public int iso180006BLockTag(byte r, byte[] uid, byte a) throws RemoteException { return -1; }
            @Override public int iso180006BQueryLockTag(byte r, byte[] uid, byte a) throws RemoteException { return -1; }
        }
    }
}
