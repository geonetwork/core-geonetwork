package org.fao.geonet.kernel.harvest.harvester.csw;

import com.google.common.base.Predicate;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.harvest.AbstractHarvesterIntegrationTest;
import org.fao.geonet.kernel.harvest.MockRequestFactoryGeonet;
import org.fao.geonet.utils.*;
import org.jdom.Element;

import javax.annotation.Nullable;

import static junit.framework.Assert.assertEquals;

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
    public static final String REQUEST = PROTOCOL+"://"+HOST+":"+PORT+"/geonetwork/srv/eng/csw";
    public static final String CAPABILITIES_QUERY_STRING = "?service=CSW&request=GetCapabilities";
    public static final String CAPABILITIES_URL = REQUEST + CAPABILITIES_QUERY_STRING;
    public static final String OUTPUT_SCHEMA = "http://www.isotc211.org/2005/gmd";

    public CswHarvesterIntegrationTest() {
        super("csw");
    }

    protected void mockHttpRequests(MockRequestFactoryGeonet bean) {
        final MockXmlRequest cswServerRequest = new MockXmlRequest(HOST, PORT, PROTOCOL);
        cswServerRequest.when(CAPABILITIES_URL)
                .thenReturn(fileStream("capabilities.xml"));
        final String queryString = "?request=GetRecordById&service=CSW&version=2.0.2&outputSchema=http://www.isotc211" +
                         ".org/2005/gmd&elementSetName=full&id=";
        cswServerRequest.when(REQUEST+queryString+"7e926fbf-00fb-4ff5-a99e-c8576027c4e7")
                .thenReturn(fileStream("GetRecordById-7e926fbf-00fb-4ff5-a99e-c8576027c4e7.xml"));
        cswServerRequest.when(REQUEST+queryString+"da165110-88fd-11da-a88f-000d939bc5d8")
                .thenReturn(fileStream("GetRecordById-da165110-88fd-11da-a88f-000d939bc5d8.xml"));
        cswServerRequest.when(new Predicate<HttpRequestBase>(){
            @Override
            public boolean apply(@Nullable HttpRequestBase input) {

                final boolean isHttpPost = input instanceof HttpPost;
                final boolean correctPath = input.getURI().toString().equalsIgnoreCase(REQUEST);
                if (!(isHttpPost && correctPath)) {
                    return false;
                }
                final Element xml;
                try {
                    xml = Xml.loadStream(((HttpPost) input).getEntity().getContent());
                } catch (Throwable e) {
                    return false;
                }
                final boolean isGetRecords = "GetRecords".equalsIgnoreCase(xml.getName());
                final Element queryEl = xml.getChild("Query", Csw.NAMESPACE_CSW);
                final String typeNames = queryEl.getAttributeValue("typeNames");
                final boolean correctTypeNames = typeNames.contains("gmd:MD_Metadata") && typeNames.contains("csw:Record");
                final boolean isSummary = queryEl.getChild("ElementSetName", Csw.NAMESPACE_CSW).getText().equals("summary");
                final boolean noQueryFilter = queryEl.getChildren().size() == 1;

                return isGetRecords && correctTypeNames && isSummary && noQueryFilter;
            }
        }).thenReturn(fileStream("getRecords.xml"));


        bean.registerRequest(true, HOST, PORT, PROTOCOL, cswServerRequest);
        bean.registerRequest(true, null, 80, PROTOCOL, cswServerRequest);
    }

    protected void customizeParams(Element params) {
        addCswSpecificParams(params);
    }

    public static void addCswSpecificParams(Element params) {
        params.getChild("site")
                .addContent(new Element("capabilitiesUrl").setText(CAPABILITIES_URL))
                .addContent(new Element("outputSchema").setText(OUTPUT_SCHEMA));
    }

    @Override
    protected int getExpectedAdded() {
        return 2;
    }

    @Override
    protected int getExpectedTotalFound() {
        return 2;
    }
}
