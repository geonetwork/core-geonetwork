package org.fao.geonet.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.fao.geonet.utils.Xml;

public class LangUtils {

    /**
     * Find all the translations for a given key in the <type>.xml file.  normally you will want 
     * 'type' to == 'string'.  In fact the 2 parameter method can be used for this.
     * 
     * @param type the type of translations file, typically strings
     * @param key the key to look up.  may contain / but cannot start with one.  for example: categories/water
     * @return
     */
    public static Map<String, String> translate(ServiceContext context, String type, String key) throws JDOMException, IOException {
        String appPath = context.getAppPath();
        XmlCacheManager cacheManager = context.getXmlCacheManager();
        File loc = new File(appPath, "loc");
        String typeWithExtension = "xml"+File.separator+type+".xml";
        Map<String, String> translations = new HashMap<String, String>();
        
        for (File file : loc.listFiles()) {
            if(file.isDirectory() && new File(file, typeWithExtension).exists()) {
                Element xml = cacheManager.get(context, true, loc.getAbsolutePath(), typeWithExtension, file.getName(), file.getName());
                String translation = Xml.selectString(xml, key);
                if(translation != null && !translation.trim().isEmpty()) {
                    translations.put(file.getName(), translation);
                }
            }
        }
        
        return translations;
    }
    /**
     * same as translate(context, "string", key)
     */
    public static Map<String, String> translate(ServiceContext context, String key) throws JDOMException, IOException {
        return translate(context, "strings", key);
    }

}
