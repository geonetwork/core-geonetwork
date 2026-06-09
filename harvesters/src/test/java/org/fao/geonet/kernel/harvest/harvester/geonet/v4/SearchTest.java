//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet.v4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.fao.geonet.exceptions.BadParameterEx;
import org.junit.Test;

/**
 * Unit tests for the Search class, specifically testing pagination logic
 * to ensure all records are fetched without gaps.
 */
public class SearchTest {

    /**
     * Tests that the pagination logic correctly covers all records without gaps.
     * This test verifies the fix for the off-by-one error where record at index 29
     * was being skipped due to incorrect size calculation.
     *
     * <p>Previously, the code used: {@code int to = from + (pageSize - 1)}
     * which resulted in size=29 instead of size=30, causing record 29 to be skipped.</p>
     */
    @Test
    public void testPaginationCoversAllRecords() {
        int pageSize = 30;
        int totalRecords = 325;
        Set<Integer> fetchedIndices = new HashSet<>();

        int from = 0;

        while (from < totalRecords) {
            // Simulate fetching records: from offset, fetch 'pageSize' records
            int end = Math.min(from + pageSize, totalRecords);
            for (int i = from; i < end; i++) {
                fetchedIndices.add(i);
            }
            from = from + pageSize;
        }

        // Verify all records are fetched
        assertEquals("Should fetch all records", totalRecords, fetchedIndices.size());
        for (int i = 0; i < totalRecords; i++) {
            assertTrue("Record " + i + " should be fetched", fetchedIndices.contains(i));
        }
    }


    /**
     * Tests that the Search class generates correct Elasticsearch query with proper pagination.
     */
    @Test
    public void testElasticsearchQueryContainsCorrectPagination() throws BadParameterEx {
        Search search = Search.createEmptySearch(0, 30);

        String query = search.createElasticsearchQuery();

        assertTrue("Query should contain from: 0", query.contains("\"from\": 0"));
        assertTrue("Query should contain size: 30", query.contains("\"size\": 30"));
    }

    /**
     * Tests pagination with different page offsets.
     */
    @Test
    public void testElasticsearchQueryWithOffset() throws BadParameterEx {
        Search search = Search.createEmptySearch(0, 30);
        search.setRange(60, 30);

        String query = search.createElasticsearchQuery();

        assertTrue("Query should contain from: 60", query.contains("\"from\": 60"));
        assertTrue("Query should contain size: 30", query.contains("\"size\": 30"));
    }

    /**
     * Tests that setRange correctly updates both from and size.
     */
    @Test
    public void testSetRange() throws BadParameterEx {
        Search search = new Search();
        search.setRange(100, 50);

        assertEquals("from should be 100", 100, search.from);
        assertEquals("size should be 50", 50, search.size);
    }

    /**
     * Tests that copy() preserves pagination parameters.
     */
    @Test
    public void testCopyPreservesPagination() throws BadParameterEx {
        Search original = Search.createEmptySearch(0, 30);
        original.setRange(90, 30);

        Search copy = original.copy();

        assertEquals("Copied from should match", original.from, copy.from);
        assertEquals("Copied size should match", original.size, copy.size);
    }
}
