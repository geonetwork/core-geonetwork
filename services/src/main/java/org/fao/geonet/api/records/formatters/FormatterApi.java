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

package org.fao.geonet.api.records.formatters;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.xlink.Processor;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.formatters.cache.*;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;

import static com.google.common.io.Files.getNameWithoutExtension;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.api.records.formatters.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;
import static org.springframework.data.jpa.domain.Specification.where;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
@Tag(name = "records",
    description = "Metadata record operations")
@Controller("recordFormatter")
@Lazy
public class FormatterApi extends AbstractFormatService implements ApplicationListener {
    private static final Set<String> ALLOWED_PARAMETERS = Sets.newHashSet("id", "uuid", "xsl", "skippopularity", "hide_withheld");

    private static final String PARAM_LANGUAGE_ALL_VALUES = "all";

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    IsoLanguagesMapper isoLanguagesMapper;

    @Autowired
    PdfOrHtmlResponseWriter writer;

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter
     * bundle's loc directory)
     */
    private final WeakHashMap<String, Element> pluginLocs = new WeakHashMap<>();
    private final Map<Path, Boolean> isFormatterInSchemaPluginMap = Maps.newHashMap();

    /**
     * We will copy all formatter files to the data directory so that the formatters should always
     * compile in data directory without administrators manually keeping all the formatter
     * directories up-to-date.
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof GeonetworkDataDirectory.GeonetworkDataDirectoryInitializedEvent) {
            GeonetworkDataDirectory.GeonetworkDataDirectoryInitializedEvent dataDirEvent =
                (GeonetworkDataDirectory.GeonetworkDataDirectoryInitializedEvent) event;
            final String webappPath = "WEB-INF/data/data/" + SCHEMA_PLUGIN_FORMATTER_DIR;
            final GeonetworkDataDirectory geonetworkDataDirectory = dataDirEvent.getSource();
            final Path fromDir = geonetworkDataDirectory.getWebappDir().resolve(webappPath);
            final Path toDir = geonetworkDataDirectory.getFormatterDir();
            try {
                copyNewerFilesToDataDir(fromDir, toDir);
                SchemaManager schemaManager = dataDirEvent.getApplicationContext().getBean(SchemaManager.class);
                final Set<String> schemas = schemaManager.getSchemas();
                for (String schema : schemas) {
                    final String webappSchemaPath = "WEB-INF/data/config/schema_plugins/" + schema + "/" + SCHEMA_PLUGIN_FORMATTER_DIR;
                    final Path webappSchemaDir = geonetworkDataDirectory.getWebappDir().resolve(webappSchemaPath);
                    final Path dataDirSchemaFormatterDir = schemaManager.getSchemaDir(schema).resolve(SCHEMA_PLUGIN_FORMATTER_DIR);
                    copyNewerFilesToDataDir(webappSchemaDir, dataDirSchemaFormatterDir);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void copyNewerFilesToDataDir(final Path fromDir, final Path toDir) throws IOException {
        if (Files.exists(fromDir)) {
            Files.walkFileTree(fromDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path path = IO.relativeFile(fromDir, file, toDir);
                    if (!file.getFileName().toString().toLowerCase().endsWith(".iml") &&
                        (!Files.exists(path) || Files.getLastModifiedTime(path).compareTo(Files.getLastModifiedTime(file)) < 0)) {
                        Files.deleteIfExists(path);
                        IO.copyDirectoryOrFile(file, path, false);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        }
    }

    @RequestMapping(value = {
        "/{portal}/api/records/{metadataUuid:.+}/formatters/{formatterId:.+}"
    },
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            "application/pdf",
            MediaType.ALL_VALUE
            // TODO: PDF
        })
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a formatted metadata record"
    )
    @ResponseBody
    public void getRecordFormattedBy(
        @Parameter(
            description = "Formatter type to use."
        )
        @PathVariable(
            value = "formatterId"
        ) final String formatterId,
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @RequestParam(
            value = "width",
            defaultValue = "_100") final FormatterWidth width,
        @RequestParam(
            value = "mdpath",
            required = false) final String mdPath,
        @Parameter(
            description = "Optional language ISO 3 letters code to override HTTP Accept-language header.",
            required = false
        )
        @RequestParam(
            value = "language",
            required = false) final String iso3lang,
        @RequestParam(
            value = "output",
            required = false)
            FormatType formatType,
        @Parameter(description = "Download the approved version",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean approved,
        @Parameter(hidden = true) final NativeWebRequest request,
        final HttpServletRequest servletRequest) throws Exception {

        Locale locale = languageUtils.parseAcceptLanguage(servletRequest.getLocales());

        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT)) ? MediaType.TEXT_HTML_VALUE : request.getHeader(HttpHeaders.ACCEPT);

        // TODO :
        // if text/html > xsl_view
        // if application/pdf > xsl_view and PDF output
        // if application/x-gn-<formatterId>+(xml|html|pdf|text)
        // Force PDF output when URL parameter is set.
        // This is useful when making GET link to PDF which
        // can not use headers.
        if (MediaType.ALL_VALUE.equals(acceptHeader)) {
            acceptHeader = MediaType.TEXT_HTML_VALUE;
        }
        if (formatType == null) {
            formatType = FormatType.find(acceptHeader);
        }
        if (formatType == null) {
            formatType = FormatType.xml;
        }

        String language;
        if (StringUtils.isNotEmpty(iso3lang)) {
            if (PARAM_LANGUAGE_ALL_VALUES.equalsIgnoreCase(iso3lang)) {
                language = iso3lang;
            } else if (languageUtils.getUiLanguages().contains(iso3lang)) {
                language = isoLanguagesMapper.iso639_2T_to_iso639_2B(iso3lang);
            } else {
                language = languageUtils.getDefaultUiLanguage();
            }
        } else {
            language = isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        }

        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, servletRequest);

        if (approved) {
            metadata = ApplicationContextHolder.get().getBean(MetadataRepository.class).findOneByUuid(metadataUuid);
        }


        final ServiceContext context = createServiceContext(
            language,
            formatType,
            request.getNativeRequest(HttpServletRequest.class));

        Boolean hideWithheld = !context.getBean(AccessManager.class).canEdit(context, String.valueOf(metadata.getId()));
        Key key = new Key(metadata.getId(), language, formatType, formatterId, hideWithheld, width);
        final boolean skipPopularityBool = false;

        ISODate changeDate = metadata.getDataInfo().getChangeDate();

        Validator validator;

        if (changeDate != null) {
            final long changeDateAsTime = changeDate.toDate().getTime();
            long roundedChangeDate = changeDateAsTime / 1000 * 1000;
            if (request.checkNotModified(language, roundedChangeDate) &&
                context.getBean(CacheConfig.class).allowCaching(key)) {
                if (!skipPopularityBool && approved) {
                    context.getBean(DataManager.class).increasePopularity(context, String.valueOf(metadata.getId()));
                }
                return;
            }
            validator = new ChangeDateValidator(changeDateAsTime);
        } else {
            validator = new NoCacheValidator();
        }
        final FormatMetadata formatMetadata = new FormatMetadata(context, key, request);

        byte[] bytes;
        if (hasNonStandardParameters(request)) {
            // the http headers can cause a formatter to output custom output due to the parameters.
            // because it is not known how the parameters may affect the output then we have two choices
            // 1. make a unique cache for each configuration of parameters
            // 2. don't cache anything that has extra parameters beyond the standard parameters used to
            //    create the key
            // #1 has a major flaw because an attacker could simply make new requests always changing the parameters
            // and completely swamp the cache.  So we go with #2.  The formatters are pretty fast so it is a fine solution
            bytes = formatMetadata.call().data;
        } else {
            bytes = context.getBean(FormatterCache.class).get(key, validator, formatMetadata, false);
        }
        if (bytes != null) {
            if (!skipPopularityBool && approved) {
                context.getBean(DataManager.class).increasePopularity(context, String.valueOf(metadata.getId()));
            }
            writer.writeOutResponse(context, metadataUuid,
                isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language()),
                request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
        }
    }


    /**
     * @param lang     ui language
     * @param type     output type, Must be one of {@link org.fao.geonet.api.records.formatters.FormatType}
     * @param xslid    the id of the formatter
     * @param metadata the xml to format (either metadata or url must be defined)
     * @param url      a url to call and format either metadata or url must be defined)
     * @param schema   the schema of the xml retrieved from the url or of the metadata xml
     * @param width    the approximate size of the element that the formatter output will be
     *                 embedded in compared to the full device width.  Allowed options are the enum
     *                 values: {@link org.fao.geonet.api.records.formatters.FormatterWidth} The
     *                 default is _100 (100% of the screen)
     * @param mdPath   (optional) the xpath to the metadata node if it's not the root node of the
     *                 XML
     */
    @RequestMapping(value = "/{portal}/{lang}/xml.format.{type}")
    @ResponseBody
    @Deprecated
    @io.swagger.v3.oas.annotations.Operation(hidden = true)
    public void execXml(
        @PathVariable final String lang,
        @PathVariable final String type,
        @RequestParam(value = "xsl", required = false) final String xslid,
        @RequestParam(value = "metadata", required = false) String metadata,
        @RequestParam(value = "url", required = false) final String url,
        @RequestParam(value = "schema") final String schema,
        @RequestParam(value = "width", defaultValue = "_100") final FormatterWidth width,
        @RequestParam(value = "mdpath", required = false) final String mdPath,
        final NativeWebRequest request) throws Exception {

        if (url == null && metadata == null) {
            throw new IllegalArgumentException("Either the metadata or url parameter must be declared.");
        }
        if (url != null && metadata != null) {
            throw new IllegalArgumentException("Only one of metadata or url parameter must be declared.");
        }

        FormatType formatType = FormatType.valueOf(type.toLowerCase());
        final ServiceContext context = createServiceContext(lang, formatType, request.getNativeRequest(HttpServletRequest.class));
        if (metadata == null) {
            metadata = getXmlFromUrl(context, lang, url, request);
        }
        Element metadataEl = Xml.loadString(metadata, false);

        if (mdPath != null) {
            final List<Namespace> namespaces = context.getBean(SchemaManager.class).getSchema(schema).getNamespaces();
            metadataEl = Xml.selectElement(metadataEl, mdPath, namespaces);
            metadataEl.detach();
        }
        Metadata metadataInfo = new Metadata();
        metadataInfo.setData(metadata).setId(1).setUuid("uuid");
        metadataInfo.getDataInfo().setType(MetadataType.METADATA).setRoot(metadataEl.getQualifiedName()).setSchemaId(schema);

        Pair<FormatterImpl, FormatterParams> result = createFormatterAndParams(lang, formatType, xslid, width,
            request, context, metadataEl, metadataInfo);
        final String formattedMetadata = result.one().format(result.two());
        byte[] bytes = formattedMetadata.getBytes(Constants.CHARSET);

        writer.writeOutResponse(context, "", lang, request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
    }

    /**
     * This service will read directly from the cache and return the value.  If it is not in the
     * cache then a 404 will be returned.
     * <p>
     * This is a service to use if there is process to keep the cache at least periodically
     * up-to-date and if maximum performance is required.
     */
    @RequestMapping(value = "/{portal}/{lang}/md.format.public.{type}")
    @io.swagger.v3.oas.annotations.Operation(hidden = true)
    public HttpEntity<byte[]> getCachedPublicMetadata(
        @PathVariable final String lang,
        @PathVariable final String type,
        @RequestParam(required = false) final String id,
        @RequestParam(value = "uuid", required = false) final String uuid,
        @RequestParam(value = "xsl", required = false) final String xslid) throws Exception {
        final FormatType formatType = FormatType.valueOf(type.toLowerCase());

        FormatterCache formatterCache = ApplicationContextHolder.get().getBean(FormatterCache.class);

        String resolvedId = resolveId(id, uuid);
        Key key = new Key(Integer.parseInt(resolvedId), lang, formatType, xslid, true, FormatterWidth._100);
        byte[] bytes = formatterCache.getPublished(key);

        if (bytes != null) {
            return new HttpEntity<>(bytes);
        }
        return null;
    }

    /**
     * Run the a formatter against a metadata.
     *
     * @param lang           ui language
     * @param type           output type, Must be one of {@link org.fao.geonet.api.records.formatters.FormatType}
     * @param id             the id, uuid or fileIdentifier of the metadata
     * @param xslid          the id of the formatter
     * @param skipPopularity if true then don't increment popularity
     * @param width          the approximate size of the element that the formatter output will be
     *                       embedded in compared to the full device width.  Allowed options are the
     *                       enum values: {@link org.fao.geonet.api.records.formatters.FormatterWidth}
     *                       The default is _100 (100% of the screen)
     */
    @RequestMapping(value = "/{portal}/{lang}/md.format.{type}")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(hidden = true)
    public void exec(
        @PathVariable final String lang,
        @PathVariable final String type,
        @RequestParam(value = "id", required = false) final String id,
        @RequestParam(value = "uuid", required = false) final String uuid,
        @RequestParam(value = "xsl", required = false) final String xslid,
        @RequestParam(defaultValue = "n") final String skipPopularity,
        @RequestParam(value = "width", defaultValue = "_100") final FormatterWidth width,
        final NativeWebRequest request) throws Exception {
        final FormatType formatType = FormatType.valueOf(type.toLowerCase());

        String resolvedId = resolveId(id, uuid);
        ServiceContext context = createServiceContext(lang, formatType, request.getNativeRequest(HttpServletRequest.class));
        Lib.resource.checkPrivilege(context, resolvedId, ReservedOperation.view);

        final boolean hideWithheld = !context.getBean(AccessManager.class).canEdit(context, resolvedId);
        Key key = new Key(Integer.parseInt(resolvedId), lang, formatType, xslid, hideWithheld, width);
        final boolean skipPopularityBool = skipPopularity.equals("y");

        ISODate changeDate = context.getBean(EsSearchManager.class).getDocChangeDate(resolvedId);

        Validator validator;
        if (changeDate != null) {
            final long changeDateAsTime = changeDate.toDate().getTime();
            validator = new ChangeDateValidator(changeDateAsTime);
        } else {
            validator = new NoCacheValidator();
        }
        final FormatMetadata formatMetadata = new FormatMetadata(context, key, request);

        byte[] bytes;
        if (hasNonStandardParameters(request)) {
            // the http headers can cause a formatter to output custom output due to the parameters.
            // because it is not known how the parameters may affect the output then we have two choices
            // 1. make a unique cache for each configuration of parameters
            // 2. don't cache anything that has extra parameters beyond the standard parameters used to
            //    create the key
            // #1 has a major flaw because an attacker could simply make new requests always changing the parameters
            // and completely swamp the cache.  So we go with #2.  The formatters are pretty fast so it is a fine solution
            bytes = formatMetadata.call().data;
        } else {
            bytes = context.getBean(FormatterCache.class).get(key, validator, formatMetadata, false);
        }
        if (bytes != null) {
            if (!skipPopularityBool) {
                context.getBean(DataManager.class).increasePopularity(context, resolvedId);
            }

            writer.writeOutResponse(context, resolvedId, lang, request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
        }
    }

    private boolean hasNonStandardParameters(NativeWebRequest request) {
        Iterator<String> iter = request.getParameterNames();
        while (iter.hasNext()) {
            if (!ALLOWED_PARAMETERS.contains(iter.next())) {
                return true;
            }

        }
        return false;
    }

    private String getXmlFromUrl(ServiceContext context, String lang, String url, WebRequest request) throws IOException, URISyntaxException {
        String adjustedUrl = url;
        if (!url.startsWith("http")) {
            adjustedUrl = context.getBean(SettingManager.class).getSiteURL(lang) + url;
        } else {
            final URI uri = new URI(url);
            Set allowedRemoteHosts = context.getBean("formatterRemoteFormatAllowedHosts", Set.class);
            Assert.isTrue(allowedRemoteHosts.contains(uri.getHost()), "xml.format is not allowed to make requests to " + uri.getHost());
        }

        HttpUriRequest getXmlRequest = new HttpGet(adjustedUrl);
        final Iterator<String> headerNames = request.getHeaderNames();
        while (headerNames.hasNext()) {
            String headerName = headerNames.next();
            final String[] headers = request.getHeaderValues(headerName);
            for (String header : headers) {
                getXmlRequest.addHeader(headerName, header);
            }
        }

        GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
        final ClientHttpResponse execute = requestFactory.execute(getXmlRequest);
        if (execute.getRawStatusCode() != 200) {
            throw new IllegalArgumentException("Request " + adjustedUrl + " did not succeed.  Response Status: " + execute.getStatusCode() + ", status text: " + execute.getStatusText());
        }
        return new String(ByteStreams.toByteArray(execute.getBody()), Constants.CHARSET);
    }


    @VisibleForTesting
    Pair<FormatterImpl, FormatterParams> loadMetadataAndCreateFormatterAndParams(ServiceContext context, Key key, final NativeWebRequest request) throws Exception {
        final Pair<Element, AbstractMetadata> elementMetadataPair = getMetadata(context, key.mdId, key.hideWithheld);
        Element metadata = elementMetadataPair.one();
        AbstractMetadata metadataInfo = elementMetadataPair.two();

        return createFormatterAndParams(key.lang, key.formatType, key.formatterId, key.width, request, context, metadata, metadataInfo);
    }

    private ServiceContext createServiceContext(String lang, FormatType type, HttpServletRequest request) {
        final ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        return serviceManager.createServiceContext("metadata.formatter" + type, lang, request);
    }


    private Pair<FormatterImpl, FormatterParams> createFormatterAndParams(String lang, FormatType type, String xslid,
                                                                          FormatterWidth width,
                                                                          NativeWebRequest request,
                                                                          ServiceContext context,
                                                                          Element metadata,
                                                                          AbstractMetadata metadataInfo) throws Exception {
        final String schema = metadataInfo.getDataInfo().getSchemaId();
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = context.getBean(SchemaManager.class).getSchemaDir(schema);
        }
        GeonetworkDataDirectory geonetworkDataDirectory = context.getBean(GeonetworkDataDirectory.class);
        Path formatDir = getAndVerifyFormatDir(geonetworkDataDirectory, "xsl", xslid, schemaDir);

        ConfigFile config = new ConfigFile(formatDir, true, schemaDir);

        if (!isCompatibleMetadata(schema, config)) {
            throw new IllegalArgumentException("The bundle cannot format metadata with the " + schema + " schema");
        }

        FormatterParams fparams = new FormatterParams();
        fparams.config = config;
        fparams.format = this;
        fparams.webRequest = request;
        fparams.context = context;
        fparams.formatDir = formatDir.toRealPath();
        fparams.metadata = metadata;
        fparams.schema = schema;
        fparams.schemaDir = schemaDir;
        fparams.formatType = type;
        fparams.url = context.getBean(SettingManager.class).getSiteURL(lang);
        fparams.metadataInfo = metadataInfo;
        fparams.width = width;
        fparams.formatterInSchemaPlugin = isFormatterInSchemaPlugin(formatDir, schemaDir);

        Path viewXslFile = formatDir.resolve(FormatterConstants.VIEW_XSL_FILENAME);
        FormatterImpl formatter;
        if (Files.exists(viewXslFile)) {
            fparams.viewFile = viewXslFile.toRealPath();
            formatter = context.getBean(XsltFormatter.class);
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

    public Pair<Element, AbstractMetadata> getMetadata(ServiceContext context, int id,
                                                       Boolean hide_withheld) throws Exception {

        AbstractMetadata md = loadMetadata(context.getBean(IMetadataUtils.class), id);
        XmlSerializer serializer = context.getBean(XmlSerializer.class);
        boolean doXLinks = serializer.resolveXLinks();


        Element metadata = serializer.removeHiddenElements(false, md, true);
        if (doXLinks) Processor.processXLink(metadata, context);

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
     * Get the localization files from current format plugin.  It will load all xml file in the
     * loc/lang/ directory as children of the returned element.
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

    public synchronized Element getPluginLocResources(final ServiceContext context, Path formatDir) throws Exception {
        final String formatDirPath = formatDir.toString();
        Element allLangResources = this.pluginLocs.get(formatDirPath);
        if (isDevMode(context) || allLangResources == null) {
            allLangResources = new Element("loc");
            Path baseLoc = formatDir.resolve("loc");
            if (Files.exists(baseLoc)) {
                final Element finalAllLangResources = allLangResources;
                Files.walkFileTree(baseLoc, new SimpleFileVisitor<Path>() {
                    private void addTranslations(String locDirName, Element fileElements) {
                        if (locDirName != null && !locDirName.isEmpty()) {
                            Element resources = finalAllLangResources.getChild(locDirName);
                            if (resources == null) {
                                resources = new Element(locDirName);
                                finalAllLangResources.addContent(resources);
                            }
                            resources.addContent(fileElements);
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().toLowerCase().endsWith(".xml")) {
                            try {
                                final Element fileElements = Xml.loadFile(file);
                                final String fileName = getNameWithoutExtension(file.getFileName().toString());
                                fileElements.setName(fileName);
                                final String locDirName = getNameWithoutExtension(file.getParent().getFileName().toString());
                                addTranslations(locDirName, fileElements);
                            } catch (JDOMException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (file.getFileName().toString().toLowerCase().endsWith(".json")) {
                            try {
                                final String fileName = getNameWithoutExtension(file.getFileName().toString());
                                final String[] nameParts = fileName.split("-", 2);
                                IsoLanguagesMapper isoLanguagesMapper = context.getBean(IsoLanguagesMapper.class);
                                String lang = isoLanguagesMapper.iso639_1_to_iso639_2(nameParts[0].toLowerCase(), nameParts[0]);
                                final JSONObject json = new JSONObject(new String(Files.readAllBytes(file), Constants.CHARSET));
                                Element fileElements = new Element(nameParts[1]);
                                final Iterator keys = json.keys();
                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    fileElements.addContent(new Element(key).setText(json.getString(key)));
                                }
                                addTranslations(lang, fileElements);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return super.visitFile(file, attrs);
                    }
                });
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
        return context.getBean(SystemInfo.class).isDevMode();
    }

    public class FormatMetadata implements Callable<StoreInfoAndDataLoadResult> {
        private final Key key;
        private final NativeWebRequest request;
        private final ServiceContext serviceContext;

        public FormatMetadata(ServiceContext context, Key key, NativeWebRequest request) {
            this.key = key;
            this.request = request;
            this.serviceContext = context;
        }

        @Override
        public StoreInfoAndDataLoadResult call() throws Exception {
            serviceContext.setAsThreadLocal();

            Pair<FormatterImpl, FormatterParams> result =
                loadMetadataAndCreateFormatterAndParams(serviceContext, key, request);
            FormatterImpl formatter = result.one();
            FormatterParams fparams = result.two();
            final String formattedMetadata = formatter.format(fparams);
            byte[] bytes = formattedMetadata.getBytes(Constants.CHARSET);
            long changeDate = fparams.metadataInfo.getDataInfo().getChangeDate().toDate().getTime();
            final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
            final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(key.mdId);
            final Optional<OperationAllowed> one = serviceContext.getBean(OperationAllowedRepository.class).findOne(where(hasMdId).and(isPublished));
            final boolean isPublishedMd = one.isPresent();

            Key withheldKey = null;
            FormatMetadata loadWithheld = null;
            if (!key.hideWithheld && isPublishedMd) {
                withheldKey = new Key(key.mdId, key.lang, key.formatType, key.formatterId, true, key.width);
                loadWithheld = new FormatMetadata(serviceContext, withheldKey, request);
            }
            return new StoreInfoAndDataLoadResult(bytes, changeDate, isPublishedMd, withheldKey, loadWithheld);
        }
    }
}
