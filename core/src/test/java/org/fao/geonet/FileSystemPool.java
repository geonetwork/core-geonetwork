/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;

/**
 * Responsible for obtaining in memory filesystems for tests.  In order to reduce memory consumption
 * this class will recycle filesystems.  The reason this is needed is because when each test obtains
 * a new FS and copies all the files to the new FS, then a great deal of memory is consumed and then
 * needs to be garbage collected.  This can result in 2 types of OOM errors.  One is a heap space
 * error where all the heap space is used up.  The other is a GC overhead exceeded error where there
 * is too much garbage collection and the JVM throws an error regarding this.
 * <p/>
 * This class also synchronizes each filesystem that is obtained with the template file system by
 * ensuring that all files are the same as the files in the template.
 *
 * @author Jesse on 11/24/2014.
 */
public class FileSystemPool {
    private static final int MAX_FS = 5;
    private final CreatedFs template = new CreatedFs(Jimfs.newFileSystem("template", Configuration.unix()), "/", "data");
    private final Stack<CreatedFs> pool = new Stack<>();
    private int openFs = 0;

    public synchronized CreatedFs getTemplate() {
        return template;
    }

    public synchronized CreatedFs get(String fsId) throws IOException {
        while (openFs > MAX_FS && pool.isEmpty()) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
                Jimfs.newFileSystem(fsId, Configuration.unix());
            }
        }

        final CreatedFs fileSystem;
        if (pool.isEmpty()) {
            openFs++;
            fileSystem = new CreatedFs(Jimfs.newFileSystem(fsId, Configuration.unix()), "nodes", "default_data_dir");
        } else {
            fileSystem = pool.pop();
            org.junit.Assert.assertNotNull(fileSystem);
        }

        syncWithTemplate(fileSystem);
        IO.setFileSystemThreadLocal(fileSystem.fs);

        return fileSystem;
    }

    public synchronized void release(CreatedFs fs) {
        org.junit.Assert.assertNotNull(fs);
        pool.add(fs);
    }


    private void syncWithTemplate(CreatedFs fileSystem) throws IOException {
        org.junit.Assert.assertNotNull(fileSystem);
        removeModifiedFiles(fileSystem);
        copyMissingFiles(fileSystem);
    }

    private void removeModifiedFiles(final CreatedFs fileSystem) throws IOException {
        if (!exists(fileSystem.dataDir)) {
            return;
        }

        Files.walkFileTree(fileSystem.dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = fileSystem.dataDir.relativize(dir);

                if (!exists(template.dataDir.resolve(relativePath.toString()))) {
                    IO.deleteFileOrDirectory(dir);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path templatePath = template.dataDir.resolve(fileSystem.dataDir.relativize(file).toString());
                if (!exists(templatePath)) {
                    Files.delete(file);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private void copyMissingFiles(final CreatedFs fileSystem) throws IOException {
        Files.walkFileTree(template.dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path newDir = fileSystem.dataDir.resolve(template.dataDir.relativize(dir).toString());
                Files.createDirectories(newDir);
                Files.setLastModifiedTime(newDir, Files.getLastModifiedTime(dir));

                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path newFsPath = fileSystem.dataDir.resolve(template.dataDir.relativize(file).toString());
                if (!exists(newFsPath) || getLastModifiedTime(file).toMillis() < getLastModifiedTime(newFsPath).toMillis()) {
                    Files.copy(file, newFsPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    public class CreatedFs {
        public final FileSystem fs;
        public final Path dataDirContainer;
        public final Path dataDir;

        public CreatedFs(FileSystem fs, String dataDirContainer, String dataDir) {
            this.fs = fs;
            this.dataDirContainer = fs.getPath(dataDirContainer);
            this.dataDir = this.dataDirContainer.resolve(dataDir);
        }
    }
}
