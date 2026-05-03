package com.ubx.rfid.model;

/** Modelo para un registro de la tabla Errors. */
public class ErrorModel {
    private long   id;
    private String errorDate;
    private String source;
    private String message;

    public ErrorModel(long id, String errorDate, String source, String message) {
        this.id        = id;
        this.errorDate = errorDate;
        this.source    = source;
        this.message   = message;
    }

    public long   getId()        { return id; }
    public String getErrorDate() { return errorDate; }
    public String getSource()    { return source; }
    public String getMessage()   { return message; }
}
