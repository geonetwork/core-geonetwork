//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.wro4j;

import com.google.common.base.Joiner;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

/**
 * Wro4j caching strategy that in addition to using an in-memory cache, also writes to disk so that
 * the cache is maintained even after server restarts.
 *
 * @author Jesse on 3/7/2015.
 */
public class DiskbackedCache implements CacheStrategy<CacheKey, CacheValue>, Closeable {
    public static final String NAME = "disk-memory";
    public static final String DB_PROP_KEY = "cacheDB";
    private static final String TABLE = "cache";
    private static final String SQL_CLEAR = "DELETE FROM " + TABLE;

    private static final String SQL_DELETE_CSS = "DELETE FROM " + TABLE + " WHERE TYPE='CSS'";
    private static final String SQL_SHUTDOWN = "SHUTDOWN";
    private static final String GROUPNAME = "groupname";
    private static final String TYPE = "type";
    private static final String HASH = "hash";
    private static final String RAW_DATA = "rawdata";
    public static final String SQL_PUT_CACHE_VALUE = "MERGE INTO " + TABLE + " (" + GROUPNAME + ", " + TYPE + ", " + HASH + ", " +
        RAW_DATA + ") VALUES"
        + " (?,?,?,?)";
    public static final String SQL_GET_QUERY = "SELECT " + HASH + "," + RAW_DATA + " FROM " + TABLE + " WHERE " +
        GROUPNAME + "=? and " + TYPE + " = ?";
    private final String dbPathString;
    private CacheStrategy<CacheKey, CacheValue> defaultCache;
    private Connection dbConnection;
    private boolean destroyed = false;

    public DiskbackedCache(int lruSize, String dbPathString) {
        this.defaultCache = new LruMemoryCacheStrategy<>(lruSize);
        this.dbPathString = dbPathString;
    }

    DiskbackedCache(int lruSize) {
        this(lruSize, "mem:" + UUID.randomUUID());
    }

    private synchronized void init() {
        if (this.destroyed) {
            throw new WroRuntimeException("DiskbackedCache has already been destroyed/closed");
        }
        // The db has already been initialized, no need to (try to) open it back
        if (dbConnection != null)
            return;
        String path = this.dbPathString;
        if (path == null) {
            if (ApplicationContextHolder.get() != null) {
                GeonetworkDataDirectory geonetworkDataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
                path = geonetworkDataDirectory.getHtmlCacheDir().resolve("wro4j-cache").toString();
            }
        }

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
        String[] initSql = {
            "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + GROUPNAME + "  VARCHAR(128) NOT NULL, " + TYPE + " VARCHAR(3) NOT NULL, " +
                HASH + " VARCHAR(256) NOT NULL, " + RAW_DATA + " CLOB NOT NULL, PRIMARY KEY (" + GROUPNAME + ", " + TYPE + "))"
        };
        String init = ";INIT=" + Joiner.on("\\;").join(initSql) + ";DB_CLOSE_ON_EXIT=FALSE;";

        try {
            this.dbConnection = DriverManager.getConnection("jdbc:h2:" + path + init, "wro4jcache", "");
        } catch (SQLException e) {
            throw new WroRuntimeException("Error creating the wro4j disk-cache", e);
        }
    }

    @Override
    public void put(CacheKey key, CacheValue value) {
        init();
        defaultCache.put(key, value);
        try (PreparedStatement statement = dbConnection.prepareStatement(SQL_PUT_CACHE_VALUE)) {
            statement.setString(1, key.getGroupName());
            statement.setString(2, key.getType().toString());
            statement.setString(3, value.getHash());
            statement.setString(4, value.getRawContent());

            statement.execute();
        } catch (SQLException e) {
            throw new WroRuntimeException("Error putting a value into the cache", e);
        }
    }

    @Override
    public CacheValue get(CacheKey key) {
        init();
        final CacheValue cacheValue = defaultCache.get(key);
        if (cacheValue != null) {
            return cacheValue;
        }
        try (PreparedStatement statement = dbConnection.prepareStatement(SQL_GET_QUERY)) {
            statement.setString(1, key.getGroupName());
            statement.setString(2, key.getType().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hash = resultSet.getString(HASH);
                    String rawData = resultSet.getString(RAW_DATA);
                    final CacheValue value = CacheValue.valueOf(rawData, hash);
                    defaultCache.put(key, value);
                    return value;
                }
            }
        } catch (SQLException e) {
            throw new WroRuntimeException("Error removing a value from the cache", e);
        }
        return null;
    }

    @Override
    public void clear() {
        init();
        try (Statement statement = dbConnection.createStatement()) {
            statement.execute(SQL_CLEAR);
        } catch (SQLException e) {
            throw new WroRuntimeException("Error clearing the cache", e);
        }
        defaultCache.clear();
    }

    @Override
    public void destroy() {
        try {
            if (dbConnection != null) {
                dbConnection.createStatement().execute(SQL_SHUTDOWN);
                dbConnection.close();
            }
        } catch (SQLException e) {
            throw new WroRuntimeException("Database is already closed", e);
        } finally {
            defaultCache.destroy();
            this.destroyed = true;
        }
    }

    @Override
    public void close() throws IOException {
        destroy();
    }

    /**
     * Deletes all the items of type CSS from the database.
     */
    public void deleteCSSItems() {
        init();
        try (Statement statement = dbConnection.createStatement()) {
            statement.execute(SQL_DELETE_CSS);
        } catch (SQLException e) {
            throw new WroRuntimeException("Error deleting CSS items from cache", e);
        }
    }
}
