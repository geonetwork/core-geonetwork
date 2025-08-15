package org.fao.geonet.kernel.security.openidconnect.oidclogout;

import org.springframework.security.oauth2.jwt.Jwt;

public interface OidcSessionRegistry {
    void saveSessionInformation(OidcSessionInformation info);

    OidcSessionInformation removeSessionInformation(String clientSessionId);

    Iterable<OidcSessionInformation> removeSessionInformation(Jwt logoutToken);
}
