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

package org.fao.geonet.api.anonymousAccessLink;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnonymousAccessLinkService {

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Autowired
    private AnonymousAccessLinkMapper mapper;

    public AnonymousAccessLinkDto createAnonymousAccessLink(AnonymousAccessLinkDto anonymousAccessLinkDto) {
        String uuid = anonymousAccessLinkDto.getMetadataUuid();
        AnonymousAccessLink anonymousAccessLinkToCreate = new AnonymousAccessLink()
                .setMetadataId(metadataUtils.findOneByUuid(uuid).getId())
                .setMetadataUuid(uuid)
                .setHash(AnonymousAccessLink.getRandomHash());
        anonymousAccessLinkRepository.save(anonymousAccessLinkToCreate);
        return mapper.toDto(anonymousAccessLinkToCreate);
    }

    public List<AnonymousAccessLinkDto> getAllAnonymousAccessLinks() {
        return mapper.toDtoList(anonymousAccessLinkRepository.findAll());
    }

    public void deleteAnonymousAccessLink(AnonymousAccessLinkDto anonymousAccessLinkDto) {
        AnonymousAccessLink linkToDelete = anonymousAccessLinkRepository
                .findOneByMetadataUuid(anonymousAccessLinkDto.getMetadataUuid());
        if (linkToDelete != null) {
            anonymousAccessLinkRepository.delete(linkToDelete);
        }
    }

}
