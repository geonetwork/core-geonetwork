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

import java.util.LinkedHashMap;

import org.fao.geonet.kernel.Thesaurus;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import com.google.common.base.Function;

/**
 * Return a map of column names -> column values for each row.
 *
 * @author jeichar
 */
public class IdentityResultInterpreter extends ResultInterpreter<LinkedHashMap<String, Value>> {

    @Override
    public LinkedHashMap<String, Value> createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex) {
        LinkedHashMap<String, Value> map = new LinkedHashMap<String, Value>();
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            map.put(resultTable.getColumnName(i), resultTable.getValue(rowIndex, i));
        }
        return map;
    }

    public ResultInterpreter<Value> onlyColumn(final String columnName) {
        return map(new Function<LinkedHashMap<String, Value>, Value>() {

            @Override
            public Value apply(LinkedHashMap<String, Value> input) {
                return input.get(columnName);
            }
        });
    }

}
