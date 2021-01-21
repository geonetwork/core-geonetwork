package org.fao.geonet.util.spring;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MarineIdCas20ProxyTicketValidatorTest {
    private MarineIdCas20ProxyTicketValidator toTest;

    @Before
    public void setUp() {
        this.toTest = new MarineIdCas20ProxyTicketValidator("http://localhost/cas");

    }

    @Test
    public void testExtractParameters() throws IOException {
        URL specUrl = this.getClass().getResource("pmauduit-spec.xml");
        String xmlSpec = IOUtils.toString(specUrl);

        Map<String, Object> ret = this.toTest.extractCustomAttributes(xmlSpec);

        assertTrue(
            ret.containsKey("uid") &&
                ret.containsKey("mail") &&
                ret.containsKey("sn") &&
                ret.containsKey("c") &&
                ret.containsKey("cn") /* maybe not useful to test all of them */
            );
        assertTrue(ret.containsValue("pmauduit"));
    }

    @Test
    public void testExtractParametersEmptySpec() throws IOException {
        URL specUrl = this.getClass().getResource("empty-attrs-spec.xml");
        String xmlSpec = IOUtils.toString(specUrl);

        Map<String, Object> ret = this.toTest.extractCustomAttributes(xmlSpec);

        assertTrue(ret.size() == 0);
    }
}
