//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.group;

import static org.springframework.data.jpa.domain.Specifications.not;

import java.nio.file.Path;
import java.util.LinkedList;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Retrieves all groups in the system
 */

@Deprecated
public class List implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        UserSession session = context.getUserSession();
        Element elRes = null;
        if (session.getProfile().equals(Profile.Administrator)) {
            elRes = context.getBean(GroupRepository.class)
                .findAllAsXml(not(GroupSpecs.isReserved()));
        } else if (session.isAuthenticated()) {
            java.util.List<UserGroup> usergroups = context.getBean(UserGroupRepository.class).findAll(
                GroupSpecs.isEditorOrMore(session.getUserIdAsInt()));
            java.util.List<Integer> ids = new LinkedList<Integer>();
            java.util.List<Integer> editableIds = new LinkedList<Integer>();
            for (UserGroup ug : usergroups) {
                ids.add(ug.getGroup().getId());
                if (ug.getProfile().equals(Profile.UserAdmin)) {
                    editableIds.add(ug.getGroup().getId());
                }
            }
            elRes = context.getBean(GroupRepository.class).findAllAsXml(GroupSpecs.in(ids));

            for (Object o : elRes.getChildren()) {
                Element e = (Element) o;
                for (Object o2 : e.getChildren("id")) {
                    Element e2 = (Element) o2;
                    if (editableIds.contains(Integer.valueOf(e2.getTextTrim()))) {
                        e.setAttribute("editable", "true");
                    }
                }
            }

        } else {
            throw new SecurityException("You are not authorized to see this");
        }

        final Path resourcesDir = context.getBean(GeonetworkDataDirectory.class)
            .getResourcesDir();
        Resources resources = context.getBean(Resources.class);
        final Path logosDir = resources.locateLogosDir(context);
        final java.util.List<?> logoElements = Xml.selectNodes(elRes,
            "*//logo");
        for (Object logoObj : logoElements) {
            Element logoEl = (Element) logoObj;
            final String logoRef = logoEl.getTextTrim();
            if (logoRef != null && !logoRef.isEmpty()
                && !logoRef.startsWith("http://")) {
                try (Resources.ResourceHolder image = resources.getImage(context, logoRef, logosDir)) {
                    if (image != null) {
                        logoEl.setText(context.getBaseUrl() + '/' + image.getRelativePath());
                    }
                }
            }
        }

        Element elOper = params.getChild(Jeeves.Elem.OPERATION);

        if (elOper != null)
            elRes.addContent(elOper.detach());

        return elRes.setName(Jeeves.Elem.RESPONSE);
    }
}

// =============================================================================
