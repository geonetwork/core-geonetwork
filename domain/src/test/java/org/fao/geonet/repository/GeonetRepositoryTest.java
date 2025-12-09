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

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataId;
import static org.junit.Assert.*;
import static org.springframework.data.jpa.domain.Specification.where;

/**
 * Test class for GeonetRepository.
 * <p/>
 * User: jeichar Date: 9/5/13 Time: 11:44 AM
 */
public class GeonetRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository _repo;

    @Test(expected = JpaObjectRetrievalFailureException.class)
    public void testUpdateMetadataBadId() throws Exception {
        _repo.update(123123325, new Updater<Metadata>() {
            @Nullable
            @Override
            public void apply(@Nullable Metadata input) {
                // do nothing
            }
        });
    }

    @Test
    public void testUpdateMetadataReturnSameMd() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));

        final String updatedUUID1 = "Updated uuid";
        md = _repo.update(md.getId(), new Updater<Metadata>() {
            @Nullable
            @Override
            public void apply(@Nullable Metadata input) {
                input.setUuid(updatedUUID1);
            }
        });

        assertEquals(updatedUUID1, md.getUuid());
        assertEquals(updatedUUID1, _repo.findById(md.getId()).get().getUuid());
    }

    @Test
    public void testBatchUpdateAttributes() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));


        final Specification<Metadata> spec = where((Specification<Metadata>)hasMetadataId(md2.getId())).or((Specification<Metadata>)hasMetadataId(md3.getId()));
        PathSpec<Metadata, String> dataPathSpec = new PathSpec<Metadata, String>() {
            @Override
            public Path<String> getPath(Root<Metadata> root) {
                return root.get(Metadata_.data);
            }
        };
        String newData = "Updated DataElem";

        PathSpec<Metadata, Integer> sourcePathSpec = new PathSpec<Metadata, Integer>() {
            @Override
            public Path<Integer> getPath(Root<Metadata> root) {
                return root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);
            }
        };
        Integer newGroupId = 12345678;

        final BatchUpdateQuery<Metadata> updateQuery = _repo.createBatchUpdateQuery(dataPathSpec, newData);
        updateQuery.add(sourcePathSpec, newGroupId);

        updateQuery.setSpecification(spec);

        int updated = updateQuery.execute();
        assertEquals(2, updated);


        Metadata reloadedMd1 = _repo.findById(md.getId()).get();
        assertEquals(md.getData(), reloadedMd1.getData());
        assertFalse(newGroupId == reloadedMd1.getSourceInfo().getGroupOwner());

        Metadata reloadedMd2 = _repo.findById(md2.getId()).get();
        assertEquals(newData, reloadedMd2.getData());
        assertEquals(newGroupId, reloadedMd2.getSourceInfo().getGroupOwner());

        Metadata reloadedMd3 = _repo.findById(md3.getId()).get();
        assertEquals(newData, reloadedMd3.getData());
        assertEquals(newGroupId, reloadedMd3.getSourceInfo().getGroupOwner());
    }

    @Test
    public void testDeleteAllSpec() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));


        final Specification<Metadata> spec = where((Specification<Metadata>)hasMetadataId(md2.getId())).or((Specification<Metadata>)hasMetadataId(md3.getId()));

        final int deleted = _repo.deleteAll(spec);

        assertEquals(2, deleted);
        assertEquals(1, _repo.count());

        assertNotNull(_repo.findById(md.getId()).get());
        assertFalse(_repo.findById(md2.getId()).isPresent());
        assertFalse(_repo.findById(md3.getId()).isPresent());

        assertEquals(md.getId(), _repo.findAll().get(0).getId());
    }

    @Test
    public void testFindAllAsXmlSpec() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));


        final Specification<Metadata> spec = where((Specification<Metadata>)hasMetadataId(md2.getId())).or((Specification<Metadata>)hasMetadataId(md3.getId()));

        Element xmlResponse = _repo.findAllAsXml();
        assertEquals(3, Xml.selectNodes(xmlResponse, "record").size());

        xmlResponse = _repo.findAllAsXml(spec);
        assertEquals(2, Xml.selectNodes(xmlResponse, "record").size());
        assertNotNull(Xml.selectElement(xmlResponse, "record[id='" + md2.getId() + "']"));
        assertNotNull(Xml.selectElement(xmlResponse, "record[id='" + md3.getId() + "']"));

        Sort sort = Sort.by(Sort.Direction.DESC, SortUtils.createPath(Metadata_.id));
        xmlResponse = _repo.findAllAsXml(sort);
        assertEquals(3, Xml.selectNodes(xmlResponse, "record").size());
        assertEquals("" + md3.getId(), Xml.selectElement(xmlResponse, "record[1]/id").getText());
        assertEquals("" + md2.getId(), Xml.selectElement(xmlResponse, "record[2]/id").getText());
        assertEquals("" + md.getId(), Xml.selectElement(xmlResponse, "record[3]/id").getText());

        xmlResponse = _repo.findAllAsXml(spec, sort);
        assertEquals(2, Xml.selectNodes(xmlResponse, "record").size());
        assertEquals("" + md3.getId(), Xml.selectElement(xmlResponse, "record[1]/id").getText());
        assertEquals("" + md2.getId(), Xml.selectElement(xmlResponse, "record[2]/id").getText());

        final PageRequest pageRequest = PageRequest.of(0, 2, sort);
        xmlResponse = _repo.findAllAsXml(pageRequest);
        assertEquals(2, Xml.selectNodes(xmlResponse, "record").size());
        assertEquals("" + md3.getId(), Xml.selectElement(xmlResponse, "record[1]/id").getText());
        assertEquals("" + md2.getId(), Xml.selectElement(xmlResponse, "record[2]/id").getText());

        xmlResponse = _repo.findAllAsXml(pageRequest.next());
        assertEquals(1, Xml.selectNodes(xmlResponse, "record").size());
        assertEquals("" + md.getId(), Xml.selectElement(xmlResponse, "record[1]/id").getText());

        xmlResponse = _repo.findAllAsXml(pageRequest.next().next());
        assertEquals(0, Xml.selectNodes(xmlResponse, "record").size());

        xmlResponse = _repo.findAllAsXml(spec, pageRequest);
        assertEquals(2, Xml.selectNodes(xmlResponse, "record").size());
        assertEquals("" + md3.getId(), Xml.selectElement(xmlResponse, "record[1]/id").getText());
        assertEquals("" + md2.getId(), Xml.selectElement(xmlResponse, "record[2]/id").getText());

        xmlResponse = _repo.findAllAsXml(spec, pageRequest.next());
        assertEquals(0, Xml.selectNodes(xmlResponse, "record").size());
    }
}
