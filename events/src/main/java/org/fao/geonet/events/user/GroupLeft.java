/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.UserGroup;

public class GroupLeft extends UserUpdated {

    private static final long serialVersionUID = 62462987237984509L;

    private UserGroup userGroup;

    public GroupLeft(UserGroup userGroup) {
        super(userGroup.getUser());
        if (userGroup.getGroup() == null) {
            throw new NullPointerException("Group cannot be null");
        }
        this.userGroup = userGroup;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

}
