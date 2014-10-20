package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.eclipse.jetty.util.IO;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.annotation.Nullable;

import static org.fao.geonet.domain.Pair.read;
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
    private ServiceContext serviceContext;
    private int id;
    private String schema;

    @Before
    public void setUp() throws Exception {
        this.serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = getSampleMetadataXml();
        final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        this.id =  importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);
        this.schema = schemaManager.autodetectSchema(sampleMetadataXml);

    }

    @Test(expected = AssertionError.class)
    public void testGroovyUseEnvDuringConfigStage() throws Exception {
        final String formatterName = "groovy-illegal-env-access-formatter";
        final URL testFormatterViewFile = FormatIntegrationTest.class.getResource(formatterName+"/view.groovy");
        final File testFormatter = new File(testFormatterViewFile.getFile()).getParentFile();
        IO.copy(testFormatter, new File(this.dataDirectory.getFormatterDir(), formatterName));
        final String functionsXslName = "functions.xsl";
        IO.copy(new File(testFormatter.getParentFile(), functionsXslName), new File(this.dataDirectory.getFormatterDir(), functionsXslName));

        MockHttpServletRequest request = new MockHttpServletRequest();
        formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request);
    }
    @Test
    public void testExec() throws Exception {


        final ListFormatters listService = new ListFormatters();
        final Element formattersEl = listService.exec(createParams(read("schema", schema)), serviceContext);

        final List<String> formatters = Lists.transform(formattersEl.getChildren("formatter"), new Function() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element)input).getText();
            }
        });

        for (String formatter : formatters) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            final Element view = Xml.loadString(formatService.exec("eng", "html", "" + id, null, formatter, null, null, request), false);
            view.setName("body");
            Element html = new Element("html").addContent(view);
            assertFalse(html.getChildren().isEmpty());
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
        final Element view = Xml.loadString(formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request), false);
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

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("h2IdentInfo", "true");
//        long start = System.nanoTime();
//        final long fiveSec = TimeUnit.SECONDS.toNanos(5);
//        while (System.nanoTime() - start < fiveSec) {
//            formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request);
//        }
//        final long thirtySec = TimeUnit.SECONDS.toNanos(30);
//        start = System.nanoTime();
//        int executions = 0;
//        while (System.nanoTime() - start < thirtySec) {
//            formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request);
//            executions++;
//        }
//
//        System.out.println("Executed " + executions + " in 30 seconds.  Average of " + (30000.0/executions) + "ms per execution");


        final String viewString = formatService.exec("eng", "html", "" + id, null, formatterName, null, null, request);
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
    }

    private void assertElement(Element view, String onlineResourceHeaderXpath, String expected, int numberOfElements) throws JDOMException {
        assertEquals(Xml.getString(view), numberOfElements, Xml.selectNodes(view, onlineResourceHeaderXpath).size());
        assertEqualsText(expected, view, onlineResourceHeaderXpath);
    }


}