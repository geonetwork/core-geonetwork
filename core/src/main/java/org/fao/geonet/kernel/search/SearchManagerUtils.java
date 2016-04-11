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

import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class SearchManagerUtils {
    public static final String INDEXING_ERROR_MSG = "_indexingErrorMsg";
    public static final String INDEXING_ERROR_FIELD = "_indexingError";

    public static Path getIndexFieldsXsl(Path schemaDir, String root, String indexName) {
        if (root == null) {
            root = "";
        }
        root = root.toLowerCase();
        if (root.contains(":")) {
            root = root.split(":", 2)[1];
        }

        final String basicName = "index-fields";
        Path defaultLangStyleSheet = schemaDir.resolve(basicName).resolve(indexName + root + ".xsl");
        if (!Files.exists(defaultLangStyleSheet)) {
            defaultLangStyleSheet = schemaDir.resolve(basicName).resolve(indexName + "default.xsl");
        }
        if (!Files.exists(defaultLangStyleSheet)) {
            // backward compatibility
            defaultLangStyleSheet = schemaDir.resolve(indexName + basicName + ".xsl");
        }
        return defaultLangStyleSheet;
    }

    /**
     * Creates a new XML field for the Lucene index and add it to the document.
     *
     * @param xmlDoc
     * @param name
     * @param value
     * @param store
     * @param index
     */
    public static void addField(Element xmlDoc, String name, String value, boolean store, boolean index) {
        Element field = makeField(name, value, store, index);
        xmlDoc.addContent(field);
    }

    /**
     * Creates a new XML field for the Lucene index.
     *
     * @param name
     * @param value
     * @param store
     * @param index
     * @return
     */
    public static Element makeField(String name, String value, boolean store, boolean index) {
        Element field = new Element("Field");
        field.setAttribute(SearchManager.LuceneFieldAttribute.NAME.toString(), name);
        field.setAttribute(SearchManager.LuceneFieldAttribute.STRING.toString(), value == null ? "" : value);
        field.setAttribute(SearchManager.LuceneFieldAttribute.STORE.toString(), Boolean.toString(store));
        field.setAttribute(SearchManager.LuceneFieldAttribute.INDEX.toString(), Boolean.toString(index));
        return field;
    }

    /**
     * Extracts text from metadata record.
     *
     * @param metadata
     * @param sb
     * @return all text in the metadata elements for indexing
     */
    static void allText(Element metadata, StringBuilder sb) {
        String text = metadata.getText().trim();
        if (text.length() > 0) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(text);
        }
        @SuppressWarnings("unchecked")
        List<Element> children = metadata.getChildren();
        for (Element aChildren : children) {
            allText(aChildren, sb);
        }
    }
}
