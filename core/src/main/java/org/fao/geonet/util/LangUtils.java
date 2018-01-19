/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

import com.google.common.collect.Maps;

import jeeves.server.dispatchers.guiservices.XmlCacheManager;

import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LangUtils {

    protected static final Map<TranslationKey, Map<String, String>> translationsCache = Maps.newConcurrentMap();

    /**
     * Find all the translations for a given key in the <type>.xml file.  normally you will want
     * 'type' to == 'string'.  In fact the 2 parameter method can be used for this.
     *
     * @param type the type of translations file, typically strings
     * @param key  the key to look up.  may contain / but cannot start with one.  for example:
     *             categories/water
     */
    public static Map<String, String> translate(ApplicationContext context, String type, String key) throws JDOMException, IOException {
        TranslationKey translationKey = new TranslationKey(type, key);
        Map<String, String> translations = translationsCache.get(translationKey);

        if (translations == null || context.getBean(SystemInfo.class).isDevMode()) {
            Path webappDir = context.getBean(GeonetworkDataDirectory.class).getWebappDir();
            Path loc = webappDir.resolve("loc");
            XmlCacheManager cacheManager = context.getBean(XmlCacheManager.class);

            String xmlTypeWithExtension = "xml/" + type + ".xml";
            String jsonTypeWithExtension = "json/" + type + ".json";

            Map<String, String> translations1 = new HashMap<>();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(loc, IO.DIRECTORIES_FILTER)) {
                for (Path path : paths) {
                    final String lang = path.getFileName().toString();
                    String translation = null;
                    if (Files.exists(path.resolve(jsonTypeWithExtension))) {
                        Path jsonFile = path.resolve(jsonTypeWithExtension);
                        try {
                            JSONObject json = new JSONObject(new String(Files.readAllBytes(jsonFile), Constants.CHARSET));
                            translation = json.optString(key);
                        } catch (JSONException e) {
                            throw new RuntimeException("Failed to parse the following file as a json file: " + jsonFile, e);
                        }
                    } else if (Files.exists(path.resolve(xmlTypeWithExtension))) {
                        Element xml = cacheManager.get(context, true, loc, xmlTypeWithExtension, lang, lang, false);
                        if (key.contains("/") || key.contains("[") || key.contains(":")) {
                            translation = Xml.selectString(xml, key);
                        } else {
                            translation = xml.getChildText(key);
                        }
                    }
                    if (translation != null && !translation.trim().isEmpty()) {
                        translations1.put(lang, translation);
                    }
                }
            }
            translations = translations1;
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

    protected static final class TranslationKey {
        private String type;
        private String key;

        public TranslationKey(String type, String key) {
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

}
