/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords.es;

import static junit.framework.TestCase.assertEquals;

import org.checkerframework.checker.units.qual.A;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.services.getrecords.SortByParser;
import org.jdom.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * CswFilter2Es converts (XML-based) CSW queries into ElasticSearch queries.
 * These ES-queries are in JSON-notation. We do not want to test the resulting
 * JSON-String char-by-char as this is error-prone.<br>
 * <p>
 * Instead, we deserialize the output string back to a JSON-tree. We then
 * compare this output tree with an expected tree we built up using an
 * Elasticsearch oriented DSL.
 *
 * @author fgravin
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CswFilter2EsTestConfiguration.class, CswSortBy2EsTest.TestConfig.class})
class CswSortBy2EsTest {

    @Autowired
    SortByParser toTest;

    @Autowired
    CatalogConfiguration configuration;

    @Test
    void configureDefaultSort() {
        configuration.setDefaultSortField("resourceTitleObject.default.keyword");
        configuration.setDefaultSortOrder("DESC");
        Element request =  createSortByBaseRequest(
            new Element("Empty", Geonet.Namespaces.OGC));

        List<SortBuilder<FieldSortBuilder>> sortFields = toTest.parseSortBy(request);

        assertEquals(1, sortFields.size());
        FieldSortBuilder sortField = (FieldSortBuilder)sortFields.get(0);
        assertEquals(sortField.getFieldName(), "resourceTitleObject.default.keyword");
        assertEquals(sortField.order().toString(), "desc");
    }

    @Test
    void sortByRelevanceDESC() {
        Element request = createSortByBaseRequest(
                new Element("SortBy", Geonet.Namespaces.OGC)
                        .addContent(new Element("SortProperty", Geonet.Namespaces.OGC)
                                .addContent(new Element("PropertyName", Geonet.Namespaces.OGC).setText("Relevance"))
                                .addContent(new Element("SortOrder", Geonet.Namespaces.OGC).setText("DESC"))));

        List<SortBuilder<FieldSortBuilder>> sortFields = toTest.parseSortBy(request);

        assertEquals(1, sortFields.size());
        FieldSortBuilder sortField = (FieldSortBuilder)sortFields.get(0);
        assertEquals(sortField.getFieldName(), "_score");
        assertEquals(sortField.order().toString(), "desc");
    }

    @Test
    void sortByRelevanceASC() {
        Element request = createSortByBaseRequest(
                new Element("SortBy", Geonet.Namespaces.OGC)
                        .addContent(new Element("SortProperty", Geonet.Namespaces.OGC)
                                .addContent(new Element("PropertyName", Geonet.Namespaces.OGC).setText("Relevance"))
                                .addContent(new Element("SortOrder", Geonet.Namespaces.OGC).setText("ASC"))));

        List<SortBuilder<FieldSortBuilder>> sortFields = toTest.parseSortBy(request);

        assertEquals(1, sortFields.size());
        FieldSortBuilder sortField = (FieldSortBuilder)sortFields.get(0);
        assertEquals(sortField.getFieldName(), "_score");
        assertEquals(sortField.order().toString(), "asc");
    }

    @Test
    void sortByIndexField() {
        Element request = createSortByBaseRequest(
                new Element("SortBy", Geonet.Namespaces.OGC)
                        .addContent(new Element("SortProperty", Geonet.Namespaces.OGC)
                                .addContent(new Element("PropertyName", Geonet.Namespaces.OGC).setText("title"))
                                .addContent(new Element("SortOrder", Geonet.Namespaces.OGC).setText("DESC"))));

        List<SortBuilder<FieldSortBuilder>> sortFields = toTest.parseSortBy(request);

        assertEquals(1, sortFields.size());
        FieldSortBuilder sortField = (FieldSortBuilder)sortFields.get(0);
        assertEquals(sortField.getFieldName(), "title");
        assertEquals(sortField.order().toString(), "desc");
    }

    @Test
    void sortByMultipleProperties() {
        Element request = createSortByBaseRequest(
                new Element("SortBy", Geonet.Namespaces.OGC)
                        .addContent(new Element("SortProperty", Geonet.Namespaces.OGC)
                                .addContent(new Element("PropertyName", Geonet.Namespaces.OGC).setText("title"))
                                .addContent(new Element("SortOrder", Geonet.Namespaces.OGC).setText("DESC")))
                        .addContent(new Element("SortProperty", Geonet.Namespaces.OGC)
                                .addContent(new Element("PropertyName", Geonet.Namespaces.OGC).setText("Relevance"))
                                .addContent(new Element("SortOrder", Geonet.Namespaces.OGC).setText("DESC"))));

        List<SortBuilder<FieldSortBuilder>> sortFields = toTest.parseSortBy(request);

        assertEquals(2, sortFields.size());
        FieldSortBuilder sortField = (FieldSortBuilder)sortFields.get(0);
        assertEquals(sortField.getFieldName(), "title");
        assertEquals(sortField.order().toString(), "desc");
        FieldSortBuilder sortField2 = (FieldSortBuilder)sortFields.get(1);
        assertEquals(sortField2.getFieldName(), "_score");
        assertEquals(sortField2.order().toString(), "desc");
    }

    private Element createSortByBaseRequest(Element SortBy) {
        return new Element("GetRecords", Csw.NAMESPACE_CSW)
                .setAttribute("service", "CSW")
                .setAttribute("version", "2.0.2")
                .setAttribute("resultType", "results")
                .setAttribute("startPosition", "3")
                .setAttribute("maxRecords", "1")
                .setAttribute("outputSchema", "csw:Record")
                .addContent(new Element("Query", Csw.NAMESPACE_CSW)
                        .addContent(SortBy));
    }

    static class TestConfig {
        @Bean
        public SortByParser sortByParser() {
            return new SortByParser();
        }
    }
}
