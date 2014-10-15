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

import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Show;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
public class Format extends AbstractFormatService {

    private Show showService;
    private WeakHashMap<String, List<SchemaLocalization>> labels = new WeakHashMap<String, List<SchemaLocalization>>();

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
        Element metadata = showService.exec(params, context);
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

    Element getResources(ServiceContext context, File formatDir, String lang) throws Exception {
        Element resources = new Element("loc");
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

    synchronized List<SchemaLocalization> getLabels(ServiceContext context, String lang) throws IOException, JDOMException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        List<SchemaLocalization> localization = labels.get(lang);
        if (localization == null) {
            SchemaManager schemamanager = gc.getBean(SchemaManager.class);
            Set<String> schemas = schemamanager.getSchemas();
            localization = Lists.newArrayList();
            for (String schema : schemas) {
                String schemaLocDir = schemamanager.getSchemaDir(schema) + File.separator + "loc" + File.separator + lang + File
                        .separator;
                localization.add(new SchemaLocalization(schema, schemaLocDir));
                labels.put(lang, localization);
            }
        }

        return localization;
    }

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);

        showService = new Show();
        showService.init(appPath, params);
    }
}
