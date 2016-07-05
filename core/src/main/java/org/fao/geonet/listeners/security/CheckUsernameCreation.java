/**
 * 
 */
package org.fao.geonet.listeners.security;

import org.fao.geonet.events.user.UserCreated;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * Check that the username is correctly scaped on creation.
 * 
 * @author delawen
 * 
 * 
 */

@Service
public class CheckUsernameCreation extends CheckUsername
    implements ApplicationListener<UserCreated> {

  @Override
  public void onApplicationEvent(UserCreated event) {
    check(event);
  }

}
