package jeeves.server.dispatchers.guiservices;

import jeeves.XmlFileCacher;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.servlet.ServletContext;

public class XmlCacheManager {
    WeakHashMap<String, Map<String, XmlFileCacher>> xmlCaches = new WeakHashMap<String, Map<String, XmlFileCacher>>();
    private Map<String, XmlFileCacher> getCacheMap(boolean localized, Path base, String file) {
        String key = localized+":"+base+":"+file;
        Map<String, XmlFileCacher> cacheMap = xmlCaches.get(key);
        if(cacheMap == null) {
            cacheMap = new HashMap<>(10);
            xmlCaches.put(key, cacheMap);
        }
        
        return cacheMap;
    }
    public synchronized Element get(ServiceContext context, boolean localized, Path base, String file, String preferedLanguage, String defaultLang) throws JDOMException, IOException {

        Map<String, XmlFileCacher> cacheMap = getCacheMap(localized, base, file);
        
        Path appPath = context.getAppPath();
        Path xmlFilePath;

        boolean isBaseAbsolutePath = base.isAbsolute();
        Path rootPath = (isBaseAbsolutePath) ? base : appPath.resolve(base);

        if (localized) {
            xmlFilePath = rootPath.resolve(preferedLanguage).resolve(file);
        } else {
            xmlFilePath = rootPath.resolve(file);
            if (!Files.exists(xmlFilePath)) {
                xmlFilePath = appPath.resolve(file);
            }
        }

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        
        XmlFileCacher xmlCache = cacheMap.get(preferedLanguage);
        Path xmlFile = xmlFilePath;
        if (xmlCache == null){
            xmlCache = new XmlFileCacher(xmlFile, servletContext, appPath);
            cacheMap.put(preferedLanguage, xmlCache);
        }

        Element result;
        try {
            result = (Element)xmlCache.get().clone();
        } catch (Exception e) {
            Log.error(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: "+xmlFilePath+"but failed so falling back to default language");
            Log.debug(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: "+xmlFilePath+"but failed so falling back to default language", e);
            Path xmlDefaultLangFilePath = rootPath.resolve(defaultLang).resolve(file);
            xmlCache = new XmlFileCacher(xmlDefaultLangFilePath, servletContext, appPath);
            cacheMap.put(preferedLanguage, xmlCache);
            result = (Element)xmlCache.get().clone();
        }
        String name = com.google.common.io.Files.getNameWithoutExtension(xmlFile.getFileName().toString());

        return result.setName(name);
    }

}
