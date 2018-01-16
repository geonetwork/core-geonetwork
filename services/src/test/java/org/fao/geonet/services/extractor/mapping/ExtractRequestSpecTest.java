package org.fao.geonet.services.extractor.mapping;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.fao.geonet.services.extractor.SextantExtractor;
import org.junit.Test;

public class ExtractRequestSpecTest {

    @Test
    public void testAnonymousExtractRequestSpec() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("extract-request.json").getFile();

        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();

        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);
        assertTrue("firstname is not correct", s.getUser().getFirstname().equals("prenom"));
        assertTrue("bad number of layers", s.getLayers().size() == 2);

        SextantExtractor se = new SextantExtractor();
        String xmlSpec = se.createXmlSpecification(s, false, null, null);

        assertTrue("Unexpected XML extraction specification",
                xmlSpec.contains("<layer id=\"1\">") && xmlSpec.contains("<layer id=\"2\">")
                        && xmlSpec.contains(
                                "<user lastname=\"FAMILLE\" firstname=\"prenom\" mail=\"prenom.nom@camptocamp.com\" is_ifremer=\"false\"")
                        && xmlSpec.contains(
                                "<input format=\"vecteur\" epsg=\"4326\" protocol=\"WFS\" linkage=\"http://plifplafplof.ifremer.com\" filter=\"\" />")
                        && xmlSpec.contains("<output format=\"GeoTiff\" name=\"blah\" epsg=\"4326\" xmin=\"-180.0\" "
                                + "ymin=\"-90.0\" xmax=\"180.0\" ymax=\"90.0\" mercator_lat=\"\" />"));
    }

    @Test(expected = RuntimeException.class)
    public void testAnonymousExtracRequestSpecWithoutUser() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("extract-request-without-user.json").getFile();
        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();

        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);
        assertTrue("bad number of layers", s.getLayers().size() == 2);

        // XML serialization should fail because the spec does not contain a
        // user definition
        SextantExtractor se = new SextantExtractor();
        se.createXmlSpecification(s, false, null, null);
    }

    @Test
    public void testLayerParsing() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("only-layer.json").getFile();
        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();
        LayerSpec s = mapper.readValue(jsonString, LayerSpec.class);

        assertTrue("Unexpected id for layer", s.getId().equals("1"));
        assertTrue("Unexpected output format", s.getOutput().getFormat().equals("ESRI Shapefile"));

    }

    @Test
    public void testLayersParsing() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("layers.json").getFile();

        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();
        LayerSpec[] s = mapper.readValue(jsonString, LayerSpec[].class);
        assertTrue(s.length == 2);

    }

    /**
     * See https://forge.ifremer.fr/mantis/view.php?id=36515
     * @throws Exception
     */
    @Test
    public void testSurvalMantis36515() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("issue-36515.json").getFile();

        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();
        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);
        assertTrue(s.getLayers().get(0).getAdditionalInput() != null);
    }

    @Test
    public void testSurvalSpecWithBbox() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("surval-spec-with-bbox.json").getFile();

        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory fac = mapper.getTypeFactory();
        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);
        assertTrue(s.getLayers().get(0).getAdditionalInput() != null);

        AdditionalInputSpec spec = s.getLayers().get(0).getAdditionalInput().get(0);
        /* All these parameters are optional and are not filled in into surval-spec-with-bbox.json */
        assertTrue("Unexpected additional input", spec.getIdentifier().equals("") &&
                spec.getOutputIdentifier().equals("") &&
                spec.getOutputMimeType().equals(""));
        assertTrue("Wrong protocol in additional input, expected WPS", spec.getProtocol().equals("WPS"));
        /* ensures there is actually a bounding box in the parsed parameters */
        assertTrue("No bounding box found in the additional input",
                spec.getParams().contains("limits=-5.5591,46.6193,-1.2085,49.4396"));

        // Serializes back the object to XML
        SextantExtractor se = new SextantExtractor();
        String xmlSpec = se.createXmlSpecification(s, false, null, null);
        assertTrue("Invalid XML specification generated", xmlSpec.contains("zone_marine_quadrige=abcd&"
                + "produit_id=30140&limits=-5.5591,46.6193,-1.2085,49.4396&programme_suivi=1234&"));
    }

    @Test
    public void testSurvalExtractionSpec() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("extract-surval-spec.json").getFile();

        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);
        List<LayerSpec> l = s.getLayers();

        assertTrue("unexpected number of layers, expected 1", l.size() == 1);
        assertNotNull("additional input is null", l.get(0).getAdditionalInput());
        assertTrue("additional input is empty", l.get(0).getAdditionalInput().size() > 0);
        assertTrue("Unexpected additionalInput's identifier", l.get(0).getAdditionalInput()
                .get(0).getIdentifier().equals("r:extractionsurval"));
        // Then serializes back the object to XML consumed by the Python
        // Extractor
        SextantExtractor se = new SextantExtractor();
        String xmlSpec = se.createXmlSpecification(s, false, null, null);

        assertTrue("Unexpected generated XML specification", xmlSpec.contains("lastname=\"admin\"")
                && xmlSpec.contains("<input format=\"vector\" epsg=\"4326\" protocol=\"OGC:WFS\" ")
                && xmlSpec.contains(
                        "<output format=\"ESRI Shapefile\" name=\"surval_30140_all_point_12_12_2016_postgis\" "
                                + "epsg=\"4326\" xmin=\"-10.546875000000005\" ymin=\"41.50857729743939\" xmax=\"9.140624999999993\" "
                                + "ymax=\"51.17934297928923\" mercator_lat=\"\" />")
                && xmlSpec.contains("      <additionalInput protocol=\"WPS\" linkage=\"http://sextant-test.ifremer.fr/cgi-bin/sextant/qgis-server/wps/R\" "
                        + "params=\"PHdwczpFeGVjdXRlIHhtbG5zOndwcz0iaHR0cDovL3d3dy5vcGVuZ2lzLm5ldC93cHMvMS4wL")
                && xmlSpec.contains("identifier=\"r:extractionsurval\" outputMimeType=\"\" outputIdentifier=\"\""));
    }

    /**
     * https://forge.ifremer.fr/mantis/view.php?id=38409
     * 
     * In case the JSON spec does not define a bounding box, we end up with xmin="null" instead of xmin="".
     *
     * @throws Exception
     */
    @Test
    public void testMantisIssue38409() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("mantis-38409.json").getFile();
        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);

        SextantExtractor se = new SextantExtractor();
        String xmlSpec = se.createXmlSpecification(s, false, null, null);

        assertFalse("XML specification contains xmin=\"null\"", xmlSpec.contains("xmin=\"null\""));
        
    }
    /** See mantis issue 39238 */
    @Test
    public void testEscapeXmlMantis39238() throws Exception {
        String f = ExtractRequestSpecTest.class.getResource("extract-spec-mantis-39238.json").getFile();
        String jsonString = FileUtils.readFileToString(new File(f));
        ObjectMapper mapper = new ObjectMapper();
        ExtractRequestSpec s = mapper.readValue(jsonString, ExtractRequestSpec.class);

        SextantExtractor se = new SextantExtractor();
        String xmlSpec = se.createXmlSpecification(s, false, null, null);

        assertTrue("Unexpected XML output", xmlSpec.contains("&quot;double-quotes&quot;")
                && xmlSpec.contains("&lt;/xml&gt;"));
    }

    @Test
    public void linkageAreXmlEscaped() throws UnsupportedEncodingException {
        AdditionalInputSpec toTest1 = new AdditionalInputSpec();
        toTest1.setLinkage("&-&");
        InputLayerSpec toTest2 = new InputLayerSpec();
        toTest2.setLinkage("&-&");

        assertTrue("xml unsupported chars not escaped", toTest1.asXml().contains("&amp;-&amp;"));
        assertTrue("xml unsupported chars not escaped", toTest2.asXml().contains("&amp;-&amp;"));
    }
}
