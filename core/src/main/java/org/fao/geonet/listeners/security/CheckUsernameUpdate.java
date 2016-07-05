/**
 * 
 */
package org.fao.geonet.listeners.security;

import org.fao.geonet.events.user.UserUpdated;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * Check that the username is correctly scaped on update.
 * 
 * @author delawen
 * 
 * 
 */

@Service
public class CheckUsernameUpdate extends CheckUsername
    implements ApplicationListener<UserUpdated> {

  @Override
  public void onApplicationEvent(UserUpdated event) {
    check(event);
  }

}
