/**
 * Copyright (C) 2012 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with GeoNetwork.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * @author María Arias de Reyna
 */
package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.metadata.View;
import org.jdom.Element;

import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

/**
 * main.search service. Perform a search and show metadata (if uuid on
 * parameters)
 */

public class Home implements Service {
    private ServiceConfig _config;
    private Path _appPath;

    /**
     * Save the initialize parameters. They are used if there is some metadata
     * to show
     */
    public void init(Path appPath, ServiceConfig config) throws Exception {
        this._config = config;
        this._appPath = appPath;
    }

    /**
     * UI Servlet controller
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {

        String location = context.getBaseUrl() + "/srv/"
            + context.getLanguage() + "/search";

        StringBuilder parameters = new StringBuilder();
        parameters.append("?");
        String tabs = "#";

        Element result = params;
        // Should we forward to a cleaner url?
        boolean forward = false;

        Element tmp = new Element("tmp");

        for (Object e : params.getChildren()) {
            String uuid = ((Element) e).getText();
            if (((Element) e).getName().equalsIgnoreCase("uuid")) {
                forward = true;
                tabs = tabs + "|" + uuid;
                addNewUUID(tmp, uuid);
            } else {
                parameters.append(((Element) e).getName() + "=" + uuid
                    + "&");
            }
        }
        result.addContent(tmp);

        UserSession session = context.getUserSession();
        if (forward) {// If we have uuid parameters, forward to a clean url
            session.setProperty(Geonet.Session.METADATA_UUIDS, params);
            forwardTo(context, location, parameters.toString(), tabs);
        } else { // Maybe we come from a forward, let's restore uuids:
            result = (Element) session
                .getProperty(Geonet.Session.METADATA_UUIDS);

            if (result != null) {
                session.removeProperty(Geonet.Session.METADATA_UUIDS);

                for (Object e : result.getChildren()) {
                    Element el = (Element) e;
                    if (el.getName().equalsIgnoreCase("tmp")) {
                        tmp = el;
                    }
                }

                View view = new View();
                view.init(_appPath, _config);

                result = view.exec(result, context);

                for (Object e : tmp.getChildren()) {
                    Element el = (Element) e;
                    result.addContent((Element) el.clone());
                }
            } else {
                result = params;
            }

            setUser(result, session);
        }

        return result;
    }

    /**
     * Forward the result to url
     *
     * @param context
     * @param location
     * @param parameters
     * @param tabs
     */
    private void forwardTo(ServiceContext context, String location,
                           String parameters, String tabs) {
        context.setStatusCode(HttpServletResponse.SC_MOVED_TEMPORARILY);
        context.getResponseHeaders().put("Location",
            location + parameters + tabs);
    }

    /**
     * If the user is authenticated, set up the user data on the result Element
     *
     * @param result
     * @param session
     */
    private void setUser(Element result, UserSession session) {
        Element user = new Element("user");
        user.setText("user");
        result.addContent(user);

        if (session.isAuthenticated()) {
            Element authenticated = new Element("authenticated");
            authenticated.setText("true");
            user.addContent(authenticated);

            Element name = new Element("name");
            name.setText(session.getName());
            user.addContent(name);

            Element username = new Element("username");
            username.setText(session.getUsername());
            user.addContent(username);

            Element profile = new Element("profile");
            profile.setText(session.getProfile().name());
            user.addContent(profile);
        } else {
            Element authenticated = new Element("authenticated");
            authenticated.setText("false");
            user.addContent(authenticated);
        }
    }

    /**
     * Add new UUID to result Element
     *
     * @param result
     * @param uuid
     */
    private void addNewUUID(Element result, String uuid) {
        Element articleEl2 = new Element(Jeeves.Elem.RECORD);
        articleEl2.setText(Jeeves.Elem.RECORD);

        Element uuidEl2 = new Element("uuid");
        uuidEl2.setText(uuid);
        articleEl2.addContent(uuidEl2);
        result.addContent(articleEl2);
    }
}
