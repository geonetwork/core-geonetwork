package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.TestFunction;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentProxy;
import org.fao.geonet.services.metadata.format.groovy.Handler;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.fao.geonet.services.metadata.format.groovy.Transformer;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static com.google.common.xml.XmlEscapers.xmlContentEscaper;

/**
 * @author Jesse on 10/23/2014.
 */
public abstract class AbstractFormatterTest extends AbstractCoreIntegrationTest {
    protected static final String UUID = "80f1c261-1495-4e3b-bd22-f32a5f0ad643";
    @Autowired
    protected Format formatService;
    @Autowired
    protected SystemInfo systemInfo;
    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    SettingManager settingManager;
    @Autowired
    DataManager dataManager;

    protected int id;
    protected String xml;

    @Before
    public void setUp() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        this.xml = Files.toString(getTestMetadataFile(), Constants.CHARSET);
        final Element md = Xml.loadString(this.xml, false);
        final String schemaId = schemaManager.autodetectSchema(md);

        Metadata metadata = new Metadata();
        MetadataSourceInfo sourceInfo = new MetadataSourceInfo().setSourceId(settingManager.getSiteId()).setOwner(1);
        metadata.setSourceInfo(sourceInfo);
        MetadataDataInfo dataInfo = new MetadataDataInfo().
                setChangeDate(new ISODate()).
                setSchemaId(schemaId).
                setCreateDate(new ISODate()).
                setType(MetadataType.METADATA).
                setRoot(md.getQualifiedName());
        metadata.setDataInfo(dataInfo);
        metadata.setUuid(UUID);
        MetadataHarvestInfo harvestInfo = new MetadataHarvestInfo().setHarvested(isHarvested());
        metadata.setHarvestInfo(harvestInfo);
        metadata.setData(xml);
        this.id = dataManager.insertMetadata(serviceContext, metadata, metadata.getXmlData(false), false, true, false,
                UpdateDatestamp.NO, false, false).getId();
    }

    public boolean isHarvested() {
        return false;
    }

    protected abstract File getTestMetadataFile();

    protected Handlers getHandlers(MockHttpServletRequest request, String formatterId) throws Exception {
        final Pair<FormatterImpl, FormatterParams> formatterAndParams = getFormatterFormatterParamsPair(request, formatterId);

        if (formatterAndParams.one() instanceof GroovyFormatter) {
            GroovyFormatter formatter = (GroovyFormatter) formatterAndParams.one();
            final Transformer transformer = formatter.findTransformer(formatterAndParams.two());
            return transformer.getHandlers();
        } else {
            throw new AssertionError("This method only applies to groovy formatters");
        }
    }

    protected Pair<FormatterImpl, FormatterParams> getFormatterFormatterParamsPair(MockHttpServletRequest request, String formatterId) throws Exception {
        return this.formatService.loadMetadataAndCreateFormatterAndParams(getUILang(), getOutputType(),
                "" + id, null, formatterId, "true", false, request);
    }

    protected void measureFormatterPerformance(final MockHttpServletRequest request, final String formatterId) throws Exception {
        TestFunction testFunction = new TestFunction() {
            @Override
            public void exec() throws Exception{
                formatService.exec(getUILang(), getOutputType().name(), "" + id, null, formatterId, "true", false, request,
                        new MockHttpServletResponse());
            }
        };

        super.measurePerformance(testFunction);
    }

    private FormatType getOutputType() {
        return FormatType.html;
    }

    private String getUILang() {
        return "eng";
    }

    private double round(double num) {
        return Math.round(num * 1000) /1000;
    }

    public static GPathResult parseXml(String xmlString, Namespace... namespaces) throws Exception {
        Map<String, String> namespaceUriToPrefix = Maps.newHashMap();
        for (Namespace namespace : namespaces) {
            namespaceUriToPrefix.put(namespace.getPrefix(), namespace.getURI());
        }
        final XmlSlurper xmlSlurper = new XmlSlurper(false, false);
        return xmlSlurper.parseText(xmlString).declareNamespace(namespaceUriToPrefix);
    }

    protected String executeHandler(MockHttpServletRequest request, String formatterId, GPathResult elem, Handler handler) throws Exception {

        final Pair<FormatterImpl, FormatterParams> formatterFormatterParamsPair = getFormatterFormatterParamsPair(request, formatterId);
        try {
            EnvironmentProxy.setCurrentEnvironment(formatterFormatterParamsPair.two(), this.mapper);
            TransformationContext context = new TransformationContext(null, null, new EnvironmentProxy());
            context.setThreadLocal();
            StringBuilder result = new StringBuilder();
            handler.handle(context, Collections.singletonList(elem), result);
            return result.toString();
        } finally {
            EnvironmentProxy.clearContext();
        }
    }

    protected String escapeXmlText(String text) {
        return xmlContentEscaper().escape(text.replaceAll("\\s+", " ")).replaceAll("\\&", "&amp;").replaceAll("\"", "&quot;");
    }
}
