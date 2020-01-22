/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.component.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.UiSetting;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.CodeListTranslator;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.Translator;
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
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.owasp.esapi.errors.EncodingException;
import org.owasp.esapi.reference.DefaultEncoder;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.kernel.search.spatial.SpatialIndexWriter.parseGml;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_SITE_ORGANIZATION;
import static org.fao.geonet.utils.Xml.getXmlFromJSON;

/**
 * These are all extension methods for calling from xsl docs.  Note:  All params are objects because
 * it is hard to determine what is passed in from XSLT. Most are converted to string by calling
 * tostring.
 *
 * @author jesse
 */
public final class XslUtil {

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
        final String defaultUiConfiguration = NodeInfo.DEFAULT_NODE;
        NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);
        SourceRepository sourceRepository= ApplicationContextHolder.get().getBean(SourceRepository.class);
        UiSettingsRepository uiSettingsRepository = ApplicationContextHolder.get().getBean(UiSettingsRepository.class);

        Source portal = sourceRepository.findOne(nodeInfo.getId());

        if (uiSettingsRepository != null) {
            UiSetting one = null;
            if (portal != null && StringUtils.isNotEmpty(portal.getUiConfig())) {
                one = uiSettingsRepository.findOne(portal.getUiConfig());
            }
            else if (StringUtils.isNotEmpty(key)) {
                one = uiSettingsRepository.findOne(key);
            }
            else if (one == null) {
                one = uiSettingsRepository.findOne(defaultUiConfiguration);
            }

            if (one != null) {
                return one.getConfiguration();
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
     * @param key   Sub portal key
     * @return
     */
    public static String getNodeName(String key, String lang, boolean withOrganization) {
        SettingManager settingsMan = ApplicationContextHolder.get().getBean(SettingManager.class);
        if (StringUtils.isEmpty(key)) {
            key =  ApplicationContextHolder.get().getBean(NodeInfo.class).getId();
        }
        if (NodeInfo.DEFAULT_NODE.equals(key)) {
            key = settingsMan.getSiteId();
        }
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        Source source = sourceRepository.findOne(key);

        return source != null ? source.getLabel(lang) : settingsMan.getSiteName()
            + (withOrganization ? " - " + settingsMan.getValue(SYSTEM_SITE_ORGANIZATION) : "");
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
            Log.error(Geonet.GEONETWORK,"XslUtil getJsonSettingValue error: " + e.getMessage(), e);
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
     * Check if bean is defined in the context
     *
     * @param beanId id of the bean to look up
     */
    public static boolean existsBean(String beanId) {
        return ProfileManager.existsBean(beanId);
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
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndex(language, id, fieldname);
            if (fieldValue == null) {
                return getIndexFieldById(appName, uuid, field, lang);
            } else {
                return fieldValue;
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
    }

    public static String getIndexFieldById(Object appName, Object id, Object field, Object lang) {
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndexById(language, id.toString(), fieldname);
            return fieldValue == null ? "" : fieldValue;
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
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
     * Return 2 iso lang code from a 3 iso lang code. If any error occurs return "".
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
                return twoCharLangCode(Geonet.DEFAULT_LANGUAGE);
            }
        } else {
            if (iso3LangCode.equalsIgnoreCase("FRA")) {
                return "FR";
            }

            if (iso3LangCode.equalsIgnoreCase("DEU")) {
                return "DE";
            }
            String iso2LangCode = null;

            try {
                final IsoLanguagesMapper mapper = ApplicationContextHolder.get().getBean(IsoLanguagesMapper.class);
                /*if the language  is 2 characters long...*/
                if (iso3LangCode.length() == 2) {
                    iso2LangCode = iso3LangCode;
                    /*Catch language entries longer than 3 characters with a semicolon*/
                } else if (iso3LangCode.length() > 3 && (iso3LangCode.indexOf(';') != -1)) {
                    iso2LangCode = mapper.iso639_2_to_iso639_1(iso3LangCode.substring(0, 3));
                    /** This final else works properly for languages with exactly three characters, so
                     * an exception will occur if gmd:language has more than 3 characters but
                     * does not have a semicolon.
                     */
                } else {
                    iso2LangCode = mapper.iso639_2_to_iso639_1(iso3LangCode);
                }
            } catch (Exception ex) {
                Log.error(Geonet.GEONETWORK, "Failed to get iso 2 language code for " + iso3LangCode + " caused by " + ex.getMessage());

            }
            /* Triggers when the language can't be matched to a code */
            if (iso2LangCode == null) {
                Log.error(Geonet.GEONETWORK, "Cannot convert " + iso3LangCode + " to 2 char iso lang code", new Error());
                return iso3LangCode.substring(0, 2);
            } else {
                return iso2LangCode;
            }
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

        User user = ApplicationContextHolder.get().getBean(UserRepository.class).findOne(contactId);
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
            Parser[] parsers = GMLParsers.create();
            Parser parser = null;
            if (geomElement.getNamespace().equals(Geonet.Namespaces.GML32)) {
              parser = parsers[1];
            } else {
              parser = parsers[0];
            }
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
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataManager = applicationContext.getBean(DataManager.class);
        try {
            String id = dataManager.getMetadataId(uuid);
            if (id != null) {
                Element metadata = dataManager.getMetadata(id);

                DOMOutputter outputter = new DOMOutputter();
                return outputter.output(new Document(metadata));
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,"XslUtil getRecord error: " + e.getMessage(), e);
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
            Log.error(Geonet.GEONETWORK,"XslUtil evaluate error: " + e.getMessage(), e);
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

    public static String getLanguage() {
        ServiceContext context = ServiceContext.get();
        if (context != null) {
            return context.getLanguage();
        } else {
            return "eng";
        }
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
            Log.error(Geonet.GEONETWORK,"XslUtil encode for URL error: " + ex.getMessage(), ex);
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

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            is = conn.getInputStream();
            res = db.parse(is);

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
            Log.error(Geonet.GEONETWORK,"XslUtil decodeURLParameter error: " + ex.getMessage(), ex);
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

    private static final Cache<String, Boolean> URL_VALIDATION_CACHE;

    static {
        URL_VALIDATION_CACHE = CacheBuilder.<String, Boolean>newBuilder().
            maximumSize(100000).
            expireAfterAccess(25, TimeUnit.HOURS).
            build();
    }

    public static boolean validateURL(final String urlString) throws ExecutionException {
        return URL_VALIDATION_CACHE.get(urlString, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    return (Integer.parseInt(getUrlStatus(urlString)) / 100 == 2);
                } catch (Exception e) {
                    return false;
                }
            }
        });
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
}
