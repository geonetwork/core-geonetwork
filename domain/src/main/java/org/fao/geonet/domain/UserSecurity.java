package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Encapsulates security information about the user. This is a JPA Embeddable object that is embedded into a {@link User} Entity
 * 
 * @author Jesse
 */
@Embeddable
public class UserSecurity {
    private char[] password;
    private String securityNotifications = "";
    private String authType;

    /**
     * Get the hashed password.  This is a required property.
     *
     * @return the hashed password
     */
    @Column(nullable = false, length = 120)
    public @Nonnull char[] getPassword() {
        return password;
    }

    /**
     * Set the hashed password.  This is a required property.
     * 
     * @param password the hashed password.
     * @return this UserSecurity object
     */
    public @Nonnull UserSecurity setPassword(@Nonnull char[] password) {
        this.password = password;
        return this;
    }

    /**
     * Set the hashed password.  This is a required property.
     * 
     * @param password the hashed password.
     * @return this UserSecurity object
     */
    public UserSecurity setPassword(String password) {
        setPassword(password.toCharArray());
        return this;
    }

    @Column(name = "security", length = 128)
    public String getSecurityNotifications() {
        return securityNotifications;
    }

    public UserSecurity setSecurityNotifications(String securityNotifications) {
        this.securityNotifications = securityNotifications;
        return this;
    }

    @Column(name = "authtype", length = 32)
    public String getAuthType() {
        return authType;
    }

    public UserSecurity setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    /**
     * Merge all data from other security into this security.
     * 
     * @param otherSecurity other user to merge data from.
     * @param mergeNullData if true then also set null values from other security. If false then only merge non-null data
     */
    public void mergeSecurity(UserSecurity otherSecurity, boolean mergeNullData) {
        if (mergeNullData || otherSecurity.getPassword() != null) {
            setPassword(otherSecurity.getPassword());
        }
        if (mergeNullData || otherSecurity.getSecurityNotifications() != null) {
            setSecurityNotifications(otherSecurity.getSecurityNotifications());
        }
        if (mergeNullData || otherSecurity.getAuthType() != null) {
            setAuthType(otherSecurity.getAuthType());
        }

    }
}
