package com.ubx.rfid.p006ui.scan;

/**
 * Modelo de datos para un TAG RFID escaneado.
 * El campo 'displayText' muestra la descripción del inventario si existe,
 * o el EPC raw si el TAG no está en el inventario local.
 */
public class ScanModel {

    private String epc;         // EPC del tag (Electronic Product Code) — siempre el código real
    private String displayText; // Descripción del inventario, o el EPC si no está catalogado
    private int count;          // Número de veces detectado
    private String rssi;        // Potencia de señal recibida (ej: "-65dBm")
    private String pc;          // Protocol Control
    private String crc;         // CRC
    private boolean selected;   // Seleccionado en la lista

    public ScanModel(String epc, int count, String rssi, String pc, String crc, boolean selected) {
        this.epc         = epc;
        this.displayText = epc; // por defecto muestra el EPC
        this.count       = count;
        this.rssi        = rssi;
        this.pc          = pc;
        this.crc         = crc;
        this.selected    = selected;
    }

    public String getEpc()          { return epc; }
    public void   setEpc(String e)  { this.epc = e; }

    /** Texto a mostrar en la columna EPC/Descripción. */
    public String getDisplayText()         { return displayText; }
    public void   setDisplayText(String t) { this.displayText = t; }

    /** @return true si el TAG tiene descripción en el inventario */
    public boolean hasDescription() {
        return displayText != null && !displayText.equals(epc);
    }

    public int    getCount()          { return count; }
    public void   setCount(int c)     { this.count = c; }

    public String getRssi()           { return rssi; }
    public void   setRssi(String r)   { this.rssi = r; }

    public String getPc()             { return pc; }
    public void   setPc(String p)     { this.pc = p; }

    public String getCrc()            { return crc; }
    public void   setCrc(String c)    { this.crc = c; }

    public boolean isSelected()             { return selected; }
    public void    setSelected(boolean s)   { this.selected = s; }
}
