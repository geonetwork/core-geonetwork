package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.eclipse.jetty.util.IO;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentProxy;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FormatIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private Format formatService;
    @Autowired
    private ListFormatters listService;
    @Autowired
    private IsoLanguagesMapper mapper;
    private ServiceContext serviceContext;
    private int id;
    private String schema;
    private String uuid;

    @Before
    public void setUp() throws Exception {
        this.serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = getSampleMetadataXml();
        this.uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid);
        final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        this.id =  importMetadataXML(serviceContext, uuid, stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), uuid);
        this.schema = schemaManager.autodetectSchema(sampleMetadataXml);

    }

    @Test(expected = AssertionError.class)
    public void testGroovyUseEnvDuringConfigStage() throws Exception {
        final FormatterParams fparams = new FormatterParams();
        fparams.context = this.serviceContext;
        fparams.params = Collections.emptyMap();
        // make sure context is cleared
        EnvironmentProxy.setCurrentEnvironment(fparams, mapper);


        final String formatterName = "groovy-illegal-env-access-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.groovy");
        final File testFormatter = new File(testFormatterViewFile.getFile()).getParentFile();
        IO.copy(testFormatter, new File(this.dataDirectory.getFormatterDir(), formatterName));
        final String functionsXslName = "functions.xsl";
        IO.copy(new File(testFormatter.getParentFile(), functionsXslName), new File(this.dataDirectory.getFormatterDir(), functionsXslName));

        MockHttpServletRequest request = new MockHttpServletRequest();
        formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request, new MockHttpServletResponse());
    }
    @Test
    public void testExec() throws Exception {


        final ListFormatters.FormatterDataResponse formatters = listService.exec(null, null, schema, false);

        for (ListFormatters.FormatterData formatter : formatters.getFormatters()) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();
            formatService.exec("eng", "html", "" + id, null, formatter.getId(), "true", false, request, response);
            final String view = response.getContentAsString();
            Element html = new Element("html").addContent(Xml.loadString(view, false));
            assertFalse(formatter.getSchema() + "/" + formatter.getId(), html.getChildren().isEmpty());
        }
    }

    @Test
    public void testExecXslt() throws Exception {
        final String formatterName = "xsl-test-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.xsl");
        final File testFormatter = new File(testFormatterViewFile.getFile()).getParentFile();
        IO.copy(testFormatter, new File(this.dataDirectory.getFormatterDir(), formatterName));
        final String functionsXslName = "functions.xsl";
        IO.copy(new File(testFormatter.getParentFile(), functionsXslName), new File(this.dataDirectory.getFormatterDir(), functionsXslName));

        MockHttpServletRequest request = new MockHttpServletRequest();

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterName, "true", false, request, response);
        final String viewXml = response.getContentAsString();
        final Element view = Xml.loadString(viewXml, false);
        assertEqualsText("fromFunction", view, "*//p");
    }

    @Test
    public void testExecGroovy() throws Exception {
        final String formatterName = "groovy-test-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.groovy");
        final File testFormatter = new File(testFormatterViewFile.getFile()).getParentFile();
        IO.copy(testFormatter, new File(this.dataDirectory.getFormatterDir(), formatterName));
        final String groovySharedClasses = "groovy";
        IO.copy(new File(testFormatter.getParentFile(), groovySharedClasses), new File(this.dataDirectory.getFormatterDir(), groovySharedClasses));


        final File dublinCoreSchemaDir = new File(this.schemaManager.getSchemaDir("dublin-core"), "formatter/groovy");
        dublinCoreSchemaDir.mkdirs();
        IO.copy(new File(FormatIntegrationTest.class.getResource(formatterName+"/dublin-core-groovy").getFile()), new File(dublinCoreSchemaDir, "DCFunctions.groovy"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("h2IdentInfo", "true");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterName, "true", false, request, response);
        final String viewString = response.getContentAsString();
//        Files.write(viewString, new File("e:/tmp/view.html"), Constants.CHARSET);
        final Element view = Xml.loadString(viewString, false);
        assertEquals("html", view.getName());
        assertNotNull("body", view.getChild("body"));

        // Check that the "handlers.add 'gmd:abstract', { el ->" correctly applied
        assertElement(view, "body//p[@class = 'abstract']/span[@class='label']", "Abstract", 1);
        assertElement(view, "body//p[@class = 'abstract']/span[@class='value']", "Abstract {uuid}", 1);

        // Check that the "handlers.add ~/...:title/, { el ->" correctly applied
        assertElement(view, "body//p[@class = 'title']/span[@class='label']", "Title", 1);
        assertElement(view, "body//p[@class = 'title']/span[@class='value']", "Title", 1);

        // Check that the "handlers.withPath ~/[^>]+>gmd:identificationInfo>.*extent/, Iso19139Functions.&handleExtent" correctly applied
        assertElement(view, "body//p[@class = 'formatter']", "fromFormatterGroovy", 1);

        // Check that the "handlers.withPath ~/[^>]+>gmd:identificationInfo>.*extent/, Iso19139Functions.&handleExtent" correctly applied
        assertElement(view, "body//p[@class = 'shared']", "fromSharedFunctions", 1);


        // Check that the "handlers.add ~/...:title/, { el ->" correctly applied
        assertElement(view, "body//p[@class = 'code']/span[@class='label']", "Unique resource identifier", 1);
        assertElement(view, "body//p[@class = 'code']/span[@class='value']", "WGS 1984", 1);

        // Check that the handlers.add 'gmd:CI_OnlineResource', { el -> handler is applied
        assertElement(view, "body//p[@class = 'online-resource']/h3", "OnLine resource", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div/strong", "REPOM", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div[@class='desc']", "", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div[@class='linkage']/span[@class='label']", "URL:", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div[@class='linkage']/span[@class='value']", "http://services.sandre.eaufrance.fr/geo/ouvrage", 1);

        // Check that the handler:
        //   handlers.add select: {el -> el.name() == 'gmd:identificationInfo' && f.param('h2IdentInfo').toBool()},
        //                processChildren: true, { el, childData ->
        // was applied
        assertElement(view, "*//div[@class = 'identificationInfo']/h2", "Data identification", 1);
        List<Element> identificationElements = (List<Element>) Xml.selectNodes(view, "*//div[@class = 'identificationInfo']/p");
        assertEquals(viewString, 4, identificationElements.size());
        assertEquals(viewString, "abstract", identificationElements.get(0).getAttributeValue("class"));
        assertEquals(viewString, "shared", identificationElements.get(1).getAttributeValue("class"));
        assertEquals(viewString, "block", identificationElements.get(2).getAttributeValue("class"));
        assertEquals(viewString, "block", identificationElements.get(3).getAttributeValue("class"));
        assertEquals(viewString, "block", identificationElements.get(3).getAttributeValue("class"));

        // Verify that handler
        // handlers.add name: 'codelist handler', select: isoHandlers.matchers.isCodeListEl, isoHandlers.isoCodeListEl
        // is handled
        assertElement(view, "body//span[@class = 'fileId']", this.uuid, 1);
        assertElement(view, "body//span[@class = 'creatorTranslated']", "Creator", 1);
    }

    private void assertElement(Element view, String onlineResourceHeaderXpath, String expected, int numberOfElements) throws JDOMException {
        assertEquals(Xml.getString(view), numberOfElements, Xml.selectNodes(view, onlineResourceHeaderXpath).size());
        assertEqualsText(expected, view, onlineResourceHeaderXpath);
    }


}