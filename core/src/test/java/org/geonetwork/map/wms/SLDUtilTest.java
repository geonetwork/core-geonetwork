package org.geonetwork.map.wms;

import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test SLD helpers.
 */
public class SLDUtilTest {
    @Test
    public void testGenerateFullSLD() throws Exception {
        testInsertFilter("full", "customfilter");
    }

    @Test
    public void testInsertFilterNoFilter() throws Exception {
        testInsertFilter("qgis-nofilter", "simplefilter");
    }

    @Test
    public void testInsertFilterOneFilter() throws Exception {
        testInsertFilter("onefilter", "simplefilter");
    }

//     @Test
//     public void testGetGetStyleRequest() throws Exception {
//         String req = "http://sextant-test.ifremer.fr/cgi-bin/sextant/qgis-server/ows/surval?service=WMS&request=GetStyles&version=1.1.1&layers=surval_30140_all_point_postgis";

//         String s = SLDUtil.getGetStyleRequest(new URI("http://sextant-test.ifremer.fr/cgi-bin/sextant/qgis-server/ows/surval"), "surval_30140_all_point_postgis");
//         assertEquals(s, req);
//         s = SLDUtil.getGetStyleRequest(new URI("http://sextant-test.ifremer.fr/cgi-bin/sextant/qgis-server/ows/surval?"), "surval_30140_all_point_postgis");
//         assertEquals(s, req);
//     }

//
//    @Test
//    @Ignore
//    public void testParseSLD() throws Exception {
//        String url = "http://sextant-test.ifremer.fr/cgi-bin/sextant/wms/bgmb";
//        String layers= "SISMER_prelevements";
//
//        Map<String, String> hash = SLDUtil.parseSLD(new URL(url), layers);
//        assertNotNull(hash.get("content"));
//        assertEquals(hash.get("charset"), "UTF-8");
//    }

    private void testInsertFilter(final String filePattern, final String rulePattern) throws Exception {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(this.getRessourceAsString("sld/test-sld-" + rulePattern + ".json")));

        Element root = Xml.loadFile(classloader.getResource("sld/sxt-" + filePattern + "-sld.xml"));
        SLDUtil.insertFilter(root, customFilter);

        XMLOutputter outputter = new XMLOutputter();
        Document doc = new Document(root);

        String sldDoc = outputter.outputString(doc);

        org.xmlunit.diff.Diff diff = DiffBuilder
            .compare(Input.fromString(sldDoc))
            .withTest(Input.fromString(
                this.getRessourceAsString("sld/sxt-" + filePattern + "-sld-merged.xml")
            ))
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .ignoreWhitespace()
            .checkForSimilar()
            .build();

        assertFalse(
            "Process does not alter the document.",
            diff.hasDifferences());
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
