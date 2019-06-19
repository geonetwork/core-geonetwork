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

import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.services.Utils;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Given a metadata id returns all operation allowed on it. Called by the metadata.admin service
 */
@Deprecated
public class GetAdminOper implements Service {
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
        AccessManager am = context.getBean(AccessManager.class);

        String metadataId = Utils.getIdentifierFromParameters(params, context);

        //-----------------------------------------------------------------------
        //--- check access

        AbstractMetadata info = context.getBean(IMetadataUtils.class).findOne(metadataId);

        if (info == null)
            throw new MetadataNotFoundEx(metadataId);

        Element ownerId = new Element("ownerid").setText(info.getSourceInfo().getOwner() + "");
        Element groupOwner = new Element("groupOwner").setText(info.getSourceInfo().getGroupOwner() + "");
        Element hasOwner = new Element("owner");
        if (am.isOwner(context, metadataId))
            hasOwner.setText("true");
        else
            hasOwner.setText("false");

        //--- get all operations

        OperationRepository opRepository = context.getBean(OperationRepository.class);
        List<Operation> operationList = opRepository.findAll();


        // elOper is for returned XML and keeps backwards compatibility
        Element elOper = new Element(Geonet.Elem.OPERATIONS);

        for (Operation operation : operationList) {
            elOper.addContent(operation.asXml());
        }

        //-----------------------------------------------------------------------
        //--- retrieve groups operations

        Set<Integer> userGroups = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

        Element elGroup = context.getBean(GroupRepository.class).findAllAsXml();

        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
        OperationAllowedRepository opAllowRepository = context.getBean(OperationAllowedRepository.class);

        @SuppressWarnings("unchecked")
        List<Element> list = elGroup.getChildren();
        for (Element el : list) {
            el.setName(Geonet.Elem.GROUP);
            String sGrpId = el.getChildText("id");
            int grpId = Integer.parseInt(sGrpId);

            //--- get all group informations (user member and user profile)

            el.setAttribute("userGroup", userGroups.contains(grpId) ? "true" : "false");

            final Specification<UserGroup> hasGroupId = UserGroupSpecs.hasGroupId(grpId);
            final Specification<UserGroup> hasUserId = UserGroupSpecs.hasUserId(context.getUserSession().getUserIdAsInt());
            final Specifications<UserGroup> hasUserIdAndGroupId = where(hasGroupId).and(hasUserId);
            List<UserGroup> userGroupEntities = userGroupRepository.findAll(hasUserIdAndGroupId);
            for (UserGroup ug : userGroupEntities) {
                el.addContent(new Element("userProfile").setText(ug.getProfile().toString()));
            }

            //--- get all operations that this group can do on given metadata
            Specifications<OperationAllowed> hasGroupIdAndMetadataId = where(OperationAllowedSpecs.hasGroupId(grpId)).and
                (OperationAllowedSpecs.hasMetadataId(metadataId));
            List<OperationAllowed> opAllowList = opAllowRepository.findAll(hasGroupIdAndMetadataId);

            for (Operation operation : operationList) {
                String operId = Integer.toString(operation.getId());

                Element elGrpOper = new Element(Geonet.Elem.OPER)
                    .addContent(new Element(Geonet.Elem.ID).setText(operId));

                boolean bFound = false;

                for (OperationAllowed operationAllowed : opAllowList) {
                    if (operation.getId() == operationAllowed.getId().getOperationId()) {
                        bFound = true;
                        break;
                    }
                }

                if (bFound) {
                    elGrpOper.addContent(new Element(Geonet.Elem.ON));
                }

                el.addContent(elGrpOper);
            }
        }

        //-----------------------------------------------------------------------
        //--- put all together

        Element elRes = new Element(Jeeves.Elem.RESPONSE)
            .addContent(new Element(Geonet.Elem.ID).setText(metadataId))
            .addContent(elOper)
            .addContent(elGroup)
            .addContent(ownerId)
            .addContent(hasOwner)
            .addContent(groupOwner);

        return elRes;
    }
}

//=============================================================================


