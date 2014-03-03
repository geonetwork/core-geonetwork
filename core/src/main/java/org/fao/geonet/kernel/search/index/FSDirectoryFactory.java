package org.fao.geonet.kernel.search.index;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.utils.IO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Create filesystem based Directory objects.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 11:25 AM
 */
public class FSDirectoryFactory implements DirectoryFactory {

    public static final String NON_SPATIAL_DIR = "index";
    public static final String TAXONOMY_DIR = "taxonomy";
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
            indexFile = new File(luceneDir, NON_SPATIAL_DIR);
            taxonomyFile = new File(luceneDir, TAXONOMY_DIR);
        }
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
        FileUtils.cleanDirectory(taxonomyFile);
    }

    @Override
    public void resetIndex() throws IOException {
        init();
        if (indexFile.exists()) {
            FileUtils.cleanDirectory(indexFile);
        }
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
}
