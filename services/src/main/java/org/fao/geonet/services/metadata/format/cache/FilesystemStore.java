package org.fao.geonet.services.metadata.format.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * A {@link org.fao.geonet.services.metadata.format.cache.PersistentStore} that saves the files to disk.
 *
 * @author Jesse on 3/5/2015.
 */
public class FilesystemStore implements PersistentStore {
    private static final String BASE_CACHE_DIR = "formatter-cache";
    private static final String INFO_TABLE = "info";
    private static final String KEY = "keyhash";
    private static final String CHANGE_DATE = "changedate";
    private static final String PUBLISHED = "published";
    private static final String PATH = "path";
    private static final String STATS_TABLE = "stats";
    private static final String NAME = "name";
    private static final String CURRENT_SIZE = "currentsize";
    private static final String VALUE = "value";

    private static final String QUERY_GET_INFO = "SELECT * FROM " + INFO_TABLE + " WHERE " + KEY + "=?";
    private static final String QUERY_GET_INFO_FOR_RESIZE = "SELECT " +KEY + "," + PATH + " FROM " + INFO_TABLE + " ORDER BY " + CHANGE_DATE + " ASC";
    private static final String QUERY_PUT = "MERGE INTO " + INFO_TABLE + " (" + KEY + "," + CHANGE_DATE + "," + PUBLISHED + "," + PATH + ") VALUES (?,?,?, ?)";
    private static final String QUERY_REMOVE = "DELETE FROM " + INFO_TABLE + " WHERE " + KEY + "=?";
    public static final String QUERY_SETCURRENT_SIZE = "MERGE INTO "+STATS_TABLE + " (" + NAME + ", " + VALUE + ") VALUES ('" + CURRENT_SIZE + "', ?)";
    public static final String QUERY_GETCURRENT_SIZE = "SELECT "+VALUE+" FROM " + STATS_TABLE + " WHERE "+NAME+" = '"+CURRENT_SIZE+"'";

    @Autowired
    private GeonetworkDataDirectory geonetworkDataDir;
    @VisibleForTesting
    Connection metadataDb;
    @VisibleForTesting
    boolean testing = false;
    private volatile long maxSizeB = 10000;
    private volatile long currentSize = 0;

    @PostConstruct
    void init() throws ClassNotFoundException, SQLException {
        // using a h2 database and not normal geonetwork DB to ensure that the accesses are always on localhost and therefore
        // hopefully quick.
        Class.forName("org.h2.Driver");

        String[] initSql = {
                "CREATE SCHEMA IF NOT EXISTS " + INFO_TABLE,
                "CREATE TABLE IF NOT EXISTS " + INFO_TABLE + "(" + KEY + " INT PRIMARY KEY, " + CHANGE_DATE + " BIGINT NOT NULL, " +
                PUBLISHED + " BOOL NOT NULL, " + PATH + " VARCHAR(256) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS " + STATS_TABLE + " (" + NAME + " VARCHAR(32) PRIMARY KEY, " + VALUE + " VARCHAR(32) NOT NULL)"
        };
        String init = ";INIT=" + Joiner.on("\\;").join(initSql) + ";DB_CLOSE_DELAY=-1";
        String dbPath = testing ? "mem:" + UUID.randomUUID() : getBaseCacheDir().resolve("info-store").toString();
        metadataDb = DriverManager.getConnection("jdbc:h2:" + dbPath + init, "fsStore", "");

        try (
                Statement statement = metadataDb.createStatement();
                ResultSet rs = statement.executeQuery(QUERY_GETCURRENT_SIZE)) {
            if (rs.next()) {
                this.currentSize = Long.parseLong(rs.getString(1));
            }
        }
    }

    @PreDestroy
    void close() throws ClassNotFoundException, SQLException {
        metadataDb.close();
    }

    @Override
    public synchronized StoreInfoAndData get(@Nonnull Key key) throws IOException, SQLException {
        StoreInfo info = getInfo(key);
        if (info == null) {
            return null;
        }
        byte[] data = Files.readAllBytes(getPrivatePath(key));
        return new StoreInfoAndData(info, data);
    }

    @Override
    public synchronized StoreInfo getInfo(@Nonnull Key key) throws SQLException {
        try (PreparedStatement statement = this.metadataDb.prepareStatement(QUERY_GET_INFO)) {
            statement.setInt(1, key.hashCode());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long date = resultSet.getLong(CHANGE_DATE);
                    boolean isPublished = resultSet.getBoolean(PUBLISHED);
                    return new StoreInfo(date, isPublished);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public synchronized void put(@Nonnull Key key, @Nonnull StoreInfoAndData data) throws IOException, SQLException {
        resizeIfRequired(key, data);
        final Path privatePath = getPrivatePath(key);

        if (Files.exists(privatePath)) {
            currentSize -= Files.size(privatePath);
        }

        Files.createDirectories(privatePath.getParent());
        Files.write(privatePath, data.data);
        currentSize += data.data.length;

        updateDbCurrentSize();

        Path publicPath = getPublicPath(key);
        Files.deleteIfExists(publicPath);
        if (data.isPublished()) {
            Files.createDirectories(publicPath.getParent());
            try {
                Files.createLink(publicPath, privatePath);
            } catch (UnsupportedOperationException | SecurityException e) {
                // Link likely not supported on this FS use copy then.
                Files.copy(privatePath, publicPath);
            }
        }
        try (PreparedStatement statement = this.metadataDb.prepareStatement(QUERY_PUT)) {
            statement.setInt(1, key.hashCode());
            statement.setLong(2, data.getChangeDate());
            statement.setBoolean(3, data.isPublished());
            statement.setString(4, privatePath.toUri().toString());
            statement.execute();
        }
    }

    private void updateDbCurrentSize() throws SQLException {
        try (PreparedStatement statement = this.metadataDb.prepareStatement(QUERY_SETCURRENT_SIZE)) {
            statement.setString(1, String.valueOf(currentSize));
            statement.execute();
        }
    }

    private void resizeIfRequired(Key key, StoreInfoAndData data) throws IOException, SQLException {
        if (this.currentSize + data.data.length > this.maxSizeB) {
            final Path privatePath = getPrivatePath(key);
            if (Files.exists(privatePath)) {
                long fileSize = Files.size(privatePath);
                if (currentSize - fileSize + data.data.length > this.maxSizeB) {
                    resize();
                }
            } else {
                resize();
            }
        }
    }

    private void resize() throws SQLException, IOException {
        int targetSize = (int) (maxSizeB / 2);
        Log.warning(Geonet.FORMATTER, "Resizing Formatter cache.  Required to reduce size by " + targetSize);
        long startTime = System.currentTimeMillis();
        try (
                Statement statement = metadataDb.createStatement();
                ResultSet resultSet = statement.executeQuery(QUERY_GET_INFO_FOR_RESIZE);
        ) {
            while (currentSize > targetSize && resultSet.next()) {
                Path path = IO.toPath(new URI(resultSet.getString(PATH)));
                doRemove(path, resultSet.getInt(KEY), false);
            }
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
        Log.warning(Geonet.FORMATTER, "Resize took " + (System.currentTimeMillis() - startTime) + "ms to complete");
    }

    @Nullable
    @Override
    public byte[] getPublic(@Nonnull Key key) throws IOException {
        final Path publicPath = getPublicPath(key);
        if (Files.exists(publicPath)) {
            return Files.readAllBytes(publicPath);
        } else {
            return null;
        }
    }

    @Override
    public synchronized void remove(@Nonnull Key key) throws IOException, SQLException {
        final Path path = getPrivatePath(key);
        final int keyHashCode = key.hashCode();
        doRemove(path, keyHashCode, true);
    }

    private void doRemove(Path path, int keyHashCode, boolean updateDbCurrentSize) throws IOException, SQLException {
        try {
            if (Files.exists(path)) {
                currentSize -= Files.size(path);
                Files.delete(path);
            }
        } finally {
            try {
                Path relativePrivate = getBaseCacheDir().resolve(Params.Access.PRIVATE).relativize(path);
                Files.deleteIfExists(getBaseCacheDir().resolve(Params.Access.PUBLIC).resolve(relativePrivate));
            } finally {
                try (PreparedStatement statement = metadataDb.prepareStatement(QUERY_REMOVE)) {
                    statement.setInt(1, keyHashCode);
                    statement.execute();
                } finally {
                    if (updateDbCurrentSize) {
                        updateDbCurrentSize();
                    }
                }
            }
        }
    }

    public void setGeonetworkDataDir(GeonetworkDataDirectory geonetworkDataDir) {
        this.geonetworkDataDir = geonetworkDataDir;
    }

    public Path getPrivatePath(Key key) {
        return getCacheFile(key, false);
    }

    public Path getPublicPath(Key key) {
        return getCacheFile(key, true);
    }

    private Path getCacheFile(Key key, boolean isPublicCache) {
        final String accessDir = isPublicCache ? Params.Access.PUBLIC : Params.Access.PRIVATE;
        final String sMdId = String.valueOf(key.mdId);
        final Path metadataDir = Lib.resource.getMetadataDir(getBaseCacheDir().resolve(accessDir), sMdId);
        return metadataDir.resolve(key.formatterId).resolve(key.lang).resolve(key.hashCode() + "." + key.formatType.name());
    }

    private Path getBaseCacheDir() {
        return geonetworkDataDir.getHtmlCacheDir().resolve(BASE_CACHE_DIR);
    }

    public void setMaxSizeKb(long maxSize) {
        this.maxSizeB = maxSize * 1024;
    }

    public void setMaxSizeMb(int maxSize) {
        setMaxSizeKb(maxSize * 1024);
    }

    public void setMaxSizeGb(int maxSize) {
        setMaxSizeMb(maxSize * 1024);
    }
}
