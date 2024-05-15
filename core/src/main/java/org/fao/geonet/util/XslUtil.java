/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import com.neovisionaries.i18n.LanguageCode;
import jeeves.component.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.attachments.FilesystemStore;
import org.fao.geonet.api.records.attachments.FilesystemStoreResourceContainer;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.kernel.search.CodeListTranslator;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.Translator;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.url.UrlChecker;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UiSettingsRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.xsd.Parser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jsoup.Jsoup;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.owasp.esapi.errors.EncodingException;
import org.owasp.esapi.reference.DefaultEncoder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.kernel.setting.Settings.*;
import static org.fao.geonet.utils.Xml.getXmlFromJSON;



/**
 * These are all extension methods for calling from xsl docs.  Note:  All params are objects because
 * it is hard to determine what is passed in from XSLT. Most are converted to string by calling
 * tostring.
 *
 * @author jesse
 */
public final class XslUtil {
    public static MultiPolygon parseGml(Parser parser, String gml) throws IOException, SAXException,
        ParserConfigurationException {
        Object value = parser.parse(new StringReader(gml));
        if (value instanceof HashMap) {
            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;
            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                addToList(geoms, entry);
            }
            if (geoms.isEmpty()) {
                return null;
            } else if (geoms.size() > 1) {
                GeometryFactory factory = geoms.get(0).getFactory();
                return factory.createMultiPolygon(geoms.toArray(new Polygon[0]));
            } else {
                return toMultiPolygon(geoms.get(0));
            }

        } else if (value == null) {
            return null;
        } else {
            return toMultiPolygon((Geometry) value);
        }
    }

    public static String gmlToGeoJson(String gml,
                                      Boolean applyPrecisionModel,
                                      Integer numberOfDecimals) {
        if (applyPrecisionModel == null) {
            applyPrecisionModel = true;
        }
        if (numberOfDecimals == null) {
            numberOfDecimals = 5;
        }

        try {
            if (StringUtils.isNotEmpty(gml)) {
                Element geomElement = Xml.loadString(gml, false);
                Parser parser = GMLParsers.create(geomElement);
                Geometry geom = parseGml(parser, gml);

                if (geom == null) {
                    return "Warning: GML geometry is null.";
                }

                Object userData = geom.getUserData();
                if (userData instanceof DefaultProjectedCRS) {
                    geom = JTS.transform(
                        geom,
                        CRS.findMathTransform((DefaultProjectedCRS) userData,
                            CRS.decode("EPSG:4326", true), true)
                    );
                }
                else if (userData instanceof DefaultGeographicCRS) {
                    geom = JTS.transform(
                        geom,
                        CRS.findMathTransform((DefaultGeographicCRS) userData,
                            CRS.decode("EPSG:4326", true), true)
                    );
                }

                if (!geom.isValid()) {
                    IsValidOp isValidOp = new IsValidOp(geom);
                    return String.format(
                        "Warning: GML geometry is not valid. %s",
                        isValidOp.getValidationError().toString());
                }

                Geometry reducedGeom = null;
                // An issue here is that GeometryJSON conversion may over simplify
                // the geometry by truncating coordinates based on numberOfDecimals
                // which on default constructor is set to 4. This may lead to
                // invalid geometry and Elasticsearch will fail parsing the GeoJSON
                // with the following type of error:
                // Caused by: org.locationtech.spatial4j.exception.InvalidShapeException:
                // Provided shape has duplicate
                // consecutive coordinates at: (-3.9997, 48.7463, NaN)
                //
                // To avoid this, it may be relevant to apply the reduction model
                // preserving topology.
                if (applyPrecisionModel) {
                    PrecisionModel precisionModel =
                        new PrecisionModel(Math.pow(10, numberOfDecimals - 1));
                    reducedGeom = GeometryPrecisionReducer.reduce(geom, precisionModel);

                    if (reducedGeom.isEmpty()) {
                        int numberOfDecimalsForSmallGeom = 10;

                        precisionModel =
                            new PrecisionModel(Math.pow(10, numberOfDecimalsForSmallGeom - 1));
                        reducedGeom = GeometryPrecisionReducer.reduce(geom, precisionModel);
                        return new GeometryJSON(numberOfDecimalsForSmallGeom).toString(reducedGeom);
//                    return String.format(
//                        "Warning: Empty geometry after applying precision reducer with %d decimals.",
//                        numberOfDecimals);
                    }
                }
                return new GeometryJSON(numberOfDecimals).toString(reducedGeom);
            }
        } catch (Exception e) {
            return String.format("Error: %s, %s parsing %s to GeoJSON",
                e.getClass().getSimpleName(), e.getMessage(), gml);
        }
        return "";
    }



    public static void addToList(List<Polygon> geoms, Object entry) {
        if (entry instanceof Polygon) {
            geoms.add((Polygon) entry);
        } else if (entry instanceof Collection) {
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) entry;
            for (Object object : collection) {
                geoms.add((Polygon) object);
            }
        }
    }

    public static MultiPolygon toMultiPolygon(Geometry geometry) {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;

            return geometry.getFactory().createMultiPolygon(
                new Polygon[]{polygon});
        } else if (geometry instanceof MultiPolygon) {
            return (MultiPolygon) geometry;
        }
        String message = geometry.getClass() + " cannot be converted to a polygon. Check Metadata";
        Log.error(Geonet.INDEX_ENGINE, message);
        throw new IllegalArgumentException(message);
    }

    private static final char TS_DEFAULT = ' ';
    private static final char CS_DEFAULT = ',';
    private static final char TS_WKT = ',';
    private static final char CS_WKT = ' ';
    private static ThreadLocal<Boolean> allowScripting = new InheritableThreadLocal<Boolean>();

    /**
     * clean the src of ' and <>
     */
    public static String clean(Object src) {
        String result = src.toString().replaceAll("'", "\'").replaceAll("[><\n\r]", " ");
        return result;
    }

    /**
     * Returns 'true' if the pattern matches the src
     */
    public static String countryMatch(Object src, Object pattern) {
        if (src.toString().trim().length() == 0) {
            return "false";
        }
        boolean result = src.toString().toLowerCase().contains(pattern.toString().toLowerCase());
        return String.valueOf(result);
    }

    /**
     * Replace the pattern with the substitution
     */
    public static String replace(Object src, Object pattern, Object substitution) {
        String result = src.toString().replaceAll(pattern.toString(), substitution.toString());
        return result;
    }

    public static boolean isCasEnabled() {
        return ProfileManager.isCasEnabled();
    }

    /**
     * Return a service handler config parameter
     *
     * @see org.fao.geonet.constants.Geonet.Config
     */
    public static String getConfigValue(String key) {
        if (key == null) {
            return "";
        }

        ServiceConfig config = ApplicationContextHolder.get().getBean(ServiceConfig.class);
        if (config != null) {
            String value = config.getValue(key);
            if (value != null) {
                return value;
            } else {
                return "";
            }
        }
        return "";
    }

    public static String getBuildNumber() {
        return ApplicationContextHolder.get().getBean(SystemInfo.class).getScmRevision();
    }

    /**
     * Get the UI configuration. UI configuration can be defined
     * at portal level (see Source table) or as a UI (see settings_iu table).
     *
     *
     * @param key Optional key, if null,
     *            check the portal UI config and if null,
     *            return a default configuration named 'srv' if exist.
     *            If not, empty config is returned.
     *
     * @return Return the JSON config as string or an empty object.
     */
    public static String getUiConfiguration(String key) {
        String nodeId = org.fao.geonet.NodeInfo.DEFAULT_NODE;
        try {
            org.fao.geonet.NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(org.fao.geonet.NodeInfo.class);
            nodeId = nodeInfo.getId();
        } catch (BeanCreationException e) {
        }
        SourceRepository sourceRepository= ApplicationContextHolder.get().getBean(SourceRepository.class);
        UiSettingsRepository uiSettingsRepository = ApplicationContextHolder.get().getBean(UiSettingsRepository.class);

        Optional<org.fao.geonet.domain.Source> portalOpt = sourceRepository.findById(nodeId);
        org.fao.geonet.domain.Source portal = null;
        if (portalOpt.isPresent()) {
            portal = portalOpt.get();
        }

        if (uiSettingsRepository != null) {
            Optional<UiSetting> oneOpt = null;
            UiSetting one = null;
            if (portal != null && StringUtils.isNotEmpty(portal.getUiConfig())) {
                oneOpt = uiSettingsRepository.findById(portal.getUiConfig());
            }
            else if (StringUtils.isNotEmpty(key)) {
                oneOpt = uiSettingsRepository.findById(key);
            }
            else if (oneOpt == null) {
                oneOpt = uiSettingsRepository.findById(org.fao.geonet.NodeInfo.DEFAULT_NODE);
            }

            if (oneOpt.isPresent()) {
                return oneOpt.get().getConfiguration();
            } else {
                return "{}";
            }
        }
        return "{}";
    }

    /**
     * Get a precise value in the JSON UI configuration
     *
     * @param key
     * @param path JSON path to the property
     * @return
     */
    public static String getUiConfigurationJsonProperty(String key, String path) {
        String json = getUiConfiguration(key);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Object jsonObj = objectMapper.readValue(json, Object.class);

            Object value = PropertyUtils.getProperty(jsonObj, path);
            if (value != null) {
                return value.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get a setting value
     */
    public static String getSettingValue(String key) {
        if (key == null) {
            return "";
        }

        SettingManager settingsMan = ApplicationContextHolder.get().getBean(SettingManager.class);
        if (settingsMan != null) {
            String value;
            if ("nodeUrl".equals(key)) {
                value = settingsMan.getNodeURL();
            } else {
                value = settingsMan.getValue(key);
            }
            if (value != null) {
                return value;
            } else {
                return "";
            }
        }

        return "";
    }

    /**
     * Return the name of the current catalogue.
     * If the main one, then get the name on the source table with the site id.
     * If a sub portal, use the sub portal key.
     *
     * @param key   Sub portal key or UUID
     * @return
     */
    public static String getNodeName(String key, String lang, boolean withOrganization) {
        SettingManager settingsMan = ApplicationContextHolder.get().getBean(SettingManager.class);
        Optional<Source> source = getSource(key);
        return source.isPresent() ? source.get().getLabel(lang) : settingsMan.getSiteName()
            + (withOrganization ? " - " + settingsMan.getValue(SYSTEM_SITE_ORGANIZATION) : "");
    }


    /**
     * Return the ID of the current node (catalog or subportal).
     * If the main one, then srv.
     * If a sub portal, use the sub portal key.
     *
     * @return
     */
    public static String getNodeId() {
        return ApplicationContextHolder.get().getBean(org.fao.geonet.NodeInfo.class).getId();
    }


    public static String getNodeLogo(String key) {
        Optional<Source> source = getSource(key);
        return source.isPresent() ? source.get().getLogo() : "";
    }

    public static String getDiscoveryServiceUuid(String key) {
        Optional<Source> source = getSource(key);
        if (source.isPresent() && source.get().getType() == SourceType.subportal) {
            return source.get().getServiceRecord();
        } else {
            SettingManager settingsMan = ApplicationContextHolder.get().getBean(SettingManager.class);
            String uuid = settingsMan.getValue(SYSTEM_CSW_CAPABILITY_RECORD_UUID);
            return "-1".equals(uuid) ? "" : uuid;
        }
    }

    private static Optional<Source> getSource(String idOrUuid) {
        SettingManager settingsMan = ApplicationContextHolder.get().getBean(SettingManager.class);
        if (StringUtils.isEmpty(idOrUuid)) {
            idOrUuid =  ApplicationContextHolder.get().getBean(org.fao.geonet.NodeInfo.class).getId();
        }
        if (org.fao.geonet.NodeInfo.DEFAULT_NODE.equals(idOrUuid)) {
            idOrUuid = settingsMan.getSiteId();
        }
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        Optional<Source> source = sourceRepository.findById(idOrUuid);

        if (!source.isPresent()) {
            source = Optional.ofNullable(sourceRepository.findOneByUuid(idOrUuid));
        }
        return source;
    }


    public static String getJsonSettingValue(String key, String path) {
        if (key == null) {
            return "";
        }
        try {
            final ServiceContext serviceContext = ServiceContext.get();
            if (serviceContext != null) {
                SettingManager settingsMan = serviceContext.getBean(SettingManager.class);
                if (settingsMan != null) {
                    String json = settingsMan.getValue(key);

                    if (StringUtils.isEmpty(json)) return "";

                    ObjectMapper objectMapper = new ObjectMapper();
                    Object jsonObj = objectMapper.readValue(json, Object.class);

                    Object value = PropertyUtils.getProperty(jsonObj, path);
                    if (value != null) {
                        return value.toString();
                    } else {
                        return "";
                    }
                }
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,"XslUtil getJsonSettingValue '" + key + "' error: " + e.getMessage(), e);
        }
        return "";
    }


    public static Node downloadJsonAsXML(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        try {
            final HttpResponse httpResponse = client.execute(httpGet);
            final String jsonResponse = IOUtils.toString(
                httpResponse.getEntity().getContent(),
                String.valueOf(StandardCharsets.UTF_8)).trim();
            Element element = getXmlFromJSON(jsonResponse);
            DOMOutputter outputter = new DOMOutputter();
            return outputter.output(new Document(element));
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if user is authenticated.
     */
    public static boolean isAuthenticated() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }


    /**
	 * Check if security provider require login form
	 */
	public static boolean isDisableLoginForm() {
        SecurityProviderConfiguration securityProviderConfiguration = SecurityProviderConfiguration.get();

        if (securityProviderConfiguration != null) {
            // No login form if providing a link or autologin
            return securityProviderConfiguration.getLoginType().equals(SecurityProviderConfiguration.LoginType.AUTOLOGIN.toString().toLowerCase())
                || securityProviderConfiguration.getLoginType().equals(SecurityProviderConfiguration.LoginType.LINK.toString().toLowerCase());
        }
        // If we cannot find SecurityProviderConfiguration then default to false.
        return false;
	}

    /**
     * Check if security provider require login link
     */
    public static boolean isShowLoginAsLink() {
        SecurityProviderConfiguration securityProviderConfiguration = SecurityProviderConfiguration.get();

        if (securityProviderConfiguration != null) {
            return securityProviderConfiguration.getLoginType().equals(SecurityProviderConfiguration.LoginType.LINK.toString().toLowerCase());
        }
        // If we cannot find SecurityProviderConfiguration then default to false.
        return false;
    }

    /**
     * Check if user profile update is enabled.
     */
    public static boolean isUserProfileUpdateEnabled() {
        SecurityProviderConfiguration securityProviderConfiguration = SecurityProviderConfiguration.get();

        if (securityProviderConfiguration != null) {
            return securityProviderConfiguration.isUserProfileUpdateEnabled();
        }
        return true;
    }

    /**
     * Check if user group update is enabled.
     */
    public static boolean isUserGroupUpdateEnabled() {
        SecurityProviderConfiguration securityProviderConfiguration = SecurityProviderConfiguration.get();

        if (securityProviderConfiguration != null) {
            return securityProviderConfiguration.isUserGroupUpdateEnabled();
        }
        return true;
    }

    /**
     * get security provider
     */
    public static String getSecurityProvider() {
        SecurityProviderConfiguration securityProviderConfiguration = SecurityProviderConfiguration.get();

        if (securityProviderConfiguration != null) {
            return securityProviderConfiguration.getSecurityProvider();
        }
        // If we cannot find SecurityProviderConfiguration then default to empty string.
        return "";
    }

    /**
     * get external manager url for resource.
     *
     * @param metadataUuid uuid of the record
     * @param approved is metadata approved
     * @return url to access the resource. Or null if not supported
     */
    public static MetadataResourceContainer getResourceContainerDescription(String metadataUuid, Boolean approved) throws Exception {
        Store store = BeanFactoryAnnotationUtils.qualifiedBeanOfType(ApplicationContextHolder.get().getBeanFactory(), Store.class, "filesystemStore");

        if (store != null) {
            if (store.getResourceManagementExternalProperties() != null && store.getResourceManagementExternalProperties().isFolderEnabled()) {
                ServiceContext context = ServiceContext.get();
                return store.getResourceContainerDescription(ServiceContext.get(), metadataUuid, approved);
            } else {
                // Return an empty object which should not be used because the folder is not enabled.
                return new FilesystemStoreResourceContainer(metadataUuid, -1, null, null, null, approved);
            }
        }
        Log.error(Geonet.RESOURCES, "Could not locate a Store bean in getResourceContainerDescription");
        return null;
    }

    /**
     * get resource management external properties.
     *
     * @return the windows parameters to be used.
     */
    public static Store.ResourceManagementExternalProperties getResourceManagementExternalProperties() {
        Store store = BeanFactoryAnnotationUtils.qualifiedBeanOfType(ApplicationContextHolder.get().getBeanFactory(), Store.class, "filesystemStore");
        if (store != null) {
            return store.getResourceManagementExternalProperties();
        }
        Log.error(Geonet.RESOURCES,"Could not locate a Store bean in getResourceManagementExternalProperties");
        return null;
    }

    /**
     * Optimistically check if user can access a given url.  If not possible to determine then the
     * methods will return true.  So only use to show url links, not check if a user has access for
     * certain.  Spring security should ensure that users cannot access restricted urls though.
     *
     * @param serviceName the raw services name (main.home) or (admin)
     * @return true if accessible or system is unable to determine because the current thread does
     * not have a ServiceContext in its thread local store
     */
    public static boolean isAccessibleService(Object serviceName) {
        return ProfileManager.isAccessibleService(serviceName);
    }

    /**
     * Takes the characters until the pattern is matched
     */
    public static String takeUntil(Object src, Object pattern) {
        String src2 = src.toString();
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(src2);

        if (!matcher.find())
            return src2;

        int index = matcher.start();

        if (index == -1) {
            return src2;
        }
        return src2.substring(0, index);
    }

    /**
     * Convert a serialized XML node in JSON
     */
    public static String xmlToJson(Object xml) {
        try {
            return Xml.getJSON(xml.toString());
        } catch (IOException e) {
            Log.error(Geonet.GEONETWORK, "XMLtoJSON conversion I/O error. Error is " + e.getMessage() + ". XML is " + xml.toString());
        }
        return "";
    }

    /**
     * Try to preserve some HTML layout to text layout.
     *
     * Replace br tag by new line, li by new line with leading *.
     */
    public static String htmlElement2textReplacer(String html) {
        return html
            .replaceAll("<br */?>", System.getProperty("line.separator"))
            .replaceAll("<li>(.*)</li>", System.getProperty("line.separator") + "* $1");
    }
    public static String html2text(String html) {
        return Jsoup.parse(html).wholeText();
    }
    public static String html2text(String html, boolean substituteHtmlToTextLayoutElement) {
        return html2text(
            substituteHtmlToTextLayoutElement ? htmlElement2textReplacer(html) : html);
    }
    public static String html2textNormalized(String html) {
        return Jsoup.parse(html).text();
    }

    /**
     * Converts the seperators of the coords to the WKT from ts and cs
     *
     * @param coords the coords string to convert
     * @param ts     the separator that separates 2 coordinates
     * @param cs     the separator between 2 numbers in a coordinate
     */
    public static String toWktCoords(Object coords, Object ts, Object cs) {
        String coordsString = coords.toString();
        char tsString;
        if (ts == null || ts.toString().length() == 0) {
            tsString = TS_DEFAULT;
        } else {
            tsString = ts.toString().charAt(0);
        }
        char csString;
        if (cs == null || cs.toString().length() == 0) {
            csString = CS_DEFAULT;
        } else {
            csString = cs.toString().charAt(0);
        }

        if (tsString == TS_WKT && csString == CS_WKT) {
            return coordsString;
        }

        if (tsString == CS_WKT) {
            tsString = ';';
            coordsString = coordsString.replace(CS_WKT, tsString);
        }
        coordsString = coordsString.replace(csString, CS_WKT);
        String result = coordsString.replace(tsString, TS_WKT);
        char lastChar = result.charAt(result.length() - 1);
        if (result.charAt(result.length() - 1) == TS_WKT || lastChar == CS_WKT) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String posListToWktCoords(Object coords, Object dim) {
        String[] coordsString = coords.toString().split(" ");

        int dimension;
        if (dim == null) {
            dimension = 2;
        } else {
            try {
                dimension = Integer.parseInt(dim.toString());
            } catch (NumberFormatException e) {
                dimension = 2;
            }
        }
        StringBuilder results = new StringBuilder();

        for (int i = 0; i < coordsString.length; i++) {
            if (i > 0 && i % dimension == 0) {
                results.append(',');
            } else if (i > 0) {
                results.append(' ');
            }
            results.append(coordsString[i]);
        }

        return results.toString();
    }

    public static String wktGeomToBbox(Object WKT) throws Exception {
        String ret = "";
        try {
            String wktString = (String) WKT;
            if (wktString != null && wktString.length() > 0) {
                WKTReader reader = new WKTReader();
                Geometry geometry = reader.read(wktString);
                if (geometry != null) {
                    final Envelope envelope = geometry.getEnvelopeInternal();
                    // Use Locale.US to make Java use dot "." as decimal separator
                    return
                        String.format(Locale.US, "%f|%f|%f|%f",
                            envelope.getMinX(), envelope.getMinY(),
                            envelope.getMaxX(), envelope.getMaxY());
                }
            }
        } catch (Throwable e) {
        }
        return ret;
    }

    public static String geoJsonGeomToBbox(Object WKT) throws Exception {
        String ret = "";
        try {
            Geometry geometry = new GeometryJSON().read(WKT);
            if (geometry != null) {
                final Envelope envelope = geometry.getEnvelopeInternal();
                return
                    String.format("%f|%f|%f|%f",
                        envelope.getMinX(), envelope.getMinY(),
                        envelope.getMaxX(), envelope.getMaxY());
            }
        } catch (Throwable e) {
        }
        return ret;
    }

    /**
     * Get field value for metadata identified by uuid.
     *
     * @param appName Web application name to access Lucene index from environment variable
     * @param uuid    Metadata uuid
     * @param field   Lucene field name
     * @param lang    Language of the index to search in
     * @return metadata title or an empty string if Lucene index or uuid could not be found
     */
    public static String getIndexField(Object appName, Object uuid, Object field, Object lang) {
        String id = uuid.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        final EsSearchManager searchManager = applicationContext.getBean(EsSearchManager.class);

        try {
            Set<String> fields = new HashSet<>();
            fields.add(fieldname);
            final Map<String, String> values = searchManager.getFieldsValues(id, fields, language);
            return values.get(fieldname);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field '" + fieldname + "' value on '" + id + "', caused by " + e.getMessage());
        }
        return "";
    }

    public static String getIndexFieldById(Object appName, Object id, Object field, Object lang) {
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        throw new NotImplementedException("getIndexFieldById not implemented in ES");
//        try {
//            String fieldValue = LuceneSearcher.getMetadataFromIndexById(language, id.toString(), fieldname);
//            return fieldValue == null ? "" : fieldValue;
//        } catch (Exception e) {
//            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
//            return "";
//        }
    }

    /**
     * Return a translation for a codelist or enumeration element.
     *
     * @param codelist The codelist name (eg. gmd:MD_TopicCategoryCode)
     * @param value    The value to search for in the translation file
     * @param langCode The language
     * @return The translation, the code list value if not found or an empty string if no codelist
     * value provided.
     */
    public static String getCodelistTranslation(Object codelist, Object value, Object langCode) {
        String codeListValue = (String) value;
        if (codeListValue != null && codelist != null && langCode != null) {
            String translation = codeListValue;
            try {
                Translator t = new CodeListTranslator(ApplicationContextHolder.get().getBean(SchemaManager.class),
                    (String) langCode,
                    (String) codelist);
                translation = t.translate(codeListValue);
            } catch (Exception e) {
                Log.error(
                    Geonet.GEONETWORK,
                    String.format("Failed to translate codelist value '%s' in language '%s'. Error is %s",
                        codeListValue, langCode, e.getMessage()));
            }
            return translation;
        } else {
            return "";
        }
    }

    /**
     * Convert the iso639_2B to iso639_2T
     *
     * @param iso639_2B The 3 iso lang code B
     * @return The iso639_2T lang code
     */
    public static
    @Nonnull
    String iso639_2B_to_iso639_2T(String iso639_2B) {
        return IsoLanguagesMapper.iso639_2B_to_iso639_2T(iso639_2B);
    }

    /**
     * Convert the iso639_2T to iso639_2B
     *
     * @param iso639_2T The 3 iso lang code T
     * @return The iso639_2B lang code
     */
    public static
    @Nonnull
    String iso639_2T_to_iso639_2B(String iso639_2T) {
        return IsoLanguagesMapper.iso639_2T_to_iso639_2B(iso639_2T);
    }

    /**
     * Return 2 iso lang code from a 3 iso lang code. If any error occurs return "".
     *
     * @param iso3LangCode The 2 iso lang code
     * @return The related 3 iso lang code
     */
    public static
    @Nonnull
    String twoCharLangCode(String iso3LangCode) {
        return twoCharLangCode(iso3LangCode, twoCharLangCode(Geonet.DEFAULT_LANGUAGE, null));
    }

    /**
     * Return 2 char iso lang code from a 3 iso lang code.
     *
     * @param iso3LangCode The 2 iso lang code
     * @return The related 3 iso lang code
     */
    public static
    @Nonnull
    String twoCharLangCode(String iso3LangCode, String defaultValue) {
        if (iso3LangCode == null || iso3LangCode.length() == 0) {
            if (defaultValue != null) {
                return defaultValue;
            } else {
                iso3LangCode = Geonet.DEFAULT_LANGUAGE;
            }
        }
        String iso2LangCode = null;

        // Catch language entries longer than 3 characters with a semicolon
        if (iso3LangCode.length() > 3 && (iso3LangCode.indexOf(';') != -1)) {
            //This will extract text similar to the following "fr;CAN", "fra;CAN", "fr ;CAN"
            //In the case of "fr;CAN",  fr would be extracted even though it is not a 3 char code - but that is ok because LanguageCode.getByCode supports 2 and 3 char codes.
            iso3LangCode = iso3LangCode.split(";")[0].trim();
        }

        LanguageCode languageCode = LanguageCode.getByCode(iso3LangCode.toLowerCase());
        if (languageCode != null) {
            iso2LangCode = languageCode.name();
        }

        // Triggers when the language can't be matched to a code
        if (iso2LangCode == null) {
            Log.info(Geonet.GEONETWORK, "Cannot convert " + iso3LangCode + " to 2 char iso lang code", new Error());
            return iso3LangCode.substring(0, 2);
        } else {
            return iso2LangCode;
        }
    }

    /**
     * Returns the HTTP code  or error message if error occurs during URL connection.
     *
     * @param url The URL to ckeck.
     * @return the numeric code of the HTTP request or a String with an error.
     */
    public static String getUrlStatus(String url) {
        UrlChecker urlChecker = ApplicationContextHolder.get().getBean(UrlChecker.class);
        LinkStatus urlStatus = urlChecker.getUrlStatus(url);
        if (urlStatus.getStatusValue().equalsIgnoreCase("4XX") || urlStatus.getStatusValue().equalsIgnoreCase("310")) {
           return urlStatus.getStatusInfo();
        }
        return urlStatus.getStatusValue();
    }

    public static String threeCharLangCode(String langCode) {
        if (langCode == null || langCode.length() < 2) {
            return Geonet.DEFAULT_LANGUAGE;
        }

        if (langCode.length() == 3) {
            return langCode;
        }

        final IsoLanguagesMapper mapper;
        mapper = ApplicationContextHolder.get().getBean(IsoLanguagesMapper.class);
        return mapper.iso639_1_to_iso639_2(langCode);

    }

    public static boolean match(Object src, Object pattern) {
        if (src == null || src.toString().trim().isEmpty()) {
            return false;
        }
        return src.toString().matches(pattern.toString());
    }

    public static void setNoScript() {
        allowScripting.set(false);
    }

    public static boolean allowScripting() {
        return allowScripting.get() == null || allowScripting.get();
    }

    public static String getUserDetails(Object contactIdentifier) {
        String contactDetails = "";
        int contactId = Integer.parseInt((String) contactIdentifier);

        Optional<User> userOpt = ApplicationContextHolder.get().getBean(UserRepository.class).findById(contactId);
        User user = null;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        if (user != null) {
            contactDetails = Xml.getString(user.asXml());
        }

        return contactDetails;
    }

    public static String reprojectCoords(Object minx, Object miny, Object maxx,
                                         Object maxy, Object fromEpsg) {
        String ret = "";
        try {
            Double minxf = new Double((String) minx);
            Double minyf = new Double((String) miny);
            Double maxxf = new Double((String) maxx);
            Double maxyf = new Double((String) maxy);
            CoordinateReferenceSystem fromCrs = CRS.decode((String) fromEpsg);
            CoordinateReferenceSystem toCrs = CRS.decode("EPSG:4326");

            ReferencedEnvelope env = new ReferencedEnvelope(minxf, maxxf, minyf, maxyf, fromCrs);
            ReferencedEnvelope reprojected = env.transform(toCrs, true);

            ret = reprojected.getMinX() + "," + reprojected.getMinY() + "," + reprojected.getMaxX() + "," + reprojected.getMaxY();

            Element elemRet = new Element("EX_GeographicBoundingBox", ISO19139Namespaces.GMD);

            boolean forceXY = Boolean.getBoolean(System.getProperty("org.geotools.referencing.forceXY", "false"));
            Element elemminx, elemmaxx, elemminy, elemmaxy;
            if (forceXY) {
                elemminx = new Element("westBoundLongitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMinX()));
                elemmaxx = new Element("eastBoundLongitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMaxX()));
                elemminy = new Element("southBoundLatitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMinY()));
                elemmaxy = new Element("northBoundLatitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMaxY()));
            } else {
                elemminx = new Element("westBoundLongitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMinY()));
                elemmaxx = new Element("eastBoundLongitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMaxY()));
                elemminy = new Element("southBoundLatitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMinX()));
                elemmaxy = new Element("northBoundLatitude", ISO19139Namespaces.GMD)
                    .addContent(new Element("Decimal", ISO19139Namespaces.GCO).setText("" + reprojected.getMaxX()));
            }
            elemRet.addContent(elemminx);
            elemRet.addContent(elemmaxx);
            elemRet.addContent(elemminy);
            elemRet.addContent(elemmaxy);

            ret = Xml.getString(elemRet);

        } catch (Throwable e) {
        }

        return ret;
    }


    public static String geomToBbox(Object geom) {
        String ret = "";
        try {
            String gml = (String) geom;

            Element geomElement = Xml.loadString(gml, false);
            String srs = geomElement.getAttributeValue("srsName");
            CoordinateReferenceSystem geomSrs = DefaultGeographicCRS.WGS84;
            if (srs != null && !(srs.equals(""))) geomSrs = CRS.decode(srs);
            Parser parser = GMLParsers.create(geomElement);
            MultiPolygon jts = parseGml(parser, gml);


            // if we have an srs and its not WGS84 then transform to WGS84
            if (!CRS.equalsIgnoreMetadata(geomSrs, DefaultGeographicCRS.WGS84)) {
                MathTransform tform = CRS.findMathTransform(geomSrs, DefaultGeographicCRS.WGS84);
                jts = (MultiPolygon) JTS.transform(jts, tform);
            }

            final Envelope envelope = jts.getEnvelopeInternal();
            return
                String.format(Locale.US, "%f|%f|%f|%f",
                    envelope.getMinX(), envelope.getMinY(),
                    envelope.getMaxX(), envelope.getMaxY());
        } catch (Throwable e) {
        }

        return ret;
    }

    /**
     * Retrieve a metadata record. Use this function only
     * to retrieve records visible for current user. This
     * does not make any security checks.
     */
    public static Node getRecord(String uuid) {
        return getRecord(uuid, null);
    }
    public static Node getRecord(String uuid, String schema) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataManager = applicationContext.getBean(DataManager.class);
        try {
            String id = dataManager.getMetadataId(uuid);
            if (id != null) {
                Element metadata = dataManager.getMetadata(id);
                String metadataSchema = dataManager.getMetadataSchema(id);

                if (StringUtils.isNotEmpty(schema)
                    && !metadataSchema.equals(schema)) {
                    final Path styleSheet = dataManager
                        .getSchemaDir(metadataSchema)
                        .resolve(String.format( "formatter/%s/view.xsl", schema));
                    final boolean exists = java.nio.file.Files.exists(styleSheet);
                    if (!exists) {
                        Log.warning(Geonet.GEONETWORK,String.format(
                            "XslUtil getRecord warning: Can't retrieve record %s (schema %s) in schema %s. A formatter is required for this conversion.",
                            uuid, metadataSchema, schema));

                    };
                    metadata = Xml.transform(metadata, styleSheet);
                }
                DOMOutputter outputter = new DOMOutputter();
                return outputter.output(new Document(metadata));
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,"XslUtil getRecord '" + uuid + "' error: " + e.getMessage(), e);
        }
        return null;
    }


    /**
     * @param formula    Math expression to evaluate
     * @param parameters List of parameters and values used in the expression
     *                   to evaluate. PARAM_KEY1=VALUE|PARAM_KEY2=VALUE
     * @return
     */
    public static Double evaluate(String formula, String parameters) {
        try {
            // Tokenize parameter
            Map<String, Double> variables = new HashMap();
            Arrays.stream(parameters.split("\\|")).forEach(e -> {
                String[] tokens = e.trim().split("=");
                if (tokens.length == 2) {
                    variables.put(tokens[0], Double.valueOf(tokens[1].trim()));
                }
            });
            Expression e = new ExpressionBuilder(formula)
                .variables(variables.keySet())
                .build()
                .setVariables(variables);

            return e.evaluate();
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,"XslUtil evaluate '" + formula + "' error: " + e.getMessage(), e);
            return null;
        }
    }

    public static String getSiteUrl() {
        ServiceContext context = ServiceContext.get();
        String baseUrl = "";
        if (context != null) baseUrl = context.getBaseUrl();

        SettingInfo si = new SettingInfo();
        return si.getSiteUrl() + (!baseUrl.startsWith("/") ? "/" : "") + baseUrl;
    }

    public static String getPermalink(String uuid, String language) {
        BaseMetadataUtils metadataUtils = ApplicationContextHolder.get().getBean(BaseMetadataUtils.class);
        return metadataUtils.getPermalink(uuid, language);
    }

    public static String getDefaultUrl(String uuid, String language) {
        BaseMetadataUtils metadataUtils = ApplicationContextHolder.get().getBean(BaseMetadataUtils.class);
        return metadataUtils.getDefaultUrl(uuid, language);
    }

    /**
     * Return default iso lang code.
     *
     * @return The default 3 char iso lang code
     */
    public static
    @Nonnull
    String getDefaultLangCode() {
        return Geonet.DEFAULT_LANGUAGE;
    }

    public static String getLanguage() {
        ServiceContext context = ServiceContext.get();
        if (context != null) {
            return context.getLanguage();
        } else {
            return Geonet.DEFAULT_LANGUAGE;
        }
    }

    /**
     * Build data URL like data:image/png;base64, iVBORw...
     */
    public static String buildDataUrl(String url, Integer size) {
        StringBuilder sb = new StringBuilder("data:");
        String supportedExtension = "jpg|jpeg|png|gif|tif|tiff";

        String extension = Files.getFileExtension(url).toLowerCase();
        if (extension.matches(supportedExtension)) {

            InputStream in = null;
            try {
                SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
                Matcher m = Pattern.compile(settingManager.getNodeURL() + "api/records/(.*)/attachments/(.*)$").matcher(url);
                BufferedImage image;
                if (m.find()) {
                    Store store = ApplicationContextHolder.get().getBean(FilesystemStore.class);
                    try (Store.ResourceHolder file = store.getResourceInternal(
                        m.group(1),
                        MetadataResourceVisibility.PUBLIC,
                        m.group(2), true)) {
                        image = ImageIO.read(file.getPath().toFile());
                    }
                } else {
                    URL imageUrl = new URL(url);
                    URLConnection con = imageUrl.openConnection();
                    con.setConnectTimeout(1000);
                    con.setReadTimeout(10000);
                    in = con.getInputStream();
                    image = ImageIO.read(in);
                }

                if (image != null) {
                    BufferedImage resized = ImageUtil.resize(image, size != null ? size : 140);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    ImageIO.write(resized, "png", output);
                    output.flush();
                    byte[] imagesB = output.toByteArray();
                    output.close();

                    sb.append("image/png;base64, ");
                    sb.append(Base64.getEncoder().encodeToString(imagesB));
                    return sb.toString();
                } else {
                    Log.info(Geonet.GEONETWORK, String.format(
                        "Image '%s' is null and can't be converted to Data URL.",
                        url));
                }
            } catch (Exception e) {
                Log.info(Geonet.GEONETWORK, String.format(
                    "Image '%s' is not accessible or can't be converted to Data URL. Error is: %s",
                    url, e.getMessage()));
            } finally {
                IOUtils.closeQuietly(in);
            }
        } else {
            Log.info(Geonet.GEONETWORK, String.format(
                "Image '%s' is not of one supported type %s and can't be encoded as a data URL.",
                url, supportedExtension));
        }
        return "";
    }

    public static String encodeForJavaScript(String str) {
        return DefaultEncoder.getInstance().encodeForJavaScript(str);
    }

    public static String encodeForHTML(String str) {
        return DefaultEncoder.getInstance().encodeForHTML(str);
    }

    public static String md5Hex(String str) {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(str);
    }

    public static String encodeForURL(String str) {
        try {
            return DefaultEncoder.getInstance().encodeForURL(str);
        } catch (EncodingException ex) {
            Log.error(Geonet.GEONETWORK,"XslUtil encode for URL '" + str + "' error: " + ex.getMessage(), ex);
            return str;
        }
    }

    /**
     * To get the xml content of an url
     * It supports the usage of a proxy
     *
     * @param surl
     * @return
     */
    public static Node getUrlContent(String surl) {

        Node res = null;
        InputStream is = null;

        ServiceContext context = ServiceContext.get();

        try {
            URL url = new URL(surl);
            URLConnection conn = Lib.net.setupProxy(context, url);

            is = conn.getInputStream();

            SAXBuilder builder = Xml.getSAXBuilder(false);
            Document jdoc = builder.build(is);

            DOMOutputter outputter = new DOMOutputter();
            return outputter.output(jdoc);

        } catch (Throwable e) {
            Log.error(Geonet.GEONETWORK, "Failed fetching url: " + surl, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return res;
    }

    public static String decodeURLParameter(String str) {
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (Exception ex) {
            Log.error(Geonet.GEONETWORK,"XslUtil decodeURLParameter '" + str + "' error: " + ex.getMessage(), ex);
            return str;
        }
    }

    private static final Random RANDOM = new Random();

    public static String randomId() {
        return "N" + RANDOM.nextInt(Integer.MAX_VALUE);
    }

    public static String getMax(Object values) {
        String[] strings = values.toString().split(" ");
        String max = "";

        for (int i = 0; i < strings.length; i++) {
            String val = strings[i];
            if (val.compareTo(max) > 0) {
                max = val;
            }
        }
        return max;
    }

    private static final Cache<String, Integer> URL_VALIDATION_CACHE;

    static {
        URL_VALIDATION_CACHE = CacheBuilder.<String, Integer>newBuilder().
            maximumSize(100000).
            expireAfterAccess(25, TimeUnit.HOURS).
            build();
    }

    public static Integer getURLStatus(final String urlString) throws ExecutionException {
        return URL_VALIDATION_CACHE.get(urlString, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    return Integer.parseInt(getUrlStatus(urlString));
                } catch (Exception e) {
                    Log.info(Geonet.GEONETWORK,"validateURL: exception - ",e);
                    return -1;
                }
            }
        });
    }

    public static String getURLStatusAsString(final String urlString) throws ExecutionException {
        Integer status = getURLStatus(urlString);
        return status == -1 ? "UNKNOWN" :
            String.format("%s (%d)",
                HttpStatus.valueOf(status).name(), status);
    }

    public static boolean validateURL(final String urlString) throws ExecutionException {
        Integer status = getURLStatus(urlString);
        return status == -1 ? false : status / 100 == 2;
    }

    /**
     * Utility method to retrieve the thesaurus dir from xsl processes.
     * <p>
     * Usage:
     * <p>
     * <xsl:stylesheet
     * xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
     * ...
     * xmlns:java="java:org.fao.geonet.util.XslUtil" ...>
     * <p>
     * <xsl:variable name="thesauriDir" select="java:getThesaurusDir()"/>
     *
     * @return Thesaurus directory
     */
    public static String getThesaurusDir() {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);

        return thesaurusManager.getThesauriDirectory().toString();
    }

    public static String getThesaurusIdByTitle(String title) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);
        Thesaurus thesaurus = thesaurusManager.getThesaurusByTitle(title);

        return thesaurus == null ? "" : "geonetwork.thesaurus." + thesaurus.getKey();
    }


    /**
     * Utility method to retrieve the name (label) for an iso language using it's code for a specific language.
     * <p>
     * Usage:
     * <p>
     * <xsl:stylesheet
     * xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
     * ...
     * xmlns:java="java:org.fao.geonet.util.XslUtil" ...>
     * <p>
     * <xsl:variable name="thesauriDir" select="java:getIsoLanguageLabel('dut', 'eng')"/>
     *
     * @param code      Code of the IsoLanguage to retrieve the name.
     * @param language  Language to retrieve the IsoLanguage name.
     * @return
     */
    public static String getIsoLanguageLabel(String code, String language) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        IsoLanguageRepository isoLanguageRepository = applicationContext.getBean(IsoLanguageRepository.class);

        List<IsoLanguage> languageValues = isoLanguageRepository.findAllByCode(code);

        String languageLabel = code;

        if (!languageValues.isEmpty()) {
            languageLabel = languageValues.get(0).getLabelTranslations().get(language);
        }

        return languageLabel;
    }


    public static List<String> getKeywordHierarchy(String keyword, String thesaurusId, String langCode) {
        List<String> res = new ArrayList<String>();
        if (StringUtils.isEmpty(thesaurusId)) {
            return res;
        }

        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);

            thesaurusId = thesaurusId.replaceAll("geonetwork.thesaurus.", "");
            Thesaurus thesaurus = thesaurusManager.getThesaurusByName(thesaurusId);

            if (thesaurus != null) {
                res = thesaurus.getKeywordHierarchy(keyword, langCode);
            }
            return res;
        } catch (Exception ex) {
        }
        return res;
    }

    public static String getKeywordValueByUri(String uri, String thesaurusId, String langCode) {
        if (StringUtils.isEmpty(thesaurusId)) {
            return "";
        }

        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);

            thesaurusId = thesaurusId.replaceAll("geonetwork.thesaurus.", "");
            Thesaurus thesaurus = thesaurusManager.getThesaurusByName(thesaurusId);

            if (thesaurus != null) {
                KeywordBean keywordBean = thesaurus.getKeyword(uri, langCode);
                if (keywordBean != null) {
                    return keywordBean.getPreferredLabel(langCode);
                }
            }
            return "";
        } catch (Exception ex) {
        }
        return "";
    }


    public static String getKeywordUri(String keyword, String thesaurusId, String langCode) {
        if (StringUtils.isEmpty(thesaurusId)) {
            return "";
        }

        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);

            thesaurusId = thesaurusId.replaceAll("geonetwork.thesaurus.", "");
            Thesaurus thesaurus = thesaurusManager.getThesaurusByName(thesaurusId);

            if (thesaurus != null) {
                KeywordBean keywordBean = thesaurus.getKeywordWithLabel(keyword, langCode);
                if (keywordBean != null) {
                    return keywordBean.getUriCode();
                }
            }
            return "";
        } catch (Exception ex) {
        }
        return "";
    }

    /**
     * Associated resource like
     * <ul>
     * <li>parent</li>
     * <li>source</li>
     * <li>dataset (for service record)</li>
     * <li>siblings</li>
     * <li>feature catalogue</li>
     * </ul>
     * are stored in current records
     * BUT
     * some other relations are stored in the other side of the relation record ie.
     * <ul>
     * <li>service operatingOn current = +recordOperateOn:currentUuid</li>
     * <li>siblings of current = +recordLink.type:siblings +recordLink.to:currentUuid</li>
     * <li>children of current = +parentUuid:currentUuid</li>
     * <li>brothersAndSisters = +parentUuid:currentParentUuid</li>
     * </ul>
     * Instead of relying on related API, it can make sense to index all relations
     * (including bidirectional links) at indexing time to speed up rendering of
     * associated resources which is slow task on search results.
     *
     * MetadataUtils#getRelated has the logic to search for all associated resources
     * and also takes into account privileges in case of target record is not visible
     * to current user.
     *
     * BTW in some cases, all records are public (or it is not an issue to only display
     * a title of a private record) and a more direct approach can be used.
     *
     * @param uuid
     * @return
     */
    public static Element getTargetAssociatedResources(String uuid, String parentUuid) {
        EsRestClient client = ApplicationContextHolder.get().getBean(EsRestClient.class);
        EsSearchManager searchManager = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        Element recordLinks = new Element("recordLinks");

        try {
            MultiSearchRequest request = new MultiSearchRequest();


            SearchRequest serviceRequest = new SearchRequest(searchManager.getDefaultIndex());
            SearchSourceBuilder serviceSearchSourceBuilder = new SearchSourceBuilder();
            serviceSearchSourceBuilder.fetchSource(
                    new String[]{"resourceTitleObject.default"},
                    null
            );
            serviceSearchSourceBuilder.query(QueryBuilders.matchQuery(
                    "recordOperateOn", uuid));
            serviceRequest.source(serviceSearchSourceBuilder);
            request.add(serviceRequest);


            SearchRequest childrenRequest = new SearchRequest(searchManager.getDefaultIndex());
            SearchSourceBuilder childrenSearchSourceBuilder = new SearchSourceBuilder();
            childrenSearchSourceBuilder.fetchSource(
                    new String[]{"resourceTitleObject.default"},
                    null
            );
            childrenSearchSourceBuilder.query(QueryBuilders.matchQuery(
                    "parentUuid", uuid));
            childrenRequest.source(childrenSearchSourceBuilder);
            request.add(childrenRequest);


            SearchRequest siblingsRequest = new SearchRequest(searchManager.getDefaultIndex());
            SearchSourceBuilder siblingsSearchSourceBuilder = new SearchSourceBuilder();
            siblingsSearchSourceBuilder.fetchSource(
                    new String[]{"resourceTitleObject.default"},
                    null
            );
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            List<QueryBuilder> must = boolQuery.must();
            must.add(QueryBuilders.matchQuery("recordLink.type", "siblings"));
            must.add(QueryBuilders.matchQuery("recordLink.to", uuid));
            siblingsSearchSourceBuilder.query(
                    QueryBuilders.nestedQuery(
                            Geonet.IndexFieldNames.RECORDLINK,
                            boolQuery,
                            ScoreMode.Avg));
            siblingsRequest.source(siblingsSearchSourceBuilder);
            request.add(siblingsRequest);



            boolean hasParent = StringUtils.isNotEmpty(parentUuid);
            if (hasParent) {
                SearchRequest brothersAndSistersRequest = new SearchRequest(searchManager.getDefaultIndex());
                SearchSourceBuilder brothersAndSistersSearchSourceBuilder = new SearchSourceBuilder();
                brothersAndSistersSearchSourceBuilder.fetchSource(
                        new String[]{"resourceTitleObject.default"},
                        null
                );
                brothersAndSistersSearchSourceBuilder.query(QueryBuilders.matchQuery(
                        "parentUuid", parentUuid));
                brothersAndSistersRequest.source(brothersAndSistersSearchSourceBuilder);
                request.add(brothersAndSistersRequest);
            }


            MultiSearchResponse response = client.getClient().msearch(request, RequestOptions.DEFAULT);
            recordLinks.addContent(buildRecordLink(response.getResponses()[0].getResponse().getHits(), "services"));
            recordLinks.addContent(buildRecordLink(response.getResponses()[1].getResponse().getHits(), "children"));
            recordLinks.addContent(buildRecordLink(response.getResponses()[2].getResponse().getHits(), "siblings"));

            if (hasParent) {
                recordLinks.addContent(buildRecordLink(response.getResponses()[3].getResponse().getHits(), "brothersAndSisters"));
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,
                    "Get related document '" + uuid + "' error: " + e.getMessage(), e);
        }
        return recordLinks;
    }

    public static Node getTargetAssociatedResourcesAsNode(String uuid, String parentUuid) {
        DOMOutputter outputter = new DOMOutputter();
        try {
            return outputter.output(
                    new Document(
                            getTargetAssociatedResources(uuid, parentUuid)));
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,
                    "Get related document '" + uuid + "' error: " + e.getMessage(), e);
        }
        return null;
    }

    private static List<Element> buildRecordLink(SearchHits hits, String type) {
        ObjectMapper mapper = new ObjectMapper();
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String recordUrlPrefix = settingManager.getNodeURL() + "api/records/";
        ArrayList<Element> listOfLinks = new ArrayList<>();
        hits.forEach(record -> {
            Element recordLink = new Element("recordLink");
            recordLink.setAttribute("type", "object");
            ObjectNode recordLinkProperties = mapper.createObjectNode();

            recordLinkProperties.put("to", record.getId());
            recordLinkProperties.put("origin", "catalog");
            recordLinkProperties.put("created", "bySearch");
            Map<String, String> titleObject = (Map<String, String>) record.getSourceAsMap().get("resourceTitleObject");
            if (titleObject != null) {
                recordLinkProperties.put("title", titleObject.get("default"));
            }
            recordLinkProperties.put("url", recordUrlPrefix + record.getId());
            recordLinkProperties.put("type", type);

            try {
                recordLink.setText(mapper.writeValueAsString(recordLinkProperties));
                listOfLinks.add(recordLink);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return listOfLinks;
    }

    public static String escapeForJson(String value) {
        return StringEscapeUtils.escapeJson(value);
    }
}
