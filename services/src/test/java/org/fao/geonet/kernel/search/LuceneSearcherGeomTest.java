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

import com.google.common.collect.Sets;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

public class LuceneSearcherGeomTest extends AbstractServiceIntegrationTest {
    @Autowired
    SearchManager sm;
    private ServiceContext serviceContext;
    private Element sampleMetadataXml;
    private int metadataId;
    private Element bbox;

    @Before
    public void setUp() throws Exception {
        serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        sampleMetadataXml = getSampleMetadataXml();
        byte[] bytes = Xml.getString(sampleMetadataXml).getBytes(Constants.ENCODING);
        metadataId = importMetadataXML(serviceContext, "uuid:" + System.currentTimeMillis(), new ByteArrayInputStream(bytes),
            MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);

        bbox = Xml.selectElement(sampleMetadataXml, "gmd:EX_GeographicBoundingBox", Arrays.asList(ISO19139Namespaces.GMD));

    }

    @Test
    public void testGeomSearchBBox() throws Exception {

        Geometry geom = new GeometryFactory().toGeometry(new Envelope(-180, 180, -90, 90));
        final String geomValue = new WKTWriter().write(geom);
        final Element element = doGeomSearch(geomValue);

        assertNotNull(element);
        assertEquals("1", element.getChild("summary").getAttributeValue("count"));
    }

    @Test
    public void testGeomSearchRegion() throws Exception {
        Geometry geom = new GeometryFactory().toGeometry(new Envelope(-180, 180, -90, 90));
        RegionsDAO regionDAO = Mockito.mock(RegionsDAO.class);
        Mockito.when(regionDAO.getGeom(Mockito.<ServiceContext>anyObject(), anyString(), anyBoolean(), Mockito.<CoordinateReferenceSystem>anyObject())).thenReturn(geom);
//        final ThesaurusBasedRegionsDAO thesaurusBasedRegionsDAO = new ThesaurusBasedRegionsDAO(languages);
//        Object languages = Sets.newHashSet("eng");
//        this.serviceContext.getApplicationContext().getBeanFactory().registerSingleton("languages", languages);
        this.serviceContext.getApplicationContext().getBeanFactory().registerSingleton("thesRegions", regionDAO);
        final Element element = doGeomSearch("region:testRegion");

        assertNotNull(element);
        assertEquals("1", element.getChild("summary").getAttributeValue("count"));
    }

    public Element doGeomSearch(String geomValue) throws Exception {
        Element request = new Element("request").addContent(Arrays.asList(
            new Element("fast").setText("index"),
            new Element("from").setText("1"),
            new Element("geometry").setText(geomValue),
            new Element("relation").setText("intersection"),
            new Element("sortBy").setText(""),
            new Element("to").setText("10")
        ));
        try (MetaSearcher metaSearcher = sm.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            metaSearcher.search(this.serviceContext, request, new ServiceConfig());
            return metaSearcher.present(this.serviceContext, request, new ServiceConfig());
        }
    }

}
