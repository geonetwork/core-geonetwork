package org.fao.geonet.wro4j;

import com.google.common.base.Joiner;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Wro4j caching strategy that in addition to using an in-memory cache, also writes to disk so that the cache is maintained even after
 * server restarts.
 *
 * @author Jesse on 3/7/2015.
 */
public class DiskbackedCache implements CacheStrategy<CacheKey, CacheValue>, Closeable {
    public static final String NAME = "disk-memory";
    public static final java.lang.String DB_PROP_KEY = "cacheDB";
    private static final String TABLE = "cache";
    private static final String SQL_CLEAR = "DELETE FROM " + TABLE;
    private static final String GROUPNAME = "groupname";
    private static final String TYPE = "type";
    private static final String HASH = "hash";
    private static final String RAW_DATA = "rawdata";
    public static final String SQL_PUT_CACHE_VALUE = "MERGE INTO " + TABLE + " (" + GROUPNAME + ", " + TYPE + ", " + HASH + ", " +
                                                     RAW_DATA + ") VALUES"
                                                     + " (?,?,?,?)";
    public static final String SQL_GET_QUERY = "SELECT " + HASH + "," + RAW_DATA + " FROM " + TABLE + " WHERE " +
                                               GROUPNAME + "=? and " + TYPE + " = ?";
    private final CacheStrategy<CacheKey, CacheValue> defaultCache;
    private final Connection dbConnection;

    public DiskbackedCache(int lruSize, String path) throws SQLException {
        this.defaultCache = new LruMemoryCacheStrategy<>(lruSize);
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
        String[] initSql = {
                "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + GROUPNAME + "  VARCHAR(32) NOT NULL, " + TYPE + " VARCHAR(3) NOT NULL, " +
                HASH + " VARCHAR(256) NOT NULL, " + RAW_DATA + " CLOB NOT NULL, PRIMARY KEY (" + GROUPNAME + ", " + TYPE + "))"
        };
        String init = ";INIT=" + Joiner.on("\\;").join(initSql) + ";DB_CLOSE_DELAY=-1";

        this.dbConnection = DriverManager.getConnection("jdbc:h2:" + path + init, "wro4jcache", "");
    }

    DiskbackedCache(int lruSize) throws SQLException {
        this(lruSize, "mem:" + UUID.randomUUID());
    }

    @Override
    public void put(CacheKey key, CacheValue value) {
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
            dbConnection.close();
        } catch (SQLException e) {
            throw new WroRuntimeException("Database is already closed", e);
        } finally {
            defaultCache.destroy();
        }
    }

    @Override
    public void close() throws IOException {
        destroy();
    }
}
