/**
 * 
 */
package org.fao.geonet.listeners.security;

import org.fao.geonet.domain.User;
import org.fao.geonet.events.user.UserEvent;

/**
 * Check that the username is correctly scaped.
 * 
 * @author delawen
 * 
 * 
 */

public class CheckUsername {


  public void check(UserEvent event) {
    User user = event.getUser();

    if (user.getUsername().contains("<")) {
      user.setUsername(user.getUsername().replaceAll("<", ""));
    }

    if (user.getSurname().contains("<")) {
      user.setSurname(user.getSurname().replaceAll("<", ""));
    }

    if (user.getName().contains("<")) {
      user.setName(user.getName().replaceAll("<", ""));
    }

    if (user.getOrganisation().contains("<")) {
      user.setOrganisation(user.getOrganisation().replaceAll("<", ""));
    }
  }

}
