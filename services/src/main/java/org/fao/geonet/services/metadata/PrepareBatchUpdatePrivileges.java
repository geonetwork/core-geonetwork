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

package org.fao.geonet.services.metadata;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationRepository;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

//=============================================================================

/**
 * Return all groups of the current user with operations included.
 */
@Deprecated
public class PrepareBatchUpdatePrivileges implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        AccessManager am = gc.getBean(AccessManager.class);
        UserSession us = context.getUserSession();

        Element ownerId = new Element("ownerid").setText(us.getUserId());
        Element hasOwner = new Element("owner").setText("true");

        //--- get all operations
        List<Operation> operationList = context.getBean(OperationRepository.class).findAll();

        // elOper is for returned XML and keeps backwards compatibility
        Element elOper = new Element(Geonet.Elem.OPERATIONS);

        for (Operation operation : operationList) {
            elOper.addContent(operation.asXml());
        }

        //--- retrieve groups operations
        Set<Integer> userGroups = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

        Element elGroup = context.getBean(GroupRepository.class).findAllAsXml();

        @SuppressWarnings("unchecked")
        List<Element> list = elGroup.getChildren();

        for (Element el : list) {
            el.setName(Geonet.Elem.GROUP);

            //--- get all operations that this group can do on given metadata
            String sGrpId = el.getChildText("id");

            el.setAttribute("userGroup", userGroups.contains(Integer.valueOf(sGrpId)) ? "true" : "false");

            //--- now extend the group list adding proper operations
            @SuppressWarnings("unchecked")
            List<Element> listOper = elOper.getChildren();

            for (int j = 0; j < listOper.size(); j++) {
                String operId = ((Element) listOper.get(j)).getChildText("id");
                Element elGrpOper = new Element(Geonet.Elem.OPER)
                    .addContent(new Element(Geonet.Elem.ID).setText(operId));
                el.addContent(elGrpOper);
            }
        }

        //-----------------------------------------------------------------------
        //--- put all together

        Element elRes = new Element(Jeeves.Elem.RESPONSE)
            .addContent(elOper)
            .addContent(elGroup)
            .addContent(ownerId)
            .addContent(hasOwner);

        return elRes;
    }
}

//=============================================================================


