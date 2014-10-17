package jeeves.server.dispatchers.guiservices;

import jeeves.XmlFileCacher;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.servlet.ServletContext;

public class XmlCacheManager {
    WeakHashMap<String, Map<String, XmlFileCacher>> xmlCaches = new WeakHashMap<String, Map<String, XmlFileCacher>>();
    private Map<String, XmlFileCacher> getCacheMap(boolean localized, String base, String file) {
        String key = localized+":"+base+":"+file;
        Map<String, XmlFileCacher> cacheMap = xmlCaches.get(key);
        if(cacheMap == null) {
            cacheMap = new HashMap<String, XmlFileCacher>(10);
            xmlCaches.put(key, cacheMap);
        }
        
        return cacheMap;
    }

    /**
     * Obtain the stings for the provided xml file.
     *
     * @param context
     * @param localized        if this xml is a localized file or is a normal xml file
     * @param base             the directory to the localization directory (often is loc).
     *                         If file is not localized then this is the directory that contains the xml file.
     * @param file             the name of the file to load
     * @param preferedLanguage the language to attempt to load if it exists
     * @param defaultLang      a fall back language
     * @param makeCopy         if false then xml is not cloned and MUST NOT BE MODIFIED!
     */
    public synchronized Element get(ServiceContext context, boolean localized, String base, String file, String preferedLanguage,
                                    String defaultLang, boolean makeCopy) throws JDOMException, IOException {

        Map<String, XmlFileCacher> cacheMap = getCacheMap(localized, base, file);
        
        String appPath = context.getAppPath();
        String xmlFilePath;

        boolean isBaseAbsolutePath = (new File(base)).isAbsolute();
        String rootPath = (isBaseAbsolutePath) ? base : appPath + base;

        if (localized) {
            xmlFilePath = rootPath + File.separator + preferedLanguage +File.separator + file;
        } else {
            xmlFilePath = rootPath + File.separator + file;
            if (!new File(xmlFilePath).exists()) {
                xmlFilePath = appPath + file;
            }
        }

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        
        XmlFileCacher xmlCache = cacheMap.get(preferedLanguage);
        File xmlFile = new File(xmlFilePath);
        if (xmlCache == null){
            xmlCache = new XmlFileCacher(xmlFile,servletContext,appPath);
            cacheMap.put(preferedLanguage, xmlCache);
        }

        Element result;
        try {
            if (makeCopy) {
                result = (Element) xmlCache.get().clone();
            } else {
                return xmlCache.get();
            }
        } catch (Exception e) {
            Log.error(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: "+xmlFilePath+"but failed so falling back to default language");
            Log.debug(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: "+xmlFilePath+"but failed so falling back to default language", e);
            String xmlDefaultLangFilePath = rootPath + File.separator + defaultLang + File.separator + file;
            xmlCache = new XmlFileCacher(new File(xmlDefaultLangFilePath),servletContext, appPath);
            cacheMap.put(preferedLanguage, xmlCache);
            result = (Element)xmlCache.get().clone();
        }
        String name = xmlFile.getName();
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot > 0) {
            name = name.substring(0,lastIndexOfDot);
        }
        return result.setName(name);
    }

}
