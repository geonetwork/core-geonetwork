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

import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnonymousAccessLinkService {

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Autowired
    private EsRestClient esRestClient;

    @Value("${es.index.records:gn-records}")
    private String defaultIndex = "records";

    @Autowired
    private AnonymousAccessLinkMapper mapper;

    public AnonymousAccessLinkDto createAnonymousAccessLink(String uuid) throws ResourceAlreadyExistException {
        if (anonymousAccessLinkRepository.findOneByMetadataUuid(uuid) != null) {
            throw new ResourceAlreadyExistException();
        }
        String randomHash = AnonymousAccessLink.getRandomHash();
        AnonymousAccessLink anonymousAccessLinkToCreate = new AnonymousAccessLink()
                .setMetadataId(metadataUtils.findOneByUuid(uuid).getId())
                .setMetadataUuid(uuid)
                .setHash(randomHash);
        anonymousAccessLinkRepository.save(anonymousAccessLinkToCreate);
        return mapper.toDto(anonymousAccessLinkToCreate).setHash(randomHash);
    }

    public List<AnonymousAccessLinkDto> getAllAnonymousAccessLinksWithMdInfos() throws IOException {
        List<AnonymousAccessLink> anonymousAccessLinks = anonymousAccessLinkRepository.findAll();

        List<String> uuids = anonymousAccessLinks.stream().map(AnonymousAccessLink::getMetadataUuid).collect(Collectors.toList());
        MgetRequest request = new MgetRequest.Builder()
                .ids(uuids)
                .index(defaultIndex)
                .sourceIncludes("resourceTitleObject", "recordOwner", "dateStamp", "resourceAbstractObject")
                .build();
        MgetResponse<Object> response = esRestClient.getClient().mget(request, Object.class);

        return IntStream.range(0, anonymousAccessLinks.size()) //
                .mapToObj(i -> mapper.toDto(anonymousAccessLinks.get(i)).setGetResultSource(response.docs().get(i).result().source()))
                .collect(Collectors.toList());
    }

    public AnonymousAccessLinkDto getAnonymousAccessLink(String uuid) {
        return mapper.toDto(anonymousAccessLinkRepository.findOneByMetadataUuid(uuid));
    }

    public void deleteAnonymousAccessLink(String uuid) {
        AnonymousAccessLink linkToDelete = anonymousAccessLinkRepository.findOneByMetadataUuid(uuid);
        if (linkToDelete != null) {
            anonymousAccessLinkRepository.delete(linkToDelete);
        }
    }
}