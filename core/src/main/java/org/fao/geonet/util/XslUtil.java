package org.fao.geonet.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jeeves.component.ProfileManager;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.CodeListTranslator;
import org.fao.geonet.kernel.search.Translator;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.languages.IsoLanguagesMapper;

import javax.annotation.Nonnull;

/**
 * These are all extension methods for calling from xsl docs.  Note:  All
 * params are objects because it is hard to determine what is passed in from XSLT.
 * Most are converted to string by calling tostring.
 *
 * @author jesse
 */
public final class XslUtil
{

    private static final char TS_DEFAULT = ' ';
    private static final char CS_DEFAULT = ',';
    private static final char TS_WKT = ',';
    private static final char CS_WKT = ' ';
    /**
     * clean the src of ' and <>
     */
    public static String clean(Object src)
    {
        String result = src.toString().replaceAll("'","\'").replaceAll("[><\n\r]", " ");
        return result;
    }

    /**
     * Returns 'true' if the pattern matches the src
     */
    public static String countryMatch(Object src, Object pattern)
    {
        if( src.toString().trim().length()==0){
            return "false";
        }
        boolean result = src.toString().toLowerCase().contains(pattern.toString().toLowerCase());
        return String.valueOf(result);
    }

    /**
     * Replace the pattern with the substitution
     */
    public static String replace(Object src, Object pattern, Object substitution)
    {
        String result = src.toString().replaceAll(pattern.toString(), substitution.toString());
        return result;
    }
    
    public static boolean isCasEnabled() {
		return ProfileManager.isCasEnabled();
	}

    /**
     * Return a service handler config parameter
     * @see org.fao.geonet.constants.Geonet.Config.
     * @param key
     * @return
     */
    public static String getConfigValue(String key) {
        if (key == null) {
            return "";
        }

        final ServiceContext serviceContext = ServiceContext.get();
        if (serviceContext != null) {
            ServiceConfig config = serviceContext.getBean(ServiceConfig.class);
            if (config != null) {
                String value = config.getValue(key);
                if (value != null) {
                    return value;
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    /**
     * Get a setting value
     * @param key
     * @return
     */
    public static String getSettingValue(String key) {
        if (key == null) {
            return "";
        }

        final ServiceContext serviceContext = ServiceContext.get();
        if (serviceContext != null) {
            SettingManager settingsMan = serviceContext.getBean(SettingManager.class);
            if (settingsMan != null) {
                String value = settingsMan.getValue(key);
                if (value != null) {
                    return value;
                } else {
                    return "";
                }
            }
        }
        return "";
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
	 * Optimistically check if user can access a given url.  If not possible to determine then
	 * the methods will return true.  So only use to show url links, not check if a user has access
	 * for certain.  Spring security should ensure that users cannot access restricted urls though.
	 *  
	 * @param serviceName the raw services name (main.home) or (admin) 
	 * 
	 * @return true if accessible or system is unable to determine because the current
	 * 				thread does not have a ServiceContext in its thread local store
	 */
	public static boolean isAccessibleService(Object serviceName) {
		return ProfileManager.isAccessibleService(serviceName);
	}
    /**
     * Takes the characters until the pattern is matched
     */
    public static String takeUntil(Object src, Object pattern)
    {
        String src2 = src.toString();
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(src2);

        if( !matcher.find() )
            return src2;

        int index = matcher.start();

        if( index==-1 ){
            return src2;
        }
        return src2.substring(0,index);
    }

    /**
     * Convert a serialized XML node in JSON
     * 
     * @param xml
     * @return
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
     * @param ts the separator that separates 2 coordinates
     * @param cs the separator between 2 numbers in a coordinate
     */
    public static String toWktCoords(Object coords, Object ts, Object cs){
        String coordsString = coords.toString();
        char tsString;
        if( ts==null || ts.toString().length()==0){
            tsString = TS_DEFAULT;
        }else{
            tsString = ts.toString().charAt(0);
        }
        char csString;
        if( cs==null || cs.toString().length()==0){
            csString = CS_DEFAULT;
        }else{
            csString = cs.toString().charAt(0);
        }

        if( tsString == TS_WKT && csString == CS_WKT ){
            return coordsString;
        }

        if( tsString == CS_WKT ){
            tsString=';';
            coordsString = coordsString.replace(CS_WKT, tsString);
        }
        coordsString = coordsString.replace(csString, CS_WKT);
        String result = coordsString.replace(tsString, TS_WKT);
        char lastChar = result.charAt(result.length()-1);
        if(result.charAt(result.length()-1)==TS_WKT || lastChar==CS_WKT ){
            result = result.substring(0, result.length()-1);
        }
        return result;
    }


    public static String posListToWktCoords(Object coords, Object dim){
        String[] coordsString = coords.toString().split(" ");

        int dimension;
        if( dim==null ){
            dimension = 2;
        }else{
            try{
                dimension=Integer.parseInt(dim.toString());
            }catch (NumberFormatException e) {
                dimension=2;
            }
        }
        StringBuilder results = new StringBuilder();

        for (int i = 0; i < coordsString.length; i++) {
            if( i>0 && i%dimension==0 ){
                results.append(',');
            }else if( i>0 ){
                results.append(' ');
            }
            results.append(coordsString[i]);
        }

        return results.toString();
    }
    

    /**
     * Get field value for metadata identified by uuid.
     * 
     * @param appName 	Web application name to access Lucene index from environment variable
     * @param uuid 		Metadata uuid
     * @param field 	Lucene field name
     * @param lang 		Language of the index to search in
     * 
     * @return metadata title or an empty string if Lucene index or uuid could not be found
     */
    public static String getIndexField(Object appName, Object uuid, Object field, Object lang) {
        String id = uuid.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndex(language, id, fieldname);
            if(fieldValue == null) {
                return getIndexFieldById(appName,uuid,field,lang);
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
     * @param value The value to search for in the translation file
     * @param langCode  The language
     * @return  The translation, the code list value if not found or an empty string
     * if no codelist value provided.
     */
    public static String getCodelistTranslation(Object codelist, Object value, Object langCode) {
        String codeListValue = (String) value;
        if (codeListValue != null && codelist != null && langCode != null) {
            String translation = codeListValue;
            try {
                final GeonetContext gc = (GeonetContext) ServiceContext.get().getHandlerContext(Geonet.CONTEXT_NAME);
                Translator t = new CodeListTranslator(gc.getBean(SchemaManager.class),
                        (String) langCode,
                        (String) codelist);
                translation = t.translate(codeListValue);
            } catch (Exception e) {
                Log.error(Geonet.GEONETWORK, "Failed to translate codelist " + e.getMessage());
            }
            return translation;
        } else {
            return "";
        }
    }

    /**
     * Return 2 iso lang code from a 3 iso lang code. If any error occurs return "".
     *
     * @param iso3LangCode   The 2 iso lang code
     * @return The related 3 iso lang code
     */
    public static @Nonnull String twoCharLangCode(String iso3LangCode) {
    	if(iso3LangCode==null || iso3LangCode.length() == 0) {
    		return twoCharLangCode(Geonet.DEFAULT_LANGUAGE);
    	} else {
            String iso2LangCode = null;

            try {
                if (iso3LangCode.length() == 2){
                    iso2LangCode = iso3LangCode;
                } else {
                    if (ServiceContext.get() != null) {
                        final IsoLanguagesMapper mapper = ServiceContext.get().getBean(IsoLanguagesMapper.class);
                        iso2LangCode = mapper.iso639_2_to_iso639_1(iso3LangCode);
                    }
                }
            } catch (Exception ex) {
                Log.error(Geonet.GEONETWORK, "Failed to get iso 2 language code for " + iso3LangCode + " caused by " + ex.getMessage());

            }

            if(iso2LangCode == null) {
                return iso3LangCode.substring(0,2);
            } else {
                return iso2LangCode;
            }
        }
    }
    /**
     * Return '' or error message if error occurs during URL connection.
     * 
     * @param url   The URL to ckeck
     * @return
     */
    public static String getUrlStatus(String url){
        URL u;
        URLConnection conn;
        int connectionTimeout = 500;
        try {
            u = new URL(url);
            conn = u.openConnection();
            conn.setConnectTimeout(connectionTimeout);
            
            // TODO : set proxy
            
            if (conn instanceof HttpURLConnection) {
               HttpURLConnection httpConnection = (HttpURLConnection) conn;
               httpConnection.setInstanceFollowRedirects(true);
               httpConnection.connect();
               httpConnection.disconnect();
               // FIXME : some URL return HTTP200 with an empty reply from server 
               // which trigger SocketException unexpected end of file from server
               int code = httpConnection.getResponseCode();

               if (code == HttpURLConnection.HTTP_OK) {
                   return "";
               } else {
                   return "Status: " + code;
               } 
            } // TODO : Other type of URLConnection
        } catch (Throwable e) {
            e.printStackTrace();
            return e.toString();
        }
        
        return "";
    }
    
	public static String threeCharLangCode(String langCode) {
	    if (langCode == null || langCode.length() < 2) {
            return Geonet.DEFAULT_LANGUAGE;
        }

		if (langCode.length() == 3) {
            return langCode;
        }

        final ServiceContext serviceContext = ServiceContext.get();
        if (serviceContext != null) {
            final IsoLanguagesMapper mapper;
            mapper = serviceContext.getBean(IsoLanguagesMapper.class);
            return mapper.iso639_1_to_iso639_2(langCode);
        } else {
            return langCode;
        }

    }

	public static boolean match(Object src, Object pattern) {
		if (src == null || src.toString().trim().isEmpty()) {
			return false;
		}
		return src.toString().matches(pattern.toString());
	}

    private static ThreadLocal<Boolean> allowScripting = new InheritableThreadLocal<Boolean>();
    public static void setNoScript() {
        allowScripting.set(false);
    }
    public static boolean allowScripting() {
        return allowScripting.get() == null || allowScripting.get();
    }
}