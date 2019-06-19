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

package org.fao.geonet;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Test that the aspect defined work correctly.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class DataManagerWorksWithoutTransactionIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    DataManager _dataManager;

    @Autowired
    PlatformTransactionManager _tm;

    @Test
    public void testDataManagerCutpoints() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final ServiceContext serviceContext = createServiceContext();
                loginAsAdmin(serviceContext);

                final Element sampleMetadataXml = getSampleMetadataXml();
                final UserSession userSession = serviceContext.getUserSession();
                final int userIdAsInt = userSession.getUserIdAsInt();
                final String mdId = _dataManager.insertMetadata(serviceContext, "iso19139", sampleMetadataXml,
                        "uuid" + _inc.incrementAndGet(), userIdAsInt, "2", "source", MetadataType.METADATA.codeString, null, "maps",
                        new ISODate().getDateAndTime(), new ISODate().getDateAndTime(), false, false);
                Element newMd = new Element("MD_Metadata", GMD)
                        .addContent(new Element("fileIdentifier", GMD).addContent(new Element("CharacterString", GCO)));

                AbstractMetadata updateMd = _dataManager.updateMetadata(serviceContext, mdId, newMd, false, false, false, "eng",
                        new ISODate().getDateAndTime(), false);
                assertNotNull(updateMd);
                final boolean hasNext = updateMd.getCategories().iterator().hasNext();
                assertTrue(hasNext);
            }
        });

    }

}
