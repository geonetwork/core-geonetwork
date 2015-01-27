/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.User;

/**
 * Event launched when a user is removed from the database
 * 
 * @author delawen
 *
 */
public class UserDeleted extends UserEvent {

    private static final long serialVersionUID = 664629348720509L;

    public UserDeleted(User u) {
        super(u);
    }

}
