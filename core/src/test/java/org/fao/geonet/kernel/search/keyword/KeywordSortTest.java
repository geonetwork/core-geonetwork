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

package org.fao.geonet.kernel.search.keyword;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 4/22/2015.
 */
public class KeywordSortTest {
    @Test
    public void testSearchResultsSorter() throws Exception {
        Comparator<KeywordBean> comparator = KeywordSort.searchResultsSorter("wate", SortDirection.DESC);
        KeywordBean waterBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Water", "eng").setDefaultLang("eng");
        KeywordBean waterSurfaceBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Water (Surface)", "eng").setDefaultLang("eng");
        KeywordBean waterAgricultureBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Water Agriculture", "eng").setDefaultLang("eng");
        KeywordBean waterGeogBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Water (Geographic)", "eng").setDefaultLang("eng");
        KeywordBean wasteWaterBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Waste Water", "eng").setDefaultLang("eng");
        KeywordBean wasteWaterTreatmentBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Waste Water treatment", "eng").setDefaultLang("eng");
        KeywordBean atmosphereWaterBean = new KeywordBean(new IsoLanguagesMapper()).setValue("Atmosphere Water", "eng").setDefaultLang("eng");

        ArrayList<KeywordBean> expected = Lists.newArrayList(waterBean, waterAgricultureBean, waterGeogBean, waterSurfaceBean,
            atmosphereWaterBean, wasteWaterBean, wasteWaterTreatmentBean);
        Collection<List<KeywordBean>> permutations = Collections2.permutations(expected);

        for (List<KeywordBean> permutation : permutations) {
            ArrayList<KeywordBean> copy = Lists.newArrayList(permutation);
            Collections.sort(copy, comparator);
            assertArrayEquals(expected.toArray(), copy.toArray());
        }
    }

    @Test
    public void testNormalizeDesc() throws Exception {
        assertEquals("aoeᶚ能", KeywordSort.normalizeDesc("{(aöÈᶚ能͓}"));
        assertEquals("", KeywordSort.normalizeDesc(null));
        assertEquals("", KeywordSort.normalizeDesc(""));
        assertEquals("", KeywordSort.normalizeDesc("            ")); // test whitespaces: 4 spaces and 1 tab
        assertEquals("", KeywordSort.normalizeDesc("'"));
        assertEquals("1", KeywordSort.normalizeDesc(".[{#$£)(//&1@"));
        assertEquals("o", KeywordSort.normalizeDesc("\u00F6"));
    }
}
