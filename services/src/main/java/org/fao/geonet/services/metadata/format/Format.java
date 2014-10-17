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

import com.google.common.collect.Maps;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
public class Format extends AbstractFormatService {

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<String, Element>();

    public Element exec(Element params, ServiceContext context) throws Exception {
        ensureInitializedDir(context);

        String xslid = Util.getParam(params, "xsl", null);
        String id;
        try {
            id = Utils.getIdentifierFromParameters(params, context);
            Integer.parseInt(id); // check that it is an id and not a uuid
        } catch (Exception e) {
            id = Utils.getIdentifierFromParameters(params, context, Params.ID, Params.ID);
        }

        if (id == null) {
            throw new IllegalArgumentException("Either '" + Params.UUID + "' or '" + Params.ID + "'is a required parameter");
        }
        params.removeChild(Params.ID);
        params.removeChild(Params.UUID);
        params.addContent(new Element(Params.ID).setText(id));
        Element metadata = getMetadata(params, context);
        final SchemaManager bean = context.getBean(SchemaManager.class);
        final String schema = bean.autodetectSchema(metadata, null);
        File schemaDir = null;
        if (schema != null) {
            schemaDir = new File(bean.getSchemaDir(schema));
        }
        File formatDir = getAndVerifyFormatDir("xsl", xslid, schemaDir);

        ConfigFile config = new ConfigFile(formatDir);

        if (!isCompatibleMetadata(params, context, config)) {
            final String metadataSchema = getMetadataSchema(params, context);
            throw new IllegalArgumentException("The bundle cannot format metadata with the " + metadataSchema + " schema");
        }

        File viewXslFile = new File(formatDir, FormatterConstants.VIEW_XSL_FILENAME);
        File viewGroovyFile = new File(formatDir, FormatterConstants.VIEW_GROOVY_FILENAME);

        FormatterParams fparams = new FormatterParams();
        fparams.config = config;
        fparams.format = this;
        fparams.params = params;
        fparams.context = context;
        fparams.formatDir = formatDir;
        fparams.metadata = metadata;
        fparams.schema = schema;
        fparams.url = context.getBean(SettingManager.class).getSiteURL(context);

        if (viewXslFile.exists()) {
            fparams.viewFile = viewXslFile;
            return context.getBean(XsltFormatter.class).format(fparams);
        } else if (viewGroovyFile.exists()){
            fparams.viewFile = viewGroovyFile;
            return context.getBean(GroovyFormatter.class).format(fparams);
        } else {
            throw new IllegalArgumentException("The 'xsl' parameter must be a valid id of a formatter");
        }

    }

    public Element getMetadata(Element params, ServiceContext context) throws Exception {
        DataManager dm = context.getBean(DataManager.class);
        SchemaManager sm = context.getBean(SchemaManager.class);

        String id = Utils.getIdentifierFromParameters(params, context);
        boolean skipPopularity = false;
        if (!skipPopularity) { // skipPopularity could be a URL param as well
            String skip = Util.getParam(params, "skipPopularity", "n");
            skipPopularity = skip.equals("y");
        }

        boolean withholdWithheldElements = Util.getParam(params, "hide_withheld", false);
        if (XmlSerializer.getThreadLocal(false) != null || withholdWithheldElements) {
            XmlSerializer.getThreadLocal(true).setForceFilterEditOperation(withholdWithheldElements);
        }
        if (id == null) {
            throw new MetadataNotFoundEx("Metadata not found.");
        }

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
        final Element md = dm.getMetadataNoInfo(context, id);


        if (md == null) {
            throw new MetadataNotFoundEx(id);
        }

        if (!skipPopularity) {
            dm.increasePopularity(context, id);
        }

        return md;

    }
    private boolean isCompatibleMetadata(Element params, ServiceContext context, ConfigFile config) throws Exception {
        String schema = getMetadataSchema(params, context);
        List<String> applicable = config.listOfApplicableSchemas();
        return applicable.contains(schema) || applicable.contains("all");

    }

    Element getStrings(String appPath, String lang) throws IOException, JDOMException {
        File baseLoc = new File(appPath, "loc");
        File locDir = findLocDir(lang, baseLoc);
        if (locDir.exists()) {
            return Xml.loadFile(new File(locDir, "xml" + File.separator + "strings.xml"));
        }
        return new Element("strings");
    }

    /**
     * Get the localization files from current format plugin.  It will load all xml file in the loc/lang/ directory as children
     * of the returned element.
     */
    synchronized Element getPluginLocResources(ServiceContext context, File formatDir, String lang) throws Exception {
        final String canonicalPath = formatDir.getCanonicalPath();
        Element resources = this.pluginLocs.get(canonicalPath);
        if (isDevMode(context) || resources == null) {
            resources = new Element("loc");
            File baseLoc = new File(formatDir, "loc");
            File locDir = findLocDir(lang, baseLoc);

            resources.addContent(new Element("iso639_2").setAttribute("codeLength", "3").setText(locDir.getName()));
            String iso639_1 = context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(locDir.getName());

            resources.addContent(new Element("iso639_1").setAttribute("codeLength", "2").setText(iso639_1));

            if (locDir.exists()) {
                Collection<File> files = FileUtils.listFiles(locDir, new String[]{"xml"}, false);
                for (File file : files) {
                    resources.addContent(Xml.loadFile(file));
                }
            }
            this.pluginLocs.put(canonicalPath, resources);
        }
        return resources;
    }

    private File findLocDir(String lang, File baseLoc) {
        File locDir = new File(baseLoc, lang);
        if (!locDir.exists()) {
            locDir = new File(baseLoc, Geonet.DEFAULT_LANGUAGE);
        }
        if (!locDir.exists()) {
            File[] files = baseLoc.listFiles();
            if (files != null && files.length > 0 && files[0].isDirectory()) {
                locDir = files[0];
            }
        }
        return locDir;
    }

    /**
     * Get the strings.xml, codelists.xml and labels.xml for the correct language from the schema plugin
     *
     * @return Map(SchemaName, SchemaLocalizations)
     */
    Map<String, SchemaLocalization> getSchemaLocalizations(ServiceContext context)
            throws IOException, JDOMException {

        Map<String, SchemaLocalization> localization =  Maps.newHashMap();
        final SchemaManager schemaManager = context.getBean(SchemaManager.class);
        final Set<String> allSchemas = schemaManager.getSchemas();
        for (String schema : allSchemas) {
            Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);
            localization.put(schema, new SchemaLocalization(context, schema, schemaInfo));
        }

        return localization;
    }

    boolean isDevMode(ServiceContext context) {
        return Geonet.StagingProfile.DEVELOPMENT.equals(context.getApplicationContext().getBean(Geonet.StagingProfile.BEAN_NAME));
    }

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }
}
