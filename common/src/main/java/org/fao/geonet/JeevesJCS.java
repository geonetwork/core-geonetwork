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

import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author jeichar
 */
public class JeevesJCS extends GroupCacheAccess
{

    /** The manager returns cache instances. */
    private static CompositeCacheManager cacheMgr;
    private static Path configFilename;

    protected JeevesJCS(CompositeCache cacheControl)
    {
        super(cacheControl);
    }
    /**
     * Get a GeonetworkJCS which accesses the provided region.
     * <p>
     * @param region Region that return GeonetworkJCS will provide access to
     * @return A GeonetworkJCS which provides access to a given region.
     * @exception CacheException
     */
    public static JeevesJCS getInstance( String region )
        throws CacheException
    {
        ensureCacheManager();

        return new JeevesJCS( cacheMgr.getCache( region ) );
    }

    /**
     * Get a GeonetworkJCS which accesses the provided region.
     * <p>
     * @param region Region that return GeonetworkJCS will provide access to
     * @param icca CacheAttributes for region
     * @return A GeonetworkJCS which provides access to a given region.
     * @exception CacheException
     */
    public static JeevesJCS getInstance( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        ensureCacheManager();

        return new JeevesJCS( cacheMgr.getCache( region, icca ) );
    }

    /**
     * Gets an instance of CompositeCacheManager and stores it in the cacheMgr class field, if it is
     * not already set. Unlike the implementation in CacheAccess, the cache manager is a
     * CompositeCacheManager. NOTE: This can will be moved up into GroupCacheAccess.
     */
    protected static synchronized void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            if ( configFilename == null )
            {
                cacheMgr = CompositeCacheManager.getInstance();
            }
            else
            {
                cacheMgr = CompositeCacheManager.getUnconfiguredInstance();

                configure( );
            }
        }
    }
    

    private static void configure()
    {

        Properties props = new Properties();

        try (Reader is = IO.newBufferedReader(configFilename, Constants.CHARSET)) {
            props.load( is );
        } catch (IOException e) {
            throw new IllegalStateException( "Unable to load cache configuration from: "+configFilename, e);
        }

        cacheMgr.configure( props );
            
    }
    /**
     * Set the filename that the cache manager will be initialized with. Only matters before the
     * instance is initialized.
     * <p>
     * @param configFilename
     */
    public static void setConfigFilename( Path configFilename )
    {
        cacheMgr=null;
        JeevesJCS.configFilename = configFilename;
    }
    
    
}
