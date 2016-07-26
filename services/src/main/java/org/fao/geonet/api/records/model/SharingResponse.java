package org.fao.geonet.api.records.model;

import java.util.List;

/**
 * Created by francois on 16/06/16.
 */
public class SharingResponse {
    private List<GroupPrivilege> privileges;
    private String owner;
    private String groupOwner;

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

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }
}
