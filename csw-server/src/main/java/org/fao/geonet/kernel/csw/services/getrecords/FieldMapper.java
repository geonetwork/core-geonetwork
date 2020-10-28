//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords;

import java.util.HashMap;
import java.util.Set;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogConfigurationGetRecordsField;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

public class FieldMapper implements IFieldMapper {

    @Autowired
    private CatalogConfiguration _catalogConfig;

    public String map(String field) {
        CatalogConfigurationGetRecordsField fieldInfo = _catalogConfig.getFieldMapping()
            .get(getAbsolute(field));

        return (fieldInfo != null) ? fieldInfo.getLuceneField() : field;
    }

    public String mapXPath(String field, String schema) {
        String xpath = null;

        CatalogConfigurationGetRecordsField fieldInfo = _catalogConfig.getFieldMapping()
            .get(getAbsolute(field));

        if (fieldInfo != null) {
            HashMap<String, String> xpaths = fieldInfo.getXpaths();

            if (xpaths != null) {
                xpath = xpaths.get(schema);
            }
        }

        return xpath;
    }

    public String mapSort(String field) {
        CatalogConfigurationGetRecordsField fieldInfo = _catalogConfig.getFieldMapping()
            .get(getAbsolute(field));

        return (fieldInfo != null) ? fieldInfo.getLuceneSortField() : "";
    }

    public Iterable<CatalogConfigurationGetRecordsField> getMappedFields() {
        return _catalogConfig.getFieldMapping().values();
    }

    public boolean match(Element elem, Set<String> elemNames) {
        String name = elem.getQualifiedName();

        for (String field : elemNames)
        // Here we supposed that namespaces prefix are equals when removing elements
        // when an ElementName parameter is set.
        {
            if (field.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getPropertiesByType(String type) {
        return _catalogConfig.getTypeMapping(type);
    }

    private String getAbsolute(String field) {
        if (field.startsWith("./")) {
            field = field.substring(2);
        }

        // Remove any namespaces ... to be validated
        if (field.contains(":")) {
            field = field.substring(field.indexOf(':') + 1);
        }

        return field.toLowerCase();
    }

}
