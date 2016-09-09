package org.fao.geonet.api.records.model;

import java.util.Map;

/**
 * Created by francois on 20/06/16.
 */
public class GroupOperations {
    Map<String, Boolean> operations;
    private Integer group;

    public Map<String, Boolean> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, Boolean> operations) {
        this.operations = operations;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}
