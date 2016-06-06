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

package org.fao.geonet.kernel.search.keyword;

import java.text.MessageFormat;

import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;

public enum KeywordSearchType {
    STARTS_WITH("{0}*"), CONTAINS("*{0}*"), MATCH("{0}");

    private String template;

    private KeywordSearchType(String template) {
        this.template = template;
    }

    public static KeywordSearchType parseString(String param) {
        String processed = param.toUpperCase().trim();
        if ("0".equals(processed)) {
            return STARTS_WITH;
        } else if ("1".equals(processed)) {
            return CONTAINS;
        } else if ("2".equals(processed)) {
            return MATCH;
        } else {
            return valueOf(processed);
        }
    }

    public Where toWhere(String columnName, KeywordLabelSearchClause clause) {
        return toWhere(columnName, clause.keyword, clause.ignoreCase);
    }

    public Where toWhere(String columnName, String value, boolean ignoreCase) {
        String finalValue = MessageFormat.format(template, value);
        if (ignoreCase) {
            return Wheres.ilike(columnName, finalValue);
        } else {
            return Wheres.like(columnName, finalValue);
        }
    }
}
