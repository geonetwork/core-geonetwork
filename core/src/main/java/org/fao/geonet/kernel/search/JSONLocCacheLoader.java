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

package org.fao.geonet.kernel.search;

import com.google.common.io.Resources;

import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import jakarta.servlet.ServletContext;

/**
 * Create a cache based on JSON translation files.
 *
 * All JSON files are combined in one cache (like the client app does).
 */
public final class JSONLocCacheLoader implements Callable<Map<String, String>> {
    /**
     * The list of files to load.
     */
    private static final List<String> files = Arrays.asList(
        new String[]{"core", "admin", "editor", "search"});
    private final String langCode;
    private ConfigurableApplicationContext applicationContext;

    public JSONLocCacheLoader(ConfigurableApplicationContext context, String langCode) {
        this.langCode = langCode;
        this.applicationContext = context;
    }

    public static String cacheKey(final String langCode) {
        return "json:" + langCode;
    }

    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> translations = new HashMap<String, String>();

        ServletContext servletContext = applicationContext.getBean(ServletContext.class);

        String iso2letterLangCode = XslUtil.twoCharLangCode(langCode, "eng");

        for (String file : files) {
            URL resource = servletContext.getResource("/catalog/locales/" + iso2letterLangCode + "-" + file + ".json");
            if (resource == null) {
                resource = servletContext.getResource("/catalog/locales/en-" + file + ".json");
            }
            addJSONLocalizationFile(translations, resource);
        }
        return translations;
    }

    /**
     * Populate the cache by loading all JSON files.
     */
    @SuppressWarnings("unchecked")
    private void addJSONLocalizationFile(Map<String, String> translation, URL file)
        throws IOException, JDOMException {
        if (file != null) {
            try {
                JSONObject json =
                    new JSONObject(Resources.toString(file, Constants.CHARSET));

                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    translation.put(key, json.getString(key));
                }
            } catch (JSONException e) {
                Log.error(Geonet.GEONETWORK, "addJSONLocalizationFile error:" + e.getMessage(), e);
            }
        }
    }
}
