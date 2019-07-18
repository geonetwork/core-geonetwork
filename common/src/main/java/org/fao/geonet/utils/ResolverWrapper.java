//=============================================================================
//===	Copyright (C) GeoNetwork
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//=============================================================================

/**
 * Static container for a single instance of Resolver
 */

public final class ResolverWrapper {

    // The map contains the reference to different resolvers initialized with schema specific oasis catalogs
    private static Map<String, Resolver> resolverMap = new HashMap<String, Resolver>();
    private static final String DEFAULT = "DEFAULT";

    // Initializes a schema specific resolver
    public static synchronized void createResolverForSchema(String schemaName, Path oasisDirFile) {
        if (!resolverMap.containsKey(schemaName)) resolverMap.put(schemaName, new Resolver(oasisDirFile));
    }

    // Returns a specific resolver OR a generic one when not possible
    public static Resolver getInstance(String schemaName) {
        if(schemaName==null) {
            return getInstance();
        } else if(!resolverMap.containsKey(schemaName)) {
            Log.error(Log.JEEVES, "Oasis catalog files not available for " + schemaName);
            return getInstance();
        }
        return resolverMap.get(schemaName);
    }

    public static Collection<Resolver> getResolvers() {
        return resolverMap.values();
    }

    // Returns or initializes a generic resolver
    public static synchronized Resolver getInstance() {
        if (!resolverMap.containsKey(DEFAULT)) resolverMap.put(DEFAULT, new Resolver());
        return resolverMap.get(DEFAULT);
    }
}

