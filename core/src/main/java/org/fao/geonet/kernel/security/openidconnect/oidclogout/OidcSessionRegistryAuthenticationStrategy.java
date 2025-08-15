package org.fao.geonet.kernel.security.openidconnect.oidclogout;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a session authentication strategy that links an OIDC user session
 * to the current HTTP session. It saves the session information in an OIDC session registry.
 *
 * Referencing org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcSessionRegistryAuthenticationStrategy
 */
public final class OidcSessionRegistryAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Autowired
    private OidcSessionRegistry oidcSessionRegistry;

    private OidcSessionRegistryAuthenticationStrategy() {
    }

    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object var6 = authentication.getPrincipal();
            if (var6 instanceof OidcUser) {
                OidcUser user = (OidcUser)var6;
                String sessionId = session.getId();
                CsrfToken csrfToken = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
               Map<String, String> headers = csrfToken != null ? new HashMap<String, String>() {{put(csrfToken.getHeaderName(), csrfToken.getToken());
    }} : Collections.emptyMap();
                OidcSessionInformation registration = new OidcSessionInformation(sessionId, headers, user);

                Log.debug(Geonet.SECURITY, String.format("Linking a provider [%s] session to this client's session", user.getIssuer()));

                this.oidcSessionRegistry.saveSessionInformation(registration);
            }
        }
    }


}
