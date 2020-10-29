//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
package org.fao.geonet.services.inspireatom;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.NotImplementedException;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * INSPIRE atom search service.
 *
 * @author Jose García
 */
public class AtomSearch implements Service {
//    private Search search = new Search();
//    private Result result = new Result();

    public void init(Path appPath, ServiceConfig params) throws Exception {
//        search.init(appPath, params);
//        result.init(appPath, params);
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        throw new NotImplementedException("Not implemented in ES");

//
//        SettingManager sm = context.getBean(SettingManager.class);
//        DataManager dm = context.getBean(DataManager.class);
//        InspireAtomService service = context.getBean(InspireAtomService.class);
//
//        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
//
//        if (!inspireEnable) {
//            Log.info(Geonet.ATOM, "Inspire is disabled");
//            throw new Exception("Inspire is disabled");
//        }
//
//        String fileIdentifier = params.getChildText("fileIdentifier");
//
//        // If fileIdentifier is provided search only in the related datasets
//        if (StringUtils.isNotEmpty(fileIdentifier)) {
//            String id = dm.getMetadataId(fileIdentifier);
//            if (id == null) throw new MetadataNotFoundEx("Metadata not found.");
//
//            Element md = dm.getMetadata(id);
//            String schema = dm.getMetadataSchema(id);
//
//            // Check if allowed to the metadata
//            Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
//
//            // Retrieve the datasets related to the service metadata
//            List<String> datasetIdentifiers = InspireAtomUtil.extractRelatedDatasetsIdentifiers(schema, md, dm);
//
//            // Remove fileIdentifier from params
//            params.removeChild("fileIdentifier");
//
//            // Add query filter
//            String values = Joiner.on(" or ").join(datasetIdentifiers);
//            params.addContent(new Element("identifier").setText(values));
//        }
//
//        // Add query filter
//        params.addContent(new Element("has_atom").setText("y"));
//        params.addContent(new Element("fast").setText("true"));
//
//
//        // Depending on INSPIRE atom format decide which service use.
//        String atomFormat = sm.getValue(Settings.SYSTEM_INSPIRE_ATOM);
//
//        search.exec(params, context);
//
//        // Create atom feed from search results.
//        if (atomFormat.equalsIgnoreCase(InspireAtomType.ATOM_LOCAL)) {
//            return result.exec(params, context);
//
//            // Create atom feed from feeds referenced in metadata.
//        } else {
//            Element results = result.exec(params, context);
//
//            Element feeds = new Element("feeds");
//
//            // Loop over the results and retrieve feeds to add in results
//            // First element in results (pos=0) is the summary, ignore it
//            for (int i = 1; i < results.getChildren().size(); i++) {
//                String id = ((Element) results.getChildren().get(i)).getChild("info", Edit.NAMESPACE).getChildText("id");
//
//                InspireAtomFeed feed = service.findByMetadataId(Integer.parseInt(id));
//                Element feedEl = Xml.loadString(feed.getAtom(), false);
//                feeds.addContent((Content) feedEl.clone());
//            }
//
//            return feeds;
//        }
    }
}
