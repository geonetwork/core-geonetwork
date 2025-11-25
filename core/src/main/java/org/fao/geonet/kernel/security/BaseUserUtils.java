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
package org.fao.geonet.kernel.security;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.GroupType;
import org.fao.geonet.domain.Language;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Utility class for managing user groups in the GeoNetwork security system.
 * <p>
 * This service provides methods to retrieve or create groups by name, ensuring that groups
 * exist before assigning them to users. It is used or extended by various authentication providers
 * (JWT Headers, Keycloak, OIDC) to manage user-group associations.
 * </p>
 * <p>
 * When creating a new group, this utility automatically populates label translations for all
 * available languages in the system.
 * </p>
 */
@Service
public class BaseUserUtils {

    @Autowired
    LanguageRepository langRepository;

    @Autowired
    GroupRepository groupRepository;

    /**
     * Get or create a group by name. Defaults to Workspace type.
     * <p>
     * This method retrieves an existing group from the database by name. If the group does not
     * exist, a new group is created with the specified name and a default type of {@link GroupType#Workspace}.
     * Label translations are automatically populated for all available languages using the group name.
     * </p>
     *
     * @param groupName the name of the group.
     * @return the group (either existing or newly created).
     */
    public Group getOrCreateGroup(String groupName) {
        return getOrCreateGroup(groupName, GroupType.Workspace);
    }

    /**
     * Get or create a group by name.
     * <p>
     * This method retrieves an existing group from the database by name. If the group does not
     * exist, a new group is created with the specified name and type. Label translations are
     * automatically populated for all available languages using the group name.
     * </p>
     * <p>
     * This method is commonly used by authentication providers to ensure that groups referenced
     * in user tokens or authentication responses exist in the database before assigning users
     * to those groups.
     * </p>
     *
     * @param groupName    the name of the group.
     * @param newGroupType the type of the group to be used if creating a new group. Must not be null.
     * @return the group (either existing or newly created).
     */
    public Group getOrCreateGroup(String groupName, @Nonnull GroupType newGroupType) {
        Group group = groupRepository.findByName(groupName);

        if (group != null && group.getType() != null && !group.getType().equals(newGroupType)) {
            // Log a warning if the existing group's type differs from the requested type
            System.out.println("Warning: Group '" + groupName + "' exists with type '" + group.getType() +
                "', but requested type is '" + newGroupType + "'. Using existing group type.");
        } else if (group == null) {
            group = new Group();
            group.setName(groupName);
            group.setType(newGroupType);

            // Populate languages for the group
            for (Language l : langRepository.findAll()) {
                group.getLabelTranslations().put(l.getId(), group.getName());
            }

            groupRepository.save(group);
        }

        return group;
    }

}
