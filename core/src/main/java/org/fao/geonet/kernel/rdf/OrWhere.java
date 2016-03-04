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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrWhere extends Where {

    private List<Where> clauses;

    public OrWhere(Where... clauses) {
        this(Arrays.asList(clauses));
    }
    public OrWhere(List<Where> newClauses) {
        this.clauses = new ArrayList<Where>(newClauses);
    }
    
    @Override
    public String getClause() {
        StringBuilder builder = new StringBuilder();
        for (Where clause : clauses) {
            if(builder.length() > 0) {
                builder.append(" OR ");
            }
            builder.append(clause.getClause());
        }
        builder.insert(0, '(');
        builder.append(')');
        return builder.toString();
    }

    @Override
    public Where or(Where other) {
        ArrayList<Where> newClauses = new ArrayList<Where>(clauses);
        if (other instanceof OrWhere) {
            OrWhere otherOr = (OrWhere) other;
            newClauses.addAll(otherOr.clauses);
        } else {
            newClauses.add(other);
        }
        return new OrWhere(newClauses);
    }
}
