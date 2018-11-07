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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic Metadata and Draft Tests
 * 
 * @author delawen María Arias de Reyna
 */
public class BasicAbstractMetadataTest extends AbstractSpringDataTest {
	
    @Autowired
    MetadataRepository _metadatarepo;
    
    @Autowired
    MetadataDraftRepository _metadatadraftrepo;

    @Autowired
    MetadataCategoryRepository _categoryRepo;

    @Test
    public void testMetadataWithRepositoryDAO() throws Exception {
        Metadata md = new Metadata();
        populate(md);
        _metadatarepo.save(md);
        
        md = _metadatarepo.findOneByUuid(md.getUuid());
        
        assertNotNull(md);
        
        _metadatarepo.delete(md);
        
        md = _metadatarepo.findOneByUuid(md.getUuid());
        
        assertNull(md);
    }
    
    @Test
    public void testDraftWithRepositoryDAO() throws Exception {
    	MetadataDraft md = new MetadataDraft();
        populate(md);
        _metadatadraftrepo.save(md);
        
        md = _metadatadraftrepo.findOneByUuid(md.getUuid());
        
        assertNotNull(md);
        
        _metadatadraftrepo.delete(md);
        
        md = _metadatadraftrepo.findOneByUuid(md.getUuid());
        
        assertNull(md);
    }

	private void populate(AbstractMetadata md) {
		md.setUuid("test-metadata");
        md.setData("<xml></xml>");
        md.getSourceInfo().setGroupOwner(1);
        md.getSourceInfo().setOwner(1);
        md.getSourceInfo().setSourceId("test-faking");
        md.getDataInfo().setSchemaId("isoFake");
	}

    
    
}
