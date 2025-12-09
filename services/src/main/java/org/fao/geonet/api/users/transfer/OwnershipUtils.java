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

package org.fao.geonet.api.users.transfer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//=============================================================================

public class OwnershipUtils {

    public static List<Element> getOwnerUsers(ServiceContext context, UserSession us) throws SQLException {
        if (!us.isAuthenticated())
            return new ArrayList<Element>();

        final List<User> allUsersThatOwnMetadata = context.getBean(UserRepository.class).findAllUsersThatOwnMetadata();

        return getUsers(context, us, allUsersThatOwnMetadata);
    }

    public static List<Element> getEditorUsers(ServiceContext context, UserSession us) throws SQLException {
        if (!us.isAuthenticated())
            return new ArrayList<Element>();

        List<User> users = context.getBean(UserRepository.class).findAll(Specification.not(UserSpecs.hasProfile(Profile.RegisteredUser)));
        return getUsers(context, us, users);
    }

    @Deprecated
    public static List<Element> getUsers(ServiceContext context, UserSession us, List<User> users) throws SQLException {

        int id = us.getUserIdAsInt();

        if (us.getProfile() == Profile.Administrator) {
            final List<Element> userXml = Lists.transform(users, new Function<User, Element>() {
                @Override
                @Nonnull
                public Element apply(@Nonnull User input) {
                    return input.asXml();

                }
            });
            return userXml;
        }

        //--- we have a user admin

        Set<String> hsMyGroups = getUserGroups(context, id);

        Set<Profile> profileSet = us.getProfile().getProfileAndAllChildren();

        //--- now filter them

        List<Element> newList = new ArrayList<Element>();

        for (User elRec : users) {
            int userId = elRec.getId();
            Profile profile = elRec.getProfile();

            if (profileSet.contains(profile)) {
                if (hsMyGroups.containsAll(getUserGroups(context, userId))) {
                    newList.add(elRec.asXml());
                }
            }
        }

        //--- return result

        return newList;
    }

    protected static Set<String> getUserGroups(ServiceContext context, int id) throws SQLException {
        HashSet<String> groupIds = new HashSet<String>();

        final List<UserGroup> users = context.getBean(UserGroupRepository.class).findAll(UserGroupSpecs.hasUserId(id));
        for (UserGroup el : users) {
            groupIds.add("" + el.getId().getGroupId());
        }

        return groupIds;
    }
}
