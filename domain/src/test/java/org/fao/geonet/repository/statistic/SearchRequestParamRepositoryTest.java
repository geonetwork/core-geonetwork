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

package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.LuceneQueryParamType;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test the User: Jesse Date: 9/29/13 Time: 11:03 PM
 */
public class SearchRequestParamRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    SearchRequestParamRepository _paramRepo;

    public static SearchRequestParam newRequestParam(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        SearchRequestParam param = new SearchRequestParam();
        param.setInclusive(val % 2 == 0);
        param.setLowerText("lower text" + val);
        final LuceneQueryParamType[] values = LuceneQueryParamType.values();
        param.setQueryType(values[val % values.length]);
        param.setSimilarity(val);
        param.setTermField("term field" + val);
        param.setTermText("term text" + val);
        param.setUpperText("upper text" + val);

        return param;
    }

    @Test
    public void testGetTermTextToRequestCount() {
        SearchRequestParam param3 = _paramRepo.save(newRequestParam(_inc));
        SearchRequestParam param4 = _paramRepo.save(newRequestParam(_inc));
        SearchRequestParam param1 = newRequestParam(_inc);
        param1 = _paramRepo.save(param1);
        SearchRequestParam param2 = newRequestParam(_inc);
        param2.setTermText(param1.getTermText());
        _paramRepo.save(param2);
        SearchRequestParam param5 = _paramRepo.save(newRequestParam(_inc));
        SearchRequestParam param6 = newRequestParam(_inc);
        param6.setTermField(SearchRequestParamRepositoryImpl.TERMS_TO_EXCLUDE_FROM_TAG_CLOUD[0]);
        SearchRequestParam param7 = newRequestParam(_inc);
        param7.setTermText("");
        _paramRepo.save(param7);

        List<Pair<String, Integer>> tagCloudSummary = _paramRepo.getTermTextToRequestCount(10);
        assertEquals(4, tagCloudSummary.size());
        assertEquals(2, find(tagCloudSummary, param1.getTermText()));
        assertEquals(1, find(tagCloudSummary, param3.getTermText()));
        assertEquals(1, find(tagCloudSummary, param4.getTermText()));
        assertEquals(1, find(tagCloudSummary, param5.getTermText()));

        tagCloudSummary = _paramRepo.getTermTextToRequestCount(2);
        assertEquals(2, tagCloudSummary.size());
        assertEquals(2, find(tagCloudSummary, param1.getTermText()));
    }

    private int find(List<Pair<String, Integer>> tagCloudSummary, String termText) {
        for (Pair<String, Integer> stringIntegerPair : tagCloudSummary) {
            if (stringIntegerPair.one().equals(termText)) {
                return stringIntegerPair.two();
            }
        }
        fail("Unable to find " + termText);
        return -1;
    }

}
