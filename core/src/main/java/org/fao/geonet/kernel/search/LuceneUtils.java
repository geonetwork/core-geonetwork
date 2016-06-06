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

import org.apache.lucene.search.BooleanClause;

/**
 * Utilities for the Lucene library.
 */
public class LuceneUtils {

    /**
     * As of Lucene 1.9, the use of <code>BooleanQuery.add(Query, boolean, boolean)</code> was
     * deprecated and replaced by <code>BooleanQuery.add(Query, BooleanClause.Occur)</code>. This
     * utility method converts the old pair of booleans to the corresponding
     * <code>BooleanClause.Occur</code> value.
     *
     * @return BooleanClause.Occur
     * @TODO throw exception if both booleans are true
     */
    public static BooleanClause.Occur convertRequiredAndProhibitedToOccur(boolean required, boolean prohibited) {
        BooleanClause.Occur occur = null;
        if (required && !prohibited) {
            occur = BooleanClause.Occur.MUST;
        } else if (!required && !prohibited) {
            occur = BooleanClause.Occur.SHOULD;
        } else if (!required && prohibited) {
            occur = BooleanClause.Occur.MUST_NOT;
        }
        return occur;
    }

}
