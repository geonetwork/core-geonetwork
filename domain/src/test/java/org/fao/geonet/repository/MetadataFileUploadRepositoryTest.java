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

import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Transactional
public class MetadataFileUploadRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    MetadataFileUploadRepository _metadataFileUploadRepo;

    @Autowired
    MetadataRepository _metadataRepo;

    @PersistenceContext
    EntityManager _entityManager;

    AtomicInteger _inc = new AtomicInteger();

    public static MetadataFileUpload newMetadataFileUpload(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataFileUpload metadataFileUpload = new MetadataFileUpload();

        metadataFileUpload.setFileName("name" + val);
        metadataFileUpload.setFileSize(200.0);
        metadataFileUpload.setUploadDate(new ISODate().toString());
        metadataFileUpload.setUserName("user" + val);

        return metadataFileUpload;
    }

    @Test
    public void testFindOne() {
        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata = _metadataRepo.save(metadata);

        MetadataFileUpload fileUpload1 = newMetadataFileUpload();
        fileUpload1.setMetadataId(metadata.getId());
        fileUpload1 = _metadataFileUploadRepo.save(fileUpload1);

        MetadataFileUpload fileUpload2 = newMetadataFileUpload();
        fileUpload2.setMetadataId(metadata.getId());
        fileUpload2 = _metadataFileUploadRepo.save(fileUpload2);

        assertEquals(fileUpload1, _metadataFileUploadRepo.findOne(fileUpload1.getId()));
        assertEquals(fileUpload2, _metadataFileUploadRepo.findOne(fileUpload2.getId()));
    }

    @Test
    public void testByMetadataIdAndFileNameNotDeleted() {
        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata = _metadataRepo.save(metadata);

        MetadataFileUpload fileUpload1 = newMetadataFileUpload();
        fileUpload1.setMetadataId(metadata.getId());
        fileUpload1 = _metadataFileUploadRepo.save(fileUpload1);

        MetadataFileUpload fileUpload2 = newMetadataFileUpload();
        fileUpload2.setMetadataId(metadata.getId());
        fileUpload2.setDeletedDate(new ISODate().toString());
        fileUpload2 = _metadataFileUploadRepo.save(fileUpload2);

        assertEquals(fileUpload1, _metadataFileUploadRepo.findByMetadataIdAndFileNameNotDeleted(
            metadata.getId(), fileUpload1.getFileName()));

        try {
            _metadataFileUploadRepo.findByMetadataIdAndFileNameNotDeleted(metadata.getId(), fileUpload2.getFileName());
            fail();
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {

        }
    }

    private MetadataFileUpload newMetadataFileUpload() {
        return newMetadataFileUpload(_inc);
    }

}
