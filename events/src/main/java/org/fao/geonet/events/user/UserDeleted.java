/**
 * 
 */
package org.fao.geonet.events.user;

import org.springframework.context.ApplicationEvent;

public class UserDeleted extends ApplicationEvent {

	private static final long serialVersionUID = 664629348720509L;
	
	private Integer id;

	public UserDeleted(Integer id) {
		super(id);
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

}
