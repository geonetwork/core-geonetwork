//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusManager;
import org.junit.Before;
import org.junit.Test;

public class TermLabelTest extends AbstractTermTest {

    private TermLabel termLabelClassifier;
    
    @Before 
    public void setup() throws Exception {
        ThesaurusManager manager = mockThesaurusManagerWith("BroaderTerm.rdf");
        termLabelClassifier = new TermLabel(manager, "scheme", "eng");
    }

    @Test
    public void testMultiLevelClassification() {
        List<CategoryPath> testTermHierarchy = termLabelClassifier.classify("sea surface temperature");
        assertCategoryListEquals(testTermHierarchy, "http://www.my.com/#ocean>http://www.my.com/#ocean_temperature>http://www.my.com/#sea_surface_temperature");
    }

    @Test
    public void testWithTermWithTwoBroaderTerms() {
        List<CategoryPath> testTermHierarchy = termLabelClassifier.classify("air sea flux");
        assertCategoryListEquals(testTermHierarchy, "http://www.my.com/#physical_air>http://www.my.com/#air_sea_flux", "http://www.my.com/#physical_water>http://www.my.com/#air_sea_flux");
    }

    @Test
    public void testWithUnknownTerm() {
        List<CategoryPath> testTermHierarchy = termLabelClassifier.classify("unkown term");
        assertEquals(0, testTermHierarchy.size());
    }

    @Test
    public void testIndexLabel() {
        termLabelClassifier.setIndexLabel(true);
        List<CategoryPath> testTermHierarchy = termLabelClassifier.classify("sea surface temperature");
        assertCategoryListEquals(testTermHierarchy, "ocean>ocean temperature>sea surface temperature");
    }
}
