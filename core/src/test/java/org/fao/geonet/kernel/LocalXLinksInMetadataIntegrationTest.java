package org.fao.geonet.kernel;

import jeeves.xlink.Processor;
import org.springframework.test.context.ContextConfiguration;



import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.*;
/**
 * Test local:// xlinks.
 *
 * Created by Jesse on 1/30/14.
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:mock-service-manager.xml")
public class LocalXLinksInMetadataIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SettingManager _settingManager;
    @Autowired
    private DataManager _dataManager;
    @Autowired
    private MockServiceManager _serviceManager;

    @Test
    public void testResolveLocalXLink() throws Exception {
        String namespaces = "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\"";
        final String responseTemplate = "<gmd:MD_Keywords " + namespaces + ">\n"
                                + "    <gmd:keyword>\n"
                                + "        <gco:CharacterString>%s</gco:CharacterString>\n"
                                + "    </gmd:keyword>\n"
                                + "</gmd:MD_Keywords>\n";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
              + "<gmd:MD_Metadata "+namespaces
              + "    gco:isoType=\"gmd:MD_Metadata\">\n"
              + "    <gmd:fileIdentifier xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
              + "        <gco:CharacterString>23b53e29-c0a2-4897-b107-141bb15f929a</gco:CharacterString>\n"
              + "    </gmd:fileIdentifier>\n"
              + "    <gmd:identificationInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
              + "        <gmd:MD_DataIdentification gco:isoType=\"gmd:MD_DataIdentification\">\n"
              + "        <gmd:descriptiveKeywords xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"local://thesaurus" +
              ".keyword?thesaurus=external.place.regions&amp;id=http%3A%2F%2Fgeonetwork-opensource.org%2Fregions%235&amp;lang=eng\">\n"
              + "        </gmd:descriptiveKeywords>\n"
              + "        </gmd:MD_DataIdentification>\n"
              + "    </gmd:identificationInfo>\n"
              + "</gmd:MD_Metadata>";

        final Element metadata = Xml.loadString(xml, false);

        ServiceContext context = createServiceContext();
        context.setAsThreadLocal();
        loginAsAdmin(context);
        _settingManager.setValue(SettingManager.SYSTEM_XLINKRESOLVER_ENABLE, true);

        String schema = "iso19139";
        String uuid = UUID.randomUUID().toString();
        int owner = context.getUserSession().getUserIdAsInt();
        String groupOwner = "" + ReservedGroup.intranet.getId();
        String source = _settingManager.getSiteId();
        String metadataType = MetadataType.METADATA.codeString;
        String changeDate;
        String createDate = changeDate = new ISODate().getDateAndTime();
        String id = _dataManager.insertMetadata(context, schema, metadata, uuid, owner, groupOwner, source,  metadataType, null,
                null, createDate, changeDate, false, false);

        final String keyword1 = "World";
        _serviceManager.setResponse(String.format(responseTemplate, keyword1));

        final String xpath = "*//gmd:descriptiveKeywords//gmd:keyword/gco:CharacterString";
        assertNull(Xml.selectElement(metadata, xpath));
        assertEquals(0, _serviceManager._numberOfCalls);

        final Element loadedMetadataNoXLinkAttributesNotEdit = _dataManager.getMetadata(context, id, false, false, false);
        assertEqualsText(keyword1, loadedMetadataNoXLinkAttributesNotEdit, xpath, GCO, GMD);
        assertEquals(1, _serviceManager.getNumberOfCalls());
        final Element loadedMetadataKeepXLinkAttributesNotEdit = _dataManager.getMetadata(context, id, false, false, true);
        assertEqualsText(keyword1, loadedMetadataKeepXLinkAttributesNotEdit, xpath, GCO, GMD);
        assertEquals(1, _serviceManager.getNumberOfCalls());

        final Element loadedMetadataNoXLinkAttributesEdit = _dataManager.getMetadata(context, id, false, true, false);
        assertEqualsText(keyword1, loadedMetadataNoXLinkAttributesEdit, xpath, GCO, GMD);
        assertEquals(1, _serviceManager.getNumberOfCalls());

        final Element loadedMetadataKeepXLinkAttributesEdit = _dataManager.getMetadata(context, id, false, true, true);
        assertEqualsText(keyword1, loadedMetadataKeepXLinkAttributesEdit, xpath, GCO, GMD);
        assertEquals(1, _serviceManager.getNumberOfCalls());

        Processor.clearCache();
        final String keyword2 = "Other Word";
        _serviceManager.setResponse(String.format(responseTemplate, keyword2));

        final Element newLoad = _dataManager.getMetadata(context, id, false, true, true);
        assertEqualsText(keyword2, newLoad, xpath, GCO, GMD);
        assertEquals(2, _serviceManager.getNumberOfCalls());


    }

    public static class MockServiceManager extends ServiceManager {
        private String _response;
        private int _numberOfCalls = 0;

        @Override
        public void dispatch(ServiceRequest req, UserSession session, ServiceContext context) {
            try {
                _numberOfCalls++;
                req.getOutputStream().write(_response.getBytes("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void setResponse(String _response) {
            this._response = _response;
        }

        public int getNumberOfCalls() {
            return _numberOfCalls;
        }
    }

}
