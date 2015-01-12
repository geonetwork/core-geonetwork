/**
 * 
 */
package org.fao.geonet.events.user;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;

public class GroupLeft extends UserUpdated {

	private static final long serialVersionUID = 62462987237984509L;
	
	private Group group;

	public GroupLeft(User user, Group group) {
		super(user);
		if(group == null) {
			throw new NullPointerException("Group cannot be null");
		}
		this.group = group;
	}
	
	/**
	 * @return the group
	 */
	public Group getGroup() {
		return group;
	}

}
