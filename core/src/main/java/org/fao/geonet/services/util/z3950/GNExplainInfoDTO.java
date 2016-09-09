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

package org.fao.geonet.services.util.z3950;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transport object for explain operation
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 */
public class GNExplainInfoDTO {

    String id;
    String title;
    Map<String, String> map = new HashMap<String, String>();

    public GNExplainInfoDTO() {

    }

    public GNExplainInfoDTO(String id) {
        this.id = id;
    }

    public GNExplainInfoDTO(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * @return map containing indices (and their namespaces) that map to this index
     */
    public Map<String, String> getMappings() {
        return map;
    }

    public void addMapping(String index, String namespace) {
        map.put(index, namespace);
    }

}
