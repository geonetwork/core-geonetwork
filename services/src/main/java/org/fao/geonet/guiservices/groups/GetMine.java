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

package org.fao.geonet.guiservices.groups;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specifications;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.not;
import static org.springframework.data.jpa.domain.Specifications.where;

//=============================================================================

/**
 * Service used to return all groups in the system
 */

public class GetMine implements Service {
    String profile;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        profile = params.getValue("profile");
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        UserSession session = context.getUserSession();

        if (!session.isAuthenticated()) {
            return new Element(Geonet.Elem.GROUPS);
        }

        //--- retrieve user groups

        if (Profile.Administrator == session.getProfile()) {
            return context.getBean(GroupRepository.class).findAllAsXml(not(GroupSpecs.isReserved()));
        } else {

            final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
            int userId = session.getUserIdAsInt();
            Specifications<UserGroup> spec = where(UserGroupSpecs.isReservedGroup(false)).and(UserGroupSpecs.hasUserId(userId));

            if (profile != null) {
                spec = spec.and(UserGroupSpecs.hasProfile(Profile.findProfileIgnoreCase(profile)));
            }

            List<Integer> ids = userGroupRepository.findGroupIds(spec);
            Element groups = context.getBean(GroupRepository.class).findAllAsXml(SortUtils.createSort(Group_.id));

            return Lib.element.pruneChildren(groups, new HashSet<Integer>(ids));
        }
    }
}

//=============================================================================

