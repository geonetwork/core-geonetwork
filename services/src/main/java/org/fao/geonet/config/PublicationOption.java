/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.config;

import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a publication configuration.
 *
 * For example:
 *  - publish to group ALL, enabling the operations view, download and dynamic.
 *  - additionally, publish to group INTRANET enabling the same operations.
 *
 */
public class PublicationOption {
    private String name;

    // Group to publish
    private ReservedGroup publicationGroup;

    // List of operations to activate in the group to publish/unpublish.
    List<ReservedOperation> publicationOperations;

    // Additional group(s)/operations(s) to publish/unpublish when the publication is selected.
    private EnumMap<ReservedGroup, List<ReservedOperation>> additionalPublications = new EnumMap<>(ReservedGroup.class);

    PublicationOption(String name, ReservedGroup publicationGroup, List<ReservedOperation> publicationOperations) {
        this.name = name;
        this.publicationGroup = publicationGroup;
        this.publicationOperations = publicationOperations;

    }

    public String getName() {
        return name;
    }

    public ReservedGroup getPublicationGroup() {
        return publicationGroup;
    }

    public List<ReservedOperation> getPublicationOperations() {
        return publicationOperations;
    }

    public Map<ReservedGroup, List<ReservedOperation>> getAdditionalPublications() {
        return additionalPublications;
    }

    public void addAdditionalPublicationInfo(ReservedGroup group, List<ReservedOperation> operations) {
        if (!publicationGroup.equals(group)) {
            additionalPublications.put(group, operations);
        }
    }

    public List<ReservedOperation> getPublicationOperationsForGroup(ReservedGroup group) {
        if (publicationGroup.equals(group)) {
            return publicationOperations;
        } else {
            return additionalPublications.get(group);
        }
    }

    public boolean hasPublicationTo(ReservedGroup group) {
        return publicationGroup.equals(group) || !additionalPublications.containsKey(group);
    }
}
