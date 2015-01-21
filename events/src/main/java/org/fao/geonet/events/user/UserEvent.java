/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.User;
import org.springframework.context.ApplicationEvent;

/**
 * Abstract class for user related events. Should not be used directly. 
 * 
 * @author delawen
 *
 */
public abstract class UserEvent extends ApplicationEvent {

    private static final long serialVersionUID = 6646733956246220509L;

    private User user;

    public UserEvent(User user) {
        super(user);
        if (user == null) {
            throw new NullPointerException("User cannot be null");
        }
        this.user = user;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

}
