package org.fao.geonet.api.records.model;

import org.fao.geonet.domain.Profile;

import java.util.List;
import java.util.Map;

/**
 * Created by francois on 16/06/16.
 */
public class GroupPrivilege {
    private Integer group;
    private List<Profile> userProfiles;
    private boolean userGroup;

    public Map<String, Boolean> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, Boolean> operations) {
        this.operations = operations;
    }

    Map<String, Boolean> operations;

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

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
}
