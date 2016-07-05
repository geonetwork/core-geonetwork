/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.User;

/** Event launched when a user gets updated **/
public class UserUpdated extends UserEvent {

    private static final long serialVersionUID = -4051701002450179299L;

    public UserUpdated(User user) {
        super(user);
    }
}
