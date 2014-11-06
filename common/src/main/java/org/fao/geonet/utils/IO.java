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
import org.fao.geonet.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//=============================================================================

/**
 * A container of I/O methods. <P>
 * 
 */
public final class IO {
    private static FileSystem defaultFs = FileSystems.getDefault();
    private static ThreadLocal<FileSystem> defaultFsThreadLocal = new InheritableThreadLocal<>();

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

    /**
    * Default constructor.
    * Builds a IO.
    */
   private IO() {}

    public static void deleteFile(Path file, boolean throwException, String loggerModule) {
        deleteFile(file, throwException, Log.createLogger(loggerModule));
    }

    public static void deleteFile(Path file, boolean throwException, Logger context) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            if(throwException) {
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
    public static void copyDirectoryOrFile(final Path from, final Path to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        final Path actualTo;
        if (Files.isDirectory(to)) {
            actualTo = to.resolve(from.getFileName().toString());
        } else {
            actualTo = to;
        }

        if (from.equals(to)) {
            return;
        }

        if (Files.isDirectory(from)) {
            Assert.isTrue(!Files.isRegularFile(actualTo), "cannot copy a directory to a file. From: " + from + " to " + actualTo);
            Files.walkFileTree(from, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectory(relativeFile(dir, actualTo, from));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, relativeFile(file, actualTo, from));
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            final Path parent = actualTo.getParent();
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.copy(from, actualTo);
        }
    }

    protected static Path relativeFile(Path dir, Path to, Path from) {
        return to.resolve(from.relativize(dir).toString().replace('\\', '/'));
    }

    public static void moveDirectoryOrFile(final Path from, final Path to) throws IOException {
        final Path actualTo;
        if (Files.isDirectory(to)) {
            actualTo = to.resolve(from.getFileName());
        } else {
            actualTo = to;
        }

        final Path parent = actualTo.getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.move(from, actualTo);
    }
    public static boolean isEmptyDir(Path dir) throws IOException {
        try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
            final Iterator<Path> iterator = children.iterator();
            return !iterator.hasNext();
        }
    }

    public static void deleteFileOrDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } else if (Files.isRegularFile(path)) {
            Files.delete(path);
        }
    }
    public static void touch(Path file) throws IOException{
        long timestamp = System.currentTimeMillis();
        touch(file, FileTime.from(timestamp, TimeUnit.MILLISECONDS));
    }

    public static void touch(Path file, FileTime timestamp) throws IOException{
        if (!Files.exists(file)) {
            Path parent = file.getParent();
            if (!Files.exists(parent)) {
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
        return Paths.get(uri);
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

}

//=============================================================================

