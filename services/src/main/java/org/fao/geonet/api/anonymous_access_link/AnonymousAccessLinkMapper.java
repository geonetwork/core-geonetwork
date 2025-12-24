/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.api.anonymous_access_link;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnonymousAccessLinkMapper {

    public AnonymousAccessLinkDto toDto(AnonymousAccessLink entity) {
        if (entity == null) {
            return null;
        }
        return new AnonymousAccessLinkDto()
                .setMetadataId(entity.getMetadataId())
                .setMetadataUuid(entity.getMetadataUuid());
    }

    public AnonymousAccessLink toEntity(AnonymousAccessLinkDto dto) {
        return new AnonymousAccessLink()
                .setMetadataId(dto.getMetadataId())
                .setMetadataUuid(dto.getMetadataUuid());
    }

    public List<AnonymousAccessLinkDto> toDtoList(List<AnonymousAccessLink> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AnonymousAccessLink> toEntityList(List<AnonymousAccessLinkDto> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
