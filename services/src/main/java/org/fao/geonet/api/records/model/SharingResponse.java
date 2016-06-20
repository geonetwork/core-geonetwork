package org.fao.geonet.api.records.model;

import java.util.List;

/**
 * Created by francois on 16/06/16.
 */
public class SharingResponse {
    private List<GroupPrivilege> privileges;
    private String owner;

    public List<GroupPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<GroupPrivilege> privileges) {
        this.privileges = privileges;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
