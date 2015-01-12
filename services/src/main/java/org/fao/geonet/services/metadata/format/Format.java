//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.format.groovy.ParamValue;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.io.Files.getNameWithoutExtension;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
@Controller("md.formatter.type")
public class Format extends AbstractFormatService {

    @Autowired
    private ApplicationContext springAppContext;

    @Autowired
    private SettingManager settingManager;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private XmlSerializer xmlSerializer;
    @Autowired
    private XsltFormatter xsltFormatter;
    @Autowired
    private GroovyFormatter groovyFormatter;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    private GeonetHttpRequestFactory requestFactory;

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<String, Element>();
    private Map<Path, Boolean> isFormatterInSchemaPluginMap = Maps.newHashMap();


    @RequestMapping(value = "/{lang}/xml.format.{type}")
    public void execXml(
            @PathVariable final String lang,
            @PathVariable final String type,
            @RequestParam(value = "xsl", required = false) final String xslid,
            @RequestParam(value = "metadata", required = false) String metadata,
            @RequestParam(value = "url", required = false) final String url,
            @RequestParam(value = "schema") final String schema,
            final HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (url == null && metadata == null) {
            throw new IllegalArgumentException("Either the metadata or url parameter must be declared.");
        }
        if (url != null && metadata != null) {
            throw new IllegalArgumentException("Only one of metadata or url parameter must be declared.");
        }

        if (metadata == null) {
            metadata = getXmlFromUrl(lang, url, request);
        }
        FormatType formatType = FormatType.valueOf(type.toLowerCase());
        Element metadataEl = Xml.loadString(metadata, false);
        Metadata metadataInfo = new Metadata().setData(metadata).setId(1).setUuid("uuid");
        metadataInfo.getDataInfo().setType(MetadataType.METADATA).setRoot(metadataEl.getQualifiedName()).setSchemaId(schema);

        final ServiceContext context = createServiceContext(lang, formatType, request);
        Pair<FormatterImpl, FormatterParams> result = createFormatterAndParams(lang, formatType, xslid,
                request, context, metadataEl, metadataInfo);

        writeOutResponse(lang, response, formatType, result);
    }

    private String getXmlFromUrl(String lang, String url, HttpServletRequest servletRequest) throws IOException, URISyntaxException {
        String adjustedUrl = url;
        if (!url.startsWith("http")) {
            adjustedUrl = settingManager.getSiteURL(lang) + url;
        } else {
            final URI uri = new URI(url);
            Set allowedRemoteHosts = springAppContext.getBean("formatterRemoteFormatAllowedHosts", Set.class);
            Assert.isTrue(allowedRemoteHosts.contains(uri.getHost()), "xml.format is not allowed to make requests to " + uri.getHost());
        }

        HttpUriRequest getXmlRequest = new HttpGet(adjustedUrl);
        final Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            final Enumeration<String> headers = servletRequest.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                getXmlRequest.addHeader(headerName, header);
            }
        }
        final ClientHttpResponse execute = requestFactory.execute(getXmlRequest);
        if (execute.getRawStatusCode() != 200) {
            throw new IllegalArgumentException("Request did not succeed.  Response Status: " + execute.getStatusCode() + ", status text: " + execute.getStatusText());
        }
        return new String(ByteStreams.toByteArray(execute.getBody()), Constants.CHARSET);
    }

    public void writeOutResponse(String lang, HttpServletResponse response, FormatType formatType, Pair<FormatterImpl, FormatterParams>
            result) throws Exception {
        FormatterImpl formatter = result.one();
        FormatterParams fparams = result.two();

        response.setContentType(formatType.contentType);
        String filename = "metadata." + formatType;
        response.addHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        final String formattedMetadata = formatter.format(fparams);
        if (formatType == FormatType.pdf) {
            writerAsPDF(response, formattedMetadata, lang);
        } else {
            final byte[] bytes = formattedMetadata.getBytes(Constants.CHARSET);
            response.setCharacterEncoding(Constants.ENCODING);
            response.setContentType("text/html");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }
    }

    @RequestMapping(value = "/{lang}/md.format.{type}")
    public void exec(
            @PathVariable final String lang,
            @PathVariable final String type,
            @RequestParam(required = false) final String id,
            @RequestParam(required = false) final String uuid,
            @RequestParam(value = "xsl", required = false) final String xslid,
            @RequestParam(defaultValue = "n") final String skipPopularity,
            @RequestParam(value = "hide_withheld", required = false) final Boolean hide_withheld,
            final HttpServletRequest request, HttpServletResponse response) throws Exception {

        FormatType formatType = FormatType.valueOf(type.toLowerCase());
        Pair<FormatterImpl, FormatterParams> result = loadMetadataAndCreateFormatterAndParams(lang, formatType, id, uuid, xslid,
                skipPopularity,
                hide_withheld, request);
        writeOutResponse(lang, response, formatType, result);
    }

    private void writerAsPDF(HttpServletResponse response, String htmlContent, String lang) throws IOException, com.itextpdf.text.DocumentException {
        try {
            XslUtil.setNoScript();
            ITextRenderer renderer = new ITextRenderer();
            String siteUrl = this.settingManager.getSiteURL(lang);
            renderer.getSharedContext().setReplacedElementFactory(new ImageReplacedElementFactory(siteUrl, renderer.getSharedContext()
                    .getReplacedElementFactory()));
            renderer.getSharedContext().setDotsPerPixel(13);
            renderer.setDocumentFromString(htmlContent, siteUrl);
            renderer.layout();
            renderer.createPDF(response.getOutputStream());
        } catch (final Exception e) {
            Log.error(Geonet.FORMATTER, "Error converting formatter output to a file: " + htmlContent, e);
            throw e;
        }
    }

    @VisibleForTesting
    Pair<FormatterImpl, FormatterParams> loadMetadataAndCreateFormatterAndParams(
            final String lang, final FormatType type, final String id, final String uuid, final String xslid,
            final String skipPopularity, final Boolean hide_withheld, final HttpServletRequest request) throws Exception {

        ServiceContext context = createServiceContext(lang, type, request);
        final Pair<Element, Metadata> elementMetadataPair = getMetadata(context, id, uuid, new ParamValue(skipPopularity), hide_withheld);
        Element metadata = elementMetadataPair.one();
        Metadata metadataInfo = elementMetadataPair.two();

        return createFormatterAndParams(lang, type, xslid, request, context, metadata, metadataInfo);
    }

    private ServiceContext createServiceContext(String lang, FormatType type, HttpServletRequest request) {
        return this.serviceManager.createServiceContext("metadata.formatter" + type, lang, request);
    }

    private Pair<FormatterImpl, FormatterParams> createFormatterAndParams(String lang, FormatType type, String xslid, HttpServletRequest request, ServiceContext context, Element metadata, Metadata metadataInfo) throws Exception {
        final String schema = metadataInfo.getDataInfo().getSchemaId();
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }
        Path formatDir = getAndVerifyFormatDir(geonetworkDataDirectory, "xsl", xslid, schemaDir);

        ConfigFile config = new ConfigFile(formatDir, true, schemaDir);

        if (!isCompatibleMetadata(schema, config)) {
            throw new IllegalArgumentException("The bundle cannot format metadata with the " + schema + " schema");
        }

        FormatterParams fparams = new FormatterParams();
        fparams.config = config;
        fparams.format = this;
        fparams.params = request.getParameterMap();
        fparams.context = context;
        fparams.formatDir = formatDir.toRealPath();
        fparams.metadata = metadata;
        fparams.schema = schema;
        fparams.schemaDir = schemaDir;
        fparams.formatType = type;
        fparams.url = settingManager.getSiteURL(lang);
        fparams.metadataInfo = metadataInfo;
        fparams.formatterInSchemaPlugin = isFormatterInSchemaPlugin(formatDir, schemaDir);

        Path viewXslFile = formatDir.resolve(FormatterConstants.VIEW_XSL_FILENAME);
        Path viewGroovyFile = formatDir.resolve(FormatterConstants.VIEW_GROOVY_FILENAME);
        FormatterImpl formatter;
        if (Files.exists(viewXslFile)) {
            fparams.viewFile = viewXslFile.toRealPath();
            formatter = this.xsltFormatter;
        } else if (Files.exists(viewGroovyFile)) {
            fparams.viewFile = viewGroovyFile.toRealPath();
            formatter = this.groovyFormatter;
        } else {
            throw new IllegalArgumentException("The 'xsl' parameter must be a valid id of a formatter");
        }

        return Pair.read(formatter, fparams);
    }

    private synchronized boolean isFormatterInSchemaPlugin(Path formatterDir, Path schemaDir) throws IOException {
        final Path canonicalPath = formatterDir.toRealPath();
        Boolean isInSchemaPlugin = this.isFormatterInSchemaPluginMap.get(canonicalPath);
        if (isInSchemaPlugin == null) {
            isInSchemaPlugin = false;
            Path current = formatterDir;
            while (current.getParent() != null && Files.exists(current.getParent())) {
                if (current.equals(schemaDir)) {
                    isInSchemaPlugin = true;
                    break;
                }
                current = current.getParent();
            }

            this.isFormatterInSchemaPluginMap.put(canonicalPath, isInSchemaPlugin);
        }
        return isInSchemaPlugin;
    }

    public Pair<Element, Metadata> getMetadata(ServiceContext context, String id, String uuid, ParamValue skipPopularity,
                                               Boolean hide_withheld) throws Exception {

        Metadata md = loadMetadata(this.metadataRepository, id, uuid);
        Element metadata = xmlSerializer.removeHiddenElements(false, md);


        boolean withholdWithheldElements = hide_withheld != null && hide_withheld;
        if (XmlSerializer.getThreadLocal(false) != null || withholdWithheldElements) {
            XmlSerializer.getThreadLocal(true).setForceFilterEditOperation(withholdWithheldElements);
        }

        id = String.valueOf(md.getId());

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        if (!skipPopularity.toBool()) {
            this.dataManager.increasePopularity(context, id);
        }


        return Pair.read(metadata, md);

    }

    private boolean isCompatibleMetadata(String schemaName, ConfigFile config) throws Exception {

        List<String> applicable = config.listOfApplicableSchemas();
        return applicable.contains(schemaName) || applicable.contains("all");
    }

    Element getStrings(Path appPath, String lang) throws IOException, JDOMException {
        Path baseLoc = appPath.resolve("loc");
        Path locDir = findLocDir(lang, baseLoc);
        if (Files.exists(locDir)) {
            return Xml.loadFile(locDir.resolve("xml").resolve("strings.xml"));
        }
        return new Element("strings");
    }

    /**
     * Get the localization files from current format plugin.  It will load all xml file in the loc/lang/ directory as children
     * of the returned element.
     */
    public synchronized Element getPluginLocResources(ServiceContext context, Path formatDir, String lang) throws Exception {
        final Element pluginLocResources = getPluginLocResources(context, formatDir);
        Element translations = pluginLocResources.getChild(lang);
        if (translations == null) {
            if (pluginLocResources.getChildren().isEmpty()) {
                translations = new Element(lang);
            } else {
                translations = (Element) pluginLocResources.getChildren().get(0);
            }
        }
        return translations;
    }
    public synchronized Element getPluginLocResources(ServiceContext context, Path formatDir) throws Exception {
        final String formatDirPath = formatDir.toString();
        Element allLangResources = this.pluginLocs.get(formatDirPath);
        if (isDevMode(context) || allLangResources == null) {
            allLangResources = new Element("loc");
            Path baseLoc = formatDir.resolve("loc");
            if (Files.exists(baseLoc)) {
                try (DirectoryStream<Path> locDirs = Files.newDirectoryStream(baseLoc)) {
                    for (Path locDir : locDirs) {
                        final String locDirName = getNameWithoutExtension(locDir.getFileName().toString());
                        Element resources = new Element(locDirName);
                        if (Files.exists(locDir)) {
                            try (DirectoryStream<Path> paths = Files.newDirectoryStream(locDir, "*.xml")) {
                                for (Path file : paths) {
                                    final Element fileElements = Xml.loadFile(file);
                                    final String fileName = getNameWithoutExtension(file.getFileName().toString());
                                    fileElements.setName(fileName);
                                    if (!fileElements.getChildren().isEmpty()) {
                                        resources.addContent(fileElements);
                                    }
                                }
                            }
                        }
                        if (!resources.getChildren().isEmpty()) {
                            allLangResources.addContent(resources);
                        }
                    }
                }
            }

            this.pluginLocs.put(formatDirPath, allLangResources);
        }
        return allLangResources;
    }

    private Path findLocDir(String lang, Path baseLoc) throws IOException {
        Path locDir = baseLoc.resolve(lang);
        if (!Files.exists(locDir)) {
            locDir = baseLoc.resolve(Geonet.DEFAULT_LANGUAGE);
        }
        if (!Files.exists(locDir) && Files.exists(baseLoc)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(baseLoc)) {
                final Iterator<Path> pathIterator = paths.iterator();
                if (pathIterator.hasNext()) {
                    locDir = pathIterator.next();
                }
            }
        }
        return locDir;
    }

    protected boolean isDevMode(ServiceContext context) {
        return context.getApplicationContext().getBean(SystemInfo.class).isDevMode();
    }

}
