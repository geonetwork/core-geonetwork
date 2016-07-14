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

import java.util.Map;

import jeeves.JeevesCacheManager;

import org.fao.geonet.kernel.SchemaManager;

/**
 * Translates code list keys into a language
 *
 * @author jesse
 */
public class CodeListTranslator implements Translator {
    private static final long serialVersionUID = 1L;
    private Map<String, String> _codeList;

    public CodeListTranslator(final SchemaManager schemaManager, final String langCode, final String codeListName) throws Exception {
        _codeList = JeevesCacheManager.findInEternalCache(CodeListCacheLoader.cacheKey(langCode, codeListName), new CodeListCacheLoader(
            langCode, codeListName, schemaManager));
    }

    public String translate(String key) {
        if (_codeList == null) {
            return key;
        }

        String value = _codeList.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        } else {
            return key;
        }
    }

}
