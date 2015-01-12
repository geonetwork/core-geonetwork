Example of class that listen to events in geoNetwork:
Remember to make it a service or some other annotation Spring auto-loads.


package org.fao.geonet.events.user;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class EventTest implements ApplicationListener<UserDeleted> {
	@Override
	public void onApplicationEvent(UserDeleted event) {
		System.out.println(event);
	}
}

