/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.JSONLocCacheLoader;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.api.records.formatters.SchemaLocalizations.loadSchemaLocalizations;

/**
 * Strategy for formatting using an xslt based formatter.
 *
 * <p> Note: to include files from the formatter dir you can use <xsl:include
 * href="sharedFormatterDir/xslt/render-layout.xsl"/> and it will be replaced with the path to the
 * formatter dir using the URI resolver. </p>
 *
 * <p> Note: For a formatter to retrieve a request parameter an xsl:param should be defined with the
 * name of the URL parameter eg. <xsl:param name="view"/> </p>
 *
 * @author Jesse on 10/15/2014.
 * @author Francois on 06/01/2015: Add request parameters transfer to XSLT and metadata info.
 */
@Component
public class XsltFormatter implements FormatterImpl {

    private final Map<String, Element> translationElements =  new HashMap<>();

    private final ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    public XsltFormatter(ConfigurableApplicationContext configurableApplicationContext)  {
        this.configurableApplicationContext = configurableApplicationContext;
    }


    /**
     * @param schema            Use all to return all schemas translations
     * @param schemasToLoadList
     */
    public static List<Element> getSchemaLocalization(final String schema, List<String> schemasToLoadList, final String language) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        Collection<SchemaLocalization> localization =
            loadSchemaLocalizations(
                applicationContext,
                applicationContext.getBean(SchemaManager.class)).values();

        List<Element> elementList = new ArrayList<>(3);
        for (SchemaLocalization schemaLocalization : localization) {
            String currentSchema = schemaLocalization.schema.trim();
            if ("all".equalsIgnoreCase(schema) || schemasToLoadList.stream().anyMatch(currentSchema::equalsIgnoreCase)) {
                Element schemaEl = new Element(currentSchema);

                Element labels = schemaLocalization.getLabels(language);
                schemaEl.addContent((Element) labels.setName("labels").clone());
                Element strings = schemaLocalization.getStrings(language);
                schemaEl.addContent((Element) strings.setName("strings").clone());
                Element codelists = schemaLocalization.getCodelists(language);
                schemaEl.addContent((Element) codelists.setName("codelists").clone());

                elementList.add(schemaEl);
            }
        }
        return elementList;
    }

    public static List<Element> getSchemaLocalization(final String schema, final String language) throws Exception {
        List<String> schemaToLoad = new ArrayList<>();
        schemaToLoad.add(schema);
        return getSchemaLocalization(null, schemaToLoad, language);
    }

    public String format(FormatterParams fparams) throws Exception {

        String lang = fparams.config.getLang(fparams.context.getLanguage());

        Element root = new Element("root");

        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        root.addContent(new Element("lang").setText(fparams.context.getLanguage()));
        root.addContent(new Element("url").setText(fparams.url));

        if (!translationElements.containsKey(fparams.context.getLanguage())) {
            // Get translation keys and add them to the cache
            Element translations = new Element("translations");
            Map<String, String> translationMap = new JSONLocCacheLoader(configurableApplicationContext, fparams.context.getLanguage()).call();
            for (Map.Entry<String, String> entry : translationMap.entrySet()) {
                // Attempt to only use name that are valid element names.
                //   https://www.w3.org/TR/REC-xml/#NT-Name
                //   https://www.w3.org/TR/REC-xml/#NT-NameStartChar
                // Skip keys that are not alphanumeric only including "." - otherwise certain chars like ':?+...' can cause problem when creating element as they are invalid element names
                // i.e. some properties look like the following
                //      "cron-0 0 12 * * ?": "Fire at 12pm (noon) every day"
                //      "system/feedback"="Feedback"

                if (entry.getKey().matches("[a-zA-Z0-9\\.]+")) {
                    try {
                        translations.addContent(new Element(entry.getKey()).setText(entry.getValue()));
                    } catch (Exception e) {
                        // If errors are generated here then it may mean that the regular expression needs to be updated.
                        Log.error(Geonet.GEONETWORK, "Failed to add translation key for \"" + entry.getKey() + "\"=\"" + entry.getValue() + "\". " + e.getMessage());
                    }
                }
            }

            translationElements.put(fparams.context.getLanguage(), translations);
        }
        root.addContent((Element)translationElements.get(fparams.context.getLanguage()).clone());
        Element gui = new Element("gui");
        String baseUrl = settingManager.getBaseURL();
        String context = baseUrl.replace(settingManager.getServerURL(), "");
        gui.addContent(new Element("url").setText(
            context.substring(0, context.length() - 1)
        ));
        gui.addContent(new Element("nodeUrl").setText(settingManager.getNodeURL()));
        gui.addContent(new Element("nodeId").setText(settingManager.getNodeId()));
        gui.addContent(new Element("baseUrl").setText(baseUrl));
        gui.addContent(new Element("serverUrl").setText(settingManager.getServerURL()));
        gui.addContent(new Element("language").setText(fparams.context.getLanguage()));
        gui.addContent(new Element("reqService").setText("md.format.html"));
        Element env = new Element("systemConfig");
        env.addContent(settingManager.getAllAsXML(true));
        gui.addContent(env);
        root.addContent(gui);


        root.addContent(new Element("locUrl").setText(fparams.getLocUrl()));

        root.addContent(new Element("resourceUrl").setText(fparams.getResourceUrl()));
        // TODO: It could be easier to put the metadata
        // record in a metadata tag so it will be easier to pick
        // it up with xpath whatever the standard is.
        root.addContent(fparams.metadata);

        // Add metadata information (ie. harvested, categories, schema, dates, ...)
        Element info = fparams.metadataInfo.asXml();
        // metadataInfo contains the XML in data which is not needed
        info.removeChild("data");
        root.addContent(new Element("info")
            .addContent(info));

        root.addContent(fparams.format.getPluginLocResources(fparams.context, fparams.formatDir, lang));
        if (fparams.config.loadStrings()) {
            root.addContent(fparams.format.getStrings(fparams.context.getAppPath(), lang));
        }

        Element schemas = new Element("schemas");
        root.addContent(schemas);

        List<String> schemasToLoadList = fparams.config.listOfSchemasToLoad();

        String schemasToLoad = fparams.config.schemasToLoad();
        if (!"none".equalsIgnoreCase(schemasToLoad)) {
            List<Element> elementList = getSchemaLocalization(
                schemasToLoad, schemasToLoadList, fparams.context.getLanguage());
            for (Element e : elementList) {
                schemas.addContent(e);
            }
        }

        // Create a map of request parameters to be passed to the XSL transformation
        // For a formatter to retrieve a request parameter
        // an xsl:param should be defined
        // eg. <xsl:param name="view"/>
        Map<String, Object> requestParameters = new HashMap<String, Object>();

        if (fparams.webRequest != null) {
            Iterator<String> iterator = fparams.webRequest.getParameterMap().keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                requestParameters.put(key, fparams.webRequest.getParameterMap().get(key));
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Xml.transform(root, fparams.viewFile, requestParameters, baos);
        String transformed = baos.toString(StandardCharsets.UTF_8);
        return transformed.startsWith("<textResponse") ?
            Xml.loadString(transformed, false).getText() :
            transformed;
    }
}
