package org.fao.geonet.services.metadata.format;

import com.google.common.io.Files;
import org.eclipse.jetty.util.IO;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ResourceTest extends AbstractServiceIntegrationTest {

    protected static final String RESOURCE_TEST_DIR = "resource-test";
    @Autowired
    private Resource resource;
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Test
    public void testExec() throws Exception {
        final File testFormatter = getFile("view.groovy").getParentFile();
        IO.copy(testFormatter, new File(this.dataDirectory.getFormatterDir(), RESOURCE_TEST_DIR));

        assertCorrectExec("test.js", 200, "application/javascript");
        assertCorrectExec("css/test.css", 200, "text/css");
        assertCorrectExec("test.html", 200, "text/html");
        assertCorrectExec("test.gif", 200, "image/gif");
        assertCorrectExec("test.bmp", 200, "image/bmp");
        assertCorrectExec("test.jpg", 200, "image/jpeg");
        assertCorrectExec("test.jpeg", 200, "image/jpeg");
        assertCorrectExec("test.tif", 200, "image/tiff");
        assertCorrectExec("test.png", 200, "image/png");
        assertCorrectExec("test.xml", 200, "application/xml");
        assertCorrectExec("test.json", 200, "application/json");
        assertCorrectExec("testyxz", 200, "application/octet-stream");
        assertCorrectExec("view.groovy", 200, "text/x-groovy-source,groovy");
        assertCorrectExec("test.xsl", 200, "application/xslt+xml");
        assertCorrectExec("test.xslt", 200, "application/xslt+xml");
        assertCorrectExec("nosuchfile", 404, null);
        assertCorrectExec("../xml_view/view.groovy", 403, null);
    }

    protected File getFile(String fileName) {
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(RESOURCE_TEST_DIR + "/" + fileName);
        return new File(testFormatterViewFile.getFile());
    }

    protected void assertCorrectExec(String fileName, int expectedCode, String expectedContentType) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        resource.exec(RESOURCE_TEST_DIR, fileName, null, response);

        assertEquals(fileName + " returned unexpected code", expectedCode, response.getStatus());
        assertEquals(fileName + " returned content type", expectedContentType, response.getContentType());

        if (expectedContentType != null) {
            assertArrayEquals(fileName + " does not return the expected data", Files.toByteArray(getFile(fileName)),
                    response.getContentAsByteArray());
        }
    }
}