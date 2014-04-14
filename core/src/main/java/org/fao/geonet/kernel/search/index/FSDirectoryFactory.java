package org.fao.geonet.kernel.search.index;

import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import com.vividsolutions.jts.util.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Create filesystem based Directory objects.
 * <p>
 *     Because windows locks files the reset methods will sometime create new index directories.  That is the
 *     reason for all the strange checking for new names and the {@link #DELETE_DIR_FLAG_FILE} file names.
 * </p>
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 11:25 AM
 */
public class FSDirectoryFactory implements DirectoryFactory {

    public static final String NON_SPATIAL_DIR = "index";
    public static final String TAXONOMY_DIR = "taxonomy";
    private static final String DELETE_DIR_FLAG_FILE = "This_directory_could_not_be_deleted_during_reindex";
    @Autowired
    private GeonetworkDataDirectory _dataDir;

    private volatile File taxonomyFile;
    private volatile File indexFile;

    public synchronized void init(){
        if (taxonomyFile == null) {
            final File luceneDir = _dataDir.getLuceneDir();
            if (luceneDir == null) {
                throw new IllegalStateException("This object cannot be constructed until GeonetworkDataDirectory has been initialized");
            }
            indexFile = findLatestIndexDir(luceneDir, NON_SPATIAL_DIR);

            taxonomyFile = findLatestIndexDir(luceneDir, TAXONOMY_DIR);
        }
    }

    private File findLatestIndexDir(File luceneDir, String baseName) {
        File[] files = luceneDir.listFiles();
        if (files == null) {
            throw new IllegalStateException("Unable to create index until luceneDir is created");
        }
        File indexDir = null;
        for (File file : files) {
            if (file.getName().startsWith(baseName) && !new File(file, DELETE_DIR_FLAG_FILE).exists()) {
                if (indexDir == null || indexDir.getName().compareTo(file.getName()) < 0) {
                    indexDir = file;
                }
            }
        }
        if (indexDir == null) {
            return new File(luceneDir, baseName);
        }
        return indexDir;
    }

    @Override
    public Directory createIndexDirectory(String indexId, LuceneConfig config) throws IOException {
        init();
        return create(new File(indexFile, indexId), config, "Lucene "+indexId+" Index directory");
    }

    @Override
    public Directory createTaxonomyDirectory(LuceneConfig config) throws IOException {
        init();
        return create(taxonomyFile, config, "Lucene Taxonomy directory");
    }

    @Override
    public void resetTaxonomy() throws IOException {
        init();
        cleanOldDirectoriesIfPossible();
        if (!cleanDirectory(taxonomyFile)) {
            Files.touch(new File(taxonomyFile, DELETE_DIR_FLAG_FILE));
        }
        taxonomyFile = createNewIndexDirectory(TAXONOMY_DIR);
    }

    @Override
    public void resetIndex() throws IOException {
        init();

        cleanOldDirectoriesIfPossible();
        if (!cleanDirectory(indexFile)) {
            Files.touch(new File(indexFile, DELETE_DIR_FLAG_FILE));
        }
        indexFile = createNewIndexDirectory(NON_SPATIAL_DIR);
    }

    private void cleanOldDirectoriesIfPossible() throws IOException {
        File[] files = _dataDir.getLuceneDir().listFiles();
        for (File file : files) {
            if (new File(file, DELETE_DIR_FLAG_FILE).exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    Log.debug(Geonet.LUCENE_TRACKING, "Unable to delete obsolete index directory: "+file);
                    Files.touch(new File(file, DELETE_DIR_FLAG_FILE));
                }
            }
        }

    }

    @Nonnull
    private File createNewIndexDirectory(String baseName) throws IOException {
        File newFile = new File(_dataDir.getLuceneDir(), baseName);
        int i = 0;
        while (newFile.exists() || new File(newFile, DELETE_DIR_FLAG_FILE).exists()) {
            i++;
            newFile = new File(_dataDir.getLuceneDir(), baseName + "_"+i);
        }
        return newFile;
    }

    private boolean cleanDirectory(File root) throws IOException {
        if (!root.exists()) {
            return true;
        }
        final FluentIterable<File> files = Files.fileTreeTraverser().postOrderTraversal(root);
        for (File file : files) {
            if (file.isDirectory() ) {
                if (!file.delete()) {
                    Log.debug(Geonet.LUCENE_TRACKING, "Unable to reset lucene index directory: " + file);
                }
            } else {
                if (!file.delete()) {
                    Log.debug(Geonet.LUCENE_TRACKING, "Unable to reset lucene index file: "+file);
                    // probably is a locked file.
                    try {
                        new FileOutputStream(file).close();
                    } catch (IOException e) {
                        Log.debug(Geonet.LUCENE_TRACKING, "Unable to zero-out file because of open file: "+file);
                    }
                }
            }
        }

        final String[] remainingFiles = root.list();
        return remainingFiles == null || remainingFiles.length == 0;
    }

    @Override
    public Set<String> listIndices() {
        init();
        Set<String> indices = new HashSet<String>();
        final File[] files = indexFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (new File(file, "segments.gen").exists()) {
                    indices.add(file.getName());
                }
            }
        }
        return indices;
    }

    private Directory create(File file, LuceneConfig luceneConfig, String descriptor) throws IOException {
        IO.mkdirs(file, descriptor);

        Directory fsDir = FSDirectory.open(file);

        double maxMergeSizeMD = luceneConfig.getMergeFactor();
        double maxCachedMB = luceneConfig.getRAMBufferSize();
        return new NRTCachingDirectory(fsDir, maxMergeSizeMD,maxCachedMB);
    }

    public void setDataDir(GeonetworkDataDirectory dataDir) {
        this._dataDir = dataDir;
    }

    public File getIndexDir() {
        return indexFile;
    }
    public File getTaxonomyDir() {
        return taxonomyFile;
    }
}
