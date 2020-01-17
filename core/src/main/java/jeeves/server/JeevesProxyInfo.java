//==============================================================================
//===
//=== JeevesProxyInfo
//===
//==============================================================================
//===	Copyright (C) GeoNetwork
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

package jeeves.server;

import java.util.Collection;

import org.fao.geonet.utils.ProxyInfo;
import org.fao.geonet.utils.Resolver;
import org.fao.geonet.utils.ResolverWrapper;

/**
 * Singleton which handles the interface to Jeeves classes that need proxy information - classes
 * that need proxy info must be added as observers when ProxyInfo is created in getInstance
 */
public class JeevesProxyInfo {

    private static ProxyInfo proxyInfo = null;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor - protected as per singleton rules
    //---
    //--------------------------------------------------------------------------

    protected JeevesProxyInfo() {
    }

    //---------------------------------------------------------------------------

    public synchronized static ProxyInfo getInstance() {

        if (proxyInfo == null) {
            proxyInfo = new ProxyInfo();
            // NOTE: Add new classes that observe ProxyInfo here
            // Resolvers
            Collection<Resolver> resolvers = ResolverWrapper.getResolvers();
            for (Resolver resolver : resolvers) {
                proxyInfo.addObserver(resolver);
            }
        }
        return proxyInfo;
    }

}

//=============================================================================

