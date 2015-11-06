package org.geonetwork.map.wms;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.styling.*;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.filter.Filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;

/**
 * Test SLD helpers.
 */
public class SLDUtilTest extends XMLTestCase {


    @Test
    public void testParseSLD() throws Exception {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        SLDTransformer styleTransform = new SLDTransformer();
        StyleFactory styleFactory = new StyleFactoryImpl();
        String layers = "IFR_RBT_PEUPL_GGASC_P";

        // Load styles
        Style[] parsedStyle = (new SLDParser(styleFactory, classloader.getResource("test-sld-getStyles.xml"))).readXML();
        assertTrue(parsedStyle.length > 0);

        // Load custom filters
        Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(this.getRessourceAsString("test-sld-customFilter.json")));

        // Merge with original styles
        Style[] modifiedFilters = SLDUtil.addAndFilter(parsedStyle, customFilter);
        StyledLayerDescriptor sld = SLDUtil.buildSLD(modifiedFilters, layers);
        String xmlMerged = styleTransform.transform(sld);

        // Compare result
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(xmlMerged,this.getRessourceAsString("test-sld-merged.xml"));
        assertTrue(diff.identical());

    }


    private String getRessourceAsString(String name) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(name)));
        String line = "";
        StringBuilder res = new StringBuilder();
        while ((line = buffer.readLine()) != null)
            res.append(line);

        return res.toString();
    }

}
