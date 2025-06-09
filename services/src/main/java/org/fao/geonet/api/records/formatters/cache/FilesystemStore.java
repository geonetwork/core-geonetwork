/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.formatters.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.UUID;

import static org.fao.geonet.constants.Params.Access.PRIVATE;
import static org.fao.geonet.constants.Params.Access.PUBLIC;

/**
 * A {@link org.fao.geonet.api.records.formatters.cache.PersistentStore} that saves the files to
 * disk.
 *
 * @author Jesse on 3/5/2015.
 */
public class FilesystemStore implements PersistentStore {
    public static final String WITHHELD_MD_DIRNAME = "withheld_md";
    public static final String FULL_MD_NAME = "full_md";
    private static final String BASE_CACHE_DIR = "formatter-cache";
    private static final String INFO_TABLE = "info";
    private static final String KEY = "keyhash";
    private static final String CHANGE_DATE = "changedate";
    private static final String PUBLISHED = "published";
    private static final String PATH = "path";
    private static final String STATS_TABLE = "stats";
    private static final String NAME = "name";
    private static final String CURRENT_SIZE = "currentsize";
    private static final String VALUE = "statvalue";
    public static final String QUERY_SETCURRENT_SIZE = "MERGE INTO " + STATS_TABLE + " (" + NAME + ", " + VALUE + ") VALUES ('" + CURRENT_SIZE + "', ?)";
    public static final String QUERY_GETCURRENT_SIZE = "SELECT " + VALUE + " FROM " + STATS_TABLE + " WHERE " + NAME + " = '" + CURRENT_SIZE + "'";
    private static final String QUERY_GET_INFO = "SELECT * FROM " + INFO_TABLE + " WHERE " + KEY + "=?";
    private static final String QUERY_GET_INFO_FOR_RESIZE = "SELECT " + KEY + "," + PATH + " FROM " + INFO_TABLE + " ORDER BY " + CHANGE_DATE + " ASC";
    private static final String QUERY_PUT = "MERGE INTO " + INFO_TABLE + " (" + KEY + "," + CHANGE_DATE + "," + PUBLISHED + "," + PATH + ") VALUES (?,?,?, ?)";
    private static final String QUERY_REMOVE = "DELETE FROM " + INFO_TABLE + " WHERE " + KEY + "=?";
    private static final String QUERY_CLEAR_INFO = "DELETE FROM " + INFO_TABLE;
    private static final String QUERY_CLEAR_STATS = "DELETE FROM " + STATS_TABLE;
    @VisibleForTesting
    Connection metadataDb;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDir;
    private boolean testing = false;
    private volatile long maxSizeB = 10000;
    private volatile long currentSize = 0;
    private volatile boolean initialized = false;

    private synchronized void init() throws SQLException {
        if (!initialized) {
            // using a h2 database and not normal geonetwork DB to ensure that the accesses are always on localhost and therefore
            // hopefully quick.
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            }

            String[] initSql = {
                "CREATE SCHEMA IF NOT EXISTS " + INFO_TABLE,
                "CREATE TABLE IF NOT EXISTS " + INFO_TABLE + "(" + KEY + " INT PRIMARY KEY, " + CHANGE_DATE + " BIGINT NOT NULL, " +
                    PUBLISHED + " BOOL NOT NULL, " + PATH + " CLOB  NOT NULL)",
                "CREATE TABLE IF NOT EXISTS " + STATS_TABLE + " (" + NAME + " VARCHAR(64) PRIMARY KEY, " + VALUE + " VARCHAR(32) NOT NULL)"

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
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        close();
                    } catch (SQLException | ClassNotFoundException e) {
                        Log.error(Geonet.FORMATTER, "Error shutting down FilesystemStore Database", e);
                    }
                }
            }));
            initialized = true;
        }

    }

    @PreDestroy
    synchronized void close() throws ClassNotFoundException, SQLException {
        Log.info(Geonet.FORMATTER, "Stopping the FileSystemStore");
        if (metadataDb != null) {
            metadataDb.close();
        }
    }

    @Override
    public synchronized StoreInfoAndData get(@Nonnull Key key) throws IOException, SQLException {
        init();
        StoreInfo info = getInfo(key);
        if (info == null) {
            return null;
        }
        byte[] data = Files.readAllBytes(getPrivatePath(key));
        return new StoreInfoAndData(info, data);
    }

    @Override
    public synchronized StoreInfo getInfo(@Nonnull Key key) throws SQLException {
        init();
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
        init();
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
        // only publish if withheld (hidden) elements are hidden.
        if (data.isPublished() && key.hideWithheld) {
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
            ResultSet resultSet = statement.executeQuery(QUERY_GET_INFO_FOR_RESIZE)
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
    public byte[] getPublished(@Nonnull Key key) throws IOException {
        try {
            init();
        } catch (SQLException e) {
            throw new Error(e);
        }
        final Path publicPath = getPublicPath(key);
        if (Files.exists(publicPath)) {
            return Files.readAllBytes(publicPath);
        } else {
            return null;
        }
    }

    @Override
    public synchronized void remove(@Nonnull Key key) throws IOException, SQLException {
        init();
        final Path path = getPrivatePath(key);
        final int keyHashCode = key.hashCode();
        doRemove(path, keyHashCode, true);
    }

    @Override
    public void setPublished(int metadataId, final boolean published) throws IOException {
        // TODO: Datastore review
        final Path metadataDir = Lib.resource.getMetadataDir(getBaseCacheDir().resolve(PRIVATE), String.valueOf(metadataId));
        if (Files.exists(metadataDir)) {
            Files.walkFileTree(metadataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getFileName().toString().equals(FULL_MD_NAME)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path privatePath, BasicFileAttributes attrs) throws IOException {
                    final Path publicPath = toPublicPath(privatePath);
                    if (published) {
                        if (!Files.exists(publicPath)) {
                            if (!Files.exists(publicPath.getParent())) {
                                Files.createDirectories(publicPath.getParent());
                            }
                            Files.createLink(publicPath, privatePath);
                        }
                    } else {
                        Files.deleteIfExists(publicPath);
                    }
                    return super.visitFile(privatePath, attrs);
                }
            });
        }
    }

    @Override
    public void clear() throws SQLException, IOException {
        init();
        try (Statement statement = this.metadataDb.createStatement()) {
            statement.execute(QUERY_CLEAR_INFO);
            statement.execute(QUERY_CLEAR_STATS);
            currentSize = 0;
            Files.walkFileTree(getBaseCacheDir(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        // ignore
                    }
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                    } catch (IOException e) {
                        // ignore
                    }
                    return super.postVisitDirectory(dir, exc);
                }
            });
        }
    }

    private void doRemove(Path privatePath, int keyHashCode, boolean updateDbCurrentSize) throws IOException, SQLException {
        try {
            if (Files.exists(privatePath)) {
                currentSize -= Files.size(privatePath);
                Files.delete(privatePath);
            }
        } finally {
            try {
                final Path publicPath = toPublicPath(privatePath);
                Files.deleteIfExists(publicPath);
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

    private Path toPublicPath(Path privatePath) {
        Path relativePrivate = getBaseCacheDir().resolve(PRIVATE).relativize(privatePath);
        return getBaseCacheDir().resolve(PUBLIC).resolve(relativePrivate);
    }

    public void setGeonetworkDataDir(GeonetworkDataDirectory geonetworkDataDir) {
        this.geonetworkDataDir = geonetworkDataDir;
    }

    public Path getPrivatePath(Key key) throws IOException {
        return getCacheFile(key, false);
    }

    public Path getPublicPath(Key key) throws IOException {
        return getCacheFile(key, true);
    }

    private Path getCacheFile(Key key, boolean isPublicCache) throws IOException {
        final String accessDir = isPublicCache ? PUBLIC : PRIVATE;
        final String sMdId = String.valueOf(key.mdId);
        final Path metadataDir = Lib.resource.getMetadataDir(getBaseCacheDir().resolve(accessDir), sMdId);
        String hidden = key.hideWithheld ? WITHHELD_MD_DIRNAME : FULL_MD_NAME;
        return metadataDir.resolve(key.formatterId).resolve(key.lang).resolve(hidden).resolve(key.hashCode() + "." + key.formatType.name());
    }

    private Path getBaseCacheDir() {
        return geonetworkDataDir.getHtmlCacheDir().resolve(BASE_CACHE_DIR);
    }

    public void setMaxSizeKb(long maxSize) {
        this.maxSizeB = maxSize * 1024;
    }

    public void setMaxSizeMb(int maxSize) {
        setMaxSizeKb(((long) maxSize) * 1024);
    }

    public void setMaxSizeGb(int maxSize) {
        setMaxSizeMb(maxSize * 1024);
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }
}
