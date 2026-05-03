package com.ubx.rfid.model;

/** Modelo para un registro de la tabla TagRead. */
public class TagReadModel {
    private long   id;
    private String tagId;
    private String readDate;  // YYYY-MM-DD HH:mm:ss
    private boolean synced;

    public TagReadModel(long id, String tagId, String readDate) {
        this.id       = id;
        this.tagId    = tagId;
        this.readDate = readDate;
        this.synced   = false;
    }

    public long    getId()       { return id; }
    public String  getTagId()    { return tagId; }
    public String  getReadDate() { return readDate; }
    public boolean isSynced()    { return synced; }
    public void    setSynced(boolean s) { this.synced = s; }
}
