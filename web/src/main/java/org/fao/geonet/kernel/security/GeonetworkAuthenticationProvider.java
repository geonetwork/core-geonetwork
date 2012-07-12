package org.fao.geonet.kernel.security;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GeonetworkAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider 
	implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private PasswordEncoder encoder;
	
	public GeonetworkAuthenticationProvider(PasswordEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		GeonetworkUser gnDetails = (GeonetworkUser) userDetails;
		if (authentication.getCredentials() == null) {
			Log.warning(Log.JEEVES, "Authentication failed: no credentials provided");
			throw new BadCredentialsException("Authentication failed: no credentials provided");
		}

		if (!encoder.matches(authentication.getCredentials().toString(), gnDetails.getPassword())) {
			Log.warning(Log.JEEVES, "Authentication failed: wrong password provided");
			throw new BadCredentialsException("Authentication failed: wrong password provided");
		}
	}

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		Dbms dbms = null;
		ResourceManager resourceManager = null;
		try {
			resourceManager = applicationContext.getBean(ResourceManager.class);
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
			Element selectRequest = dbms.select("SELECT * FROM Users where username=?", username);
			Element userXml = selectRequest.getChild("record");
			if (userXml != null) {
				ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
				GeonetworkUser userDetails = new GeonetworkUser(profileManager, username, userXml);
				return userDetails;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbms != null){
				try {
					resourceManager.close(Geonet.Res.MAIN_DB, dbms);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		throw new UsernameNotFoundException(username+" is not a valid username");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext; 
		
	}

}
