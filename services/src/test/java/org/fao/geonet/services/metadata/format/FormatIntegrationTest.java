package org.fao.geonet.services.metadata.format;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.log4j.Level;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentProxy;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletContext;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
        final Path testFormatter = IO.toPath(testFormatterViewFile.toURI()).getParent();
        final Path formatterDir = this.dataDirectory.getFormatterDir();
        IO.copyDirectoryOrFile(testFormatter, formatterDir.resolve(formatterName), false);
        final String functionsXslName = "functions.xsl";
        Files.deleteIfExists(formatterDir.resolve(functionsXslName));
        IO.copyDirectoryOrFile(testFormatter.getParent().resolve(functionsXslName), formatterDir.resolve(functionsXslName), false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request, new MockHttpServletResponse());
    }

    @Test
    public void testLoggingNullPointerBug() throws Exception {
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Geonet.FORMATTER);
        Level level = logger.getLevel();
        logger.setLevel(Level.ALL);
        try {
            final FormatterParams fparams = new FormatterParams();
            fparams.context = this.serviceContext;
            fparams.params = Collections.emptyMap();
            // make sure context is cleared
            EnvironmentProxy.setCurrentEnvironment(fparams, mapper);


            final String formatterName = "logging-null-pointer";
            final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName + "/view.groovy");
            final Path testFormatter = IO.toPath(testFormatterViewFile.toURI()).getParent();
            final Path formatterDir = this.dataDirectory.getFormatterDir();
            IO.copyDirectoryOrFile(testFormatter, formatterDir.resolve(formatterName), false);
            final String functionsXslName = "functions.xsl";
            Files.deleteIfExists(formatterDir.resolve(functionsXslName));
            IO.copyDirectoryOrFile(testFormatter.getParent().resolve(functionsXslName), formatterDir.resolve(functionsXslName), false);


            MockHttpServletRequest request = new MockHttpServletRequest();
            formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request, new MockHttpServletResponse());

            // no Error is success
        } finally {
            logger.setLevel(level);
        }
    }

    @Test
    public void testExec() throws Exception {
        final ListFormatters.FormatterDataResponse formatters = listService.exec(null, null, schema, false);

        for (ListFormatters.FormatterData formatter : formatters.getFormatters()) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            formatService.exec("eng", "html", "" + id, null, formatter.getId(), "true", false, request, response);

            final String view = response.getContentAsString();
            try {
                assertFalse(formatter.getSchema() + "/" + formatter.getId(), view.isEmpty());
            } catch (Throwable e) {
                e.printStackTrace();
                fail(formatter.getSchema() + " > " + formatter.getId());
            }
            try {
                response = new MockHttpServletResponse();
                formatService.exec("eng", "pdf", "" + id, null, formatter.getId(), "true", false, request, response);
//                Files.write(Paths.get("e:/tmp/view.pdf"), response.getContentAsByteArray());
//                System.exit(0);
            } catch (Throwable t) {
                t.printStackTrace();
                fail(formatter.getSchema() + " > " + formatter.getId());
            }
        }
    }

    @Test
    public void testExecXslt() throws Exception {
        final ServletContext context = _applicationContext.getBean(ServletContext.class);
        MockHttpServletRequest request = new MockHttpServletRequest(context, "GET", "http://localhost:8080/geonetwork/srv/eng/md.formatter");
        request.setPathInfo("/eng/md.formatter");

        WebApplicationContext webAppContext = new GenericWebApplicationContext((DefaultListableBeanFactory) _applicationContext.getBeanFactory());

        final String applicationContextAttributeKey = "srv";
        request.getServletContext().setAttribute(applicationContextAttributeKey, webAppContext);
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);

        RequestContextHolder.setRequestAttributes(requestAttributes);
        final String formatterName = "xsl-test-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.xsl");
        final Path testFormatter = IO.toPath(testFormatterViewFile.toURI()).getParent();
        final Path formatterDir = this.dataDirectory.getFormatterDir();
        Files.deleteIfExists(formatterDir.resolve("functions.xsl"));
        IO.copyDirectoryOrFile(testFormatter, formatterDir.resolve(formatterName), false);
        IO.copyDirectoryOrFile(testFormatter.getParent().resolve("functions.xsl"), formatterDir, true);
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(applicationContextAttributeKey);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterName, "true", false, request, response);
        final String viewXml = response.getContentAsString();
        final Element view = Xml.loadString(viewXml, false);
        assertEqualsText("fromFunction", view, "*//p");
        assertEqualsText("Title", view, "*//div[@class='tr']");
    }

    @Test
    public void testExecGroovy() throws Exception {
        final String formatterName = "groovy-test-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.groovy");
        final Path testFormatter = IO.toPath(testFormatterViewFile.toURI()).getParent();
        final Path formatterDir = this.dataDirectory.getFormatterDir();
        IO.copyDirectoryOrFile(testFormatter, formatterDir.resolve(formatterName), false);
        final String groovySharedClasses = "groovy";
        IO.copyDirectoryOrFile(testFormatter.getParent().resolve(groovySharedClasses), formatterDir.resolve(groovySharedClasses), false);


        final Path iso19139ConfigProperties = this.schemaManager.getSchemaDir("iso19139").resolve("formatter/config.properties");
        Files.write(iso19139ConfigProperties, "dependsOn=dublin-core".getBytes("UTF-8"));

        final Path dublinCoreSchemaDir = this.schemaManager.getSchemaDir("dublin-core").resolve("formatter/groovy");
        Files.createDirectories(dublinCoreSchemaDir);
        IO.copyDirectoryOrFile(IO.toPath(FormatIntegrationTest.class.getResource(formatterName+"/dublin-core-groovy").toURI()),
                dublinCoreSchemaDir.resolve("DCFunctions.groovy"), false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("h2IdentInfo", "true");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        formatService.exec("eng", "html", "" + id, null, formatterName, "true", false, request, response);
        final String viewString = response.getContentAsString();
//        com.google.common.io.Files.write(viewString, new File("e:/tmp/view.html"), Constants.CHARSET);

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
        assertElement(view, "body//p[@class = 'online-resource']/h3", "OnLine resource\n", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div/strong", "REPOM\n", 1);
        assertElement(view, "body//p[@class = 'online-resource']/div[@class='desc']", "\n", 1);
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

        assertElement(view, "body//span[@class = 'extents']", "2", 1);

        assertNull(Xml.selectElement(view, "body//h1[text() = 'Reference System Information']"));
    }

    private void assertElement(Element view, String onlineResourceHeaderXpath, String expected, int numberOfElements) throws JDOMException {
        assertEquals(Xml.getString(view), numberOfElements, Xml.selectNodes(view, onlineResourceHeaderXpath).size());
        assertEqualsText(expected, view, onlineResourceHeaderXpath);
    }


}