package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentProxy;
import org.fao.geonet.services.metadata.format.groovy.Handler;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.fao.geonet.services.metadata.format.groovy.Transformer;
import org.jdom.Namespace;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private GroovyFormatter groovyFormatter;

    protected int id;
    protected String xml;

    @Before
    public void setUp() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        this.xml = Files.toString(getTestMetadataFile(), Constants.CHARSET);
        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(Constants.ENCODING));
        this.id = importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), UUID);

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

    private Pair<FormatterImpl, FormatterParams> getFormatterFormatterParamsPair(MockHttpServletRequest request, String formatterId) throws Exception {
        return this.formatService.createFormatterAndParams(getUILang(), getOutputType(),
                    "" + id, null, formatterId, "true", false, request);
    }

    protected void measureFormatterPerformance(MockHttpServletRequest request, String formatterId) throws Exception {
        long start = System.nanoTime();
        final long fiveSec = TimeUnit.SECONDS.toNanos(5);
        systemInfo.setStagingProfile(SystemInfo.STAGE_PRODUCTION);
        while (System.nanoTime() - start < fiveSec) {
            formatService.exec(getUILang(), getOutputType(), "" + id, null, formatterId, "true", false, request);
        }
        System.out.println("Starting big run");
        final int secondsRan = 30;
        final long thirtySec = TimeUnit.SECONDS.toNanos(secondsRan);
        start = System.nanoTime();
        double executions = 0;
        while (System.nanoTime() - start < thirtySec) {
            formatService.exec(getUILang(), getOutputType(), "" + id, null, formatterId, "true", false, request);
            executions++;
        }
        long end = System.nanoTime();

        final long duration = end - start;
        System.out.println("Executed " + executions + " in "+ (TimeUnit.NANOSECONDS.toSeconds(duration * 1000) / 1000)+" seconds.");
        System.out.println("   Average of " + round(((double) TimeUnit.NANOSECONDS.toMillis(duration))/executions) + "ms per execution;");
        System.out.println("   Average of " + round(executions / TimeUnit.NANOSECONDS.toSeconds(duration)) + " executions per second;");
    }

    private String getOutputType() {
        return "html";
    }

    private String getUILang() {
        return "eng";
    }

    private double round(double num) {
        return Math.round(num * 1000) /1000;
    }

    protected GPathResult parseXml(String xmlString, Namespace... namespaces) throws Exception {
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
            TransformationContext context = new TransformationContext();
            context.setThreadLocal();
            StringBuilder result = new StringBuilder();
            handler.handle(context, elem, result);
            return result.toString();
        } finally {
            EnvironmentProxy.clearContext();
        }
    }
}
