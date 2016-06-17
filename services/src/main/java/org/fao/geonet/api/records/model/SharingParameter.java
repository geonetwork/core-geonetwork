package org.fao.geonet.api.records.model;

import java.util.List;

/**
 * Created by francois on 16/06/16.
 */
public class SharingParameter {
    private List<PrivilegeParameter> privileges;
    private boolean clear;

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public List<PrivilegeParameter> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<PrivilegeParameter> privileges) {
        this.privileges = privileges;
    }
}
