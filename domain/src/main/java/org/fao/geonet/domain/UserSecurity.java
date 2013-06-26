package org.fao.geonet.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Encapsulates security information about the user.
 *
 * @author Jesse
 */
@Embeddable
public class UserSecurity {
    private char[] password;
    private String securityNotifications;
    private String authType;

    @Column(nullable=false, length=120)
    public char[] getPassword() {
        return password;
    }
    public UserSecurity setPassword(char[] password) {
        this.password = password;
        return this;
    }
    @Column(name="security", length = 128)
    public String getSecurityNotifications() {
        return securityNotifications;
    }
    public UserSecurity setSecurityNotifications(String securityNotifications) {
        this.securityNotifications = securityNotifications;
        return this;
    }
    @Column(name="authtype", length=32)
    public String getAuthType() {
        return authType;
    }
    public UserSecurity setAuthType(String authType) {
        this.authType = authType;
        return this;
    }
}
