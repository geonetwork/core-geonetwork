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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataFileDownload;
import org.fao.geonet.domain.MetadataFileUpload;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class MetadataFileDownloadRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    MetadataFileDownloadRepository _metadataFileDownloadRepo;

    @Autowired
    MetadataFileUploadRepository _metadataFileUploadRepo;

    @Autowired
    MetadataRepository _metadataRepo;

    @PersistenceContext
    EntityManager _entityManager;

    AtomicInteger _inc = new AtomicInteger();

    public static MetadataFileDownload newMetadataFileDownload(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataFileDownload metadataFileDownload = new MetadataFileDownload();

        metadataFileDownload.setFileName("name" + val);
        metadataFileDownload.setUserName("user" + val);
        metadataFileDownload.setDownloadDate(new ISODate().toString());
        metadataFileDownload.setUserName("user" + val);

        return metadataFileDownload;
    }

    @Test
    public void testFindOne() {
        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata = _metadataRepo.save(metadata);

        MetadataFileUpload fileUpload = MetadataFileUploadRepositoryTest.newMetadataFileUpload(_inc);
        fileUpload.setMetadataId(metadata.getId());
        fileUpload = _metadataFileUploadRepo.save(fileUpload);

        MetadataFileDownload fileDownload1 = newMetadataFileDownload();
        fileDownload1.setMetadataId(metadata.getId());
        fileDownload1.setFileUploadId(fileUpload.getId());
        fileDownload1 = _metadataFileDownloadRepo.save(fileDownload1);

        MetadataFileDownload fileDownload2 = newMetadataFileDownload();
        fileDownload2.setMetadataId(metadata.getId());
        fileDownload2.setFileUploadId(fileUpload.getId());
        fileDownload2 = _metadataFileDownloadRepo.save(fileDownload2);


        assertEquals(fileDownload1, _metadataFileDownloadRepo.findOne(fileDownload1.getId()));
        assertEquals(fileDownload2, _metadataFileDownloadRepo.findOne(fileDownload2.getId()));
    }

    private MetadataFileDownload newMetadataFileDownload() {
        return newMetadataFileDownload(_inc);
    }

}
