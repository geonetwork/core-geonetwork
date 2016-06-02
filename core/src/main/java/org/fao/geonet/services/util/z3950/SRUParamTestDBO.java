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

package org.fao.geonet.services.util.z3950;

import java.util.Set;


public class SRUParamTestDBO {

    private String op;
    private Set<String> notSupported;
    private Set<String> missingArgs;
    private Set<String> cannotParse;


    public SRUParamTestDBO(String op, Set<String> notSupported, Set<String> missingArgs, Set<String> cannotParse) {

        this.op = op;
        this.notSupported = notSupported;
        this.missingArgs = missingArgs;
        this.cannotParse = cannotParse;

    }

    public String getOp() {
        return op;
    }

    public Set<String> getArgNotSupported() {
        return notSupported;
    }

    public Set<String> getMissingArgs() {
        return missingArgs;
    }

    public Set<String> getCannotParseArg() {
        return cannotParse;
    }

}
