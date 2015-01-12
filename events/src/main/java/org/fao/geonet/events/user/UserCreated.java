/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.User;

public class UserCreated extends UserEvent {

	private static final long serialVersionUID = 6646733956246220509L;

	/**
	 * @param user
	 */
	public UserCreated(User user) {
		super(user);
	}

}
