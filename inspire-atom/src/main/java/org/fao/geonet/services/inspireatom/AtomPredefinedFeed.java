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

import io.swagger.v3.oas.annotations.Hidden;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Services for converting Service and Data metadata into INSPIRE ATOM feeds
 *
 * @author fgravin on 7/15/15.
 * @author Emanuele Tajariol (etj at geo-solutions dot it)
 */

@Controller
@RequestMapping(value = "/{portal}")
@Hidden
public class AtomPredefinedFeed {

    /**
     * Main entry point for local service ATOM feed description
     *
     * @param language the language to be used for translation of title, etc. in the resulting service ATOM feed
     * @param uuid identifier of the metadata of service (this could be made optional once a system-wide top level metadata could be set)
     */
    @RequestMapping(value = "/" + InspireAtomUtil.LOCAL_DESCRIBE_SERVICE_URL_SUFFIX)
    @ResponseBody
    public HttpEntity<byte[]> localServiceDescribe(
            @RequestParam("uuid") String uuid,
            @RequestParam(value = "language", required = false) String language,
            NativeWebRequest webRequest) throws Exception {

        ServiceContext context = createServiceContext(Geonet.DEFAULT_LANGUAGE, webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }

        Element feed = getServiceFeed(context, uuid, language);
        return writeOutResponse(Xml.getString(feed),"application", "atom+xml");
    }

    /**
     * Main entry point for local dataset ATOM feed description.
     *
     * @param spIdentifier the spatial dataset identifier
     * @param spNamespace the spatial dataset namespace (not used for the moment)
     * @param language the language to be used for translation of title, etc. in the resulting dataset ATOM feed
     * @param searchTerms the searchTerms for filtering of the spatial datasets
     * @param webRequest the request object
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/" + InspireAtomUtil.LOCAL_DESCRIBE_DATASET_URL_SUFFIX)
    @ResponseBody
    public HttpEntity<byte[]> localDatasetDescribe(
            @RequestParam("spatial_dataset_identifier_code") String spIdentifier,
            @RequestParam(value = "spatial_dataset_identifier_namespace", required = false) String spNamespace,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "q", required = false) String searchTerms,
            NativeWebRequest webRequest) throws Exception
    {
        ServiceContext context = createServiceContext("eng", webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }


        Map<String, Object> params = getDefaultXSLParams(sm, context, XslUtil.twoCharLangCode(context.getLanguage()));
        if (StringUtils.isNotBlank(searchTerms)) {
            params.put("searchTerms", searchTerms.toLowerCase());
        }
        Element feed = InspireAtomUtil.getMetadataFeedByResourceIdentifier(context, spIdentifier, spNamespace, params, language);
        return writeOutResponse(Xml.getString(feed), "application", "atom+xml");
    }

    private Element getServiceFeed(ServiceContext context, final String uuid, final String language) throws Exception {

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
            throw new ResourceNotFoundException("No service metadata found with uuid:" + uuid);
        }

        String defaultLanguage = dm.extractDefaultLanguage(schema, md);
        String requestedLanguage = StringUtils.isNotBlank(language) ? language : XslUtil.twoCharLangCode(defaultLanguage);
        Element inputDoc = InspireAtomUtil.prepareServiceFeedEltBeforeTransform(schema, md, dm);

        Map<String, Object> params = getDefaultXSLParams(sm, context, requestedLanguage);
        return InspireAtomUtil.convertDatasetMdToAtom("iso19139", inputDoc, dm, params);
    }

    private Map<String, Object> getDefaultXSLParams(SettingManager settingManager, ServiceContext context, String requestedLanguage) {
        Map<String, Object> params = new HashMap<>();
        params.put("isLocal", true);
        params.put("inspire", context.getBean(SettingManager.class).getValue(Settings.SYSTEM_INSPIRE_ENABLE));
        params.put("thesauriDir", context.getApplicationContext().getBean(GeonetworkDataDirectory.class).getThesauriDir().toAbsolutePath().toString());
        params.put("requestedLanguage", requestedLanguage);
        params.put("baseUrl", settingManager.getBaseURL());
        params.put("nodeUrl", settingManager.getNodeURL());
        params.put("opensearchUrlSuffix", InspireAtomUtil.LOCAL_OPENSEARCH_URL_SUFFIX);
        params.put("atomDescribeServiceUrlSuffix", InspireAtomUtil.LOCAL_DESCRIBE_SERVICE_URL_SUFFIX);
        params.put("atomDescribeDatasetUrlSuffix", InspireAtomUtil.LOCAL_DESCRIBE_DATASET_URL_SUFFIX);
        params.put("atomDownloadDatasetUrlSuffix", InspireAtomUtil.LOCAL_DOWNLOAD_DATASET_URL_SUFFIX);
        params.put("nodeName", ApplicationContextHolder.get().getBean(NodeInfo.class).getId());
        return params;
    }

    private ServiceContext createServiceContext(String lang, HttpServletRequest request) {
        final ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        return serviceManager.createServiceContext("atom.service", lang, request);
    }

    private HttpEntity<byte[]> writeOutResponse(String content, String contentType, String contentSubType) throws Exception {
        byte[] documentBody = content.getBytes(Constants.ENCODING);

        HttpHeaders header = new HttpHeaders();
        // TODO: character-set encoding ?
        header.setContentType(new MediaType(contentType, contentSubType, Charset.forName(Constants.ENCODING)));
        header.setContentLength(documentBody.length);
        return new HttpEntity<>(documentBody, header);
    }

    /**
     * Main entry point for local dataset ATOM feed download.
     *
     * @param spIdentifier the spatial dataset identifier
     * @param spNamespace the spatial dataset namespace (not used for the moment)
     * @param crs the crs of the dataset
     * @param language the language to be used for translation of title, etc. in the resulting dataset ATOM feed
     * @param searchTerms the searchTerms for filtering of the spatial datasets
     * @param webRequest the request object
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/" + InspireAtomUtil.LOCAL_DOWNLOAD_DATASET_URL_SUFFIX)
    @ResponseBody
    public HttpEntity<byte[]> localDatasetDownload(
            @RequestParam("spatial_dataset_identifier_code") String spIdentifier,
            @RequestParam(value = "spatial_dataset_identifier_namespace", required = false) String spNamespace,
            @RequestParam(value = "crs", required = false) String crs,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "q", required = false) String searchTerms,
            NativeWebRequest webRequest) throws Exception
    {
        ServiceContext context = createServiceContext(Geonet.DEFAULT_LANGUAGE, webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }

        Map<String, Object> params = getDefaultXSLParams(sm, context, context.getLanguage());
        if (StringUtils.isNotBlank(crs)) {
        	crs = URLDecoder.decode(crs,Constants.ENCODING);
            params.put("requestedCrs", crs);
        }
        if (StringUtils.isNotBlank(searchTerms)) {
            params.put("searchTerms", searchTerms.toLowerCase());
        }
        Element feed = InspireAtomUtil.getMetadataFeedByResourceIdentifier(context, spIdentifier, spNamespace, params, language);
        Map<Integer, Element> crsCounts = new HashMap<Integer, Element>();;
        Namespace ns = Namespace.getNamespace("http://www.w3.org/2005/Atom");
        if (crs!=null) {
            crsCounts = countDatasetsForCrs(feed, crs, ns);
        } else {
            List<Element> entries = (feed.getChildren("entry", ns));
            if (entries.size()==1) {
                crsCounts.put(1, entries.get(0));
            }
        }
        int downloadCount = crsCounts.size()>0 ? crsCounts.keySet().iterator().next() : 0;
        Element selectedEntry = crsCounts.get(downloadCount);

        // No download  for the CRS specified
        if (downloadCount == 0) {
            throw new Exception("No downloads available for dataset (spatial_dataset_identifier_code: " + spIdentifier + ", spatial_dataset_identifier_namespace: " + spNamespace + ", crs: " + crs + ", searchTerms: " + searchTerms + ")");

        // Only one download for the CRS specified
        } else if (downloadCount == 1) {
            String type = null;
            Element link = selectedEntry.getChild("link", ns);
            if (link!=null) {
                type = link.getAttributeValue("type");
            }
            HttpServletResponse nativeRes = webRequest.getNativeResponse(HttpServletResponse.class);
            nativeRes.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
//            nativeRes.setHeader("Location", selectedEntry.getChildText("id",ns));
            return redirectResponse(selectedEntry.getChildText("id",ns));
        // Otherwise, return a feed with the downloads for the specified CRS
        } else {
            // Filter the dataset feed by CRS code.
            InspireAtomUtil.filterDatasetFeedByCrs(feed, crs);
            return writeOutResponse(Xml.getString(feed),"application", "atom+xml");
        }
    }

    private HttpEntity<byte[]> redirectResponse(String location) throws Exception {
        HttpHeaders header = new HttpHeaders();
        // TODO: character-set encoding ?
        header.setContentType(new MediaType("text", "plain", Charset.forName(Constants.ENCODING)));
        header.setContentLength(0);
        header.setLocation(new URI(location));
        return new HttpEntity<>(header);
    }

    private Map<Integer,Element> countDatasetsForCrs(Element datasetAtomFeed, String datasetCrs, Namespace ns) {
        int downloadCount = 0;
        Map<Integer,Element> entryMap = new HashMap<Integer, Element>();
        Element selectedEntry = null;
        Iterator<Element> entries = (datasetAtomFeed.getChildren("entry", ns)).iterator();
        while(entries.hasNext()) {
            Element entry = entries.next();
            Element category = entry.getChild("category",ns);
            if (category!=null) {
                String term = category.getAttributeValue("term");
                if (datasetCrs.equals(term)) {
                    selectedEntry = entry;
                    downloadCount++;
                }
            }
        }
        entryMap.put(downloadCount, selectedEntry);
        return entryMap;
    }

    /**
     * Main entry point for local open search description
     *
     * @param language the language to be used for translation of title, etc. in the resulting opensearchdescription
     * @param uuid identifier of the metadata of service (this could be made optional once a system-wide top level metadata could be set)
     */
    @RequestMapping(value = "/" + InspireAtomUtil.LOCAL_OPENSEARCH_URL_SUFFIX + "/" + InspireAtomUtil.LOCAL_OPENSEARCH_DESCRIPTION_FILE_NAME)
    @ResponseBody
    public HttpEntity<byte[]> localOpenSearchDescription(
            @RequestParam("uuid") String uuid,
            @RequestParam(value = "language", required = false) String language,
            NativeWebRequest webRequest) throws Exception {

        ServiceContext context = createServiceContext(Geonet.DEFAULT_LANGUAGE, webRequest.getNativeRequest(HttpServletRequest.class));

        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }

        Element description = getOpenSearchDescription(context, uuid);
        return writeOutResponse(Xml.getString(description), "application", "opensearchdescription+xml");
    }

    private Element getOpenSearchDescription(ServiceContext context, final String uuid) throws Exception {

        Log.debug(Geonet.ATOM, "Processing openseachdescription  ( uuid : " + uuid + " )");

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
            throw new ResourceNotFoundException("No service metadata found with uuid:" + uuid);
        }

        String defaultLanguage = dm.extractDefaultLanguage(schema, md);
        Map<String, Object> params = getDefaultXSLParams(sm, context, XslUtil.twoCharLangCode(defaultLanguage));

        Element inputDoc = InspireAtomUtil.prepareOpenSearchDescriptionEltBeforeTransform(context, params, uuid,
            InspireAtomUtil.convertDatasetMdToAtom("iso19139",
                InspireAtomUtil.prepareServiceFeedEltBeforeTransform(schema, md, dm), dm, params), defaultLanguage);

        return InspireAtomUtil.convertServiceMdToOpenSearchDescription(context, inputDoc, params);
    }

}
