package org.fao.geonet.kernel.harvest.harvester.csw;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.http.client.methods.HttpRequestBase;
import org.fao.geonet.kernel.harvest.AbstractHarvesterIntegrationTest;
import org.fao.geonet.kernel.harvest.MockRequestFactoryGeonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.utils.*;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Integration Test for the Csw Harvester class.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:01 PM
 */
public class CswHarvesterIntegrationTest extends AbstractHarvesterIntegrationTest {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String PROTOCOL = "http";
    public static final String REQUEST = PROTOCOL+"://"+HOST+":"+PORT;
    public static final String CAPABILITIES_QUERY_STRING = "/geonetwork/srv/eng/csw?service=CSW&request=GetCapabilities";
    public static final String CAPABILITIES_URL = REQUEST + CAPABILITIES_QUERY_STRING;
    public static final String OUTPUT_SCHEMA = "http://www.isotc211.org/2005/gmd";
    @Autowired
    private CswHarvester _harvester;

    protected void mockHttpRequests(MockRequestFactoryGeonet bean) {
        final MockXmlRequest cswServerRequest = new MockXmlRequest(HOST, PORT, PROTOCOL);
        cswServerRequest.when(CAPABILITIES_QUERY_STRING).thenReturn(fileStream("capabilities.xml"));
        cswServerRequest.when(new Predicate<HttpRequestBase>(){

            @Override
            public boolean apply(@Nullable HttpRequestBase input) {
                return input.getURI().toString().equals("");
            }
        }).thenReturn(fileStream("capabilities.xml"));


        bean.registerRequest(HOST, PORT, PROTOCOL, cswServerRequest);
        bean.registerRequest(null, 80, PROTOCOL, cswServerRequest);
    }

    protected void customizeParams(Element params) {
        params.getChild("site")
                .addContent(new Element("capabilitiesUrl").setText(CAPABILITIES_URL))
                .addContent(new Element("outputSchema").setText(OUTPUT_SCHEMA));
    }

    @Override
    protected AbstractHarvester getHarvesterUnderTest() {
        return _harvester;
    }

}
