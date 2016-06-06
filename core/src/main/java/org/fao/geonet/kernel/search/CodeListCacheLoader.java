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

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public final class CodeListCacheLoader implements Callable<Map<String, String>> {
    private final String langCode;
    private final String codeListName;
    private final SchemaManager schemaManager;

    public CodeListCacheLoader(String langCode, String codeListName, SchemaManager schemaManager) {
        this.langCode = langCode;
        this.codeListName = codeListName;
        this.schemaManager = schemaManager;
    }

    public static String cacheKey(final String langCode, final String codeListName) {
        return "codelist:" + langCode + ":" + codeListName;
    }

    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> _codeList = new HashMap<String, String>();
        Set<String> schemas = schemaManager.getSchemas();
        for (String schema : schemas) {

            Path schemaDir = schemaManager.getSchemaDir(schema);
            addCodeLists(codeListName, _codeList, schemaDir.resolve("loc").resolve(langCode).resolve("codelists.xml"));
        }
        return _codeList;
    }

    @SuppressWarnings("unchecked")
    private void addCodeLists(String codeListName, Map<String, String> codeList, Path file) throws IOException, JDOMException {
        if (Files.exists(file)) {
            Element xmlDoc = Xml.loadFile(file);

            List<Element> codelists = xmlDoc.getChildren("codelist");
            for (Element element : codelists) {
                if (element.getAttributeValue("name").equals(codeListName)) {
                    List<Element> entries = element.getChildren("entry");
                    for (Element entry : entries) {
                        codeList.put(entry.getChildText("code"), entry.getChildText("label"));
                    }
                    break;
                }
            }
        }
    }

}
