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

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepository;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.Assert.assertEquals;

/**
 * Tests for MetadataIdentifierTemplateSpecs.
 *
 * @author Jose García
 */
public class MetadataIdentifierTemplateSpecsTest extends AbstractSpringDataTest {


    @Autowired
    private MetadataIdentifierTemplateRepository mdIdentifierTemplateRepository;

    @Test
    public void testIsSystemProvided() throws Exception {
        MetadataIdentifierTemplate template1 = MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc);
        template1.setSystemDefault(true);
        template1 = mdIdentifierTemplateRepository.save(template1);

        MetadataIdentifierTemplate template2 = mdIdentifierTemplateRepository.save(MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc));
        MetadataIdentifierTemplate template3 = mdIdentifierTemplateRepository.save(MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc));

        final Specification<MetadataIdentifierTemplate> specificationDefault = MetadataIdentifierTemplateSpecs.isSystemProvided(true);

        long numDefaultTemplates = mdIdentifierTemplateRepository.count(specificationDefault);
        assertEquals(1, numDefaultTemplates);

        final Specification<MetadataIdentifierTemplate> specificationNoDefault = MetadataIdentifierTemplateSpecs.isSystemProvided(false);

        long numNoDefaultTemplates = mdIdentifierTemplateRepository.count(specificationNoDefault);
        assertEquals(2, numNoDefaultTemplates);

    }
}
