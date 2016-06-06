//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet.kernel.search.lucenequeries;

import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class DateRangeQuery extends TermRangeQuery {

    public DateRangeQuery(String fld, String lowerString, String upperString, String inclusive) {
        super(fld, formatDate(lowerString), formatDate(upperString), inclusive.equalsIgnoreCase("true"), inclusive.equalsIgnoreCase("true"));
    }


    private static BytesRef formatDate(String s) {

        String ret = "";

        if (s == null) return new BytesRef();

        ret = s.trim();
        ret = ret.replaceAll("\'", "");
        ret = ret.toUpperCase();

        return new BytesRef(ret);
    }
}
