package com.ubx.rfid.p006ui.scan;

/**
 * Modelo de datos para un TAG RFID escaneado.
 */
public class ScanModel {

    private String epc;    // EPC del tag (Electronic Product Code)
    private int count;     // Número de veces detectado
    private String rssi;   // Potencia de señal recibida (ej: "-65dBm")
    private String pc;     // Protocol Control
    private String crc;    // CRC
    private boolean selected; // Seleccionado en la lista

    public ScanModel(String epc, int count, String rssi, String pc, String crc, boolean selected) {
        this.epc = epc;
        this.count = count;
        this.rssi = rssi;
        this.pc = pc;
        this.crc = crc;
        this.selected = selected;
    }

    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getRssi() { return rssi; }
    public void setRssi(String rssi) { this.rssi = rssi; }

    public String getPc() { return pc; }
    public void setPc(String pc) { this.pc = pc; }

    public String getCrc() { return crc; }
    public void setCrc(String crc) { this.crc = crc; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
