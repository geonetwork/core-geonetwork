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

package org.fao.geonet.services.ownership;

import com.google.common.collect.Iterables;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;

//=============================================================================
@Deprecated
public class Groups implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        int userId = Util.getParamAsInt(params, "id");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        UserSession us = context.getUserSession();
        AccessManager am = gc.getBean(AccessManager.class);

        Set<Integer> userGroups = am.getVisibleGroups(userId);
        Set<Integer> myGroups = am.getUserGroups(us, null, false);

        //--- remove 'Intranet' and 'All' groups
        myGroups.remove(ReservedGroup.intranet.getId());
        myGroups.remove(ReservedGroup.all.getId());

        Element response = new Element("response");

        OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        for (Integer groupId : userGroups) {
            Specifications<OperationAllowed> spec = where(hasGroupId(groupId)).and(hasMetadataId(userId));
            long count = opAllowedRepo.count(spec);

            if (count > 0) {
                Group group = groupRepository.findOne(groupId);

                if (group != null) {
                    Element record = group.asXml();
                    record.detach();
                    record.setName("group");

                    response.addContent(record);
                }
            }
        }

        for (Integer groupId : myGroups) {
            @SuppressWarnings("unchecked")
            Group group = groupRepository.findOne(groupId);

            if (group != null) {
                Element record = group.asXml();
                record.detach();
                record.setName("targetGroup");
                response.addContent(record);

                // List all group users or administrator
                final List<User> administrators = context.getBean(UserRepository.class).findAllByProfile(Profile.Administrator);

                final Specification<UserGroup> userGroupSpec = not(UserGroupSpecs.hasProfile(Profile.RegisteredUser)).and(UserGroupSpecs.hasGroupId(groupId));

                final List<User> nonRegisteredUsersInGroups = context.getBean(UserRepository.class).findAllUsersInUserGroups(userGroupSpec);
                // Avoid duplicated users: if a user is a Reviewer in a group, in the database exists a row as Editor also
                Set<Integer> editorsId = new HashSet<Integer>();

                for (User editor : Iterables.concat(administrators, nonRegisteredUsersInGroups)) {
                    if (editorsId.contains(editor.getId())) {
                        continue;
                    }
                    record.addContent(editor.asXml().setName("editor"));
                    editorsId.add(editor.getId());
                }
            }
        }

        return response;
    }
}

//=============================================================================

