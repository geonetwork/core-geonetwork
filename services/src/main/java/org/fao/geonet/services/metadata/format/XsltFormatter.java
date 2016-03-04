/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata.format;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.services.metadata.format.SchemaLocalizations.loadSchemaLocalizations;

/**
 * Strategy for formatting using an xslt based formatter.
 *
 * <p>
 *     Note: to include files from the formatter dir you can use
 *     <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
 *     and it will be replaced with the path to the formatter dir
 *     using the URI resolver.
 * </p>
 *
 * <p>
 *     Note: For a formatter to retrieve a request parameter
 *     an xsl:param should be defined with the name of the URL parameter
 *     eg. <xsl:param name="view"/>
 * </p>
 *
 * @author Jesse on 10/15/2014.
 * @author Francois on 06/01/2015: Add request parameters transfert to XSLT
 *  and metadata info.
 */
@Component
public class XsltFormatter implements FormatterImpl {

    public String format(FormatterParams fparams) throws Exception {

        String lang = fparams.config.getLang(fparams.context.getLanguage());

        Element root = new Element("root");

        root.addContent(new Element("lang").setText(fparams.context.getLanguage()));
        root.addContent(new Element("url").setText(fparams.url));
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
            SchemaManager schemaManager = fparams.context.getBean(SchemaManager.class);
            Collection<SchemaLocalization> localization = loadSchemaLocalizations(fparams.context.getApplicationContext(),
                    schemaManager).values();

            for (SchemaLocalization schemaLocalization : localization) {
                String currentSchema = schemaLocalization.schema.trim();
                if ("all".equalsIgnoreCase(schemasToLoad) || schemasToLoadList.contains(currentSchema.toLowerCase())) {
                    Element schemaEl = new Element(currentSchema);
                    schemas.addContent(schemaEl);

                    Element labels = schemaLocalization.getLabels(fparams.context.getLanguage());
                    schemaEl.addContent((Element) labels.setName("labels").clone());
                    Element strings = schemaLocalization.getStrings(fparams.context.getLanguage());
                    schemaEl.addContent((Element) strings.setName("strings").clone());
                    Element codelists = schemaLocalization.getCodelists(fparams.context.getLanguage());
                    schemaEl.addContent((Element) codelists.setName("codelists").clone());
                }
            }
        }
        if (!"false".equalsIgnoreCase(fparams.param("debug", "false"))) {
            return Xml.getString(root);
        }

        // Create a map of request parameters to be passed to the XSL transformation
        // For a formatter to retrieve a request parameter
        // an xsl:param should be defined
        // eg. <xsl:param name="view"/>
        Map<String, Object> requestParameters = new HashMap<String, Object>();
        Iterator<String> iterator = fparams.webRequest.getParameterMap().keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            requestParameters.put(key, fparams.webRequest.getParameterMap().get(key));
        }
        Element transformed = Xml.transform(root, fparams.viewFile, requestParameters);

        Element response = new Element("metadata");
        response.addContent(transformed);
        return Xml.getString(response);
    }

}