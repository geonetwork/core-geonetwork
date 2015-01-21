/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.UserGroup;

/**
 * Event launched when a user joins a group
 * 
 * @author delawen
 *
 */
public class GroupJoined extends UserUpdated {

    private static final long serialVersionUID = 66462987237984509L;

    private UserGroup userGroup;

    public GroupJoined(UserGroup userGroup) {
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
