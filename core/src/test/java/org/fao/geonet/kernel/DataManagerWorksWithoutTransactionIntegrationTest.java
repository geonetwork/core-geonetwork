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

package org.fao.geonet.kernel;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public class DataManagerWorksWithoutTransactionIntegrationTest extends AbstractDataManagerIntegrationTest {

    @Test
    public void testDataManagerCutpoints() throws Exception {
        TransactionlessTesting.get().run
            (new TestTask() {
                @Override
                public void run() throws Exception {
                    ServiceContext serviceContext = createContextAndLogAsAdmin();

                    String metadataCategory = metadataCategoryRepository.findAll().get(0).getName();
                    Element sampleMetadataXml = getSampleMetadataXml();
                    UserSession userSession = serviceContext.getUserSession();
                    int userIdAsInt = userSession.getUserIdAsInt();
                    String schema = dataManager.autodetectSchema(sampleMetadataXml);
                    String mdId = dataManager.insertMetadata(serviceContext, schema, sampleMetadataXml,
                        UUID.randomUUID().toString(), userIdAsInt, "2", "source",
                        MetadataType.METADATA.codeString, null, metadataCategory, new ISODate().getDateAndTime(),
                        new ISODate().getDateAndTime(), false, IndexingMode.none);
                    Element newMd = new Element(sampleMetadataXml.getName(), sampleMetadataXml.getNamespace()).addContent(new Element("fileIdentifier",
                        GMD).addContent(new Element("CharacterString", GCO)));

                    AbstractMetadata updateMd = dataManager.updateMetadata(serviceContext, mdId, newMd, false, false, "eng",
                        new ISODate().getDateAndTime(), false, IndexingMode.none);
                    assertNotNull(updateMd);
                    boolean hasNext = updateMd.getCategories().iterator().hasNext();
                    assertTrue(hasNext);
                }
            });
    }

    @Test
    public void testSetHarvesterData() throws Exception {
        TransactionlessTesting.get().run
            (new TestTask() {
                @Override
                public void run() throws Exception {
                    doSetHarvesterDataTest();
                }
            });
    }

}
