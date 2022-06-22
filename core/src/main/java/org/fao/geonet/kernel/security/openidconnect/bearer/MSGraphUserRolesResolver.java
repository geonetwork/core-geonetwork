/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.fao.geonet.kernel.security.openidconnect.OIDCConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * make sure your Azure AD application has "GroupMember.Read.All" permission:
 * a) go to your application in Azure AD (in the portal)
 * b) On the left, go to "API permissions"
 * c) click "Add a permission"
 * d) press "Microsoft Graph"
 * e) press "Delegated permission"
 * f) Scroll down to "GroupMember"
 * g) Choose "GroupMemeber.Read.All"
 * h) press "Add permission"
 * i) on the API Permission screen, press the "Grant admin consent for ..." text
 * <p>
 * This class will go to the "https://graph.microsoft.com/v1.0/me/memberOf" and attach your access token.
 * It will then read the response and find all the user's groups.
 * <p>
 * NOTE: to be consistent with the rest of Azure, we use the Groups OID (guid) NOT its name.
 */
public class MSGraphUserRolesResolver implements UserRolesResolver {

    public static URL memberOfEndpoint;

    static {
        try {
            memberOfEndpoint = new URL("https://graph.microsoft.com/v1.0/me/memberOf");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    OIDCConfiguration oidcConfiguration;


    /**
     * talk to the actual azure graph api to get user's group memberships.
     *    1. attaches the access token to the request.
     *    2. sets the Accepts header to  "application/json"  (required)
     *
     * @param accessToken
     * @return
     * @throws IOException
     */
    public String resolveUrl(String accessToken) throws IOException {
        String tokenHeaderValue = "Bearer " + accessToken;
        String tokenHeaderName = "Authorization";

        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection) memberOfEndpoint.openConnection();
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty(tokenHeaderName, tokenHeaderValue);
            String result = new BufferedReader(new InputStreamReader(http.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
            return result;
        } finally {
            if (http != null)
                http.disconnect();
        }
    }

    // parses the resulting json from the user's group memberships json result.
    //returns a list of the groups (object id) that the user is a member of.
    public List<String> parseJson(String jsonString) throws JSONException {
        List<String> result = new ArrayList<>();
        JSONObject json = new JSONObject(jsonString);
        JSONArray values = json.getJSONArray("value");
        for (int i = 0; i < values.length(); i++) {
            JSONObject object = (JSONObject) values.get(i);
            if (!object.get("@odata.type").equals("#microsoft.graph.group"))
                continue;
            result.add(object.get("id").toString());
        }
        return result;
    }

    @Override
    public List<String> resolveRoles(String tokenValue, Map claims, OidcUserInfo userInfo) throws Exception {
        try {
            String jsonStr = resolveUrl(tokenValue);
            List<String> result = parseJson(jsonStr);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}
