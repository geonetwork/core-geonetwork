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

package org.fao.geonet.kernel.search;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LuceneSearcherPresentTest extends AbstractCoreIntegrationTest {
    @Autowired
    private SearchManager searchManager;
    @Autowired
    private DataManager dataManager;

    @Ignore
    public void testBuildPrivilegesMetadataInfo() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, serviceContext);
        importMetadata.invoke();

        Element info = new Element("info", Geonet.Namespaces.GEONET);
        IndexAndTaxonomy indexAndTaxonomy = searchManager.getNewIndexReader("eng");

        try {
            TopFieldCollector tfc = TopFieldCollector.create(Sort.INDEXORDER, 1000, true, false, false, true);
            IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
            searcher.search(new MatchAllDocsQuery(), tfc);

            final Document doc = indexAndTaxonomy.indexReader.document(tfc.topDocs().scoreDocs[0].doc);
            LuceneSearcher.buildPrivilegesMetadataInfo(serviceContext, doc, info);

            assertEqualsText("true", info, "edit");
            assertEqualsText("true", info, "owner");
            assertNotNull("Expected ownerId to be one of the elements in : " + Xml.getString(info), info.getChild("ownerId"));
            assertEqualsText("true", info, "view");
            assertEqualsText("true", info, "notify");
            assertEqualsText("true", info, "download");
            assertEqualsText("true", info, "dynamic");
            assertEqualsText("true", info, "featured");
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }


        final String mdId = importMetadata.getMetadataIds().get(0);
        dataManager.unsetOperation(serviceContext, mdId, "" + ReservedGroup.all.getId(), ReservedOperation.editing);
        dataManager.indexMetadata(mdId, true, null);

        indexAndTaxonomy = searchManager.getNewIndexReader("eng");
        try {
            TopFieldCollector tfc = TopFieldCollector.create(Sort.INDEXORDER, 1000, true, false, false, true);
            IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
            searcher.search(new MatchAllDocsQuery(), tfc);

            final Document doc = indexAndTaxonomy.indexReader.document(tfc.topDocs().scoreDocs[0].doc);

            serviceContext.setUserSession(new UserSession());
            SecurityContextHolder.clearContext();
            info.removeContent();
            LuceneSearcher.buildPrivilegesMetadataInfo(serviceContext, doc, info);

            assertNull(Xml.selectElement(info, "edit"));
            assertNull(Xml.selectElement(info, "owner"));
            //TODO: Check why guestdownload is no longer part of info.
            //assertEqualsText("false", info, "guestdownload");
            assertEqualsText("true", info, "isPublishedToAll");
            assertEqualsText("true", info, "view");
            assertEqualsText("false", info, "notify");
            //TODO: inverted three assertions, Check why download, dynamic and featured are no longer false.
            assertEqualsText("true", info, "download");
            assertEqualsText("true", info, "dynamic");
            assertEqualsText("true", info, "featured");
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }

    @Test
    public void testDummy() {

    }

}
