/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.csw.services.getrecords;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.fasterxml.jackson.databind.JsonNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.jdom.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A GetRecords request with resultType=hits asks how many records match, not for the
 * records themselves. These pin both halves of that: nothing is fetched, and the counts
 * describe what the client actually received.
 *
 * <p>Before the change these tests cover, the response loop counted every record it had
 * retrieved whether or not it added it to the output, so a hits response went out as
 * {@code numberOfRecordsReturned="10"} with no records inside it and {@code
 * nextRecord="11"} for a result set nothing had read. CSW-discovery.xsd defines those as
 * "number of records returned to client" and "position of next record in the result set",
 * so both were wrong, and the records had been read from the database and run through a
 * presentation stylesheet only to be discarded.
 */
public class SearchControllerHitsTest {

    private static final long MATCHES = 32145L;

    private SearchController controller;
    private EsSearchManager searchManager;
    private ServiceContext context;
    private MockedStatic<EsFilterBuilder> filterBuilder;

    @BeforeEach
    public void setUp() throws Exception {
        controller = new SearchController();
        searchManager = Mockito.mock(EsSearchManager.class);

        // GetRecords reads a setting before it queries, so the context has to answer
        // getBean; a mock that returns null there fails before reaching the code
        // under test.
        context = Mockito.mock(ServiceContext.class);
        SettingManager settings = Mockito.mock(SettingManager.class);
        Mockito.when(context.getBean(SettingManager.class)).thenReturn(settings);
        Mockito.when(settings.getValueAsBool(Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(true);

        // The permissions filter is a static call on a real ServiceContext, which a unit
        // test has no way to populate; stub it out and let the controller build its query.
        // It is interpolated into "filter":{"query_string":{"query":"%s"}}, so the value
        // is a Lucene query_string expression and not JSON -- passing JSON here breaks
        // the quoting and the request fails to parse.
        filterBuilder = Mockito.mockStatic(EsFilterBuilder.class);
        filterBuilder.when(() -> EsFilterBuilder.build(
                Mockito.any(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any()))
            .thenReturn("isTemplate:n");

        inject("searchManager", searchManager);
        inject("esFilterBuilder", Mockito.mock(EsFilterBuilder.class));
        inject("fieldMapper", new FieldMapper());
        inject("node", Mockito.mock(NodeInfo.class));
        inject("metadataUtils", Mockito.mock(IMetadataUtils.class));
        inject("schemaManager", Mockito.mock(SchemaManager.class));

        Mockito.when(searchManager.query(Mockito.any(JsonNode.class), Mockito.anySet(),
            Mockito.anyInt(), Mockito.anyInt(), Mockito.any())).thenReturn(noRows());
    }

    @AfterEach
    public void tearDown() {
        if (filterBuilder != null) {
            filterBuilder.close();
        }
    }

    private void inject(String name, Object value) throws Exception {
        Field field = SearchController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    /**
     * What Elasticsearch answers when asked for a count and no rows: a hit total, and an
     * empty hit list.
     */
    private SearchResponse noRows() {
        return SearchResponse.of(b -> b
            .took(1)
            .timedOut(false)
            .shards(s -> s.total(1).successful(1).failed(0))
            .hits(h -> h
                .total(t -> t.value(MATCHES).relation(TotalHitsRelation.Eq))
                .hits(Collections.emptyList())));
    }

    private Element search(ResultType resultType) throws Exception {
        return search(resultType, 1);
    }

    private Element search(ResultType resultType, int startPosition) throws Exception {
        return controller.search(context, startPosition, 10,
            resultType, "csw", ElementSetName.SUMMARY, null, "1.1.0",
            Collections.emptyList(), Collections.emptySet(), "csw:Record", 0,
            SearchController.DEFAULT_ELEMENTNAMES_STRATEGY);
    }

    @Test
    public void hitsReturnsNoRecordsAndSaysSo() throws Exception {
        Element results = search(ResultType.HITS);

        assertEquals(0, results.getChildren().size(),
            "a hits response carries no records");
        assertEquals("0", results.getAttributeValue("numberOfRecordsReturned"),
            "numberOfRecordsReturned must describe what the client received");
        assertEquals(Long.toString(MATCHES),
            results.getAttributeValue("numberOfRecordsMatched"),
            "the match count is the point of a hits request");
        assertEquals("1", results.getAttributeValue("nextRecord"),
            "nothing was read, so the next record is still the first");
    }

    /**
     * The attributes above are correct *because* no rows were requested. Asserting the
     * query keeps the saving from being quietly undone: fetching rows and filtering them
     * out later would satisfy the assertions above while still costing a database read
     * and a stylesheet run per record.
     */
    @Test
    public void hitsAsksElasticsearchForNoRows() throws Exception {
        search(ResultType.HITS);

        Mockito.verify(searchManager).query(Mockito.any(JsonNode.class), Mockito.anySet(),
            Mockito.eq(0), Mockito.eq(0), Mockito.any());
    }

    /** VALIDATE returns no records either, so it takes the same path. */
    @Test
    public void validateAsksElasticsearchForNoRows() throws Exception {
        Element results = search(ResultType.VALIDATE);

        assertEquals("0", results.getAttributeValue("numberOfRecordsReturned"));
        Mockito.verify(searchManager).query(Mockito.any(JsonNode.class), Mockito.anySet(),
            Mockito.eq(0), Mockito.eq(0), Mockito.any());
    }

    /** A results request still asks for its page, which is the behaviour being preserved. */
    @Test
    public void resultsStillAsksForItsPage() throws Exception {
        search(ResultType.RESULTS);

        Mockito.verify(searchManager).query(Mockito.any(JsonNode.class), Mockito.anySet(),
            Mockito.eq(0), Mockito.eq(10), Mockito.any());
    }

    /**
     * startPosition is 1-based in CSW and 0-based in Elasticsearch, and the paged case is
     * where an off-by-one would hide.
     */
    @Test
    public void hitsHonoursStartPositionWithoutFetching() throws Exception {
        search(ResultType.HITS, 101);

        Mockito.verify(searchManager).query(Mockito.any(JsonNode.class), Mockito.anySet(),
            Mockito.eq(100), Mockito.eq(0), Mockito.any());
    }

    /**
     * Guards the one thing a reader might "simplify": the empty hit list must not be
     * mistaken for an empty result set.
     */
    @Test
    public void anEmptyHitListDoesNotZeroTheMatchCount() throws Exception {
        Element results = search(ResultType.HITS);

        assertTrue(Long.parseLong(results.getAttributeValue("numberOfRecordsMatched")) > 0,
            "matched must come from the hit total, not from the rows returned");
    }
}
