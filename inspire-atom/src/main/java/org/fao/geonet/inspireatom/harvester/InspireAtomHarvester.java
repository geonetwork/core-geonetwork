//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.inspireatom.harvester;


import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.inspireatom.model.DatasetFeedInfo;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.repository.specification.InspireAtomFeedSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Class to harvest the Atom documents referenced in the iso19139 in the catalog.
 *
 * @author Jose García
 */
public class InspireAtomHarvester {
    private final static String EXTRACT_DATASETS_FROM_SERVICE_XSLT = "extract-datasetinfo-from-service-feed.xsl";
    private final static String EXTRACT_DATASET_ID_XSLT = "extract-datasetid.xsl";
    private Logger logger = Log.createLogger(Geonet.ATOM);
    /**
     * GeoNetwork context.
     **/
    private GeonetContext gc;


    /**
     * Constructor.
     *
     * @param geonetGontext GeoNetwork context.
     */
    public InspireAtomHarvester(final GeonetContext geonetGontext) {
        this.gc = geonetGontext;
    }

    /**
     * Process the metadata to check if have an atom document referenced. In this case, the atom
     * document is retrieved and stored in the metadata table.
     */
    public final Element harvest() {
        initializeLog();
        EsSearchManager searchManager = gc.getBean(EsSearchManager.class);
        SettingManager sm = gc.getBean(SettingManager.class);
        DataManager dataMan = gc.getBean(DataManager.class);

        final InspireAtomFeedRepository repository = gc.getBean(InspireAtomFeedRepository.class);

        // Value used in metadata editor for online resources to identify an INSPIRE atom resource
        String atomProtocol = sm.getValue(Settings.SYSTEM_INSPIRE_ATOM_PROTOCOL);

        // Using index information, as type is only available in index and not in database.
        // If retrieved from database retrieves all iso19139 metadata and should apply for each result an xslt process
        // to identify if a service or dataset (slow process)

        List<AbstractMetadata> iso19139Metadata = InspireAtomUtil.searchMetadataByTypeAndProtocol(ServiceContext.get(),
            searchManager, "service", atomProtocol);

        Element result = new Element("response");

        try {
            logger.info("ATOM feed harvest started");

            // Removes all atom information from existing metadata. Harvester will reload with updated information
            logger.info("ATOM feed harvest: remove existing metadata feeds");
            repository.deleteAll();

            logger.info("ATOM feed harvest: retrieving service metadata feeds");

            // Retrieve the SERVICE metadata referencing atom feed documents
            Map<String, String> serviceMetadataWithAtomFeeds =
                InspireAtomUtil.retrieveServiceMetadataWithAtomFeeds(dataMan, iso19139Metadata, atomProtocol);

            logger.info("ATOM feed harvest: processing service metadata feeds (" + serviceMetadataWithAtomFeeds.size() + ")");

            // Process SERVICE metadata feeds
            //    datasetsInformation stores the dataset information for identifier, namespace and feed url
            //    described in the services feed. This information is not available in the datasets feeds.
            List<DatasetFeedInfo> datasetsInformation =
                processServiceMetadataFeeds(dataMan, serviceMetadataWithAtomFeeds, result);

            // Process DATASET metadata feeds related to the service metadata
            logger.info("ATOM feed harvest : processing dataset metadata feeds (" + datasetsInformation.size() + ")");
            processDatasetsMetadataFeeds(dataMan, datasetsInformation, result);

            logger.info("ATOM feed harvest finished");


        } catch (Exception x) {
            logger.error("ATOM feed harvest error: " + x.getMessage());
            logger.error(x);
            result.addContent(new Element("error").setText(x.getMessage()));
        }

        return result;
    }

    /**
     * Harvest an individual metadata. Used in OpenSearchDescription service to retrieve the atom
     * information for a metadata. Useful if metadata has been created in the catalog since the last
     * periodical harvesting.
     *
     * @param metadataId Metadata identifier
     */
    public final void harvestServiceMetadata(final ServiceContext context, final String metadataId) {
        Logger logger = Log.createLogger(Geonet.ATOM);

        final InspireAtomFeedRepository repository = context.getBean(InspireAtomFeedRepository.class);
        DataManager dataMan = context.getBean(DataManager.class);
        SettingManager sm = context.getBean(SettingManager.class);

        final IMetadataUtils metadataUtils = gc.getBean(IMetadataUtils.class);
        AbstractMetadata iso19139Metadata = metadataUtils.findOne(
        		Specification.where((Specification<Metadata>) MetadataSpecs.isType(MetadataType.METADATA))
        			.and((Specification<Metadata>) MetadataSpecs.isIso19139Schema())
                    .and((Specification<Metadata>) MetadataSpecs.hasMetadataId(Integer.parseInt(metadataId))));

        Element result = new Element("response");

        try {
            logger.info("ATOM feed harvest started for metadata: " + metadataId);

            // Value used in metadata editor for online resources to identify an INSPIRE atom resource
            String atomProtocol = sm.getValue(Settings.SYSTEM_INSPIRE_ATOM_PROTOCOL);

            // Removes all atom information from existing metadata. Harvester will reload with updated information
            logger.info("ATOM feed harvest: remove existing metadata feed");
            repository.deleteAll(InspireAtomFeedSpecs.hasMetadataId(Integer.parseInt(metadataId)));
            dataMan.indexMetadata(Arrays.asList(new String[]{metadataId}));

            // Process service metadata feeds
            //    datasetsInformation stores the dataset information for identifier and namespace for the services feed.
            //    This information is not available in the datasets feeds
            logger.info("ATOM feed harvest: processing service metadata feeds");

            // Retrieve the service metadata referencing atom feed document
            Map<String, String> serviceMetadataWithAtomFeed =
                InspireAtomUtil.retrieveServiceMetadataWithAtomFeed(dataMan, iso19139Metadata, atomProtocol);

            List<DatasetFeedInfo> datasetsInformation =
                processServiceMetadataFeeds(dataMan, serviceMetadataWithAtomFeed, result);

            // Process dataset metadata feeds related to the service metadata
            logger.info("ATOM feed harvest for metadata: " + metadataId + ",  processing dataset metadata feeds");
            processDatasetsMetadataFeeds(context, dataMan, datasetsInformation, result);

            logger.info("ATOM feed harvest finished for metadata: " + metadataId);
        } catch (Exception x) {
            logger.error("ATOM feed harvest error: " + x.getMessage());
            logger.error(x);
        }
    }


    /**
     * Process service metadata feeds.
     *
     * @return a Map with the datasets referenced in the service feeds (dataset-id,
     * dataset-namespace). The namespace is only available in the service feeds. Dataset feeds seem
     * not containing this information.
     */
    private List<DatasetFeedInfo> processServiceMetadataFeeds(final DataManager dataMan,
                                                            final Map<String, String> serviceMetadataWithAtomFeeds,
                                                            Element result)
        throws Exception {

        List<DatasetFeedInfo> datasetsInformation = new ArrayList<>();

        final InspireAtomFeedRepository repository = gc.getBean(InspireAtomFeedRepository.class);

        long total = serviceMetadataWithAtomFeeds.entrySet().size();
        long i = 1;

        // Process the metadata retrieving the atom feed content and store it in the catalog.
        for (Map.Entry<String, String> entry : serviceMetadataWithAtomFeeds.entrySet()) {
            String metadataId = entry.getKey();
            String metadataUuid = dataMan.getMetadataUuid(metadataId);

            try {
                logger.info("Processing feed (" + i++ + "/"+ total + ") for service metadata with uuid:" + metadataUuid);

                String atomUrl = entry.getValue();
                logger.debug("Atom feed Url for service metadata (" + metadataUuid + "): " + atomUrl);

                String atomFeedDocument = InspireAtomUtil.retrieveRemoteAtomFeedDocument(gc, atomUrl);
                logger.debug("Atom feed Document for service metadata (" + metadataUuid + "): " + atomFeedDocument);

                Element atomDoc = Xml.loadString(atomFeedDocument, false);

                if (!atomDoc.getNamespace().equals(Geonet.Namespaces.ATOM)) {
                    logger.warning("Atom feed Document (" + atomUrl + ") for service metadata (" + metadataUuid + ") is not a valid feed");
                    continue;
                }

                InspireAtomFeed inspireAtomFeed = InspireAtomFeed.build(atomDoc);
                inspireAtomFeed.setMetadataId(Integer.parseInt(metadataId));
                inspireAtomFeed.setAtomUrl(atomUrl);
                inspireAtomFeed.setAtom(atomFeedDocument);
                inspireAtomFeed.setAtomDatasetid("");
                inspireAtomFeed.setAtomDatasetns("");

                repository.save(inspireAtomFeed);

                // Index the metadata to store the atom feed information in the index
                dataMan.indexMetadata(Arrays.asList(new String[]{metadataId}));


                // Extract datasets information (identifier, namespace) from the service feed:
                //      The namespace is only available in service feed and no in dataset feeds.
                //      Also NGR metadata uses MD_Identifier instead of RS_Identifier so lacks of this information
                logger.debug("Extract datasets information (identifier, namespace) from service atom feed  (" + atomUrl + ")");

                datasetsInformation.addAll(InspireAtomUtil.extractRelatedDatasetsInfoFromServiceFeed(atomFeedDocument, dataMan));

                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid)
                    .setAttribute("feed", atomUrl).setAttribute("status", "ok"));
            } catch (Exception ex) {
                // Log exception and continue processing the other metadata
                logger.error("Failed to process atom feed for service metadata: " + metadataUuid + " " + ex.getMessage());
                logger.error(ex);
                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid)
                    .setAttribute("error", ex.getMessage()).setAttribute("status", "error"));
            }
        }

        return datasetsInformation;
    }


    /**
     * Process dataset metadata feeds.
     */
    private void processDatasetsMetadataFeeds(final DataManager dataMan,
                                              final List<DatasetFeedInfo> datasetsFeedInformation,
                                              Element result)
        throws Exception {

        processDatasetsMetadataFeeds(ServiceContext.get(), dataMan, datasetsFeedInformation, result);
    }

    /**
     * Process the feeds for a set datasets related to a service metadata.
     *
     * @param datasetsFeedInformation Datasets map (datasetid, namespace)
     */
    private void processDatasetsMetadataFeeds(final ServiceContext context,
                                              final DataManager dataMan,
                                              final List<DatasetFeedInfo> datasetsFeedInformation,
                                              final Element result)
        throws Exception {

        final InspireAtomFeedRepository repository = gc.getBean(InspireAtomFeedRepository.class);

        long total = datasetsFeedInformation.size();
        long i = 1;

        // Process the metadata retrieving the atom feed content and store it in the catalog.
        for(DatasetFeedInfo datasetFeedInfo: datasetsFeedInformation) {
            String metadataUuid = "";

            try {
                // Find the metadata UUID using the resource identifier gmd:MD_Identifier/gmd:code
                metadataUuid = InspireAtomUtil.retrieveDatasetUuidFromIdentifier(ServiceContext.get(),
                    gc.getBean(EsSearchManager.class), datasetFeedInfo.identifier);

                String atomUrl = datasetFeedInfo.feedUrl;

                logger.info("Processing feed (" + i++ + "/"+ total + ") for dataset metadata with uuid:" + metadataUuid + ", feed url: " + atomUrl);

                if (StringUtils.isEmpty(metadataUuid)) {
                    logger.warning("Metadata with dataset identifier (" + datasetFeedInfo.identifier + ") is not available. Skip dataset feed processing");
                    continue;
                }

                if (!atomUrl.toLowerCase().endsWith(".xml")) {
                    logger.warning("Atom feed Document (" + atomUrl + ") for dataset metadata (" + metadataUuid + ") is not a valid feed");
                    continue;
                }

                String metadataId = dataMan.getMetadataId(metadataUuid);

                logger.debug("Dataset, id=" + datasetFeedInfo.identifier + ", namespace=" + datasetFeedInfo.namespace);

                String atomFeedDocument = InspireAtomUtil.retrieveRemoteAtomFeedDocument(gc, atomUrl);
                logger.debug("Dataset feed: " + atomFeedDocument);

                Element atomDoc = Xml.loadString(atomFeedDocument, false);

                // Skip document if not a feed
                if (!atomDoc.getNamespace().equals(Geonet.Namespaces.ATOM)) {
                    logger.warning("Atom feed Document (" + atomUrl + ") for dataset metadata (" + metadataUuid + ") is not a valid feed");
                    continue;
                }

                InspireAtomFeed inspireAtomFeed = InspireAtomFeed.build(atomDoc);
                inspireAtomFeed.setMetadataId(Integer.parseInt(metadataId));
                inspireAtomFeed.setAtomDatasetid(datasetFeedInfo.identifier);
                inspireAtomFeed.setAtomDatasetns(datasetFeedInfo.namespace);
                inspireAtomFeed.setAtomUrl(atomUrl);
                inspireAtomFeed.setAtom(atomFeedDocument);

                repository.save(inspireAtomFeed);

                // Index the metadata to store the atom feed information in the index
                dataMan.indexMetadata(Arrays.asList(new String[]{metadataId}));
                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid)
                    .setAttribute("feed", atomUrl).setAttribute("status", "ok"));
            } catch (Exception ex) {
                // Log exception and continue processing the other metadata
                logger.error("Failed to process atom feed for dataset metadata: " + metadataUuid + " " + ex.getMessage());
                logger.error(ex);
                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid).setAttribute("error", ex.getMessage()).setAttribute("status", "error"));
            }
        }
    }


    /**
     * Process the feeds for a set datasets related to a service metadata.
     *
     */
    private void processDatasetsMetadataFeedsForService(final ServiceContext context,
                                                        final DataManager dataMan,
                                                        final List<DatasetFeedInfo> datasetsFeedInformation,
                                                        final Element result)
        throws Exception {

        // Retrieve the metadata referencing atom feed documents.

        // Value used in metadata editor for online resources to identify an INSPIRE atom resource
        String atomProtocol = gc.getBean(SettingManager.class).getValue("system/inspire/atomProtocol");

        final InspireAtomFeedRepository repository = gc.getBean(InspireAtomFeedRepository.class);

        // Process the metadata retrieving the atom feed content and store it in the catalog.
        for(DatasetFeedInfo datasetFeedInfo: datasetsFeedInformation) {
            String atomDatasetId = datasetFeedInfo.identifier;
            String metadataUuid = "";

            try {
                metadataUuid = InspireAtomUtil.retrieveDatasetUuidFromIdentifier(context,
                    gc.getBean(EsSearchManager.class), atomDatasetId);

                String atomDatasetNs = datasetFeedInfo.namespace;
                logger.debug("Dataset, id=" + atomDatasetId + ", namespace=" + atomDatasetNs);

                if (StringUtils.isEmpty(metadataUuid)) {
                    logger.warning("Can't find dataset metadata with datasetIdCode:" + atomDatasetId);
                    continue;
                }

                logger.info("Processing feed for dataset metadata with uuid:" + metadataUuid);

                String metadataId = dataMan.getMetadataId(metadataUuid);

                // Get the atom feed url for the dataset
                String atomUrl = datasetFeedInfo.feedUrl;
                logger.debug("Dataset, feedurl=" + atomUrl);

                // Retrieve the atom document and store it.
                String atomFeedDocument = InspireAtomUtil.retrieveRemoteAtomFeedDocument(gc, atomUrl);
                logger.debug("Dataset feed: " + atomFeedDocument);

                Element atomDoc = Xml.loadString(atomFeedDocument, false);

                // Skip document if not a feed
                if (!atomDoc.getNamespace().equals(Geonet.Namespaces.ATOM)) {
                    logger.warning("Atom feed Document (" + atomUrl + ") for dataset metadata (" + metadataUuid + ") is not a valid feed");
                    continue;
                }

                InspireAtomFeed inspireAtomFeed = InspireAtomFeed.build(atomDoc);
                inspireAtomFeed.setMetadataId(Integer.parseInt(metadataId));
                inspireAtomFeed.setAtomDatasetid(atomDatasetId);
                inspireAtomFeed.setAtomDatasetns(atomDatasetNs);
                inspireAtomFeed.setAtomUrl(atomUrl);
                inspireAtomFeed.setAtom(atomFeedDocument);

                repository.save(inspireAtomFeed);

                // Index the metadata to store the atom feed information in the index
                dataMan.indexMetadata(Arrays.asList(new String[]{metadataId}));

                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid).setAttribute("feed", atomUrl).setAttribute("status", "ok"));
            } catch (Exception ex) {
                // Log exception and continue processing the other metadata
                logger.error("Failed to process atom feed for dataset metadata: " + metadataUuid + " " + ex.getMessage());
                result.addContent(new Element("feed").setAttribute("uuid", metadataUuid).setAttribute("error", ex.getMessage()).setAttribute("status", "error"));

            }
        }
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    private String initializeLog() {

        // configure personalized logger
        String packagename = getClass().getPackage().getName();
        String[] packages = packagename.split("\\.");
        String packageType = packages[packages.length - 1];
        final String harvesterName = "inspireatom";
        logger = Log.createLogger("inspireatom", "geonetwork.atom");

        String directory = logger.getFileAppender();
        if (directory == null || directory.isEmpty()) {
            directory = gc.getBean(GeonetworkDataDirectory.class).getSystemDataDir() + "/harvester_logs/";
        }
        File d = new File(directory);
        if (!d.isDirectory()) {
            directory = d.getParent() + File.separator;
        }

        FileAppender fa = new FileAppender();
        fa.setName(harvesterName);
        String logfile = directory + "atomharvester_" + packageType + "_"
            + dateFormat.format(new Date(System.currentTimeMillis()))
            + ".log";
        fa.setFile(logfile);

        SettingManager settingManager = gc.getBean(SettingManager.class);

        String timeZoneSetting = settingManager.getValue(Settings.SYSTEM_SERVER_TIMEZONE);
        if (StringUtils.isBlank(timeZoneSetting)) {
            timeZoneSetting = TimeZone.getDefault().getID();
        }
        fa.setLayout(new EnhancedPatternLayout("%d{yyyy-MM-dd'T'HH:mm:ss,SSSZ}{" + timeZoneSetting +"} %-5p [%c] - %m%n"));

        fa.setThreshold(logger.getThreshold());
        fa.setAppend(true);
        fa.activateOptions();

        logger.setAppender(fa);

        return logfile;
    }
}
