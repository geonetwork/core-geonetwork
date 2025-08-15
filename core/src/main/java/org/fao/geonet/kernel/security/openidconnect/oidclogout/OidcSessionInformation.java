package org.fao.geonet.kernel.security.openidconnect.oidclogout;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/** *
 *     An OIDC session information class that extends Spring Security's SessionInformation.
      Referencing org.springframework.security.oauth2.client.oidc.session.OidcSessionInformation
 */
public class OidcSessionInformation extends SessionInformation {
    private static final long serialVersionUID = -1703808683027974918L;
    private final Map<String, String> authorities;

    public OidcSessionInformation(String sessionId, Map<String, String> authorities, OidcUser user) {
        super(user, sessionId, new Date());
        this.authorities = (Map)(authorities != null ? new LinkedHashMap(authorities) : Collections.emptyMap());
    }

    public Map<String, String> getAuthorities() {
        return this.authorities;
    }

    public OidcUser getPrincipal() {
        return (OidcUser)super.getPrincipal();
    }

    public OidcSessionInformation withSessionId(String sessionId) {
        return new OidcSessionInformation(sessionId, this.getAuthorities(), this.getPrincipal());
    }
}
