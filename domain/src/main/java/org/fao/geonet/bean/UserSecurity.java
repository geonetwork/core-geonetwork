package org.fao.geonet.bean;

import java.util.HashSet;
import java.util.Set;

import org.fao.geonet.domain.UserSecurityNotification;

/**
 * Encapsulates security information about the user. This is a JPA Embeddable
 * object that is embedded into a {@link User} Entity
 * 
 * @author Jesse
 */
public class UserSecurity extends GeonetEntity {
	private static final long serialVersionUID = 192387419289273L;
	private char[] _password;
	private Set<UserSecurityNotification> securityNotifications = new HashSet<UserSecurityNotification>();
	private String _authType;

	public char[] get_password() {
		return _password;
	}

	public void set_password(char[] _password) {
		this._password = _password;
	}

	public String get_authType() {
		return _authType;
	}

	public void set_authType(String _authType) {
		this._authType = _authType;
	}

	public Set<UserSecurityNotification> getSecurityNotifications() {
		return securityNotifications;
	}

	public void setSecurityNotifications(
			Set<UserSecurityNotification> securityNotifications) {
		this.securityNotifications = securityNotifications;
	}

}
