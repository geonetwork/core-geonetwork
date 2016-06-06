//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import jeeves.JeevesCacheManager;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Translates JSON localization files keys into a language.
 */
public class JSONLocTranslator implements Translator {
    private static final long serialVersionUID = 1L;
    private Map<String, String> translations;
    private String keyPrefix = "";

    /**
     * @param context   Context used to get the application files
     * @param langCode  The language code
     * @param keyPrefix The optional key prefix which could be used to make distinction between
     *                  facet values. eg. y for isHarvested means Yes, y for metadataTypes means
     *                  Template. Add a prefix and add custom translation to JSON translations files
     *                  if needed.
     */
    public JSONLocTranslator(final ConfigurableApplicationContext context,
                             final String langCode,
                             final String keyPrefix) throws Exception {
        this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
        translations = JeevesCacheManager.findInEternalCache(
            JSONLocCacheLoader.cacheKey(langCode),
            new JSONLocCacheLoader(context, langCode));
    }

    public String translate(String key) {
        if (translations == null) {
            return key;
        }

        // Prepend the prefix if needed
        String value = translations.get(keyPrefix + key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        } else {
            return key;
        }
    }
}
