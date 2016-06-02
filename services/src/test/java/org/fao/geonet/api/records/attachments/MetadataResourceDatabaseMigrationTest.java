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

package org.fao.geonet.api.records.attachments;

import com.google.common.collect.Lists;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataResourceDatabaseMigrationTest {
    public static final String XPATH = "*//*[contains(text(), '/resources.get?')]";
    public static final String XPATH_AFTER_UPDATE = "*//*[contains(text(), 'api/records')]";
    private static String resources =
        AbstractCoreIntegrationTest.getClassFile(
            MetadataResourceDatabaseMigrationTest.class).getParent();

    @Test
    public void testMetadataResourceLinkMigration() throws Exception {
        final String fileName = "record-with-old-links.xml";
        File metadataFile = new File(resources, fileName);
        Element metadata = Xml.loadStream(new FileInputStream(metadataFile));

        int NUMBER_OF_LINKS_TO_UPDATE = 3;

        List<Element> links =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH));
        assertEquals("One link to old resource", links.size(), NUMBER_OF_LINKS_TO_UPDATE);

        boolean changed = MetadataResourceDatabaseMigration.updateMetadataResourcesLink(
            metadata, null
        );
        assertTrue("Metadata was updated", changed);
        links =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH));
        assertEquals("Old links removed", links.size(), 0);

        links =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_AFTER_UPDATE));
//        for (Element n : links) {
//            System.out.println(n.getText());
//        }
        assertEquals("New links with correct URL", links.size(), NUMBER_OF_LINKS_TO_UPDATE);
    }
}
