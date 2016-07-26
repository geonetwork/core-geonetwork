//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.guiservices.session;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Profile;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//=============================================================================

/**
 * Service used to return information about the user
 */

public class Get implements Service {
    String groupName;
    HashSet<String> outFields;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        groupName = params.getValue(Jeeves.Config.GROUP);
        List<Element> l = params.getChildren(Jeeves.Config.OUT_FIELDS,
            Jeeves.Config.FIELD);
        if (l != null) {
            outFields = new HashSet<String>();
            for (Element field : l) {
                outFields.add(field.getName());
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public Element exec(Element params, ServiceContext context) throws Exception {
        UserSession session = context.getUserSession();
        Element sEl = getSessionAsXML(session);
        if (session != null) {
            if (groupName != null) {
                Map<?, Element> group = (Map<?, Element>) session.getProperty(groupName);
                if (group != null) {
                    Element gEl = new Element(groupName);
                    for (Element child : group.values()) {
                        if (outFields == null || outFields.contains(child.getName()))
                            gEl.addContent((Element) child.clone());
                    }
                    sEl.addContent(gEl);
                }
            }
        }
        return sEl;
    }

    public static Element getSessionAsXML(UserSession session) {
        Element sEl = new Element(Jeeves.Elem.SESSION);

        if (session != null) {

            String sUsername = session.getUsername();
            String sName = session.getName();
            String sSurname = session.getSurname();
            Profile sProfile = session.getProfile();

            if (sUsername == null)
                sUsername = Profile.Guest.name();

            if (sName == null)
                sName = sUsername;

            if (sSurname == null)
                sSurname = "";

            if (sProfile == null)
                sProfile = Profile.Guest;

            Element userId = new Element("userId").addContent(session.getUserId());
            Element username = new Element("username").addContent(sUsername);
            Element name = new Element("name").addContent(sName);
            Element surname = new Element("surname").addContent(sSurname);
            Element profile = new Element("profile").addContent(sProfile.name());

            sEl
                .addContent(userId)
                .addContent(username)
                .addContent(name)
                .addContent(surname)
                .addContent(profile);
        }
        return sEl;
    }
}

//=============================================================================

