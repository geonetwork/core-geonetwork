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
import com.lowagie.text.DocumentException;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.format.groovy.ParamValue;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
@Controller("md.formatter.type")
public class Format extends AbstractFormatService {

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

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<String, Element>();
    private Map<Path, Boolean> isFormatterInSchemaPluginMap = Maps.newHashMap();


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
        Pair<FormatterImpl, FormatterParams> result = createFormatterAndParams(lang, formatType, id, uuid, xslid, skipPopularity,
                hide_withheld, request);
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
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }
    }

    private void writerAsPDF(HttpServletResponse response, String htmlContent, String lang) throws IOException, DocumentException {
        XslUtil.setNoScript();
        ITextRenderer renderer = new ITextRenderer();
        String siteUrl = this.settingManager.getSiteURL(lang);
        renderer.getSharedContext().setReplacedElementFactory(new ImageReplacedElementFactory(siteUrl, renderer.getSharedContext()
                .getReplacedElementFactory()));
        renderer.getSharedContext().setDotsPerPixel(13);
        renderer.setDocumentFromString(htmlContent, siteUrl);
        renderer.layout();
        renderer.createPDF(response.getOutputStream());
    }

    @VisibleForTesting
    Pair<FormatterImpl, FormatterParams> createFormatterAndParams(
            final String lang, final FormatType type, final String id, final String uuid, final String xslid,
            final String skipPopularity, final Boolean hide_withheld, final HttpServletRequest request) throws Exception {

        ServiceContext context = this.serviceManager.createServiceContext("metadata.formatter" + type, lang, request);
        final Pair<Element, Metadata> elementMetadataPair = getMetadata(context, id, uuid, new ParamValue(skipPopularity), hide_withheld);
        Element metadata = elementMetadataPair.one();
        Metadata metadataInfo = elementMetadataPair.two();

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
                                               Boolean hide_withheld)
            throws Exception {

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
    protected synchronized Element getPluginLocResources(ServiceContext context, Path formatDir, String lang) throws Exception {
        final String formatDirPath = formatDir.toString();
        Element resources = this.pluginLocs.get(formatDirPath);
        if (isDevMode(context) || resources == null) {
            resources = new Element("loc");
            Path baseLoc = formatDir.resolve("loc");
            Path locDir = findLocDir(lang, baseLoc);

            final String locDirName = locDir.getFileName().toString();
            resources.addContent(new Element("iso639_2").setAttribute("codeLength", "3").setText(locDirName));
            String iso639_1 = context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(locDirName);

            resources.addContent(new Element("iso639_1").setAttribute("codeLength", "2").setText(iso639_1));

            if (Files.exists(locDir)) {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(locDir, "*.xml")) {
                    for (Path file : paths) {
                        resources.addContent(Xml.loadFile(file));
                    }
                }
            }
            this.pluginLocs.put(formatDirPath, resources);
        }
        return resources;
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

    /**
     * Get the strings.xml, codelists.xml and labels.xml for the correct language from the schema plugin
     *
     * @return Map(SchemaName, SchemaLocalizations)
     */
    protected Map<String, SchemaLocalization> getSchemaLocalizations(ServiceContext context)
            throws IOException, JDOMException {

        Map<String, SchemaLocalization> localization = Maps.newHashMap();
        final SchemaManager schemaManager = context.getBean(SchemaManager.class);
        final Set<String> allSchemas = schemaManager.getSchemas();
        for (String schema : allSchemas) {
            Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);
            localization.put(schema, new SchemaLocalization(context, schema, schemaInfo));
        }

        return localization;
    }

    protected boolean isDevMode(ServiceContext context) {
        return context.getApplicationContext().getBean(SystemInfo.class).isDevMode();
    }

}
