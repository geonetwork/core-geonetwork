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

package org.fao.geonet.kernel.csw.services.getrecords.solr;

import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.geotools.filter.FilterUtils;
import org.junit.Test;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.FactoryException;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CswFilter2SolrTest {
    private static final IFieldMapper FIELD_MAPPER = field -> field;
    private static final String expectedGeom = "POLYGON ((-170 -80, -170 80, 170 80, 170 -80, -170 -80))";

    @Test
    public void testIsEqualsTo() throws Exception {
        final Filter filter = FilterUtils.createEqualsTo("toto", "34 \"56\"");
        assertEquals("(toto:\"34 \\\"56\\\"\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testNotIsEqualsTo() throws Exception {
        final Filter filter = FilterUtils.createNotEqualsTo("toto", 56);
        assertEquals("(-toto:\"56\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testAnd() throws Exception {
        final Filter sub1 = FilterUtils.createEqualsTo("toto", 34);
        final Filter sub2 = FilterUtils.createEqualsTo("tutu", 35);
        final And filter = FilterUtils.createAnd(Arrays.asList(sub1, sub2));
        assertEquals("((toto:\"34\") AND (tutu:\"35\"))", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testOr() throws Exception {
        final Filter sub1 = FilterUtils.createEqualsTo("toto", 34);
        final Filter sub2 = FilterUtils.createEqualsTo("tutu", 35);
        final Filter sub3 = FilterUtils.createEqualsTo("foo", 36);
        final Or filter = FilterUtils.createOr(Arrays.asList(sub1, sub2, sub3));
        assertEquals("((toto:\"34\") OR (tutu:\"35\") OR (foo:\"36\"))", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testNot() throws Exception {
        final Filter sub1 = FilterUtils.createEqualsTo("toto", 34);
        final Filter filter = FilterUtils.createNot(sub1);
        assertEquals("(NOT (toto:\"34\"))", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testPropertyIsGreaterThan() throws Exception {
        final Filter filter = FilterUtils.createIsGreaterThanImpl("toto", 34);
        assertEquals("(toto:{\"34\" TO *])", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testPropertyIsGreaterThanOrEqualTo() throws Exception {
        final Filter filter = FilterUtils.createIsGreaterThanImplOrEqualTo("toto", 34);
        assertEquals("(toto:[\"34\" TO *])", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testPropertyIsLessThen() throws Exception {
        final Filter filter = FilterUtils.createIsLessThenImpl("toto", 34);
        assertEquals("(toto:[* TO \"34\"})", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testPropertyIsLessThenOrEqualTo() throws Exception {
        final Filter filter = FilterUtils.createIsLessThenImplOrEqualTo("toto", 34);
        assertEquals("(toto:[* TO \"34\"])", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testConvertLikePattern() {
        PropertyIsLike filter = FilterUtils.createPropertyIsLike("toto", "!&x!.y&z.w!!t+", "&", ".", "!");
        assertArrayEquals(new String[]{"\\&x\\.y*z?w\\!t\\+"}, CswFilter2Solr.convertLikePattern(filter));

        filter = FilterUtils.createPropertyIsLike("toto", "\\*x\\?y*z?w\\\\t+", "*", "?", "\\");
        assertArrayEquals(new String[]{"\\*x\\?y*z?w\\\\t\\+"}, CswFilter2Solr.convertLikePattern(filter));
    }

    @Test
    public void testPropertyIsLike() {
        final PropertyIsLike filter = FilterUtils.createPropertyIsLike("toto", "co*ct fu*ed", "*", "?", "\\");
        assertEquals("(toto:co*ct toto:fu*ed)", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testPropertyIsBetween() {
        final PropertyIsBetween filter = FilterUtils.createPropertyIsBetween("toto", 1, 10);
        assertEquals("(toto:[\"1\" TO \"10\"])", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testBbox() {
        final BBOX filter = FilterUtils.createBbox("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"Intersects(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testContains() throws FactoryException {
        final Contains filter = FilterUtils.createContains("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"Contains(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testDisjoint() throws FactoryException {
        final Disjoint filter = FilterUtils.createDisjoint("geom", -170, 170, 80, -80);
        assertEquals("(NOT (geom:\"Intersects(" + expectedGeom + ")\"))", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testEquals() throws FactoryException {
        final Equals filter = FilterUtils.createEquals("geom", -170, 170, 80, -80);
        assertEquals("(geom==" + expectedGeom + ")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testIntersects() throws FactoryException {
        final Intersects filter = FilterUtils.createIntersects("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"Intersects(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testOverlaps() throws FactoryException {
        final Overlaps filter = FilterUtils.createOverlaps("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"Intersects(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testWithin() throws FactoryException {
        final Within filter = FilterUtils.createWithin("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"IsWithin(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testCrosses() throws FactoryException {
        final Crosses filter = FilterUtils.createCrosses("geom", -170, 170, 80, -80);
        assertEquals("(geom:\"Intersects(" + expectedGeom + ")\")", CswFilter2Solr.translate(filter, FIELD_MAPPER));
    }

    @Test
    public void testFieldMapping() throws Exception {
        final IFieldMapper weirdFieldMapper = field -> "_" + field;
        final Filter filter = FilterUtils.createEqualsTo("toto", "34 \"56\"");
        assertEquals("(_toto:\"34 \\\"56\\\"\")", CswFilter2Solr.translate(filter, weirdFieldMapper));
    }


}
/*
(({!field f=geom}Intersects(ENVELOPE(-170.0, 170.0, 80.0, -80.0)) OR ({!field f=geom}IsWithin(ENVELOPE(-170.0, 170.0, 80.0, -80.0)) OR ({!field f=geom}Contains(ENVELOPE(-170.0, 170.0, 80.0, -80.0)))
*/
