package com.ubx.rfid.model;

/** Modelo para un registro de la tabla Inventary. */
public class InventaryModel {
    private long   id;
    private String tagId;
    private String description;

    public InventaryModel(long id, String tagId, String description) {
        this.id          = id;
        this.tagId       = tagId;
        this.description = description != null ? description : "";
    }

    public long   getId()          { return id; }
    public String getTagId()       { return tagId; }
    public String getDescription() { return description; }
    public void   setTagId(String t)       { tagId = t; }
    public void   setDescription(String d) { description = d; }
}
