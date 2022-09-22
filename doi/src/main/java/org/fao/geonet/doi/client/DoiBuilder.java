//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.doi.client;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Class to generate a DOI.
 *
 * @author Jose García
 */
public class DoiBuilder {
    @Autowired
    GroupRepository groupRepository;

    /**
     * Creates a DOI value.
     *
     * @return
     */
    public String create(String doiPattern, String prefix, AbstractMetadata record) {
        java.util.Optional<Group> groupOwner =
            record.getSourceInfo().getGroupOwner() != null
                ? groupRepository.findById(record.getSourceInfo().getGroupOwner())
                : Optional.empty();

        return prefix + "/" + doiPattern
                .replace("{{groupOwner}}", groupOwner.isPresent() ? groupOwner.get().getName() : "")
                .replace("{{id}}", record.getId() + "")
                .replace("{{uuid}}", record.getUuid());
    }
}
