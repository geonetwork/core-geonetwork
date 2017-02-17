//=============================================================================
//===	Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

import org.fao.geonet.Util;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.utils.Log;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.inspireatom.harvester.InspireAtomHarvester;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeedEntry;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fao.geonet.exceptions.ResourceNotFoundEx;

/**
 * INSPIRE OpenSearchDescription atom service.
 *
 * @author Jose García
 */
public class AtomServiceDescription implements Service {
    /**
     * Service uuid param name
     **/
    private final static String SERVICE_IDENTIFIER_PARAM = "fileIdentifier";


    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    //--------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = context.getBean(DataManager.class);
        SettingManager sm = context.getBean(SettingManager.class);

        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }

        String fileIdentifier = Util.getParam(params, SERVICE_IDENTIFIER_PARAM, "");
        if (StringUtils.isEmpty(fileIdentifier)) {
            return new Element("response");
        }

        InspireAtomService service = context.getBean(InspireAtomService.class);

        String id = dm.getMetadataId(fileIdentifier);
        if (id == null) throw new MetadataNotFoundEx("Metadata not found.");

        Element md = dm.getMetadata(id);
        String schema = dm.getMetadataSchema(id);

        // Check if allowed to the metadata
        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        // Check if it is a service metadata
        if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + fileIdentifier);
        }

        // Get dataset identifiers referenced by service metadata.
        List<String> datasetIdentifiers = null;

        InspireAtomFeed inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));

        if (inspireAtomFeed == null) {
            throw new ResourceNotFoundEx("Atom feed for metadata " + id + " not found.");
        }

        // Check the metadata has an atom document (checks in the lucene index).
        String atomUrl = inspireAtomFeed.getAtomUrl();

        // If no atom document indexed, check if still metadata has feed url --> no processed by atom harvester yet
        if (StringUtils.isEmpty(atomUrl)) {
            String atomProtocol = sm.getValue(Settings.SYSTEM_INSPIRE_ATOM_PROTOCOL);
            atomUrl = InspireAtomUtil.extractAtomFeedUrl(schema, md, dm, atomProtocol);
            if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

            // Harvest this individual record
            datasetIdentifiers = InspireAtomUtil.extractRelatedDatasetsIdentifiers(schema, md, dm);

            InspireAtomHarvester inspireAtomHarvester = new InspireAtomHarvester(gc);
            inspireAtomHarvester.harvestServiceMetadata(context, id);

            // Read again the feed
            inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));
        } else {
            datasetIdentifiers = InspireAtomUtil.extractRelatedDatasetsIdentifiers(schema, md, dm);
        }

        // Get information from the the service atom feed.
        String feedAuthorName = inspireAtomFeed.getAuthorName();
        String feedTitle = inspireAtomFeed.getTitle();
        String feedSubtitle = inspireAtomFeed.getSubtitle();
        String feedLang = inspireAtomFeed.getLang();
        String feedUrl = inspireAtomFeed.getAtomUrl();

        String keywords = LuceneSearcher.getMetadataFromIndex(context.getLanguage(), fileIdentifier, "keyword");


        // Process datasets information
        Element datasetsEl = processDatasetsInfo(datasetIdentifiers, fileIdentifier, context);

        // Build response.
        return new Element("response")
            .addContent(new Element("fileId").setText(fileIdentifier))
            .addContent(new Element("title").setText(feedTitle))
            .addContent(new Element("subtitle").setText(feedSubtitle))
            .addContent(new Element("lang").setText(feedLang))
            .addContent(new Element("keywords").setText(keywords))
            .addContent(new Element("authorName").setText(feedAuthorName))
            .addContent(new Element("url").setText(feedUrl))
            .addContent(datasetsEl);
    }


    /**
     * Retrieves the information from datasets referenced in a service metadata.
     *
     * @param datasetIdentifiers List of dataset identifiers to process.
     * @param serviceIdentifier  Service identifier.
     * @param context            Service context.
     * @return JDOM Element with the datasets information.
     * @throws Exception Exception.
     */
    private Element processDatasetsInfo(final List<String> datasetIdentifiers, final String serviceIdentifier,
                                        final ServiceContext context)
        throws Exception {
        Element datasetsEl = new Element("datasets");

        final InspireAtomFeedRepository repository = context.getBean(InspireAtomFeedRepository.class);

        DataManager dm = context.getBean(DataManager.class);

        for (String datasetIdentifier : datasetIdentifiers) {
            // Get the metadata uuid for the dataset
            String datasetUuid = repository.retrieveDatasetUuidFromIdentifier(datasetIdentifier);

            // If dataset metadata not found, ignore
            if (StringUtils.isEmpty(datasetUuid)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): metadata for dataset identifier " + datasetIdentifier + " is not found, ignoring it.");
                continue;
            }

            String id = dm.getMetadataId(datasetUuid);
            InspireAtomFeed inspireAtomFeed = repository.findByMetadataId(Integer.parseInt(id));

            String idNs = inspireAtomFeed.getAtomDatasetid();
            String namespace = inspireAtomFeed.getAtomDatasetns();

            // If dataset metadata has no identifier information, ignore
            if (StringUtils.isEmpty(idNs)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): dataset with uuid " + datasetUuid + " has no dataset identifier/namespace, ignoring it.");
                continue;
            }

            String atomUrl = inspireAtomFeed.getAtomUrl();
            // If the dataset has no atom feed, ignore it
            if (StringUtils.isEmpty(atomUrl)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): dataset with uuid " + datasetUuid + " has no dataset feed, ignoring it.");
                continue;
            }

            Element datasetEl = buildDatasetInfo(datasetIdentifier, namespace);
            datasetEl.addContent(new Element("atom_url").setText(atomUrl));

            // Get dataset download info
            // From INSPIRE spec: if a CRS has multiple downloads should be returned a link to feed document with the CRS downloads.
            Map<String, Integer> downloadsCountByCrs = new HashMap<String, Integer>();
            for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
                Integer count = downloadsCountByCrs.get(entry.getCrs());
                if (count == null) count = Integer.valueOf(0);
                downloadsCountByCrs.put(entry.getCrs(), count + 1);
            }

            for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
                Integer count = downloadsCountByCrs.get(entry.getCrs());
                if (count != null) {
                    Element downloadEl = new Element("file");
                    downloadEl.addContent(new Element("title").setText(entry.getTitle()));
                    downloadEl.addContent(new Element("lang").setText(entry.getLang()));
                    downloadEl.addContent(new Element("url").setText(entry.getUrl()));
                    if (count > 1) {
                        downloadEl.addContent(new Element("type").setText("application/atom+xml"));
                    } else {
                        downloadEl.addContent(new Element("type").setText(entry.getType()));
                    }

                    downloadEl.addContent(new Element("crs").setText(entry.getCrs()));
                    datasetEl.addContent(downloadEl);

                    // Remove from map to not process further downloads with same CRS,
                    // only 1 entry with type= is added in result
                    downloadsCountByCrs.remove(entry.getCrs());
                }
            }

            datasetsEl.addContent(datasetEl);
        }

        return datasetsEl;
    }


    /**
     * Builds JDOM element for dataset information.
     *
     * @param identifier Dataset identifier.
     * @param namespace  Dataset namespace.
     */
    private Element buildDatasetInfo(final String identifier, final String namespace) {
        Element datasetEl = new Element("dataset");

        Element codeEl = new Element("code");
        codeEl.setText(identifier);

        Element namespaceEl = new Element("namespace");
        namespaceEl.setText(namespace);

        datasetEl.addContent(codeEl);
        datasetEl.addContent(namespaceEl);

        return datasetEl;
    }
}
