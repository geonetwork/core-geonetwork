//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
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

import org.eclipse.core.runtime.Assert;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.debug.DebuggingInputStream;
import org.fao.geonet.utils.debug.DebuggingReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//=============================================================================

/**
 * A container of I/O methods. <P>
 */
public final class IO {
    public static final DirectoryStream.Filter<Path> DIRECTORIES_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isDirectory(entry);
        }
    };
    public static final DirectoryStream.Filter<Path> FILES_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isRegularFile(entry);
        }
    };
    static FileSystem defaultFs = FileSystems.getDefault();
    static ThreadLocal<FileSystem> defaultFsThreadLocal = new InheritableThreadLocal<>();

    /**
     * Default constructor. Builds a IO.
     */
    private IO() {
    }

    public static void deleteFile(Path file, boolean throwException, String loggerModule) {
        deleteFile(file, throwException, Log.createLogger(loggerModule));
    }

    public static void deleteFile(Path file, boolean throwException, Logger context) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                context.error(e);
            }
        }
    }

    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public static void copyDirectoryOrFile(@Nonnull final Path from,
                                           @Nonnull final Path to,
                                           boolean copyInto) throws IOException {
        copyDirectoryOrFile(from, to, copyInto, null);
    }

    /**
     * Copy a file or directories from the from path to the to path.
     *
     * @param from   the source
     * @param to     the destination
     * @param filter a filter to control which files to copy.  May be null
     */
    public static void copyDirectoryOrFile(@Nonnull final Path from,
                                           @Nonnull final Path to,
                                           boolean copyInto,
                                           @Nullable final DirectoryStream.Filter<Path> filter) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        final Path actualTo;
        if (copyInto && Files.isDirectory(to)) {
            actualTo = to.resolve(from.getFileName().toString());
        } else {
            actualTo = to;
        }

        if (from.equals(to)) {
            return;
        }

        if (Files.isDirectory(from)) {
            Assert.isTrue(!Files.isRegularFile(actualTo), "cannot copy a directory to a file. From: " + from + " to " + actualTo);
            if (filter == null) {
                Files.walkFileTree(from, new CopyAllFiles(from, actualTo));
            } else {
                Files.walkFileTree(from, new CopyAcceptedFiles(from, actualTo, filter));
            }
        } else if (Files.exists(from)) {
            if (filter == null || filter.accept(from)) {
                final Path parent = actualTo.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.copy(from, actualTo);
            }
        }
    }

    public static Path relativeFile(Path relativeToDirectory, Path fileOrDirectory, Path newRelativeTo) {
        return newRelativeTo.resolve(relativeToDirectory.relativize(fileOrDirectory).toString().replace('\\', '/'));
    }

    public static void moveDirectoryOrFile(final Path from, final Path to, boolean copyInto) throws IOException {
        final Path actualTo;
        if (copyInto && Files.isDirectory(to)) {
            actualTo = to.resolve(from.getFileName());
        } else {
            actualTo = to;
        }

        if (Files.exists(from)) {
            final Path parent = actualTo.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.move(from, actualTo);
        }
    }

    public static boolean isEmptyDir(Path dir) throws IOException {
        try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
            final Iterator<Path> iterator = children.iterator();
            return !iterator.hasNext();
        }
    }

    public static void deleteFileOrDirectory(Path path) throws IOException {
        deleteFileOrDirectory(path, false);
    }

    public static void deleteFileOrDirectory(Path path, final boolean ignoreErrors) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.delete(file);
                    } catch (final Throwable t) {
                        if (!ignoreErrors) {
                            throw t;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                    } catch (final Throwable t) {
                        if (!ignoreErrors) {
                            throw t;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } else if (Files.isRegularFile(path)) {
            try {
                Files.delete(path);
            } catch (final Throwable t) {
                if (!ignoreErrors) {
                    throw t;
                }
            }
        }
    }

    public static void touch(Path file) throws IOException {
        long timestamp = System.currentTimeMillis();
        touch(file, FileTime.from(timestamp, TimeUnit.MILLISECONDS));
    }

    public static void touch(Path file, FileTime timestamp) throws IOException {
        if (!Files.exists(file)) {
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.createFile(file);
        }

        Files.setLastModifiedTime(file, timestamp);
    }

    public static Path toPath(String firstPart, String... more) {
        FileSystem fileSystem = defaultFsThreadLocal.get();
        if (fileSystem == null) {
            fileSystem = defaultFs;
        }
        return fileSystem.getPath(firstPart, more);
    }

    public static Path toPath(URI uri) {
        try {
            return Paths.get(uri);
        } catch (FileSystemNotFoundException e) {
            if (uri.toString().startsWith("jar:")) {
                throw new IllegalStateException("The zip file references in URI: " + uri + " has not been opened.  " +
                    "Before you can create this path you must first call ZipUtil.openZipFS with the url to " +
                    "the zip file");
            }
            throw new FileSystemNotFoundException("No filesystem found for the uri: " + uri);
        }
    }

    public static void setFileSystem(FileSystem newFileSystem) {
        if (newFileSystem == null) {
            newFileSystem = FileSystems.getDefault();
        }
        defaultFs = newFileSystem;
    }

    public static void setFileSystemThreadLocal(FileSystem newFileSystem) {
        defaultFsThreadLocal.set(newFileSystem);
    }

    public static URL toURL(Path textFile) throws MalformedURLException {
        return toURL(textFile.toUri());
    }

    /**
     * Convert the URI to a URL.  If the file system is not a default one the URL scheme may not be
     * registered so we need to make the URL in such a way that the scheme is registered in its url
     * context.
     */
    public static URL toURL(URI uri) throws MalformedURLException {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            URL url = createFsSpecificURL(uri, defaultFs);
            if (url != null) {
                return url;
            }
            url = createFsSpecificURL(uri, defaultFsThreadLocal.get());
            if (url != null) {
                return url;
            }

            throw e;
        }
    }

    private static URL createFsSpecificURL(URI uri, FileSystem fileSystem) throws MalformedURLException {
        if (fileSystem != null && uri.getScheme().equals(fileSystem.getPath(".").toUri().getScheme())) {
            return new URL(null, uri.toString(), new FileSystemSpecificStreamHandler());
        }
        return null;
    }

    public static InputStream newInputStream(Path file) throws IOException {
        if (ApplicationContextHolder.get() != null && ApplicationContextHolder.get().getBeansOfType(SystemInfo.class).size() > 0 &&
            ApplicationContextHolder.get().getBean(SystemInfo.class).isDevMode()) {
            return new DebuggingInputStream(file.toString(), Files.newInputStream(file));
        } else {
            return Files.newInputStream(file);
        }
    }

    public static BufferedReader newBufferedReader(Path path, Charset cs) throws IOException {
        if (ApplicationContextHolder.get() != null && ApplicationContextHolder.get().getBeansOfType(SystemInfo.class).size() > 0 &&
            ApplicationContextHolder.get().getBean(SystemInfo.class).isDevMode()) {
            return new DebuggingReader(path.toString(), Files.newBufferedReader(path, cs));
        } else {
            return Files.newBufferedReader(path, cs);
        }
    }

    private static class CopyAllFiles extends SimpleFileVisitor<Path> {
        private final Path from;
        private final Path actualTo;

        public CopyAllFiles(Path from, Path actualTo) {
            this.from = from;
            this.actualTo = actualTo;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(relativeFile(from, dir, actualTo));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, relativeFile(from, file, actualTo), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            return FileVisitResult.CONTINUE;
        }
    }

    private static class CopyAcceptedFiles extends SimpleFileVisitor<Path> {
        private final Path from;
        private final Path actualTo;
        private final DirectoryStream.Filter<Path> filter;

        public CopyAcceptedFiles(@Nonnull Path from, @Nonnull Path actualTo, @Nonnull DirectoryStream.Filter<Path> filter) {
            this.from = from;
            this.actualTo = actualTo;
            this.filter = filter;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            final Path newDir = relativeFile(from, dir, actualTo);
            Files.createDirectories(newDir);
            Files.setLastModifiedTime(newDir, Files.getLastModifiedTime(dir));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (filter.accept(file)) {
                Files.copy(file, relativeFile(from, file, actualTo), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            final Path destDir = relativeFile(from, dir, actualTo);
            if (IO.isEmptyDir(destDir)) {
                Files.delete(destDir);
            }
            return FileVisitResult.CONTINUE;
        }
    }

}

//=============================================================================

