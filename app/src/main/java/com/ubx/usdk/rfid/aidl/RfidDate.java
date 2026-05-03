package com.ubx.usdk.rfid.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable data class que contiene la configuración del lector RFID.
 */
public class RfidDate implements Parcelable {

    public static final Parcelable.Creator<RfidDate> CREATOR = new Parcelable.Creator<RfidDate>() {
        @Override
        public RfidDate createFromParcel(Parcel parcel) {
            return new RfidDate(parcel);
        }

        @Override
        public RfidDate[] newArray(int i) {
            return new RfidDate[i];
        }
    };

    public byte btReadId;
    public int Powerstate;
    public int dentifierstate;
    public byte[] btAryOutputPower;
    public byte[] btAryReaderIdentifier;
    public byte btMajor;
    public byte btMinor;
    public byte btIndexBaudrate;
    public byte btPlusMinus;
    public byte btTemperature;
    public byte btWorkAntenna;
    public byte btDrmMode;
    public byte btRegion;
    public byte btFrequencyStart;
    public byte btFrequencyEnd;
    public byte btBeeperMode;
    public byte blnMonzaStore;
    public byte btAntDetector;
    public byte btMonzaStatus;
    public byte btReturnLoss;
    public byte btImpedanceFrequency;
    public int nUserDefineStartFrequency;
    public byte btUserDefineFrequencyInterval;
    public byte btUserDefineChannelQuantity;
    public byte btRfLinkProfile;
    public String mMatchEpcValue;

    @Override
    public int describeContents() {
        return 0;
    }

    public RfidDate() {
        this.btReadId = (byte) -1;
        this.Powerstate = -1;
        this.dentifierstate = -1;
        this.btAryOutputPower = new byte[16];
        this.btAryReaderIdentifier = new byte[16];
        this.mMatchEpcValue = "";
    }

    RfidDate(Parcel parcel) {
        this.btReadId = (byte) -1;
        this.Powerstate = -1;
        this.dentifierstate = -1;
        this.btAryOutputPower = new byte[16];
        this.btAryReaderIdentifier = new byte[16];
        this.mMatchEpcValue = "";
        readFromParcel(parcel);
    }

    public static void readByteArray(byte[] src, byte[] dst) {
        System.arraycopy(src, 0, dst, 0, Math.min(src.length, dst.length));
    }

    public void readFromParcel(Parcel parcel) {
        this.btReadId = parcel.readByte();
        this.Powerstate = parcel.readInt();
        this.dentifierstate = parcel.readInt();
        if (this.Powerstate == 0) {
            readByteArray(parcel.createByteArray(), this.btAryOutputPower);
        }
        if (this.dentifierstate == 0) {
            readByteArray(parcel.createByteArray(), this.btAryReaderIdentifier);
        }
        this.btMajor = parcel.readByte();
        this.btMinor = parcel.readByte();
        this.btIndexBaudrate = parcel.readByte();
        this.btPlusMinus = parcel.readByte();
        this.btTemperature = parcel.readByte();
        this.btWorkAntenna = parcel.readByte();
        this.btDrmMode = parcel.readByte();
        this.btRegion = parcel.readByte();
        this.btFrequencyStart = parcel.readByte();
        this.btFrequencyEnd = parcel.readByte();
        this.btBeeperMode = parcel.readByte();
        this.blnMonzaStore = parcel.readByte();
        this.btAntDetector = parcel.readByte();
        this.btMonzaStatus = parcel.readByte();
        this.btReturnLoss = parcel.readByte();
        this.btImpedanceFrequency = parcel.readByte();
        this.nUserDefineStartFrequency = parcel.readInt();
        this.btUserDefineFrequencyInterval = parcel.readByte();
        this.btUserDefineChannelQuantity = parcel.readByte();
        this.btRfLinkProfile = parcel.readByte();
        this.mMatchEpcValue = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte(this.btReadId);
        parcel.writeInt(this.Powerstate);
        parcel.writeInt(this.dentifierstate);
        if (this.Powerstate == 0) {
            parcel.writeByteArray(this.btAryOutputPower);
        }
        if (this.dentifierstate == 0) {
            parcel.writeByteArray(this.btAryReaderIdentifier);
        }
        parcel.writeByte(this.btMajor);
        parcel.writeByte(this.btMinor);
        parcel.writeByte(this.btIndexBaudrate);
        parcel.writeByte(this.btPlusMinus);
        parcel.writeByte(this.btTemperature);
        parcel.writeByte(this.btWorkAntenna);
        parcel.writeByte(this.btDrmMode);
        parcel.writeByte(this.btRegion);
        parcel.writeByte(this.btFrequencyStart);
        parcel.writeByte(this.btFrequencyEnd);
        parcel.writeByte(this.btBeeperMode);
        parcel.writeByte(this.blnMonzaStore);
        parcel.writeByte(this.btAntDetector);
        parcel.writeByte(this.btMonzaStatus);
        parcel.writeByte(this.btReturnLoss);
        parcel.writeByte(this.btImpedanceFrequency);
        parcel.writeInt(this.nUserDefineStartFrequency);
        parcel.writeByte(this.btUserDefineFrequencyInterval);
        parcel.writeByte(this.btUserDefineChannelQuantity);
        parcel.writeByte(this.btRfLinkProfile);
        parcel.writeString(this.mMatchEpcValue);
    }

    // --- Getters / Setters ---

    public byte getReadId() { return btReadId; }
    public void setReadId(byte b) { btReadId = b; }

    public int getPowerstate() { return Powerstate; }
    public void setPowerstate(int i) { Powerstate = i; }

    public byte getbtMajor() { return btMajor; }
    public void setbtMajor(byte b) { btMajor = b; }

    public byte getbtMinor() { return btMinor; }
    public void setbtMinor(byte b) { btMinor = b; }

    public byte getbtIndexBaudrate() { return btIndexBaudrate; }
    public void setbtIndexBaudrate(byte b) { btIndexBaudrate = b; }

    public byte getbtPlusMinus() { return btPlusMinus; }
    public void setbtPlusMinus(byte b) { btPlusMinus = b; }

    public byte getbtTemperature() { return btTemperature; }
    public void setbtTemperature(byte b) { btTemperature = b; }

    public byte[] getbtAryOutputPower() { return btAryOutputPower; }
    public void setbtAryOutputPower(byte[] bArr) {
        if (bArr == null || bArr.length == 0) { Powerstate = -1; return; }
        Powerstate = 0;
        System.arraycopy(bArr, 0, btAryOutputPower, 0, bArr.length);
    }

    public byte getbtWorkAntenna() { return btWorkAntenna; }
    public void setbtWorkAntenna(byte b) { btWorkAntenna = b; }

    public byte getbtDrmMode() { return btDrmMode; }
    public void setbtDrmMode(byte b) { btDrmMode = b; }

    public byte getbtRegion() { return btRegion; }
    public void setbtRegion(byte b) { btRegion = b; }

    public byte getbtFrequencyStart() { return btFrequencyStart; }
    public void setbtFrequencyStart(byte b) { btFrequencyStart = b; }

    public byte getbtFrequencyEnd() { return btFrequencyEnd; }
    public void setbtFrequencyEnd(byte b) { btFrequencyEnd = b; }

    public byte getbtBeeperMode() { return btBeeperMode; }
    public void setbtBeeperMode(byte b) { btBeeperMode = b; }

    public byte getBlnMonzaStore() { return blnMonzaStore; }
    public void setBlnMonzaStore(byte b) { blnMonzaStore = b; }

    public byte getbtAntDetector() { return btAntDetector; }
    public void setbtAntDetector(byte b) { btAntDetector = b; }

    public byte getbtMonzaStatus() { return btMonzaStatus; }
    public void setbtMonzaStatus(byte b) { btMonzaStatus = b; }

    public byte[] getbtAryReaderIdentifier() { return btAryReaderIdentifier; }
    public void setbtAryReaderIdentifier(byte[] bArr) {
        if (bArr == null || bArr.length == 0) { dentifierstate = -1; return; }
        dentifierstate = 0;
        System.arraycopy(bArr, 0, btAryReaderIdentifier, 0, bArr.length);
    }

    public byte getbtReturnLoss() { return btReturnLoss; }
    public void setbtReturnLoss(byte b) { btReturnLoss = b; }

    public byte getbtImpedanceFrequency() { return btImpedanceFrequency; }
    public void setbtImpedanceFrequency(byte b) { btImpedanceFrequency = b; }

    public int getnUserDefineStartFrequency() { return nUserDefineStartFrequency; }
    public void setnUserDefineStartFrequency(int i) { nUserDefineStartFrequency = i; }

    public byte getbtUserDefineFrequencyInterval() { return btUserDefineFrequencyInterval; }
    public void setbtUserDefineFrequencyInterval(byte b) { btUserDefineFrequencyInterval = b; }

    public byte getbtUserDefineChannelQuantity() { return btUserDefineChannelQuantity; }
    public void setbtUserDefineChannelQuantity(byte b) { btUserDefineChannelQuantity = b; }

    public byte getbtRfLinkProfile() { return btRfLinkProfile; }
    public void setbtRfLinkProfile(byte b) { btRfLinkProfile = b; }

    public String getmMatchEpcValue() { return mMatchEpcValue; }
    public void setmMatchEpcValue(String str) { mMatchEpcValue = str; }
}
