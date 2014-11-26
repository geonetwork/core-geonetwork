package org.fao.geonet.util;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
        Path appPath = context.getAppPath();
        XmlCacheManager cacheManager = context.getXmlCacheManager();
        Path loc = appPath.resolve("loc");
        String typeWithExtension = "xml"+File.separator+type+".xml";
        Map<String, String> translations = new HashMap<String, String>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(loc)) {
            for (Path path : paths) {
                if(Files.isDirectory(path) && Files.exists(path.resolve(typeWithExtension))) {
                    final String filename = path.getFileName().toString();
                    Element xml = cacheManager.get(context, true, loc, typeWithExtension, filename, filename);
                    String translation = Xml.selectString(xml, key);
                    if(translation != null && !translation.trim().isEmpty()) {
                        translations.put(filename, translation);
                    }
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
