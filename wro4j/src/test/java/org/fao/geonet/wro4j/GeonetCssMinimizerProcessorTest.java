package org.fao.geonet.wro4j;

import static org.junit.Assert.*
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Test {@link org.fao.geonet.wro4j.GeonetCssMinimizerProcessor}.
 * <p/>
 * Created by Jesse on 12/22/13.
 */
public class GeonetCssMinimizerProcessorTest {


    private static final String URI = "file:///home/jeichar/testfile.css";
    private static final String TEST_STRING =
            AddFileUriCommentProcessor.START_JS_COMMENT + URI +" --------\n" +
            "p\n"
            + "{\n"
            + "        color:red;\n"
            + "\t\ftext-align:center;\n"
            + "         /* a"
            + "            block comment"
            + "          */"
            + "}\n"
            + " // single line non-standard comment\n"
            + "  p.a {color:blue;text-align:left;}"
            + AddFileUriCommentProcessor.END_JS_COMMENT + URI +" --------";

    @Test @Ignore
    public void testProdProcess() throws Exception {
        final GeonetCssMinimizerProcessor p = new GeonetCssMinimizerProcessor(true);
        Resource resource = getResource();
        Reader reader = new StringReader(TEST_STRING);
        StringWriter writer = new StringWriter();
        p.process(resource, reader, writer);

        assertEquals("p{color:red;text-align:center;}p.a{color:blue;text-align:left;}", writer.toString());
    }
    @Test @Ignore
    public void testDevProcess() throws Exception {
        final GeonetCssMinimizerProcessor p = new GeonetCssMinimizerProcessor(false);
        Resource resource = getResource();
        Reader reader = new StringReader(TEST_STRING);
        StringWriter writer = new StringWriter();
        p.process(resource, reader, writer);

        assertTrue(writer.toString()+"should contain the minified css", writer.toString().contains("p{color:red;text-align:center;}p.a{color:blue;text-align:left;}"));
        assertTrue(writer.toString() + " should contain start file comment", writer.toString().contains(AddFileUriCommentProcessor.START_JS_COMMENT));
        assertTrue(writer.toString() + "should contain end file comment", writer.toString().contains(AddFileUriCommentProcessor.END_JS_COMMENT));
        assertTrue(writer.toString() +" should contain URI", writer.toString().contains(resource.getUri()));
    }

    public Resource getResource() {
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.CSS);
        resource.setUri(URI);
        return resource;
    }
}
