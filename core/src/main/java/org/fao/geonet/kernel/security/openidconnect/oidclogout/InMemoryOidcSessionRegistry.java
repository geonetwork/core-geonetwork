/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.kernel.security.openidconnect.oidclogout;


import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** * An in-memory implementation of OidcSessionRegistry that stores OIDC session information.
      Referencing org.springframework.security.oauth2.client.oidc.session.InMemoryOidcSessionRegistry
 */
public final class InMemoryOidcSessionRegistry implements OidcSessionRegistry {

    private final Map<String, OidcSessionInformation> sessions = new ConcurrentHashMap();

    public InMemoryOidcSessionRegistry() {
    }

    public void saveSessionInformation(OidcSessionInformation info) {
        this.sessions.put(info.getSessionId(), info);
    }

    public OidcSessionInformation removeSessionInformation(String clientSessionId) {
        OidcSessionInformation information = (OidcSessionInformation)this.sessions.remove(clientSessionId);
        if (information != null) {
            Log.debug(Geonet.SECURITY,"Removed client session");
        }

        return information;
    }

    public Iterable<OidcSessionInformation> removeSessionInformation(Jwt token) {
        List<String> audience = token.getClaimAsStringList("aud");
        String issuer = token.getIssuer().toString();
        String subject = token.getSubject();
        String providerSessionId = token.getClaimAsString("sid");
        Predicate<OidcSessionInformation> matcher = providerSessionId != null ? sessionIdMatcher(audience, issuer, providerSessionId) : subjectMatcher(audience, issuer, subject);

            String message = "Looking up sessions by issuer [%s] and %s [%s]";
            if (providerSessionId != null) {
                Log.debug(Geonet.SECURITY,String.format(message, issuer, "sid", providerSessionId));
            } else {
                Log.debug(Geonet.SECURITY,String.format(message, issuer, "sub", subject));
            }


        int size = this.sessions.size();
            Log.debug(Geonet.SECURITY,String.format("There are currently %d session(s) in the mapping", size));
            Log.debug(Geonet.SECURITY,this.sessions.values().toString());
        Set<OidcSessionInformation> infos = new HashSet();
        this.sessions.values().removeIf((info) -> {
            boolean result = matcher.test(info);
            if (result) {
                infos.add(info);
            }

            return result;
        });
        if (infos.isEmpty()) {
            Log.debug(Geonet.SECURITY,"Failed to remove any sessions since none matched");
        } else  {
            Log.debug(Geonet.SECURITY,String.format("Found and removed %d session(s) from mapping of %d session(s)", infos.size(), size));
        }

        return infos;
    }

    private static Predicate<OidcSessionInformation> sessionIdMatcher(List<String> audience, String issuer, String sessionId) {
        return (session) -> {
            List<String> thatAudience = session.getPrincipal().getAudience();
            String thatIssuer = session.getPrincipal().getIssuer().toString();
            String thatSessionId = session.getPrincipal().getClaimAsString("sid");
            if (thatAudience == null) {
                return false;
            } else {
                return !Collections.disjoint(audience, thatAudience) && issuer.equals(thatIssuer) && sessionId.equals(thatSessionId);
            }
        };
    }

    private static Predicate<OidcSessionInformation> subjectMatcher(List<String> audience, String issuer, String subject) {
        return (session) -> {
            List<String> thatAudience = session.getPrincipal().getAudience();
            String thatIssuer = session.getPrincipal().getIssuer().toString();
            String thatSubject = session.getPrincipal().getSubject();
            if (thatAudience == null) {
                return false;
            } else {
                return !Collections.disjoint(audience, thatAudience) && issuer.equals(thatIssuer) && subject.equals(thatSubject);
            }
        };
    }
}
