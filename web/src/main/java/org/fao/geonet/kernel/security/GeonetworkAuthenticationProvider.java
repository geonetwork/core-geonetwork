package org.fao.geonet.kernel.security;

import java.sql.SQLException;
import java.util.Arrays;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationServiceException;
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
				updatePasswordHash(username, authentication, dbms, userXml);

				ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
				GeonetworkUser userDetails = new GeonetworkUser(profileManager, username, userXml);
				return userDetails;
			}
		} catch (Exception e) {
			try {
				resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
				dbms = null;
			} catch (Exception e2) {
				e.printStackTrace();
				Log.error(Log.JEEVES, "Error closing dbms"+dbms, e2);
			}
			Log.error(Log.JEEVES, "Unexpected error while loading user", e);
			throw new AuthenticationServiceException("Unexpected error while loading user",e);
		} finally {
			if (dbms != null){
				try {
					resourceManager.close(Geonet.Res.MAIN_DB, dbms);
				} catch (Exception e) {
					e.printStackTrace();
					Log.error(Log.JEEVES, "Error closing dbms"+dbms, e);
				}
			}
		}
		throw new UsernameNotFoundException(username+" is not a valid username");
	}

	/**
	 * The security column contains a note to update the hash from the old Geonetwork hashing to the more secure spring hashing.
	 */
	private void updatePasswordHash(String username,
			UsernamePasswordAuthenticationToken authentication, Dbms dbms,
			Element userXml) throws SQLException {
		String security = userXml.getChildText(GeonetworkUser.SECURITY_FIELD);
		if(security.contains(GeonetworkUser.HASH_UPDATE_REQUIRED)) {
			String hash = userXml.getChildTextTrim(GeonetworkUser.PASSWORD_COLUMN);

			String password = authentication.getCredentials().toString();
			if(Util.scramble(password).equals(hash) || Util.oldScramble(password).equals(hash) 
					|| encoder.matches(password, hash)) {
				StringBuilder newSec = new StringBuilder();
				for (String seg: security.split(",")) {
					if(newSec.length() > 0){
						newSec.append(',');
					}
					if(!seg.trim().equals(GeonetworkUser.HASH_UPDATE_REQUIRED)){
						newSec.append(seg);
					}
				}
				dbms.execute("UPDATE Users SET "+GeonetworkUser.SECURITY_FIELD+"=? where username=?", newSec.toString(), username);
				if(Util.scramble(password).equals(hash) || Util.oldScramble(password).equals(hash)) {
					String newPass = encoder.encode(password);
					dbms.execute("UPDATE Users SET "+GeonetworkUser.PASSWORD_COLUMN+"=? where username=?", newPass, username);
					userXml.getChild(GeonetworkUser.PASSWORD_COLUMN).setText(newPass);
				}
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext; 
		
	}

}
