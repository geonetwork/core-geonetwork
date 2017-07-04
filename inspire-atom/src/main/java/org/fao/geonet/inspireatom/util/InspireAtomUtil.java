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
package org.fao.geonet.inspireatom.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.AtomFeedNotFoundEx;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.Namespace;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Utility class for INSPIRE Atom.
 *
 * @author Jose García
 */
public class InspireAtomUtil {

    /**
     * Xslt process to get the related datasets in service metadata.
     **/
    private static final String EXTRACT_DATASETS = "extract-datasets.xsl";

    /**
     * Xslt process to get if a metadata is a service or a dataset.
     **/
    private static final String EXTRACT_MD_TYPE = "extract-type.xsl";

    /**
     * Xslt process to get if a metadata is a atom service.
     **/
    private static final String EXTRACT_SERVICE_TYPE = "extract-service-type.xsl";

    /**
     * Xslt process to get the atom feed link from the metadata.
     **/
    private static final String EXTRACT_ATOM_FEED = "extract-atom-feed.xsl";

	/**
	 * Xslt process to get the related dataset fileidentifiers in service
	 * metadata.
	 **/
    private static final String EXTRACT_DATASET_FILEIDENTIFIERS = "extract-dataset-fileidentifiers.xsl";

	/** Xslt process to get the related datasets in service metadata. **/
    private static final String EXTRACT_DATASET_ENTRY_INFO = "extract-dataset-entry-info.xsl";

	/** Xslt process to get the service atomfeed. **/
	public static final String INSPIRE_ATOM_FEED = "inspire-atom-feed.xsl";

	private static final String ATOM_SERVICE_TYPE_VERSION = "INSPIRE ATOM V3.1";

	private static final String ATOM_SERVICE_TYPE = "download";

	/**
     * Issue an http request to retrieve the local Atom feed document.
     *
     * @param url Atom document url.
     * @return Atom document content.
     * @throws Exception Exception.
     */
    public static Element retrieveLocalAtomFeedDocument(final ServiceContext context,
                                                        final String url) throws Exception {
    	XmlRequest remoteRequest = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(url));

        final SettingManager sm = context.getBean(SettingManager.class);

        Lib.net.setupProxy(sm, remoteRequest);

        return remoteRequest.execute();

    }

	/**
     * Issue an http request to retrieve the local Atom feed document.
     *
     * @param url Atom document url.
     * @return Atom document content as a String.
     * @throws Exception Exception.
     */
    public static String retrieveLocalAtomFeedDocumentAsString(final ServiceContext context,
                                                        final String url) throws Exception {
        return Xml.getString(retrieveLocalAtomFeedDocument(context, url));
    }

    /**
     * Issue an http request to retrieve the remote Atom feed document.
     *
     * @param url Atom document url.
     * @return Atom document content.
     * @throws Exception Exception.
     */
    public static String retrieveRemoteAtomFeedDocument(final ServiceContext context,
                                                        final String url) throws Exception {
        XmlRequest remoteRequest = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(url));

        final SettingManager sm = context.getBean(SettingManager.class);

        Lib.net.setupProxy(sm, remoteRequest);

        Element atomFeed = remoteRequest.execute();

        return Xml.getString(atomFeed);
    }

    public static String retrieveRemoteAtomFeedDocument(final GeonetContext context,
                                                        final String url) throws Exception {
        XmlRequest remoteRequest = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(url));

        final SettingManager sm = context.getBean(SettingManager.class);

        Lib.net.setupProxy(sm, remoteRequest);

        Element atomFeed = remoteRequest.execute();

        return Xml.getString(atomFeed);
    }

    /**
     * Filters a dataset feed removing all the downloads that are not related to the CRS provided.
     *
     * This method changes feed content.
     *
     * @param feed JDOM element with dataset feed content.
     * @param crs  CRS to use in the filter.
     * @throws Exception Exception.
     */
    public static void filterDatasetFeedByCrs(final Element feed,
                                              final String crs)
        throws Exception {

        List<Element> elementsToRemove = new ArrayList<Element>();

        Iterator it = feed.getChildren().iterator();

        // Filters the entry elements for the CRS, creating a list of entry elements to remove from the feed.
        while (it.hasNext()) {
            Element el = (Element) it.next();
            String name = el.getName();
            if (name.equalsIgnoreCase("entry")) {
                Element catEl = el.getChild("category", Namespace.getNamespace("http://www.w3.org/2005/Atom"));
                if (catEl != null) {
                    String term = catEl.getAttributeValue("term");
                    if (!StringUtils.contains(term, crs)) {
                        elementsToRemove.add(el);
                    }
                }
            }
        }

        // Remove entry elements that are not related to the filter CRS
        for (Element element : elementsToRemove) {
            element.getParent().removeContent(element);
        }
    }

    public static boolean isServiceMetadata(DataManager dm, String schema, Element md) throws Exception {
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve(EXTRACT_MD_TYPE);

        Map<String, Object> paramsM = new HashMap<String, Object>();
        String mdType = Xml.transform(md, styleSheet, paramsM).getText().trim();

        return "service".equalsIgnoreCase(mdType);
    }

	public static boolean isDatasetMetadata(DataManager dm, String schema, Element md) throws Exception {
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve(EXTRACT_MD_TYPE);

        Map<String, Object> paramsM = new HashMap<String, Object>();
        String mdType = Xml.transform(md, styleSheet, paramsM).getText().trim();

		return "dataset".equalsIgnoreCase(mdType);
	}
    /**
     * @param schema      Metadata schema.
     * @param md          JDOM element with metadata content.
     * @param dataManager DataManager.
     * @return Atom feed URL.
     * @throws Exception Exception.
     */
    public static List<String> extractRelatedDatasetsIdentifiers(final String schema, final Element md, final DataManager dataManager)
        throws Exception {
        java.nio.file.Path styleSheet = dataManager.getSchemaDir(schema).resolve(EXTRACT_DATASETS);

        List<Element> datasetsEl = Xml.transform(md, styleSheet).getChildren();
        List<String> datasets = new ArrayList<String>();

        //--- needed to detach md from the document
        md.detach();

        for (Element datasetEl : datasetsEl) {
            String datasetId = datasetEl.getText();

            if (!StringUtils.isEmpty(datasetId)) datasets.add(datasetEl.getText());
        }

        return datasets;
    }


    public static Map<String, String> retrieveServiceMetadataWithAtomFeeds(final DataManager dataManager,
                                                                           final List<Metadata> iso19139Metadata,
                                                                           final String atomProtocol)
        throws Exception {

        return processAtomFeedsInternal(dataManager, iso19139Metadata, "service", atomProtocol);
    }

    public static Map<String, String> retrieveServiceMetadataWithAtomFeed(final DataManager dataManager,
                                                                          final Metadata iso19139Metadata,
                                                                          final String atomProtocol)
        throws Exception {

        List<Metadata> iso19139MetadataList = new ArrayList<Metadata>();
        iso19139MetadataList.add(iso19139Metadata);

        return retrieveServiceMetadataWithAtomFeeds(dataManager, iso19139MetadataList, atomProtocol);
    }

    public static Map<String, String> retrieveDatasetMetadataWithAtomFeeds(final DataManager dataManager,
                                                                           final List<Metadata> iso19139Metadata,
                                                                           final String atomProtocol)
        throws Exception {

        return processAtomFeedsInternal(dataManager, iso19139Metadata, "dataset", atomProtocol);
    }

    private static Map<String, String> processAtomFeedsInternal(DataManager dataManager,
                                                                List<Metadata> iso19139Metadata, String type,
                                                                String atomProtocol) throws Exception {

        Map<String, String> metadataAtomFeeds = new HashMap<String, String>();

        for (Metadata md : iso19139Metadata) {
            int id = md.getId();
            String schema = md.getDataInfo().getSchemaId();
            Element mdEl = null;
            if (md.getData() == null) {
                mdEl = dataManager.getMetadata(id + "");
            } else {
                mdEl = Xml.loadString(md.getData(), false);
            }

            String atomFeed = extractAtomFeedUrl(schema, mdEl, dataManager, atomProtocol);

            if (StringUtils.isNotEmpty(atomFeed)) {
                metadataAtomFeeds.put(id + "", atomFeed);
            }
        }

        return metadataAtomFeeds;
    }

    /**
     * @param schema      Metadata schema.
     * @param md          JDOM element with metadata content.
     * @param dataManager DataManager.
     * @return Atom feed URL.
     * @throws Exception Exception.
     */
    public static String extractAtomFeedUrl(final String schema,
                                            final Element md,
                                            final DataManager dataManager, String atomProtocol)
        throws Exception {
        java.nio.file.Path styleSheet = dataManager.getSchemaDir(schema).resolve(EXTRACT_ATOM_FEED);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("atomProtocol", atomProtocol);

        String atomFeed = Xml.transform(md, styleSheet, params).getText().trim();

        //--- needed to detach md from the document
        md.detach();

        return atomFeed;
    }

    public static List<Metadata> searchMetadataByType(ServiceContext context,
                                                      SearchManager searchMan,
                                                      String type) {

        Element request = new Element(Jeeves.Elem.REQUEST);
        request.addContent(new Element("type").setText(type));
        request.addContent(new Element("fast").setText("true"));

        // perform the search and return the results read from the index
        MetaSearcher searcher = null;
        try {
            searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            searcher.search(context, request, new ServiceConfig());

            Map<Integer, Metadata> allMdInfo = ((LuceneSearcher) searcher).getAllMdInfo(context, searcher.getSize());
            return new ArrayList<Metadata>(allMdInfo.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<Metadata>();
        } finally {
            if (searcher != null) searcher.close();
        }
    }


    public static String retrieveDatasetUuidFromIdentifier(ServiceContext context,
            String datasetIdCode, String mainSearchFieldName, String searchTerms) {

		String uuid = "";
		SearchManager searchMan = ((GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME)).getBean(SearchManager.class);
		Element request = new Element(Jeeves.Elem.REQUEST);
		if (mainSearchFieldName.equals("identifier")) {
			request.addContent(new Element("has_atom").setText("true"));			
		} else {
			request.addContent(new Element("serviceType").setText(ATOM_SERVICE_TYPE));
		}
		request.addContent(new Element("fast").setText("true"));
		if (!StringUtils.isEmpty(datasetIdCode)) {
			request.addContent(new Element(mainSearchFieldName).setText(datasetIdCode));
		}
		if (!StringUtils.isEmpty(searchTerms)) {
			request.addContent(new Element("any").setText(searchTerms));
		}

        // perform the search and return the results read from the index

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, request, new ServiceConfig());

            List<String> uuids = ((LuceneSearcher) searcher).getAllUuids(1, context);
            if (uuids.size() > 0) {
                uuid = uuids.get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return uuid;
    }

    public static Element getServiceFeed(String metadataUuid,
			ServiceContext context, List<String> selectedDatasetFileIdentifiers) throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = context.getBean(DataManager.class);
        SettingManager sm = gc.getBean(SettingManager.class);
        String atomProtocol = sm.getValue("system/inspire/atomProtocol");
		Element serviceEl = new Element("service");
        String metadataId = dm.getMetadataId(metadataUuid);
		Element md = dm.getMetadata(metadataId);
		// String schema = dm.getMetadataSchema(dbms, id);
        String schema = dm.getMetadataSchema(metadataId);
		if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
			throw new Exception("No service metadata found with uuid:"
					+ metadataUuid);
		}

		if (!InspireAtomUtil
				.isAtomDownloadServiceMetadata(dm, schema, md, atomProtocol)) {
			throw new Exception(
					"No ATOM download service metadata found with uuid:"
							+ metadataUuid);
		}

		// Get dataset identifiers referenced by service metadata.
		List<String> datasetFileIdentifiers = null;

		datasetFileIdentifiers = InspireAtomUtil
				.extractRelatedDatasetFileIdentifiers(schema, md, dm);

		String baseURL = context.getBaseUrl();
		String webappName = baseURL.substring(1);
		// String keywords = LuceneSearcher.getMetadataFromIndex(webappName,
		// context.getLanguage(), fileIdentifier, "keyword");

		// Process datasets information

		Element datasetsEl = processDatasetsInfo(dm,
				datasetFileIdentifiers, selectedDatasetFileIdentifiers, metadataUuid, context, webappName);

		// Build response.
		return serviceEl.addContent(md.addContent((Element)datasetsEl.clone()));
	}

	private static Element processDatasetsInfo(DataManager dm, 
			final List<String> datasetFileIdentifiers, final List<String> selectedDatasetFileIdentifiers,
			final String serviceIdentifier, final ServiceContext context,
			String webappName) throws Exception {
		Element datasetsEl = new Element("datasets");

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);

		for (String fileIdentifier : datasetFileIdentifiers) {
			try {
				if (selectedDatasetFileIdentifiers!=null && !selectedDatasetFileIdentifiers.contains(fileIdentifier)) {
					continue;
				}
				datasetsEl.addContent(/*
									 * InspireAtomUtil.getDatasetFeed(
									 * datasetIdentifier, context)
									 */InspireAtomUtil.getDatasetEntryInfo(dm, 
						fileIdentifier, context));
			} catch (MetadataNotFoundEx e) {
				System.out.println("Dataset with id " + fileIdentifier
						+ " not exists (uuid: " + e.toString() + ")");
			} catch (AtomFeedNotFoundEx e) {
				System.out.println("Dataset with id "
								+ fileIdentifier
								+ " has no download url with application profile INSPIRE-Download-Atom ("
								+ e.toString() + ")");
			}
		}
		return datasetsEl;
	}

	public static Element getDatasetEntryInfo(DataManager dm, String datasetFileIdentifier,
			ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		if (!StringUtils.isBlank(datasetFileIdentifier)) {
			// Retrieve metadata to check existence and permissions.
			String id = dm.getMetadataId(datasetFileIdentifier);
			if (StringUtils.isEmpty(id))
				throw new MetadataNotFoundEx(datasetFileIdentifier);

			Element md = dm.getMetadata(id);
			String schema = dm.getMetadataSchema(id);
	        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
			// Get dataset identifiers referenced by service metadata.
			Element entry = InspireAtomUtil.extractDatasetEntryInfo(schema, md, dm);
			if (entry.getChildren().size() > 0) {
				return entry;
			} else {
				throw new AtomFeedNotFoundEx(datasetFileIdentifier);
			}
		} else {
			throw new MetadataNotFoundEx(datasetFileIdentifier);
		}
	}

	public static Element extractDatasetEntryInfo(final String schema, final Element md,
			final DataManager dm) throws Exception {
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve(EXTRACT_DATASET_ENTRY_INFO);
        Element datasetEl = Xml.transform(md, styleSheet);
		md.detach();
		return datasetEl;
	}

	public static boolean isAtomDownloadServiceMetadata(
			DataManager dm, String schema ,
			Element md, String atomProtocol) throws Exception {
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve(EXTRACT_SERVICE_TYPE);
		Element service = Xml.transform(md, styleSheet);
		String serviceType = service.getChildText("serviceType").trim();
		String serviceTypeVersion = service.getChildText("serviceTypeVersion").trim();
		return atomProtocol.equalsIgnoreCase(serviceType)
				&& ATOM_SERVICE_TYPE_VERSION.equalsIgnoreCase(serviceTypeVersion);
	}

	public static List<String> extractRelatedDatasetFileIdentifiers(
			final String schema, final Element md,
			final DataManager dm) throws Exception {
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve(EXTRACT_DATASET_FILEIDENTIFIERS);
		@SuppressWarnings("unchecked")
		List<Element> fileIdentifiersEl = Xml.transform(md, styleSheet)
				.getChildren();
		List<String> fileIdentifiers = new ArrayList<String>();

		// --- needed to detach md from the document
		md.detach();

		for (Element fileIdentifierEl : fileIdentifiersEl) {
			String fileIdentifier = fileIdentifierEl.getText();

			if (!StringUtils.isEmpty(fileIdentifier))
				fileIdentifiers.add(fileIdentifier);
		}

		return fileIdentifiers;
	}
	
	public static Element getDatasetFeed(DataManager dm, ServiceContext context, String metadataId, String fileIdentifier, String datasetIdCode, String datasetIdNs, String crs, String searchTerms)
			throws Exception {
		if (!StringUtils.isBlank(fileIdentifier)) {
			Element md = dm.getMetadata(metadataId);
            String schema = dm.getMetadataSchema(metadataId);
			if (!InspireAtomUtil.isDatasetMetadata(dm, schema, md)) {
				throw new Exception("No dataset metadata found with uuid:"
						+ fileIdentifier);
			}
			boolean bCodeSpaceValueIsEqual = false;
			if (!StringUtils.isBlank(datasetIdNs)) {
				List<Namespace> nss = new ArrayList<Namespace>();
//				nss.addAll(md.getAdditionalNamespaces());
//				nss.add(md.getNamespace());
				nss.add(Namespace.getNamespace("gmd","http://www.isotc211.org/2005/gmd"));
				nss.add(Namespace.getNamespace("gco","http://www.isotc211.org/2005/gco"));
				Object o = Xml
						.selectSingle(
								md,
								"gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:codeSpace/gco:CharacterString|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString",
								nss);
				if (o != null && o instanceof Element) {
					String codeSpaceValue = ((Element) o).getText();
					if (!StringUtils.isBlank(codeSpaceValue)
							&& codeSpaceValue.equals(datasetIdNs)) {
						bCodeSpaceValueIsEqual = true;
					}
				}
			} else {
				bCodeSpaceValueIsEqual = true;
			}
			if (bCodeSpaceValueIsEqual) {
				Element datasetEl = new Element("dataset");
				try {
					Element datasetEntryInfo = InspireAtomUtil.getDatasetEntryInfo(dm, 
							fileIdentifier, context);
					if (!StringUtils.isBlank(crs)) {
						datasetEl.addContent(new Element("crs").setText(crs));
					}
					String serviceIdentifier = null;
					try {
						serviceIdentifier = retrieveDatasetUuidFromIdentifier(context, datasetIdCode, "operatesOn", searchTerms);
					} catch (Exception e) {
					}
					if (StringUtils.isNotBlank(serviceIdentifier)) {
						datasetEl.addContent(new Element("serviceIdentifier").setText(serviceIdentifier));
					}
					return datasetEl.addContent(datasetEntryInfo);
				} catch (AtomFeedNotFoundEx e) {
					throw new AtomFeedNotFoundEx(fileIdentifier);
				}
			}
			throw new MetadataNotFoundEx(fileIdentifier);
		} else {
			throw new MetadataNotFoundEx(fileIdentifier);
		}
	}

	public static String getBaseServiceAtomUrl(ServiceContext context, String fileIdentifier) {
        NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);
        return context.getBean(SettingManager.class).getSiteURL(context).replaceAll("/" + nodeInfo.getId() + "/", "/opensearch/").replaceAll(":80/","/").replaceAll(":443/","/") + fileIdentifier + "/describe";
    }

    public static String getBaseDatasetAtomUrl(ServiceContext context) {
        NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);
        return context.getBean(SettingManager.class).getSiteURL(context).replaceAll("/" + nodeInfo.getId() + "/", "/opensearch/").replaceAll(":80/","/").replaceAll(":443/","/") + "describe";
    }
}
