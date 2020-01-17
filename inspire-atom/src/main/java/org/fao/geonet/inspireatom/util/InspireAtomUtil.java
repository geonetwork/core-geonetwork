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
package org.fao.geonet.inspireatom.util;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.xpath.XPath;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
     * Xslt process to get the atom feed link from the metadata.
     **/
    private static final String EXTRACT_ATOM_FEED = "extract-atom-feed.xsl";

    /**
     * Xslt process to get the atom feed link from the metadata.
     **/
    private static final String TRANSFORM_MD_TO_ATOM_FEED = "inspire-atom-feed.xsl";

    /**
     * Xslt process to get the atom feed link from the metadata.
     **/
    private static final String TRANSFORM_ATOM_TO_OPENSEARCHDESCRIPTION = "opensearchdescription.xsl";

    /**
     * The OpenSearchDescription filename to describe local atomfeeds.
     **/
    public static final String LOCAL_OPENSEARCH_DESCRIPTION_FILE_NAME = "OpenSearchDescription.xml";

    /**
     * The opensearch url suffix to describe local atomfeeds.
     **/
    public static final String LOCAL_OPENSEARCH_URL_SUFFIX = "opensearch";

    /**
     * The describe url suffix for service atom feeds.
     **/
    public static final String LOCAL_DESCRIBE_SERVICE_URL_SUFFIX = "atom/describe/service";

    /**
     * The describe url suffix for dataset atom feeds.
     **/
    public static final String LOCAL_DESCRIBE_DATASET_URL_SUFFIX = "atom/describe/dataset";

    /**
     * The download url suffix for download of dataset atom feeds.
     **/
    public static final String LOCAL_DOWNLOAD_DATASET_URL_SUFFIX = "atom/download/dataset";

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
     * <p>
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
        java.nio.file.Path styleSheet = dm.getSchemaDir(schema).resolve("extract-type.xsl");

        Map<String, Object> paramsM = new HashMap<String, Object>();
        String mdType = Xml.transform(md, styleSheet, paramsM).getText().trim();

        return "service".equalsIgnoreCase(mdType);
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
        List<String> datasets = new ArrayList<>();

        //--- needed to detach md from the document
        md.detach();

        for (Element datasetEl : datasetsEl) {
            String datasetId = datasetEl.getText();

            if (!StringUtils.isEmpty(datasetId)) datasets.add(datasetEl.getText());
        }

        return datasets;
    }


    public static Map<String, String> retrieveServiceMetadataWithAtomFeeds(final DataManager dataManager,
                                                                           final List<AbstractMetadata> iso19139Metadata,
                                                                           final String atomProtocol)
        throws Exception {

        return processAtomFeedsInternal(dataManager, iso19139Metadata, "service", atomProtocol);
    }

    public static Map<String, String> retrieveServiceMetadataWithAtomFeed(final DataManager dataManager,
                                                                          final AbstractMetadata iso19139Metadata,
                                                                          final String atomProtocol)
        throws Exception {

        List<AbstractMetadata> iso19139MetadataList = new ArrayList<>();
        iso19139MetadataList.add(iso19139Metadata);

        return retrieveServiceMetadataWithAtomFeeds(dataManager, iso19139MetadataList, atomProtocol);
    }

    public static Map<String, String> retrieveDatasetMetadataWithAtomFeeds(final DataManager dataManager,
                                                                           final List<AbstractMetadata> iso19139Metadata,
                                                                           final String atomProtocol)
        throws Exception {

        return processAtomFeedsInternal(dataManager, iso19139Metadata, "dataset", atomProtocol);
    }

    private static Map<String, String> processAtomFeedsInternal(DataManager dataManager,
                                                                List<AbstractMetadata> iso19139Metadata, String type,
                                                                String atomProtocol) throws Exception {

        Map<String, String> metadataAtomFeeds = new HashMap<String, String>();

        for (AbstractMetadata md : iso19139Metadata) {
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

        Map<String, Object> params = new HashMap<>();
        params.put("atomProtocol", atomProtocol);

        String atomFeed = Xml.transform(md, styleSheet, params).getText().trim();

        //--- needed to detach md from the document
        md.detach();

        return atomFeed;
    }

    public static List<AbstractMetadata> searchMetadataByType(ServiceContext context,
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

            Map<Integer, AbstractMetadata> allMdInfo = ((LuceneSearcher) searcher).getAllMdInfo(context, searcher.getSize());
            return new ArrayList<>(allMdInfo.values());
        } catch (Exception ex) {
            Log.error(Geonet.ATOM, ex.getMessage(), ex);
            return new ArrayList<>();
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

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, request, new ServiceConfig());

            List<String> uuids = ((LuceneSearcher) searcher).getAllUuids(1, context);
            if (uuids.size() > 0) {
                uuid = uuids.get(0);
            }
        } catch (Exception ex) {
            Log.error(Geonet.ATOM, ex.getMessage(), ex);
        }

        return uuid;
    }

    public static Element prepareServiceFeedEltBeforeTransform(final String schema,
                                                               final Element md,
                                                               final DataManager dataManager)
        throws Exception {

        List<String> datasetsUuids = extractRelatedDatasetsIdentifiers(schema, md, dataManager);
        Element root = new Element("root");
        Element serviceElt = new Element("service");
        Element datasetElt = new Element("datasets");

        root.addContent(serviceElt);
        md.addContent(datasetElt);

        for (String uuid : datasetsUuids) {
            String id = dataManager.getMetadataId(uuid);
            if (StringUtils.isEmpty(id))
                throw new ResourceNotFoundException(String.format("Dataset '%s' attached to the requested service was not found. Check the link between the 2 records (see operatesOn element).", uuid));
            Element ds = dataManager.getMetadata(id);
            datasetElt.addContent(ds);
        }
        serviceElt.addContent(md);

        return root;
    }

    public static Element prepareDatasetFeedEltBeforeTransform(
        final Element md,
        final String serviceMdUuid)
        throws Exception {

        Document doc = new Document(new Element("root"));
        doc.getRootElement().addContent(new Element("dataset").addContent(md));
        doc.getRootElement().addContent(new Element("serviceIdentifier").setText(serviceMdUuid));

        return doc.getRootElement();
    }


    public static String convertIso19119ToAtomFeed(final String schema,
                                                   final Element md,
                                                   final DataManager dataManager,
                                                   final boolean isLocal) throws Exception {

        java.nio.file.Path styleSheet = dataManager
            .getSchemaDir(schema)
            .resolve("convert/ATOM/")
            .resolve(TRANSFORM_MD_TO_ATOM_FEED);

        Map<String, Object> params = new HashMap<>();
        params.put("isLocal", isLocal);

        Element atomFeed = Xml.transform(md, styleSheet, params);
        md.detach();
        return Xml.getString(atomFeed);

    }

    /**
     * Converts a dataset MD into an INSPIRE atom feed.
     *
     * @param schema      the target schema (mainly iso19139)
     * @param md          The document on which the XSL should be applied, the following format should be followed:
     *
     *                    <root>
     *                    <dataset>
     *                    <gmd:MD_Metadata />
     *                    </dataset>
     *                    <serviceIdentifier>[Service Metadata UUID]</serviceIdentifier>
     *                    ...
     *                    </root>
     * @param dataManager
     * @param params      extra parameters to pass to the XSL transformation, see inspire-atom-feed.xsl for the details:
     *                    <xsl:param name="isLocal" select="true()" />
     *                    <xsl:param name="serviceFeedTitle" select="string('The parent service feed')" />
     * @return the ATOM feed as a JDOM element.
     * @throws Exception See InspireAtomUtilTest.testLocalDatasetTransform() for an example of calling this method.
     */
    public static Element convertDatasetMdToAtom(final String schema, final Element md, final DataManager dataManager,
                                                 Map<String, Object> params) throws Exception {

        Path styleSheet = getAtomFeedXSLStylesheet(schema, dataManager);
        return Xml.transform(md, styleSheet, params);
    }

    private static Path getAtomFeedXSLStylesheet(final String schema, final DataManager dataManager) {
        return dataManager
            .getSchemaDir(schema)
            .resolve("convert/ATOM/")
            .resolve(TRANSFORM_MD_TO_ATOM_FEED);
    }

    public static Element getDatasetFeed(final ServiceContext context, final String spIdentifier, final String spNamespace, final Map<String, Object> params, String requestedLanguage) throws Exception {

        ServiceConfig config = new ServiceConfig();
        SearchManager searchMan = context.getBean(SearchManager.class);

        // Search for the dataset identified by spIdentifier
        AbstractMetadata datasetMd = null;
        Document dsLuceneSearchParams = createDefaultLuceneSearcherParams();
        dsLuceneSearchParams.getRootElement().addContent(new Element("identifier").setText(spIdentifier));
        if (StringUtils.isNotBlank(spNamespace)) {
            dsLuceneSearchParams.getRootElement().addContent(new Element("identifierNamespace").setText(spNamespace));
        }
        dsLuceneSearchParams.getRootElement().addContent(new Element("type").setText("dataset"));

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, dsLuceneSearchParams.getRootElement(), config);
            Element searchResult = searcher.present(context, dsLuceneSearchParams.getRootElement(), config);

            XPath xp = XPath.newInstance("//response/metadata/geonet:info/uuid/text()");
            xp.addNamespace("geonet", "http://www.fao.org/geonetwork");
            if (searchResult.getContentSize() == 0) {
                if (StringUtils.isNotBlank(spNamespace)) {
                    dsLuceneSearchParams.getRootElement().removeChild("identifierNamespace");
                    searcher.search(context, dsLuceneSearchParams.getRootElement(), config);
                    searchResult = searcher.present(context, dsLuceneSearchParams.getRootElement(), config);
                }
            }
            if (searchResult.getContentSize() > 0) {
                Text uuidTxt = (Text) xp.selectSingleNode(new Document(searchResult));
                String datasetMdUuid = uuidTxt.getText();

                IMetadataUtils repo = context.getBean(IMetadataUtils.class);
                datasetMd = repo.findOneByUuid(datasetMdUuid);
            }

        } finally {
            if (datasetMd == null) {
                throw new ResourceNotFoundException(String.format(
                    "No dataset found with resource identifier '%s'. Check that a record exist with hierarchy level is set to 'dataset' with a resource identifier set to '%s'.", spIdentifier, spIdentifier));
            }
        }

        // check user's rights
        try {
            Lib.resource.checkPrivilege(context, String.valueOf(datasetMd.getId()), ReservedOperation.view);
        } catch (Exception e) {
            // This does not return a 403 as expected Oo
            throw new UnAuthorizedException("Access denied to metadata " + datasetMd.getUuid(), e);
        }

        String serviceMdUuid = null;
        Document luceneParamSearch = createDefaultLuceneSearcherParams();
        luceneParamSearch.getRootElement().addContent(new Element("operatesOn").setText(datasetMd.getUuid() + " or " + spIdentifier));
        luceneParamSearch.getRootElement().addContent(new Element("serviceType").setText("download"));

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, luceneParamSearch.getRootElement(), config);
            Element searchResult = searcher.present(context, luceneParamSearch.getRootElement(), config);

            XPath xp = XPath.newInstance("//response/metadata/geonet:info/uuid/text()");
            xp.addNamespace("geonet", "http://www.fao.org/geonetwork");

            Text uuidTxt = (Text) xp.selectSingleNode(new Document(searchResult));
            serviceMdUuid = uuidTxt.getText();
        } finally {
            if (serviceMdUuid == null) {
                throw new ResourceNotFoundException(String.format("No service operating the dataset '%s'. Check that a service is attached to that dataset and that its service type is set to 'download'.", datasetMd.getUuid()));
            }
        }

        DataManager dm = context.getBean(DataManager.class);
        Element md = datasetMd.getXmlData(false);
        if (StringUtils.isBlank(requestedLanguage)) {
            String schema = dm.getMetadataSchema(String.valueOf(datasetMd.getId()));
            String defaultLanguage = dm.extractDefaultLanguage(schema, md);
            requestedLanguage = XslUtil.twoCharLangCode(defaultLanguage);
        }
        Element inputDoc = InspireAtomUtil.prepareDatasetFeedEltBeforeTransform(md, serviceMdUuid);

        params.put("requestedLanguage", requestedLanguage);
        return InspireAtomUtil.convertDatasetMdToAtom("iso19139", inputDoc, dm, params);
    }

    private static Document createDefaultLuceneSearcherParams() {
        Document luceneParamSearch = new Document(new Element("request").
            addContent(new Element("from").setText("1")).
            addContent(new Element("to").setText("1000")).
            addContent(new Element("fast").setText("index")));

        return luceneParamSearch;
    }

    public static Element prepareOpenSearchDescriptionEltBeforeTransform(final ServiceContext context, final Map<String, Object> params, final String fileIdentifier, final String schema, final Element serviceAtomFeed, final String defaultLanguage,
                                                                         final DataManager dataManager) throws Exception {

        List<String> keywords = retrieveKeywordsFromFileIdentifier(context, fileIdentifier);
        Namespace ns = serviceAtomFeed.getNamespace();
        Document doc = new Document(new Element("root"));
        Element response = new Element("response");
        doc.getRootElement().addContent(response);
        response.addContent(new Element("fileId").setText(fileIdentifier));
        response.addContent(new Element("title").setText(serviceAtomFeed.getChildText("title", ns)));
        response.addContent(new Element("subtitle").setText(serviceAtomFeed.getChildText("subtitle", ns)));
        List<String> languages = new ArrayList<String>();
        languages.add(XslUtil.twoCharLangCode(defaultLanguage));
        Iterator<Element> linksChildren = (serviceAtomFeed.getChildren("link", ns)).iterator();
        while (linksChildren.hasNext()) {
            Element entry = linksChildren.next();
            if ("application/atom+xml".equals(entry.getAttributeValue("type"))) {
                String language = entry.getAttributeValue("hreflang");
                if (language != null && !languages.contains(language)) {
                    languages.add(language);
                }
            }
        }
        Element languagesEl = new Element("languages");
        for (String language : languages) {
            languagesEl.addContent(new Element("language").setText(language));
        }
        response.addContent(languagesEl);
        Element serviceAuthor = serviceAtomFeed.getChild("author", ns);
        if (serviceAuthor != null) {
            response.addContent(new Element("authorName").setText(serviceAuthor.getChildText("name", ns)));
            response.addContent(new Element("authorEmail").setText(serviceAuthor.getChildText("email", ns)));
        }
        response.addContent(new Element("url").setText(serviceAtomFeed.getChildText("id", ns)));
        Element datasetsEl = new Element("datasets");
        response.addContent(datasetsEl);
        Namespace inspiredlsns = serviceAtomFeed.getNamespace("inspire_dls");
        Iterator<Element> datasets = (serviceAtomFeed.getChildren("entry", ns)).iterator();
        List<String> fileTypes = new ArrayList<String>();
        while (datasets.hasNext()) {
            Element dataset = datasets.next();
            String datasetIdCode = dataset.getChildText("spatial_dataset_identifier_code", inspiredlsns);
            String datasetIdNs = dataset.getChildText("spatial_dataset_identifier_namespace", inspiredlsns);

            Element datasetAtomFeed = null;
            try {
                datasetAtomFeed = InspireAtomUtil.getDatasetFeed(context, datasetIdCode, datasetIdNs, params, XslUtil.twoCharLangCode(defaultLanguage));
            } catch (Exception e) {
                Log.error(Geonet.ATOM, "No dataset metadata found with uuid:"
                    + fileIdentifier);
                continue;
            }
            Element datasetEl = buildDatasetInfo(datasetIdCode, datasetIdNs);
            datasetsEl.addContent(datasetEl);
            Element author = datasetAtomFeed.getChild("author", ns);
            if (author != null) {
                String authorName = author.getChildText("name", ns);
                if (StringUtils.isNotBlank(authorName)) {
                    datasetEl.addContent(new Element("authorName").setText(authorName));
                }
            }
            Map<String, Integer> downloadsCountByCrs = new HashMap<String, Integer>();
            Iterator<Element> entries = (datasetAtomFeed.getChildren("entry", ns)).iterator();
            while (entries.hasNext()) {
                Element entry = entries.next();
                Element category = entry.getChild("category", ns);
                if (category != null) {
                    String term = category.getAttributeValue("term");
                    Integer count = downloadsCountByCrs.get(term);
                    if (count == null) {
                        count = new Integer(0);
                    }
                    downloadsCountByCrs.put(term, count + 1);
                }
                Element link = entry.getChild("link", ns);
                if (link != null) {
                    String fileType = link.getAttributeValue("type");
                    if (!fileTypes.contains(fileType)) {
                        fileTypes.add(fileType);
                    }
                }
            }
            entries = (datasetAtomFeed.getChildren("entry", ns)).iterator();
            while (entries.hasNext()) {
                Element entry = entries.next();
                Element category = entry.getChild("category", ns);
                if (category != null) {
                    String term = category.getAttributeValue("term");
                    Integer count = downloadsCountByCrs.get(term);
                    if (count != null) {
                        Element downloadEl = new Element("file");
                        Element link = entry.getChild("link", ns);
                        if (link != null) {
                            String title = link.getAttributeValue("title");
                            if (title != null) {
                                int iPos = title.indexOf(" in  -");
                                if (iPos > -1) {
                                    title = title.substring(0, iPos);
                                }
                            }
                            downloadEl.addContent(new Element("title").setText(title));
                        }
                        Element lang = new Element("lang");
                        if (link != null && StringUtils.isNotBlank(link.getAttributeValue("hreflang"))) {
                            lang.setText(link.getAttributeValue("hreflang"));
                        } else {
                            lang.setText(XslUtil.twoCharLangCode(context.getLanguage()));
                        }
                        downloadEl.addContent(lang);
                        downloadEl.addContent(new Element("url").setText(entry.getChildText("id", ns)));
                        if (count > 1) {
                            downloadEl.addContent(new Element("type").setText("application/atom+xml"));
                        } else {
                            if (link != null) {
                                downloadEl.addContent(new Element("type").setText(link.getAttributeValue("type")));
                            }
                        }
                        downloadEl.addContent(new Element("crs").setText(term));
                        downloadEl.addContent(new Element("crsCount").setText("" + count));
                        datasetEl.addContent(downloadEl);

                        // Remove from map to not process further downloads with same CRS,
                        // only 1 entry with type= is added in result
                        downloadsCountByCrs.remove(term);
                    }
                }
            }
        }
        response.addContent(new Element("keywords").setText(StringUtils.join(keywords, ' ')));
        Element fileTypesEl = new Element("fileTypes");
        for (String fileType : fileTypes) {
            fileTypesEl.addContent(new Element("fileType").setText(fileType));
        }
        response.addContent(fileTypesEl);
        return doc.getRootElement();
    }

    public static Element convertServiceMdToOpenSearchDescription(final ServiceContext context, final Element md, final Map<String, Object> params) throws Exception {

        Path styleSheet = getOpenSearchDesciptionXSLStylesheet(context);
        return Xml.transform(md, styleSheet, params);
    }

    private static Path getOpenSearchDesciptionXSLStylesheet(final ServiceContext context) {
        return context.getAppPath().resolve(Geonet.Path.XSLT_FOLDER)
            .resolve("services/inspire-atom/")
            .resolve(TRANSFORM_ATOM_TO_OPENSEARCHDESCRIPTION);
    }

    /**
     * Builds JDOM element for dataset information.
     *
     * @param identifier Dataset identifier.
     * @param namespace  Dataset namespace.
     */
    private static Element buildDatasetInfo(final String identifier, final String namespace) {
        Element datasetEl = new Element("dataset");

        Element codeEl = new Element("code");
        codeEl.setText(identifier);

        Element namespaceEl = new Element("namespace");
        namespaceEl.setText(namespace);

        datasetEl.addContent(codeEl);
        datasetEl.addContent(namespaceEl);

        return datasetEl;
    }

    public static List<String> retrieveKeywordsFromFileIdentifier(ServiceContext context, String fileIdentifier) {
        List<String> keywordsList = new ArrayList<String>();
        Element request = new Element(Jeeves.Elem.REQUEST);
        request.addContent(new Element("fileId").setText(fileIdentifier));
        //request.addContent(new Element("has_atom").setText("y"));
        request.addContent(new Element("fast").setText("true"));
        SearchManager searchMan = context.getBean(SearchManager.class);
        // perform the search and return the results read from the index

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, request, new ServiceConfig());

            List<Element> keywordElems = ((LuceneSearcher) searcher).getSummary().getChild("summary").getChild("keywords").getChildren();
            for (Element elem : keywordElems) {
                String keyword = elem.getAttributeValue("name");
                if (!keywordsList.contains(keyword)) {
                    keywordsList.add(keyword);
                }
            }
        } catch (Exception ex) {
            Log.error(Geonet.ATOM, ex.getMessage(), ex);
        }

        return keywordsList;
    }
}

