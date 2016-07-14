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

package org.fao.geonet.kernel.rdf;

public abstract class Wheres {
    /**
     * Where builder methods
     */
    public static final Where ALL = new Where() {
        @Override
        public String getClause() {
            return "";
        }

        @Override
        public Where or(Where other) {
            return this;
        }
    };
    /**
     * Where builder methods
     */
    public static final Where NONE = new Where() {
        @Override
        public String getClause() {
            return "";
        }

        @Override
        public Where or(Where other) {
            return other;
        }
    };

    /**
     * Create a where clause that tries to match the id column to the provided id
     *
     * @param id the id to find
     * @return a Where object that tries to match the id column to the provided id
     */
    public static Where ID(String id) {
        return like("id", id.replace("\\", "\\\\"));
    }

    /**
     * Create a Like clause
     *
     * @param columnName the column or function to match against
     * @param value      the value to match
     * @return a Like clause
     */
    public static Where like(String columnName, String value) {
        return new WhereClause(columnName + " LIKE \"" + value + "\"");
    }

    /**
     * Create a Like clause that ignores case for matching
     *
     * @param columnName the column or function to match against
     * @param value      the value to match
     * @return Create a Like clause that ignores case for matching
     */
    public static Where ilike(String columnName, String value) {
        return new WhereClause(columnName + " LIKE \"" + value + "\" IGNORE CASE");
    }

    /**
     * Create a where clause that tries to match a preferred label column for the provided language
     * code to the provided value
     *
     * @param lang  the language to find
     * @param value the label value to find
     * @return a Where object that tries to match the preferred label column to the provided value
     */
    public static Where prefLabel(String lang, String value) {
        String columnName = lang + Selectors.LABEL_POSTFIX;
        return new WhereClause(columnName + " LIKE \"" + value + "\"");
    }

}
