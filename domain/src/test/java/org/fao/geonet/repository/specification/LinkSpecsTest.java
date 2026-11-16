/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    MetadataLinkRepository metadataLinkRepository;

    @Autowired
    LinkStatusRepository linkStatusRepository;

    @Autowired
    OperationAllowedRepository operationAllowedRepository;

    @Autowired
    LinkRepository linkRepository;

    AtomicInteger inc = new AtomicInteger();

    @Before
    public void createTestData() {
        // Create a non harvested metadata
        Metadata metadata = MetadataRepositoryTest.newMetadata(inc);
        metadata.getSourceInfo().setGroupOwner(2);
        metadataRepository.save(metadata);

        Link link = new Link();
        link.setLinkType(LinkType.HTTP);
        link.setUrl("https://test.com/link");
        link.setLastState(1);

        ISODate checkStatusDate = new ISODate();

        Set<LinkStatus> linkStatuses = new HashSet<>();
        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setLink(link);
        linkStatus.setStatusValue("200");
        linkStatus.setCheckDate(checkStatusDate);
        linkStatuses.add(linkStatus);

        link.setLinkStatus(linkStatuses);

        MetadataLink metadataLink = new MetadataLink();
        metadataLink.setMetadataId(metadata.getId());
        metadataLink.setMetadataUuid(metadata.getUuid());
        metadataLink.setLink(link);

        Set<MetadataLink> recordLinks = new HashSet<>();
        recordLinks.add(metadataLink);
        link.setRecords(recordLinks);
        link.setLastCheck(checkStatusDate);
        linkRepository.save(link);

        metadataLinkRepository.save(metadataLink);
        linkStatusRepository.save(linkStatus);

        // View in group 2, edit in group 2 in implicit from metadata owner group
        OperationAllowed operationAllowedViewMd1 = new OperationAllowed();
        OperationAllowedId operationAllowedIdViewMd1 = new OperationAllowedId();
        operationAllowedIdViewMd1.setMetadataId(metadata.getId());
        operationAllowedIdViewMd1.setGroupId(2);
        operationAllowedIdViewMd1.setOperationId(ReservedOperation.view.getId());
        operationAllowedViewMd1.setId(operationAllowedIdViewMd1);
        operationAllowedRepository.save(operationAllowedViewMd1);

        // Edit in group 3
        OperationAllowed operationAllowedEditMd1 = new OperationAllowed();
        OperationAllowedId operationAllowedIdEditMd1 = new OperationAllowedId();
        operationAllowedIdEditMd1.setMetadataId(metadata.getId());
        operationAllowedIdEditMd1.setGroupId(3);
        operationAllowedIdEditMd1.setOperationId(ReservedOperation.editing.getId());
        operationAllowedEditMd1.setId(operationAllowedIdEditMd1);
        operationAllowedRepository.save(operationAllowedEditMd1);

        // Create a harvested metadata
        Metadata metadata2 = MetadataRepositoryTest.newMetadata(inc);
        metadata2.getSourceInfo().setGroupOwner(2);
        MetadataHarvestInfo metadataHarvestInfo = new MetadataHarvestInfo();
        metadataHarvestInfo.setHarvested(true);
        metadataHarvestInfo.setUuid(UUID.randomUUID().toString());
        metadata2.setHarvestInfo(metadataHarvestInfo);

        metadataRepository.save(metadata2);

        Link link2 = new Link();
        link2.setLinkType(LinkType.HTTP);
        link2.setUrl("https://test.com/link2");
        link2.setLastCheck(checkStatusDate);
        link2.setLastState(-1);

        Set<LinkStatus> linkStatuses2 = new HashSet<>();
        LinkStatus linkStatus2 = new LinkStatus();
        linkStatus2.setLink(link2);
        linkStatus2.setStatusValue("404");
        linkStatus2.setCheckDate(checkStatusDate);
        linkStatuses2.add(linkStatus2);

        link2.setLinkStatus(linkStatuses2);

        MetadataLink metadataLink2 = new MetadataLink();
        metadataLink2.setMetadataId(metadata2.getId());
        metadataLink2.setMetadataUuid(metadata2.getUuid());
        metadataLink2.setLink(link2);

        Set<MetadataLink> recordLinks2 = new HashSet<>();
        recordLinks2.add(metadataLink2);
        link2.setRecords(recordLinks2);
        linkRepository.save(link2);

        metadataLinkRepository.save(metadataLink2);
        linkStatusRepository.save(linkStatus2);

        // View in group 2, edit in group 2 in implicit from metadata owner group
        OperationAllowed operationAllowedViewMd2 = new OperationAllowed();
        OperationAllowedId operationAllowedIdViewMd2 = new OperationAllowedId();
        operationAllowedIdViewMd2.setMetadataId(metadata2.getId());
        operationAllowedIdViewMd2.setGroupId(2);
        operationAllowedIdViewMd2.setOperationId(ReservedOperation.view.getId());
        operationAllowedViewMd2.setId(operationAllowedIdViewMd2);
        operationAllowedRepository.save(operationAllowedViewMd2);
    }

    /**
     * Register a link to the given URL in a record of its own.
     */
    private void saveRecordLink(String url) {
        Metadata metadata = MetadataRepositoryTest.newMetadata(inc);
        metadataRepository.save(metadata);

        Link link = new Link();
        link.setLinkType(LinkType.HTTP);
        link.setUrl(url);

        MetadataLink metadataLink = new MetadataLink();
        metadataLink.setMetadataId(metadata.getId());
        metadataLink.setMetadataUuid(metadata.getUuid());
        metadataLink.setLink(link);
        link.setRecords(new HashSet<>(Collections.singletonList(metadataLink)));
        linkRepository.save(link);
        metadataLinkRepository.save(metadataLink);
    }

    @Test
    public void testLinkSpecsUrlHasHost() {
        // Both links are https://test.com/..., matched in any case.
        Assert.assertEquals(2, linkRepository.count(LinkSpecs.urlHasHost("test.com")));
        Assert.assertEquals(2, linkRepository.count(LinkSpecs.urlHasHost("TEST.COM")));

        // The whole host is compared, not a part or a prefix of it.
        Assert.assertEquals(0, linkRepository.count(LinkSpecs.urlHasHost("test.co")));
        Assert.assertEquals(0, linkRepository.count(LinkSpecs.urlHasHost("est.com")));
        Assert.assertEquals(0, linkRepository.count(LinkSpecs.urlHasHost("link")));
        Assert.assertEquals(0, linkRepository.count(LinkSpecs.urlHasHost("test2.com")));

        // A longer host that starts with a registered one counts only for itself.
        saveRecordLink("http://test.com.evil.org/wms");
        Assert.assertEquals(2, linkRepository.count(LinkSpecs.urlHasHost("test.com")));
        Assert.assertEquals(1, linkRepository.count(LinkSpecs.urlHasHost("test.com.evil.org")));

        // The host may end the URL, or be followed by a port, a query or a fragment.
        saveRecordLink("http://bare.example");
        saveRecordLink("https://port.example:8443/wms");
        saveRecordLink("http://query.example?SERVICE=WMS");
        saveRecordLink("http://fragment.example#top");
        Assert.assertEquals(1, linkRepository.count(LinkSpecs.urlHasHost("bare.example")));
        Assert.assertEquals(1, linkRepository.count(LinkSpecs.urlHasHost("port.example")));
        Assert.assertEquals(1, linkRepository.count(LinkSpecs.urlHasHost("query.example")));
        Assert.assertEquals(1, linkRepository.count(LinkSpecs.urlHasHost("fragment.example")));

        // A link written with user info does not count for its host.
        saveRecordLink("http://user@userinfo.example/wms");
        Assert.assertEquals(0, linkRepository.count(LinkSpecs.urlHasHost("userinfo.example")));
    }
}
