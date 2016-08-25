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
package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.records.editing.BatchEditsApi;
import org.fao.geonet.api.records.model.BatchEditParameter;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BatchEditsServiceTest extends AbstractServiceIntegrationTest {

    List<String> uuids = new ArrayList();
    String firstMetadataId = null;
    ServiceContext context;
    @Autowired
    private MetadataRepository repository;

    @Before
    public void loadSamples() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();
        List<String> importedRecordUuids = importMetadata.getMetadataIds();

        // Check record are imported
        for (String id : importedRecordUuids) {
            final String uuid = repository.findOne(Integer.valueOf(id)).getUuid();
            uuids.add(uuid);
            if (firstMetadataId == null) {
                firstMetadataId = uuid;
            }
        }
        assertEquals(3, repository.count());
    }


    @Test
    public void testParameterMustBeSet() throws Exception {
        final BatchEditsApi batchEditsService = new BatchEditsApi();
        batchEditsService.setApplicationContext(context.getApplicationContext());
        final BatchEditParameter[] parameters = new BatchEditParameter[]{};

        try {
            batchEditsService.batchEdit(
                new String[]{firstMetadataId},
                parameters);
        } catch (java.lang.IllegalArgumentException exception) {
            assertSame("Service MUST fail if no parameter are defined",
                exception.getClass(), IllegalArgumentException.class);
        }
    }


    @Test
    public void testUpdateRecord() throws Exception {
        final BatchEditsApi batchEditsService = new BatchEditsApi();
        batchEditsService.setApplicationContext(context.getApplicationContext());

        final BatchEditParameter[] listOfupdates = new BatchEditParameter[]{
            new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                    "gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                "## UPDATED TITLE ##"
            ),
            new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                    "gmd:abstract/gco:CharacterString",
                "## UPDATED ABSTRACT ##"
            )
        };

        batchEditsService.batchEdit(
            new String[]{firstMetadataId},
            listOfupdates);
        Metadata updatedRecord = repository.findOneByUuid(firstMetadataId);
        Element xml = Xml.loadString(updatedRecord.getData(), false);

        for (BatchEditParameter p : listOfupdates) {
            assertEqualsText(p.getValue(),
                xml,
                p.getXpath(),
                GMD, GCO);
        }

    }
}
