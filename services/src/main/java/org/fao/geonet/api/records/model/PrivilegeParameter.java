package org.fao.geonet.api.records.model;

/**
 * Created by francois on 16/06/16.
 */
public class PrivilegeParameter {
    private Integer group;
    private int operation;
    private boolean published;

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
