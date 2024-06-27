package org.fao.geonet.auditable.model;

public class RevisionFieldChange {
    private String name;
    private String oldValue;

    private String newValue;

    public RevisionFieldChange(String name, String oldValue, String newValue) {
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
