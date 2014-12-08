package org.fao.geonet.util;

import com.google.common.collect.Maps;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LangUtils {

    private static final class TranslationKey {
        private String type;
        private String key;

        private TranslationKey(String type, String key) {
            this.type = type;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TranslationKey that = (TranslationKey) o;

            if (!key.equals(that.key)) return false;
            if (!type.equals(that.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }
    }
    private static final Map<TranslationKey, Map<String, String>> translationsCache = Maps.newConcurrentMap();

    /**
     * Find all the translations for a given key in the <type>.xml file.  normally you will want 
     * 'type' to == 'string'.  In fact the 2 parameter method can be used for this.
     * 
     * @param type the type of translations file, typically strings
     * @param key the key to look up.  may contain / but cannot start with one.  for example: categories/water
     */
    public static Map<String, String> translate(ApplicationContext context, String type, String key) throws JDOMException, IOException {
        TranslationKey translationKey = new TranslationKey(type, key);
        Map<String, String> translations = translationsCache.get(translationKey);

        if (translations == null || context.getBean(SystemInfo.class).isDevMode()) {
            Path webappDir = context.getBean(GeonetworkDataDirectory.class).getWebappDir();
            XmlCacheManager cacheManager = context.getBean(XmlCacheManager.class);
            Path loc = webappDir.resolve("loc");
            String typeWithExtension = "xml/" + type + ".xml";
            translations = new HashMap<>();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(loc, IO.DIRECTORIES_FILTER)) {
                for (Path path : paths) {
                    if (Files.exists(path.resolve(typeWithExtension))) {
                        final String filename = path.getFileName().toString();
                        Element xml = cacheManager.get(context, true, loc, typeWithExtension, filename, filename, false);
                        String translation;
                        if (key.contains("/") || key.contains("[") || key.contains(":")) {
                            translation = Xml.selectString(xml, key);
                        } else {
                            translation = xml.getChildText(key);
                        }
                        if (translation != null && !translation.trim().isEmpty()) {
                            translations.put(filename, translation);
                        }
                    }
                }
            }

            translationsCache.put(translationKey, translations);
        }
        
        return translations;
    }


    /**
     * same as translate(context, "string", key)
     */
    public static Map<String, String> translate(ApplicationContext context, String key) throws JDOMException, IOException {
        return translate(context, "strings", key);
    }

}
