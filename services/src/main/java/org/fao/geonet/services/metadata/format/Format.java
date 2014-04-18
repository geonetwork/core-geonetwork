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

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.Show;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        String uuid = Util.getParam(params, Params.UUID, null);
        String id = Util.getParam(params, Params.ID, null);

        if (uuid == null && id == null) {
            throw new IllegalArgumentException("Either '" + Params.UUID + "' or '" + Params.ID + "'is a required parameter");
        }

        File formatDir = getAndVerifyFormatDir("xsl", xslid);

        File viewXslFile = new File(formatDir, VIEW_XSL_FILENAME);

        if (!viewXslFile.exists())
            throw new IllegalArgumentException("The 'xsl' parameter must be a valid URL");

        Element metadata = showService.exec(params, context);

        ConfigFile config = new ConfigFile(formatDir);
        String lang = config.getLang(context.getLanguage());

        if(!isCompatibleMetadata(params, config, context)) {
        	throw new IllegalArgumentException("The bundle cannot format metadata with the "+getMetadataSchema(params, context)+" schema");
        }
        
        List<SchemaLocalization> localization = getLabels(context, lang);
        
        Element root = new Element("root");
        
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        String url = gc.getBean(SettingManager.class).getSiteURL(context);

        root.addContent (new Element("url").setText(url));
        String locUrl = url+"/" + context.getNodeId() + "/"+context.getLanguage()+"/";
        root.addContent (new Element("locUrl").setText(locUrl));
        String resourceUrl = locUrl+"/metadata.formatter.resource?"+Params.ID+"="+xslid+"&"+Params.FNAME+"=";
        root.addContent(new Element("resourceUrl").setText(resourceUrl));
        root.addContent(metadata);
        root.addContent(getResources(context, formatDir, lang));
        if(config.loadStrings()) {
        	root.addContent(getStrings(context.getAppPath(), lang));
        }
        
        Element schemas = new Element("schemas");
        root.addContent(schemas);
        
        List<String> schemasToLoadList = config.listOfSchemasToLoad();
        
        String schemasToLoad = config.schemasToLoad();
		if(!"none".equalsIgnoreCase(schemasToLoad )) {
	        for (SchemaLocalization schemaLocalization : localization) {
	            String schema = schemaLocalization.schema.trim();
	            if("all".equalsIgnoreCase(schemasToLoad) || schemasToLoadList.contains(schema.toLowerCase())) {
		            Element schemaEl = new Element(schema);
		            schemas.addContent(schemaEl);
		            
		            schemaEl.addContent((Element)schemaLocalization.labels.clone());
		            schemaEl.addContent((Element)schemaLocalization.codelists.clone());
		            schemaEl.addContent((Element)schemaLocalization.strings.clone());
	            }
	        }
        }
        if(Util.getParam(params, "debug", false)) {
            return root;
        }
        
        // verify xsl is a valid file before loading metadata and increasing
        // popularity
        Xml.loadFile(viewXslFile);
        Element transformed = Xml.transform(root, viewXslFile.getAbsolutePath());
        Element response = new Element("metadata");
        response.addContent(transformed);
        return response;
    }

    private boolean isCompatibleMetadata(Element params, ConfigFile config,
			ServiceContext context) throws Exception {
    	String schema = getMetadataSchema(params, context);
    	List<String> applicable = config.listOfApplicableSchemas();
		return applicable.contains(schema) || applicable.contains("all");
		
	}

	private Element getStrings(String appPath, String lang) throws IOException, JDOMException {
        File baseLoc = new File(appPath, "loc");
        File locDir = findLocDir(lang, baseLoc);
        if(locDir.exists()) {
            return Xml.loadFile(new File(locDir, "xml"+File.separator+"strings.xml"));
        }
        return new Element("strings");
    }

    private Element getResources(ServiceContext context, File formatDir, String lang) throws Exception {
        Element resources = new Element("loc");
        File baseLoc = new File(formatDir, "loc");
        File locDir = findLocDir(lang, baseLoc);

        resources.addContent(new Element("iso639_2").setAttribute("codeLength","3").setText(locDir.getName()));
        String iso639_1 = context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(locDir.getName());

        resources.addContent(new Element("iso639_1").setAttribute("codeLength","2").setText(iso639_1 ));

        if(locDir.exists()) {
            Collection<File> files = FileUtils.listFiles(locDir, new String[]{"xml"}, false);
            for (File file : files) {
                resources.addContent(Xml.loadFile(file));
            }
        }
        return resources;
    }

    private File findLocDir(String lang, File baseLoc) {
        File locDir = new File(baseLoc, lang);
        if(!locDir.exists()) {
            locDir = new File(baseLoc, Geonet.DEFAULT_LANGUAGE);
        }
        if(!locDir.exists()) {
            File[] files = baseLoc.listFiles();
            if(files != null && files.length > 0 && files[0].isDirectory()) {
                locDir = files[0];
            }
        }
        return locDir;
    }

    private synchronized List<SchemaLocalization> getLabels(ServiceContext context, String lang) throws IOException, JDOMException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        List<SchemaLocalization> localization = labels.get(lang);
        if(localization == null) {
            SchemaManager schemamanager = gc.getBean(SchemaManager.class);
            Set<String> schemas = schemamanager.getSchemas();
            localization = new ArrayList<SchemaLocalization>(schemas.size());
            for (String schema : schemas) {
                String schemaLocDir = schemamanager.getSchemaDir(schema)+File.separator+"loc"+File.separator+lang+File.separator;
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
    
    private static class SchemaLocalization {
        private String schema;
        private final Element strings; 
        private final Element codelists;
        private final Element labels;
        
        private SchemaLocalization(String schema, String schemaLocDir) throws IOException, JDOMException {
            this.schema = schema;
            this.strings = Xml.loadFile(schemaLocDir+"strings.xml").setName("strings");
            this.codelists = Xml.loadFile(schemaLocDir+"codelists.xml").setName("codelists");
            this.labels = Xml.loadFile(schemaLocDir+"labels.xml").setName("labels");
        }
        
    }
}
