//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.fao.geonet.kernel.search.SearchManager.LuceneFieldAttribute;
import org.jdom.Element;

/**
 * Compute a boost factor for document according to field custom field values. Field name, values
 * and boosting factor is defined by configuration.
 *
 * The boost is computed using a sum of boost factor if field name and field value match.
 *
 * @author fxprunayre
 */
public class ImportantDocument implements DocumentBoosting {

    private static final String NOTNULL = "NOTNULL";
    private HashMap<String, HashMap<String, Float>> config = new HashMap<String, HashMap<String, Float>>();

    /**
     * @param fields A comma separated value of fields.
     * @param values A comma separated value of value for each field. Use NOTNULL to check any field
     *               values.
     * @param boosts A comma separated value of boost factor for each field.
     */
    public ImportantDocument(String fields, String values, String boosts) {
        List<String> fieldList = Arrays.asList(fields.split(","));
        List<String> valueList = Arrays.asList(values.split(","));
        List<String> boostList = Arrays.asList(boosts.split(","));
        int idx = 0;
        for (String f : fieldList) {
            Float b = Float.parseFloat(boostList.get(idx));
            HashMap<String, Float> hm = config.get(f);

            if (hm == null) {
                hm = new HashMap<String, Float>();
            }
            hm.put(valueList.get(idx), b);
            config.put(f, hm);
            idx++;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.fao.geonet.kernel.search.function.DocumentBoosting#getBoost(org.apache
     * .lucene.document.Document)
     */
    public Float getBoost(Element doc) {
        Float documentBoost = null;
        for (Object o : doc.getChildren()) {
            Element field = (Element) o;
            String name = field.getAttributeValue(LuceneFieldAttribute.NAME.toString());
            HashMap<String, Float> fieldBoosts = config.get(name);

            if (fieldBoosts != null) {
                String value = field.getAttributeValue(LuceneFieldAttribute.STRING.toString());
                Float b = fieldBoosts.get(value);
                if (b != null) {
                    documentBoost = addBoost(documentBoost, b);
                } else if (fieldBoosts.containsKey(NOTNULL)) {
                    documentBoost = addBoost(documentBoost,
                        fieldBoosts.get(NOTNULL));
                }
            }
        }
        return documentBoost;
    }

    private Float addBoost(Float documentBoost, Float b) {
        if (documentBoost == null) {
            documentBoost = 1F;
        }
        documentBoost += b;
        return documentBoost;
    }
}
