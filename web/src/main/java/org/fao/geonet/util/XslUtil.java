package org.fao.geonet.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
    	return existsBean("casEntryPoint");
    }
    /** 
     * Check if bean is defined in the context
     * 
     * @param beanId id of the bean to look up
     */
    public static boolean existsBean(String beanId) {
    	ServiceContext serviceContext = ServiceContext.get();
		if(serviceContext == null) return true;
    	ServletContext servletContext = serviceContext.getServlet().getServletContext();
    	WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    	if(springContext == null) return true;
    	return springContext.containsBean(beanId);
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
    	ServiceContext serviceContext = ServiceContext.get();
		if(serviceContext == null) return true;
    	ServletContext servletContext = serviceContext.getServlet().getServletContext();
    	WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    	if(springContext == null) return true;
    	WebInvocationPrivilegeEvaluator eval = springContext.getBean(WebInvocationPrivilegeEvaluator.class);
    	SecurityContext context = SecurityContextHolder.getContext();
    	if(eval == null || context == null) return true;
		Authentication authentication = context.getAuthentication();
		boolean allowed = eval.isAllowed("/srv/"+serviceContext.getLanguage()+"/"+serviceName, authentication);
		return allowed;
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

}