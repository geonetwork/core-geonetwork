/**
 *
 */
package org.fao.geonet.repository;

public class SimpleMetadata {
    private String id;
    private String uuid;
    private String date;
    private String isTemplate;

    public SimpleMetadata(Integer id, String uuid, org.fao.geonet.domain.ISODate date, char isTemplate) {
        this.id = id.toString();
        this.uuid = uuid;
        this.date = date.getDateAndTime();
        this.isTemplate = String.valueOf(isTemplate);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(String isTemplate) {
        this.isTemplate = isTemplate;
    }
}
