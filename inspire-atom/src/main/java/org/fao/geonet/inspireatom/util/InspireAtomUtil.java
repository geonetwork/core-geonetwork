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

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.apache.commons.lang.StringUtils;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.Namespace;

import java.net.URL;
import java.util.*;

/**
 * Utility class for INSPIRE Atom.
 *
 * @author Jose García
 */
public class InspireAtomUtil {

    /** Xslt process to get the related datasets in service metadata. **/
    private static final String EXTRACT_DATASETS = "extract-datasets.xsl";

    /** Xslt process to get if a metadata is a service or a dataset. **/
    private static final String EXTRACT_MD_TYPE = "extract-type.xsl";

    /** Xslt process to get the atom feed link from the metadata. **/
    private static final String EXTRACT_ATOM_FEED = "extract-atom-feed.xsl";

    /**
     * Issue an http request to retrieve the remote Atom feed document.
     *
     * @param context
     * @param url           Atom document url.
     * @return              Atom document content.
     * @throws Exception    Exception.
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
     * @param feed          JDOM element with dataset feed content.
     * @param crs           CRS to use in the filter.
     *
     * @throws Exception    Exception.
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
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve("extract-type.xsl");

        Map<String, Object> paramsM = new HashMap<String, Object>();
        String mdType = Xml.transform(md, styleSheet, paramsM).getText().trim();

        return "service".equalsIgnoreCase(mdType);
    }


    /**
     *
     * @param schema        Metadata schema.
     * @param md            JDOM element with metadata content.
     * @param dataManager   DataManager.
     * @return              Atom feed URL.
     * @throws Exception    Exception.
     */
    public static List<String> extractRelatedDatasetsIdentifiers(final String schema, final Element md, final DataManager dataManager)
            throws Exception {
        java.nio.file.Path styleSheet = dataManager.getSchemaDir(schema).resolve(EXTRACT_DATASETS);

        List<Element> datasetsEl = Xml.transform(md, styleSheet).getChildren();
        List<String> datasets = new ArrayList<String>();

        //--- needed to detach md from the document
        md.detach();

        for (Element datasetEl: datasetsEl) {
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

        for (Metadata md: iso19139Metadata) {
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
     *
     * @param schema        Metadata schema.
     * @param md            JDOM element with metadata content.
     * @param dataManager   DataManager.
     * @return              Atom feed URL.
     * @throws Exception    Exception.
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

            Map<Integer,Metadata> allMdInfo = ((LuceneSearcher)searcher).getAllMdInfo(context, searcher.getSize());
            return new ArrayList<Metadata>(allMdInfo.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<Metadata>();
        } finally {
            if (searcher != null) searcher.close();
        }
    }


    public static String retrieveDatasetUuidFromIdentifier(ServiceContext context,
                                                      SearchManager searchMan,
                                                      String datasetIdCode) {

        String uuid = "";

        Element request = new Element(Jeeves.Elem.REQUEST);
        request.addContent(new Element("identifier").setText(datasetIdCode));
        request.addContent(new Element("has_atom").setText("y"));
        request.addContent(new Element("fast").setText("true"));

        // perform the search and return the results read from the index
        MetaSearcher searcher = null;
        try {
            searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            searcher.search(context, request, new ServiceConfig());

            List<String> uuids = ((LuceneSearcher)searcher).getAllUuids(1, context);
            if (uuids.size() > 0) {
                uuid = uuids.get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (searcher != null) searcher.close();
        }

        return uuid;
    }
}