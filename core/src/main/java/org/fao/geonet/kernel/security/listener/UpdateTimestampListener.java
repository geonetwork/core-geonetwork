package org.fao.geonet.kernel.security.listener;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;

/**
 * 
 * This class logs the last succesful login events from the app.
 * 
 * Can be de/activated adding config-security-core(-overrides).xml
 * 
 * <bean class="org.fao.geonet.kernel.security.listener.UpdateTimestampListener"
 * id="updateTimestampListener"> <property name="activate" value="true"/>
 * </bean>
 * 
 * 
 * @author delawen
 * @author Jose García
 * 
 */
public class UpdateTimestampListener implements
		ApplicationListener<AbstractAuthenticationEvent> {

	@Override
	/**
	 * Depending on which type of app event we will log one or other thing.
	 */
	public void onApplicationEvent(AbstractAuthenticationEvent e) {
		UserRepository userRepo = ApplicationContextHolder.get().getBean(UserRepository.class);

		if (e instanceof InteractiveAuthenticationSuccessEvent
				|| e instanceof AuthenticationSuccessEvent
				|| e instanceof AuthenticationSwitchUserEvent) {

			try {
				UserDetails userDetails = (UserDetails) e.getAuthentication()
						.getPrincipal();

				User user = userRepo.findOneByUsername(userDetails.getUsername());
				user.setLastLoginDate(new ISODate().toString());
				userRepo.save(user);

			} catch (Exception ex) {
				// TODO: Log exception
				ex.printStackTrace();
			}

		}

	}
}
