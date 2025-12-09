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

package jeeves.server.dispatchers.guiservices;

import jeeves.XmlFileCacher;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import jakarta.servlet.ServletContext;

public class XmlCacheManager {
    WeakHashMap<String, Map<String, XmlFileCacher>> xmlCaches = new WeakHashMap<String, Map<String, XmlFileCacher>>();

    private Map<String, XmlFileCacher> getCacheMap(boolean localized, Path base, String file) {
        String key = localized + ":" + base + ":" + file;
        Map<String, XmlFileCacher> cacheMap = xmlCaches.get(key);
        if (cacheMap == null) {
            cacheMap = new HashMap<>(10);
            xmlCaches.put(key, cacheMap);
        }

        return cacheMap;
    }

    /**
     * Obtain the stings for the provided xml file.
     *
     * @param localized        if this xml is a localized file or is a normal xml file
     * @param base             the directory to the localization directory (often is loc). If file
     *                         is not localized then this is the directory that contains the xml
     *                         file.
     * @param file             the name of the file to load
     * @param preferedLanguage the language to attempt to load if it exists
     * @param defaultLang      a fall back language
     * @param makeCopy         if false then xml is not cloned and MUST NOT BE MODIFIED!
     */
    public synchronized Element get(ApplicationContext context, boolean localized, Path base, String file, String preferedLanguage,
                                    String defaultLang, boolean makeCopy) throws JDOMException, IOException {

        Map<String, XmlFileCacher> cacheMap = getCacheMap(localized, base, file);

        Path appPath = context.getBean(GeonetworkDataDirectory.class).getWebappDir();
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

        ServletContext servletContext = context.getBean(ServletContext.class);

        XmlFileCacher xmlCache = cacheMap.get(preferedLanguage);
        Path xmlFile = xmlFilePath;
        if (xmlCache == null) {
            xmlCache = new XmlFileCacher(xmlFile, servletContext, appPath);
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
            Log.debug(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: " + xmlFilePath + " but failed so falling back to default language", e);
            Path xmlDefaultLangFilePath = rootPath.resolve(defaultLang).resolve(file);
            xmlCache = new XmlFileCacher(xmlDefaultLangFilePath, servletContext, appPath);
            cacheMap.put(preferedLanguage, xmlCache);
            result = (Element) xmlCache.get().clone();
        }
        String name = com.google.common.io.Files.getNameWithoutExtension(xmlFile.getFileName().toString());

        return result.setName(name);
    }

}
