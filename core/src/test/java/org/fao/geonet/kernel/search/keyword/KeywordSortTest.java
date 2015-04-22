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

    }
}
