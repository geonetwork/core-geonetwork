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
import org.fao.geonet.MetadataResourceDatabaseMigration;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataResourceDatabaseMigrationTest extends AbstractServiceIntegrationTest {
    @Autowired
    SettingManager settingManager;

    public static final String XPATH = "*//*[contains(text(), '/resources.get?')]";
    private static final String XPATH_THUMBNAIL =
        "*//gmd:MD_BrowseGraphic" +
            "[gmd:fileDescription/gco:CharacterString = 'thumbnail' or " +
            "gmd:fileDescription/gco:CharacterString = 'large_thumbnail']/gmd:fileName/" +
            "gco:CharacterString[not(starts-with(normalize-space(text()), 'http'))]";
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
        assertEquals("2 link to old resources",
            NUMBER_OF_LINKS_TO_UPDATE, links.size());

        List<Element> linksThumbnail =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_THUMBNAIL));
        assertEquals("One old style thumbnail link using only filename",
            1, linksThumbnail.size());



        boolean changed = MetadataResourceDatabaseMigration.updateMetadataResourcesLink(
            metadata, null, settingManager
        );
        assertTrue("Metadata was updated", changed);
        links =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH));
        assertEquals("Old links are now removed", links.size(), 0);
        linksThumbnail =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_THUMBNAIL));
        assertEquals("Old thumbnails are now removed", links.size(), 0);


        links =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_AFTER_UPDATE));


//        for (Element n : links) {
//            System.out.println(n.getText());
//        }
        // 3 links and a thumbnail
        assertEquals("New links with correct URL",
            NUMBER_OF_LINKS_TO_UPDATE + 1, links.size());
    }
    
    
    @Test
    public void testMetadataResourceThumbnailMigration() throws Exception {
        final String fileName = "record-with-external-url-for-thumbnails.xml";
        File metadataFile = new File(resources, fileName);
        Element metadata = Xml.loadStream(new FileInputStream(metadataFile));

        String XPATH_THUMBNAIL_WITH_EXTERNAL_URL =
    			"*//gmd:graphicOverview/gmd:MD_BrowseGraphic[gmd:fileDescription/gco:CharacterString]/gmd:fileName/gco:CharacterString[starts-with(normalize-space(text()), 'http://external.com')]";

        List<Element> linksThumbnail =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_THUMBNAIL_WITH_EXTERNAL_URL));
        assertEquals("Two thumbnail link using external domain",
            2, linksThumbnail.size());

        boolean changed = MetadataResourceDatabaseMigration.updateMetadataResourcesLink(
            metadata, null, settingManager
        );
        
        linksThumbnail =
            Lists.newArrayList((Iterable<? extends Element>)
                Xml.selectNodes(metadata, XPATH_THUMBNAIL_WITH_EXTERNAL_URL));
        assertEquals("Old thumbnails are now removed", linksThumbnail.size(), 0);

    }
}
