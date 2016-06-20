package org.fao.geonet.api.records.model;

import org.fao.geonet.domain.Profile;

import java.util.List;
import java.util.Map;

/**
 * Created by francois on 16/06/16.
 */
public class GroupPrivilege extends GroupOperations {
    private List<Profile> userProfiles;
    private boolean userGroup;
    private boolean reserved;

    public List<Profile> getUserProfiles() {
        return userProfiles;
    }

    public void setUserProfile(List<Profile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    public boolean isUserGroup() {
        return userGroup;
    }

    public void setUserGroup(boolean userGroup) {
        this.userGroup = userGroup;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
