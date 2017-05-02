//=============================================================================
//===	Copyright (C) 2017 Food and Agriculture Organization of the
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

import java.util.HashMap;
import java.util.Map;

import javassist.NotFoundException;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.http.ServletPathFinder;

import org.apache.commons.lang.StringUtils;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.xpath.XPath;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;


/**
 * Services for converting Service and Data metadata into INSPIRE ATOM feeds
 *
 * @author fgravin on 7/15/15.
 * @author Emanuele Tajariol (etj at geo-solutions dot it)
 */

@Controller
public class AtomPredefinedFeed {

    /**
     * Main entry point for local service ATOM feed description
     *
     * @param uiLang the language parameter
     * @param uuid identifier of the metadata of service (this could be made optional once a system-wide top level metadata could be set)
     */
    @RequestMapping(value = "/{uiLang}/atom.predefined.service")
    @ResponseBody
    public HttpEntity<byte[]> localServiceDescribe(
            @PathVariable String uiLang,
            @RequestParam("uuid") String uuid,
            NativeWebRequest webRequest) throws Exception {

        ServiceContext context = createServiceContext(uiLang, webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }

        Element feed = getServiceFeed(context, uuid);
        return writeOutResponse(Xml.getString(feed));
    }

    /**
     * Main entry point for local dataset ATOM feed description.
     *
     * @param uiLang the language parameter
     * @param spIdentifier the spatial dataset identifier
     * @param spNamespace the spatial dataset namespace (not used for the moment)
     * @param webRequest the request object
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{uiLang}/atom.predefined.dataset")
    @ResponseBody
    public HttpEntity<byte[]> localDatasetDescribe(
            @PathVariable String uiLang,
            @RequestParam("spatial_dataset_identifier_code") String spIdentifier,
            @RequestParam(value = "spatial_dataset_identifier_namespace", required = false) String spNamespace,
            NativeWebRequest webRequest) throws Exception
    {
        ServiceContext context = createServiceContext(uiLang, webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }

        Element feed = getDatasetFeed(context, spIdentifier);
        return writeOutResponse(Xml.getString(feed));
    }

    private Element getDatasetFeed(ServiceContext context, final String spIdentifier) throws Exception {

        ServiceConfig config = new ServiceConfig();
        SearchManager searchMan = context.getBean(SearchManager.class);

        // Search for the dataset identified by spIdentifier
        Metadata datasetMd = null;
        Document dsLuceneSearchParams = createDefaultLuceneSearcherParams();
        dsLuceneSearchParams.getRootElement().addContent(new Element("identifier").setText(spIdentifier));
        dsLuceneSearchParams.getRootElement().addContent(new Element("type").setText("dataset"));

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE))
        {
            searcher.search(context, dsLuceneSearchParams.getRootElement(), config);
            Element searchResult = searcher.present(context, dsLuceneSearchParams.getRootElement(), config);

            XPath xp = XPath.newInstance("//response/metadata/geonet:info/uuid/text()");
            xp.addNamespace("geonet", "http://www.fao.org/geonetwork");

            Text uuidTxt = (Text) xp.selectSingleNode(new Document(searchResult));
            String datasetMdUuid = uuidTxt.getText();

            MetadataRepository repo = context.getBean(MetadataRepository.class);
            datasetMd = repo.findOneByUuid(datasetMdUuid);

        } finally {
            if (datasetMd == null) {
                throw new ObjectNotFoundEx("metadata " + spIdentifier + " not found");
            }
        }

        // check user's rights
        try {
            Lib.resource.checkPrivilege(context, String.valueOf(datasetMd.getId()), ReservedOperation.view);
        } catch (Exception e) {
            // This does not return a 403 as expected Oo
            throw new UnAuthorizedException("Access denied to metadata " +datasetMd.getUuid(), e);
        }

        String serviceMdUuid = null;
        Document luceneParamSearch = createDefaultLuceneSearcherParams();
        luceneParamSearch.getRootElement().addContent(new Element("operatesOn").setText(datasetMd.getUuid()));

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            searcher.search(context, luceneParamSearch.getRootElement(), config);
            Element searchResult = searcher.present(context, luceneParamSearch.getRootElement(), config);

            XPath xp = XPath.newInstance("//response/metadata/geonet:info/uuid/text()");
            xp.addNamespace("geonet", "http://www.fao.org/geonetwork");

            Text uuidTxt = (Text) xp.selectSingleNode(new Document(searchResult));
            serviceMdUuid = uuidTxt.getText();
        } finally {
            if (serviceMdUuid == null) {
                throw new ObjectNotFoundEx("No related service metadata found");
            }
        }

        DataManager dm = context.getBean(DataManager.class);
        SettingManager sm = context.getBean(SettingManager.class);

        Map<String, Object> params = getDefaultXSLParams(sm, context);

        Element inputDoc = InspireAtomUtil.prepareDatasetFeedEltBeforeTransform(datasetMd.getXmlData(false), serviceMdUuid);

        Element transformed = InspireAtomUtil.convertDatasetMdToAtom("iso19139", inputDoc, dm, params);
        return transformed;
    }


    private Element getServiceFeed(ServiceContext context, final String uuid) throws Exception {

        Log.debug(Geonet.ATOM, "Processing service feed  ( uuid : " + uuid + " )");

        SettingManager sm = context.getBean(SettingManager.class);
        DataManager dm = context.getBean(DataManager.class);

        // Check if metadata exists
        String id = dm.getMetadataId(uuid);
        if (StringUtils.isEmpty(id))
            throw new MetadataNotFoundEx(uuid);

        // check user's rights
        try {
            Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
        } catch (Exception e) {
            throw new UnAuthorizedException("Access denied to metadata " + id, e);
        }

        // Check if it is a service metadata
        Element md = dm.getMetadata(id);
        String schema = dm.getMetadataSchema(id);
        if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
            throw new NotFoundException("No service metadata found with uuid:" + uuid);
        }

        Element inputDoc = InspireAtomUtil.prepareServiceFeedEltBeforeTransform(schema, md, dm);

        Map<String, Object> params = getDefaultXSLParams(sm, context);
        Element transformed = InspireAtomUtil.convertDatasetMdToAtom("iso19139", inputDoc, dm, params);
        return transformed;
    }

    private Map<String, Object> getDefaultXSLParams(SettingManager settingManager, ServiceContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put("isLocal", true);
        params.put("guiLang", context.getLanguage());
        params.put("baseUrl", getBaseURL(settingManager, context));
        params.put("nodeName", ApplicationContextHolder.get().getBean(NodeInfo.class).getId());

        return params;
    }

    private String getBaseURL(SettingManager settingManager, ServiceContext context) {

        String baseURL = new ServletPathFinder(context.getServlet().getServletContext()).getBaseUrl();
        String protocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        String host    = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
        String port    = settingManager.getValue(Settings.SYSTEM_SERVER_PORT);

        return protocol + "://" + host + (port.equals("80") ? "" : ":" + port + baseURL);
    }

    private ServiceContext createServiceContext(String lang, HttpServletRequest request) {
        final ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        return serviceManager.createServiceContext("atom.service", lang, request);
    }

    private Document createDefaultLuceneSearcherParams() {
        Document luceneParamSearch = new Document(new Element("request").
                addContent(new Element("from").setText("1")).
                addContent(new Element("to").setText("1000")).
                addContent(new Element("fast").setText("index")));

        return luceneParamSearch;
    }

    private HttpEntity<byte[]> writeOutResponse(String content) throws Exception {
        byte[] documentBody = content.getBytes();

        HttpHeaders header = new HttpHeaders();
        // TODO: character-set encoding ?
        header.setContentType(new MediaType("application", "xml"));
        header.setContentLength(documentBody.length);
        return new HttpEntity<>(documentBody, header);
    }

}
