package org.fao.geonet.harvester.wfsfeatures.worker;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.fao.geonet.Assert;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by francois on 04/03/16.
 */
public class SolrWFSFeatureIndexerTest {

    @Autowired
    SolrWFSFeatureIndexer solrWFSFeatureIndexer;

    @Test
    public void testBuildFeatureTitle() throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", "String");
        fields.put("number", "String");

        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", "A NAME");
        values.put("number", "22");
        SimpleFeature feature = buildFeature(fields, values);

        String expression = "name";
        String title = WFSFeatureUtils.buildFeatureTitle(feature, fields, expression);
        assertEquals(
                values.get("name"),
                title);

        expression = "Feature title is '{{name}} [{{number}}]'.";
        title = WFSFeatureUtils.buildFeatureTitle(feature, fields, expression);
        assertEquals(
                "Feature title is '" +
                        values.get("name") + " [" +
                        values.get("number") + "]'.",
                title);
    }

    @Test
    public void testGuessFeatureTitleAttribute() throws Exception {
        testGuessFeatureTitleAttribute("name", true);
        testGuessFeatureTitleAttribute("title", true);
        testGuessFeatureTitleAttribute("libelle", true);
        testGuessFeatureTitleAttribute("LibeLLE", true);
        testGuessFeatureTitleAttribute("dummy", false);
        testGuessFeatureTitleAttribute(null, false);
    }

    private void testGuessFeatureTitleAttribute(String colName, boolean isSuccess) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (colName != null) {
            fields.put(colName, "String");
        }
        String col = WFSFeatureUtils.guessFeatureTitleAttribute(fields);
        if (isSuccess) {
            assertEquals(colName, col);
        } else {
            assertEquals(colName, col);
        }
    }

    private SimpleFeature buildFeature(Map<String, String> fields,
                                       Map<String, String> values)
            throws SchemaException {
        Iterator<String> iterator = fields.keySet().iterator();
        StringBuffer typeDef = new StringBuffer("the_geom:Point:srid=4326");
        while (iterator.hasNext()) {
            String name = iterator.next();
            typeDef.append(",")
                    .append(name).append(":").append(fields.get(name));
        }
        final SimpleFeatureType TYPE = DataUtilities.createType(
                "Location",
                typeDef.toString()
        );

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        double latitude = 1;
        double longitude = 1;
        Point point = geometryFactory.createPoint(
                new Coordinate(longitude, latitude));
        featureBuilder.add(point);
        Iterator<String> valueIterator = values.keySet().iterator();
        while (valueIterator.hasNext()) {
            String name = valueIterator.next();
            featureBuilder.add(values.get(name));
        }
        SimpleFeature feature = featureBuilder.buildFeature(null);
        return feature;
    }
}
