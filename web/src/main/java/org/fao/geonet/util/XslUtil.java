package org.fao.geonet.util;

import static java.io.File.separator;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import jeeves.server.ProfileManager;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.UnfailingIterator;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Settings;
import org.fao.geonet.constants.Geonet.Settings.Values;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.languages.IsoLanguagesMapper;

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
    private static final Whitelist WHITE_LIST;
    static {
        WHITE_LIST = Whitelist.relaxed();
        // add tags to allow in output html as we find the ones we require
        WHITE_LIST.addTags("span", "font");
        // add attributes to allow in output html as we find the ones we require
        WHITE_LIST.addAttributes(":all", "color", "style", "align");
        WHITE_LIST.addAttributes("font", "size", "face");
        WHITE_LIST.addAttributes("a", "target", "href", "title");
        WHITE_LIST.addAttributes("a", "target", "href", "title");
    }
    /**
     * clean the src of ' and <>
     */
    public static String clean(Object src)
    {
        String result = src.toString().replaceAll("'","\'").replaceAll("[><\n\r]", " ");
        return result;
    }

    public static Object parseWikiText(NodeInfo node, String src, String markupLanguage) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, XPathException, UnsupportedEncodingException {
        NodeInfo info = (NodeInfo) node;
        MarkupParser markupParser;
        if (!markupLanguage.equals("none")) {
            markupParser = MarkupParserCache.lookup(markupLanguage);
        } else {
            markupParser = null;
        }
        String html = parseMarkupToText(src, markupParser).replace("&nbsp;", " ");
        try {
            Source xmlSource = new StreamSource(new ByteArrayInputStream(html.getBytes("UTF-8")));
            DocumentInfo doc = info.getConfiguration().buildDocument(xmlSource);
            return SingletonIterator.makeIterator(doc);
        } catch (Exception e) {
            try {
                String newHtml = extractFromFullHtml(Jsoup.parse(html).outerHtml());
                Source xmlSource = new StreamSource(new ByteArrayInputStream(newHtml.getBytes("UTF-8")));
                DocumentInfo doc = info.getConfiguration().buildDocument(xmlSource);
                return SingletonIterator.makeIterator(doc);
            } catch (Exception e2) {
                // unable to parse xml
                return src;
            }
        }

    }

    public static String parseMarkupToText(String src, MarkupParser markupParser) {
        String html;
        if(markupParser != null) {
            html = markupParser.parseToHtml(src.toString());
            html = extractFromFullHtml(html);
            return html;
        } else {
            html = src;
        }
        String cleanedHtml = Jsoup.clean(html, WHITE_LIST);
        // span is needed so that the case where there is text before or after html
        // the xml is valid and order of text elements is maintained 
        return "<span>"+cleanedHtml+"</span>";
    }

    private static String extractFromFullHtml(String html) {
        int startIndex = html.indexOf("<body>");
        int endIndex = html.indexOf("</html");
        html = html.substring(startIndex, endIndex).replace("<body", "<span").replace("</body", "</span");
        return html;
    }
    public static String markupToolTip(String template, String name, String syntaxLink) {
        String link = "<a href=\"javascript:window.open('"+syntaxLink+"', '_markupHelp')\">"+syntaxLink+"</a>";
        return template.replace("@@markupName@@", name).replace("@@syntaxLink@@", link);
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
     * @param appPath 	Web application name to access Lucene index from environment variable
     * @param uuid 		Metadata uuid
     * @param field 	Lucene field name
     * @param lang 		Language of the index to search in
     * 
     * @return metadata title or an empty string if Lucene index or uuid could not be found
     */
    public static String getIndexField(Object appName, Object uuid, Object field, Object lang) {
        String webappName = appName.toString();
        String id = uuid.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndex(webappName, language, id, fieldname);
            if(fieldValue == null) {
                return getIndexFieldById(appName,uuid,field,lang);
            }
            return fieldValue == null ? "" : fieldValue;
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
    }

    public static String getIndexFieldById(Object appName, Object id, Object field, Object lang) {
        String webappName = appName.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndexById(webappName, language, id.toString(), fieldname);
            return fieldValue == null ? "" : fieldValue;
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
    }
    /**
     * Return 2 iso lang code from a 3 iso lang code. If any error occurs return "".
     *
     * @param iso3LangCode   The 2 iso lang code
     * @return The related 3 iso lang code
     */
    public static String twoCharLangCode(String iso3LangCode) {
    	if(iso3LangCode==null || iso3LangCode.length() == 0) {
    		return Geonet.DEFAULT_LANGUAGE;
    	}
        String iso2LangCode = "";

        try {
            if (iso3LangCode.length() == 2){
                iso2LangCode = iso3LangCode;
            } else {
                iso2LangCode = IsoLanguagesMapper.getInstance().iso639_2_to_iso639_1(iso3LangCode);
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
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        
        return "";
    }

    public static Element controlForMarkup(ServiceContext context, Element metadata, String outputParamPath) throws Exception {
        SettingManager settingManager = context.getHandlerContext(GeonetContext.class).getSettingManager();
        String mefOutput = settingManager.getValue(outputParamPath);
        String wysiwygEnabled = settingManager.getValue(Geonet.Settings.WYSIWYG_EDITOR);
        String markupType = settingManager.getValue(Geonet.Settings.WIKI_SYNTAX);
        if(Geonet.Settings.Values.STRIP_MARKUP.equals(mefOutput)) {
            String styleSheetPath = context.getAppPath() + separator + "/" + separator + "xsl" + separator + "strip-wiki-markup.xsl";
            
            Map<String, String> params = new HashMap<String, String>();
            params.put("markupType", markupType);
            params.put("outputType", mefOutput);
            params.put("wysiwygEnabled", wysiwygEnabled);
            metadata = Xml.transform(metadata, styleSheetPath, params);
        }
        return metadata;
    }

}