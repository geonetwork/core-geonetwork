/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.repository;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeedEntry;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.specification.InspireAtomFeedSpecs;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Transactional
public class InspireAtomFeedRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    InspireAtomFeedRepository _repo;

    @Autowired
    MetadataRepository _repoMetadata;

    AtomicInteger _inc = new AtomicInteger();
    AtomicInteger _incMetadata = new AtomicInteger();

    @Test
    public void testFindOne() {
        InspireAtomFeed feed1 = newInspireAtomFeed();
        feed1 = _repo.save(feed1);

        InspireAtomFeed feed2 = newInspireAtomFeed();
        feed2 = _repo.save(feed2);

        assertEquals(feed2, _repo.findOne(feed2.getId()));
        assertEquals(feed1, _repo.findOne(feed1.getId()));

        assertEquals(1, _repo.findOne(feed2.getId()).getEntryList().size());
        assertEquals(feed2.getEntryList().get(0), _repo.findOne(feed2.getId()).getEntryList().get(0));
    }


    @Test
    public void testCleanAtomDocuments() {
        _repo.deleteAll();

        InspireAtomFeed feed1 = newInspireAtomFeed();
        feed1 = _repo.save(feed1);

        InspireAtomFeed feed2 = newInspireAtomFeed();
        feed2 = _repo.save(feed2);

        assertEquals(2, _repo.findAll().size());

        _repo.deleteAll();

        assertEquals(0, _repo.findAll().size());
    }

    @Test
    @Ignore("Constraint exception occurs in H2.  Has to do with entry list referencing atom feed.")
    public void testCleanAtomDocumentsByMetadataId() {
        _repo.deleteAll();

        InspireAtomFeed feed1 = newInspireAtomFeed();
        feed1.setMetadataId(1);
        feed1 = _repo.save(feed1);

        assertEquals(feed1.getMetadataId(), _repo.findOne(feed1.getId()).getMetadataId());

        InspireAtomFeed feed2 = newInspireAtomFeed();
        feed2.setMetadataId(2);
        feed2 = _repo.save(feed2);
        assertEquals(feed2.getMetadataId(), _repo.findOne(feed2.getId()).getMetadataId());

        assertEquals(2, _repo.findAll().size());

        _repo.deleteAll(InspireAtomFeedSpecs.hasMetadataId(1));

        assertEquals(1, _repo.findAll().size());
    }


    @Test
    public void testRetrieveDatasetUuidFromIdentifierNs() {
        _repo.deleteAll();

        Metadata metadata1 = newMetadata();
        metadata1 = _repoMetadata.save(metadata1);

        InspireAtomFeed feed1 = newInspireAtomFeed();
        feed1.setMetadataId(metadata1.getId());
        feed1 = _repo.save(feed1);

        String metadataUuid = _repo.retrieveDatasetUuidFromIdentifierNs(feed1.getAtomDatasetid(), feed1.getAtomDatasetns());
        assertEquals(metadata1.getUuid(), metadataUuid);

        // Test no existing values
        metadataUuid = _repo.retrieveDatasetUuidFromIdentifierNs("aaa", "aaaa");
        assertTrue(StringUtils.isEmpty(metadataUuid));
    }

    private InspireAtomFeed newInspireAtomFeed() {
        int val = _inc.incrementAndGet();
        InspireAtomFeed inspireAtomFeed = new InspireAtomFeed();
        inspireAtomFeed.setTitle("title" + val);
        inspireAtomFeed.setSubtitle("subtitle" + val);
        inspireAtomFeed.setAtomUrl("http://feed/data.xml");
        inspireAtomFeed.setLang("eng");
        inspireAtomFeed.setAuthorName("authorname");
        inspireAtomFeed.setAuthorEmail("authoremail");
        inspireAtomFeed.setAtomDatasetid("datasetid" + val);
        inspireAtomFeed.setAtomDatasetns("datasetns");
        inspireAtomFeed.setAtom("atomxml");
        inspireAtomFeed.setRights("rights");

        InspireAtomFeedEntry feedEntry1 = new InspireAtomFeedEntry();
        feedEntry1.setType("type1");
        feedEntry1.setLang("eng");
        feedEntry1.setCrs("EPSG:4326");
        feedEntry1.setUrl("http://entry1");

        inspireAtomFeed.addEntry(feedEntry1);

        return inspireAtomFeed;
    }

    private Metadata newMetadata() {
        int val = _incMetadata.incrementAndGet();
        Metadata metadata = new Metadata();
        metadata.setUuid("uuid" + val).setData("metadata" + val);
        metadata.getDataInfo().setSchemaId("customSchema" + val);
        metadata.getSourceInfo().setSourceId("source" + val);
        metadata.getSourceInfo().setOwner(1);
        metadata.getHarvestInfo().setUuid("huuid" + val);
        metadata.getHarvestInfo().setHarvested(val % 2 == 0);

        return metadata;
    }
}
