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

package org.fao.geonet;

import org.apache.commons.jcs3.access.GroupCacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Properties;

/**
 * GroupCache of Object looked up by String.
 *
 * The GroupCacheAccess is intended to store a class of objects, backed by generic type support.
 * By changing to a cache of Object we are subverting this expectation in order to maintain the JCS
 * 1.x contact.
 *
 * Makes use of CacheManager from {@link JeevesJCS#ensureCacheManager()}.
 *
 * @author jeichar
 */
public class JeevesJCSGroup extends GroupCacheAccess<String, Object> {

    private final String region;

    protected JeevesJCSGroup(CompositeCache cacheControl, String region)
    throws CacheException {
        super(cacheControl);
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    /**
     * Get a GeonetworkJCS which accesses the provided region. <p>
     *
     * @param region Region that return GeonetworkJCS will provide access to objects
     * @return A GeonetworkJCS which provides access to a given region.
     */
    public static JeevesJCSGroup getInstance(String region)
        throws CacheException {
        JeevesJCS.ensureCacheManager();

        return new JeevesJCSGroup(JeevesJCS.cacheMgr.getCache(region), region);
    }

    /**
     * Get a GeonetworkJCS which accesses the provided region. <p>
     *
     * @param region Region that return GeonetworkJCS will provide access to
     * @param icca   CacheAttributes for region
     * @return A GeonetworkJCS which provides access to a given region.
     */
    public static JeevesJCSGroup getInstance(String region, ICompositeCacheAttributes icca)
        throws CacheException {

        JeevesJCS.ensureCacheManager();

        return new JeevesJCSGroup(JeevesJCS.cacheMgr.getCache(region, icca), region);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JeevesJCSGroup{");
        sb.append("region='").append(region).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
