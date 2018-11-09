//=============================================================================
//===	Copyright (C) 2011 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Get suggestion for all metadata
 *
 * <ul> <li> 1) Discover registered processes:
 *
 * Use metadata.suggestion?id=2&action=list to retrieve the list of processes registered for the
 * metadata schema.
 *
 * <pre>
 * {@code
 *   <suggestions>
 *     <suggestion process="keywords-comma-exploder"/>
 *   </suggestions>
 * }
 * </pre>
 * A The process attribute contains the process identifier. </li> <li> 2) Check if processes have
 * suggestions for the metadata record Use metadata.suggestion?id=2&action=analyze to analyze for
 * all known processes or metadata.suggestion?id=2&action=analyze&process=keywords-comma-exploder to
 * analyze for only one process. </li> <li> 3) Apply the transformation using the @see {@link
 * org.fao.geonet.services.metadata.XslProcessing} service metadata.processing?id=41&process=keywords-comma-exploder
 * </li> </ul>
 */
@Deprecated
public class GetSuggestion implements Service {

    private static final String XSL_SUGGEST = "suggest.xsl";

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        Element response = new Element("suggestions");
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        SettingManager sm = gc.getBean(SettingManager.class);

        String action = Util.getParam(params, "action", "list");
        @SuppressWarnings("unchecked")
        List<Element> children = params.getChildren();
        Map<String, Object> xslParameter = new HashMap<String, Object>();
        xslParameter.put("guiLang", context.getLanguage());
        xslParameter.put("siteUrl", sm.getSiteURL(context));
        xslParameter.put("baseUrl", context.getBaseUrl());
        for (Element param : children) {
            xslParameter.put(param.getName(), param.getTextTrim());
        }

        // Retrieve metadata record
        String id = Utils.getIdentifierFromParameters(params, context);
        AbstractMetadata mdInfo = gc.getBean(IMetadataUtils.class).findOne(id);
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element md = gc.getBean(DataManager.class).getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

        // List or analyze all suggestions process registered for this schema
        if ("list".equals(action) || "analyze".equals(action)) {
            MetadataSchema metadataSchema = dm.getSchema(mdInfo.getDataInfo().getSchemaId());
            Path xslProcessing = metadataSchema.getSchemaDir().resolve(XSL_SUGGEST);
            if (Files.exists(xslProcessing)) {
                // -- here we send parameters set by user from
                // URL if needed.
                return Xml.transform(md, xslProcessing, xslParameter);
            } else {
                return response;
            }
        }
        return response;
    }
}
