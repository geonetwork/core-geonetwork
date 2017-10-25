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

import jeeves.xlink.Processor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.formatters.cache.CacheConfig;
import org.fao.geonet.api.records.formatters.cache.ChangeDateValidator;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.fao.geonet.api.records.formatters.cache.Key;
import org.fao.geonet.api.records.formatters.cache.NoCacheValidator;
import org.fao.geonet.api.records.formatters.cache.StoreInfoAndDataLoadResult;
import org.fao.geonet.api.records.formatters.cache.Validator;
import org.fao.geonet.api.records.formatters.groovy.ParamValue;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import springfox.documentation.annotations.ApiIgnore;

import static com.google.common.io.Files.getNameWithoutExtension;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.api.records.formatters.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
@Api(value = "records",
    tags = "records",
    description = "Metadata record operations")
@Controller("recordFormatter")
@Lazy
public class FormatterApi extends AbstractFormatService implements ApplicationListener {
    private static final Set<String> ALLOWED_PARAMETERS = Sets.newHashSet("id", "uuid", "xsl", "skippopularity", "hide_withheld");

    @Autowired
    LanguageUtils languageUtils;

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter
     * bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<>();
    private Map<Path, Boolean> isFormatterInSchemaPluginMap = Maps.newHashMap();

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
        "/api/records/{metadataUuid}/formatters/{formatterId}",
        "/api/" + API.VERSION_0_1 +
            "/records/{metadataUuid}/formatters/{formatterId}"
    },
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            "application/pdf"
            // TODO: PDF
        })
    @ApiOperation(
        value = "Get a formatted metadata record",
        nickname = "getRecordFormattedBy"
    )
    @ResponseBody
    public void getRecordFormattedBy(
        @ApiParam(
            value = "Formatter type to use."
        )
        @RequestHeader(
            value = HttpHeaders.ACCEPT,
            defaultValue = MediaType.TEXT_HTML_VALUE
        )
            String acceptHeader,
        @PathVariable(
            value = "formatterId"
        )
        final String formatterId,
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @RequestParam(
            value = "width",
            defaultValue = "_100")
        final FormatterWidth width,
        @RequestParam(
            value = "mdpath",
            required = false)
        final String mdPath,
        @RequestParam(
            value = "output",
            required = false)
        FormatType formatType,
        @ApiIgnore
        final NativeWebRequest request,
        final HttpServletRequest servletRequest) throws Exception {

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        Locale locale = languageUtils.parseAcceptLanguage(servletRequest.getLocales());

        // TODO :
        // if text/html > xsl_view
        // if application/pdf > xsl_view and PDF output
        // if application/x-gn-<formatterId>+(xml|html|pdf|text)
        // Force PDF ouutput when URL parameter is set.
        // This is useful when making GET link to PDF which
        // can not use headers.
        if(formatType == null) {
            formatType = FormatType.find(acceptHeader);
        }
        if (formatType == null) {
            formatType = FormatType.xml;
        }
        final String language = LanguageUtils.locale2gnCode(locale.getISO3Language());
        final ServiceContext context = createServiceContext(
            language,
            formatType,
            request.getNativeRequest(HttpServletRequest.class));
        Metadata metadata = ApiUtils.canViewRecord(metadataUuid, servletRequest);

        Boolean hideWithheld = true;
//        final boolean hideWithheld = Boolean.TRUE.equals(hide_withheld) ||
//            !context.getBean(AccessManager.class).canEdit(context, resolvedId);
        Key key = new Key(metadata.getId(), language, formatType, formatterId, hideWithheld, width);
        final boolean skipPopularityBool = false;

        ISODate changeDate = metadata.getDataInfo().getChangeDate();

        Validator validator;
        if (changeDate != null) {
            final long changeDateAsTime = changeDate.toDate().getTime();
            long roundedChangeDate = changeDateAsTime / 1000 * 1000;
            if (request.checkNotModified(language, roundedChangeDate) &&
                context.getBean(CacheConfig.class).allowCaching(key)) {
                if (!skipPopularityBool) {
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
            if (!skipPopularityBool) {
                context.getBean(DataManager.class).increasePopularity(context, String.valueOf(metadata.getId()));
            }
            writeOutResponse(context, metadataUuid, locale.getISO3Language(), request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
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
    @RequestMapping(value = "/{lang}/xml.format.{type}")
    @ResponseBody
    @Deprecated
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
        Metadata metadataInfo = new Metadata().setData(metadata).setId(1).setUuid("uuid");
        metadataInfo.getDataInfo().setType(MetadataType.METADATA).setRoot(metadataEl.getQualifiedName()).setSchemaId(schema);

        Pair<FormatterImpl, FormatterParams> result = createFormatterAndParams(lang, formatType, xslid, width,
            request, context, metadataEl, metadataInfo);
        final String formattedMetadata = result.one().format(result.two());
        byte[] bytes = formattedMetadata.getBytes(Constants.CHARSET);

        writeOutResponse(context, "", lang, request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
    }

    /**
     * This service will read directly from the cache and return the value.  If it is not in the
     * cache then a 404 will be returned.
     *
     * This is a service to use if there is process to keep the cache at least periodically
     * up-to-date and if maximum performance is required.
     */
    @RequestMapping(value = "/{lang}/md.format.public.{type}")
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
     * @param hide_withheld  if true hideWithheld (private) elements even if the current user would
     *                       normally have access to them.
     * @param width          the approximate size of the element that the formatter output will be
     *                       embedded in compared to the full device width.  Allowed options are the
     *                       enum values: {@link org.fao.geonet.api.records.formatters.FormatterWidth}
     *                       The default is _100 (100% of the screen)
     */
    @RequestMapping(value = "/{lang}/md.format.{type}")
    @ResponseBody
    public void exec(
        @PathVariable final String lang,
        @PathVariable final String type,
        @RequestParam(value = "id", required = false) final String id,
        @RequestParam(value = "uuid", required = false) final String uuid,
        @RequestParam(value = "xsl", required = false) final String xslid,
        @RequestParam(defaultValue = "n") final String skipPopularity,
        @RequestParam(value = "hide_withheld", required = false) final Boolean hide_withheld,
        @RequestParam(value = "width", defaultValue = "_100") final FormatterWidth width,
        final NativeWebRequest request) throws Exception {
        final FormatType formatType = FormatType.valueOf(type.toLowerCase());

        String resolvedId = resolveId(id, uuid);
        ServiceContext context = createServiceContext(lang, formatType, request.getNativeRequest(HttpServletRequest.class));
        Lib.resource.checkPrivilege(context, resolvedId, ReservedOperation.view);

        final boolean hideWithheld = Boolean.TRUE.equals(hide_withheld) ||
            !context.getBean(AccessManager.class).canEdit(context, resolvedId);
        Key key = new Key(Integer.parseInt(resolvedId), lang, formatType, xslid, hideWithheld, width);
        final boolean skipPopularityBool = new ParamValue(skipPopularity).toBool();

        ISODate changeDate = context.getBean(SearchManager.class).getDocChangeDate(resolvedId);

        Validator validator;
        if (changeDate != null) {
            final long changeDateAsTime = changeDate.toDate().getTime();
            long roundedChangeDate = changeDateAsTime / 1000 * 1000;
            if (request.checkNotModified(roundedChangeDate) && context.getBean(CacheConfig.class).allowCaching(key)) {
                if (!skipPopularityBool) {
                    context.getBean(DataManager.class).increasePopularity(context, resolvedId);
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
            if (!skipPopularityBool) {
                context.getBean(DataManager.class).increasePopularity(context, resolvedId);
            }

            writeOutResponse(context, resolvedId, lang, request.getNativeResponse(HttpServletResponse.class), formatType, bytes);
        }
    }

    private void writeOutResponse(ServiceContext context, String metadataUuid, String lang, HttpServletResponse response, FormatType formatType, byte[] formattedMetadata) throws Exception {
        response.setContentType(formatType.contentType);
        String filename = "metadata." + metadataUuid + formatType;
        response.addHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        response.setStatus(HttpServletResponse.SC_OK);
        if (formatType == FormatType.pdf) {
            writerAsPDF(context, response, formattedMetadata, lang);
        } else {
            response.setCharacterEncoding(Constants.ENCODING);
            response.setContentType("text/html");
            response.setContentLength(formattedMetadata.length);
            response.setHeader("Cache-Control", "no-cache");
            response.getOutputStream().write(formattedMetadata);
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
            Set allowedRemoteHosts = context.getApplicationContext().getBean("formatterRemoteFormatAllowedHosts", Set.class);
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

    private void writerAsPDF(ServiceContext context, HttpServletResponse response, byte[] bytes, String lang) throws IOException, com.itextpdf.text.DocumentException {
        final String htmlContent = new String(bytes, Constants.CHARSET);
        try {
            XslUtil.setNoScript();
            ITextRenderer renderer = new ITextRenderer();
            String siteUrl = context.getBean(SettingManager.class).getSiteURL(lang);
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
    Pair<FormatterImpl, FormatterParams> loadMetadataAndCreateFormatterAndParams(ServiceContext context, Key key, final NativeWebRequest request) throws Exception {
        final Pair<Element, Metadata> elementMetadataPair = getMetadata(context, key.mdId, key.hideWithheld);
        Element metadata = elementMetadataPair.one();
        Metadata metadataInfo = elementMetadataPair.two();

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
                                                                          Metadata metadataInfo) throws Exception {
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
        Path viewGroovyFile = formatDir.resolve(FormatterConstants.VIEW_GROOVY_FILENAME);
        FormatterImpl formatter;
        if (Files.exists(viewXslFile)) {
            fparams.viewFile = viewXslFile.toRealPath();
            formatter = context.getBean(XsltFormatter.class);
        } else if (Files.exists(viewGroovyFile)) {
            fparams.viewFile = viewGroovyFile.toRealPath();
            formatter = context.getBean(GroovyFormatter.class);
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

    public Pair<Element, Metadata> getMetadata(ServiceContext context, int id,
                                               Boolean hide_withheld) throws Exception {

        XmlSerializer serializer = context.getBean(XmlSerializer.class);
        boolean doXLinks = serializer.resolveXLinks();

        Metadata md = loadMetadata(context.getBean(MetadataRepository.class), id);

        Element metadata = serializer.removeHiddenElements(false, md, false);
        if (doXLinks) Processor.processXLink(metadata, context);

        boolean withholdWithheldElements = hide_withheld != null && hide_withheld;
        if (XmlSerializer.getThreadLocal(false) != null || withholdWithheldElements) {
            XmlSerializer.getThreadLocal(true).setForceFilterEditOperation(withholdWithheldElements);
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
        return context.getApplicationContext().getBean(SystemInfo.class).isDevMode();
    }

    private class FormatMetadata implements Callable<StoreInfoAndDataLoadResult> {
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
            final OperationAllowed one = serviceContext.getBean(OperationAllowedRepository.class).findOne(where(hasMdId).and(isPublished));
            final boolean isPublishedMd = one != null;

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
