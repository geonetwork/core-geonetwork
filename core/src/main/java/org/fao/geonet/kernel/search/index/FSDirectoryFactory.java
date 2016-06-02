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

package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import com.google.common.annotations.VisibleForTesting;

/**
 * Create filesystem based Directory objects. <p> Because windows locks files the reset methods will
 * sometime create new index directories.  That is the reason for all the strange checking for new
 * names and the {@link #DELETE_DIR_FLAG_FILE} file names. </p>
 *
 * User: Jesse Date: 10/18/13 Time: 11:25 AM
 */
public class FSDirectoryFactory implements DirectoryFactory {

    public static final String NON_SPATIAL_DIR = "index";
    public static final String TAXONOMY_DIR = "taxonomy";
    private static final String DELETE_DIR_FLAG_FILE = "This_directory_could_not_be_deleted_during_reindex";

    protected volatile Path taxonomyFile;
    protected volatile Path indexFile;

    public synchronized void init() throws IOException {

        GeonetworkDataDirectory dataDir = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        if (taxonomyFile == null) {
            final Path luceneDir = dataDir.getLuceneDir();
            if (luceneDir == null) {
                throw new IllegalStateException("This object cannot be constructed until GeonetworkDataDirectory has been initialized");
            }
            indexFile = findLatestIndexDir(NON_SPATIAL_DIR);

            taxonomyFile = findLatestIndexDir(TAXONOMY_DIR);
        }
    }

    private Path findLatestIndexDir(String baseName) throws IOException {
        GeonetworkDataDirectory dataDir = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);

        Path indexDir = null;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dataDir.getLuceneDir())) {
            Iterator<Path> pathIter = paths.iterator();
            while (pathIter.hasNext()) {
                Path file = pathIter.next();
                if (file.getFileName().toString().equals(baseName) && !Files.exists(file.resolve(DELETE_DIR_FLAG_FILE))) {
                    if (indexDir == null || indexDir.getFileName().compareTo(file.getFileName()) < 0) {
                        indexDir = file;
                    }
                }
            }
        }

        if (indexDir == null) {
            return dataDir.getLuceneDir().resolve(baseName);
        }
        return indexDir;
    }

    @Override
    public Directory createIndexDirectory(String indexId, LuceneConfig config) throws IOException {
        init();
        return create(indexFile.resolve(indexId), config);
    }

    @Override
    public Directory createTaxonomyDirectory(LuceneConfig config) throws IOException {
        init();
        return create(taxonomyFile, config);
    }

    @Override
    public void resetTaxonomy() throws IOException {
        init();
        cleanOldDirectoriesIfPossible();
        if (!cleanDirectory(taxonomyFile)) {
            IO.touch(taxonomyFile.resolve(DELETE_DIR_FLAG_FILE));
        }
        taxonomyFile = createNewIndexDirectory(TAXONOMY_DIR);
    }

    @Override
    public void resetIndex() throws IOException {
        init();

        cleanOldDirectoriesIfPossible();
        if (!cleanDirectory(indexFile)) {
            IO.touch(indexFile.resolve(DELETE_DIR_FLAG_FILE));
        }
        indexFile = createNewIndexDirectory(NON_SPATIAL_DIR);
    }

    private void cleanOldDirectoriesIfPossible() throws IOException {
        GeonetworkDataDirectory dataDir = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dataDir.getLuceneDir())) {
            for (Path path : directoryStream) {
                Path deleteFlagFile = path.resolve(DELETE_DIR_FLAG_FILE);
                if (Files.exists(deleteFlagFile)) {
                    try {
                        IO.deleteFileOrDirectory(path);
                    } catch (IOException e) {
                        Log.debug(Geonet.LUCENE_TRACKING, "Unable to delete obsolete index directory: " + path);
                        IO.touch(deleteFlagFile);
                    }
                }
            }
        }
    }

    @Nonnull
    @VisibleForTesting
    protected Path createNewIndexDirectory(String baseName) throws IOException {
        GeonetworkDataDirectory dataDir = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);

        Path newFile = dataDir.getLuceneDir().resolve(baseName);
        int i = 0;
        while (Files.exists(newFile) || Files.exists(newFile.resolve(DELETE_DIR_FLAG_FILE))) {
            i++;
            newFile = dataDir.getLuceneDir().resolve(baseName + "_" + i);
        }
        return newFile;
    }

    private boolean cleanDirectory(Path root) throws IOException {
        if (!Files.exists(root)) {
            return true;
        }
        final AtomicBoolean allReset = new AtomicBoolean(true);
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } catch (IOException e) {
                    Log.debug(Geonet.LUCENE_TRACKING, "Unable to reset lucene index directory: " + dir);
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                } catch (IOException e) {
                    Log.debug(Geonet.LUCENE_TRACKING, "Unable to reset lucene index file: " + file);
                    // probably is a locked file.
                    try {
                        try (OutputStream out = Files.newOutputStream(file)) {
                            Log.debug(Geonet.LUCENE_TRACKING, "Zero'd out " + file + " with outputstream: " + out);
                        }
                    } catch (IOException e2) {
                        Log.debug(Geonet.LUCENE_TRACKING, "Unable to zero-out file because of open file: " + file);
                        allReset.set(false);
                    }
                    return FileVisitResult.TERMINATE;
                }
            }
        });


        return allReset.get();
    }

    @Override
    public Set<String> listIndices() throws IOException {
        init();
        Set<String> indices = new LinkedHashSet<>();
        if (Files.exists(this.indexFile)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.indexFile)) {
                for (Path file : dirStream) {
                    if (Files.exists(file.resolve("segments.gen"))) {
                        indices.add(file.getFileName().toString());
                    }
                }
            }
        }
        return indices;
    }

    private Directory create(Path file, LuceneConfig luceneConfig) throws IOException {
        Files.createDirectories(file);

        Directory fsDir = FSDirectory.open(file.toFile());

        double maxMergeSizeMD = luceneConfig.getMergeFactor();
        double maxCachedMB = luceneConfig.getRAMBufferSize();
        return new NRTCachingDirectory(fsDir, maxMergeSizeMD, maxCachedMB);
    }

    public Path getIndexDir() {
        return indexFile;
    }

    public Path getTaxonomyDir() {
        return taxonomyFile;
    }
}
