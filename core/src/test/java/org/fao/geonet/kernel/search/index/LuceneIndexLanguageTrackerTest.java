package org.fao.geonet.kernel.search.index;

import static org.junit.Assert.assertEquals;

import com.google.common.io.Files;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test {@link org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker}
 * Created by Jesse on 3/11/14.
 */
public class LuceneIndexLanguageTrackerTest {
    private static final String LANG = "eng";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Test
    public void testResetWaitsForAllReadersToClose() throws Exception {
        GeonetworkDataDirectory datadir = Mockito.mock(GeonetworkDataDirectory.class);
        Mockito.when(datadir.getLuceneDir()).thenReturn(folder.getRoot());
        FSDirectoryFactory directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        LuceneConfig luceneConfig = Mockito.mock(LuceneConfig.class);
        Mockito.when(luceneConfig.commitInterval()).thenReturn(1L);
        Mockito.when(luceneConfig.useNRTManagerReopenThread()).thenReturn(false);
        final LuceneIndexLanguageTracker tracker = new LuceneIndexLanguageTracker(directoryFactory, luceneConfig);

        final IndexAndTaxonomy acquire = addDocumentAndAssertCorrectlyAdded(tracker);

        final boolean[] tryingToReset = new boolean[1];
        final boolean[] done = new boolean[1];
        final Exception[] error = new Exception[1];
        startThread("Close open reader thread", new Runnable() {
            @Override
            public void run() {
                try {

                    while (!tryingToReset[0]) {
                        Thread.sleep(100);
                    }
                    acquire.indexReader.releaseToNRTManager();
                } catch (Exception e) {
                    error[0] = e;
                }
                done[0] = true;
            }
        });

        tryingToReset[0] = true;
        tracker.reset(500);

        while(!done[0]) {
            Thread.sleep(100);
        }
        if (error[0] != null) {
            throw error[0];
        }

        final IndexAndTaxonomy acquire2 = tracker.acquire(LANG, -1);
        assertEquals(0, acquire2.indexReader.numDocs());
    }

    private void startThread(String name, Runnable target) {
        final Thread thread = new Thread(target);

        thread.setDaemon(true);
        thread.setName(name);
        thread.start();
    }

    @Test(expected = TimeoutException.class)
    public void testResetThrowsExceptionWhenReadersAreNotClosed() throws Exception {
        GeonetworkDataDirectory datadir = Mockito.mock(GeonetworkDataDirectory.class);
        Mockito.when(datadir.getLuceneDir()).thenReturn(folder.getRoot());
        FSDirectoryFactory directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        LuceneConfig luceneConfig = Mockito.mock(LuceneConfig.class);
        Mockito.when(luceneConfig.commitInterval()).thenReturn(1L);
        Mockito.when(luceneConfig.useNRTManagerReopenThread()).thenReturn(false);
        final LuceneIndexLanguageTracker tracker = new LuceneIndexLanguageTracker(directoryFactory, luceneConfig);

        addDocumentAndAssertCorrectlyAdded(tracker);

        tracker.reset(500);
    }

    @Test
    public void testCanResetWhenFileIsLockedByProcess() throws Exception {
        GeonetworkDataDirectory datadir = Mockito.mock(GeonetworkDataDirectory.class);
        final File root = folder.getRoot();
        Mockito.when(datadir.getLuceneDir()).thenReturn(root);
        FSDirectoryFactory directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        LuceneConfig luceneConfig = Mockito.mock(LuceneConfig.class);
        Mockito.when(luceneConfig.commitInterval()).thenReturn(1L);
        Mockito.when(luceneConfig.useNRTManagerReopenThread()).thenReturn(false);

        createAndLockFilesAndResetTracker(directoryFactory, luceneConfig, true);

        assertEquals(FSDirectoryFactory.NON_SPATIAL_DIR + "_" + 1, directoryFactory.getIndexDir().getName());
        assertEquals(FSDirectoryFactory.TAXONOMY_DIR + "_" + 1, directoryFactory.getTaxonomyDir().getName());

        directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        createAndLockFilesAndResetTracker(directoryFactory, luceneConfig, false);

        // Test that creating new searcher finds new index
        directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);

        final LuceneIndexLanguageTracker tracker2 = new LuceneIndexLanguageTracker(directoryFactory, luceneConfig);

        final IndexAndTaxonomy acquire3 = tracker2.acquire(LANG, -1);
        assertEquals(1, acquire3.indexReader.numDocs());
        acquire3.indexReader.releaseToNRTManager();

        assertEquals(FSDirectoryFactory.NON_SPATIAL_DIR + "_" + 2, directoryFactory.getIndexDir().getName());
        assertEquals(FSDirectoryFactory.TAXONOMY_DIR + "_" + 2, directoryFactory.getTaxonomyDir().getName());

        tracker2.reset(1000);

        tracker2.close(1000, true);

        directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        directoryFactory.init();

        assertEquals(FSDirectoryFactory.NON_SPATIAL_DIR, directoryFactory.getIndexDir().getName());
        assertEquals(FSDirectoryFactory.TAXONOMY_DIR, directoryFactory.getTaxonomyDir().getName());
    }

    private void createAndLockFilesAndResetTracker(FSDirectoryFactory directoryFactory, LuceneConfig luceneConfig, boolean addDoc) throws Exception {
        final LuceneIndexLanguageTracker tracker = new LuceneIndexLanguageTracker(directoryFactory, luceneConfig);

        if (addDoc) {
            addDocumentAndAssertCorrectlyAdded(tracker).indexReader.releaseToNRTManager();
        }
        List<Closeable> openFile = new ArrayList<Closeable>();
        List<MappedByteBuffer> memoryMappedBuffers = new ArrayList<MappedByteBuffer>();

        for (File file : Files.fileTreeTraverser().postOrderTraversal(folder.getRoot())) {
                if (file.isFile()) {
                    final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                    final FileChannel channel = randomAccessFile.getChannel();
                    final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, file.length());
                    openFile.add(channel);
                    openFile.add(randomAccessFile);
                }
        }
        try {
        tracker.reset(1);
        tracker.reset(1);


        final IndexAndTaxonomy acquire2 = tracker.acquire(LANG, -1);
        assertEquals(0, acquire2.indexReader.numDocs());
        acquire2.indexReader.releaseToNRTManager();

        addDocumentAndAssertCorrectlyAdded(tracker).indexReader.releaseToNRTManager();
        } finally {
            for (Closeable closeable : openFile) {
                closeable.close();
            }
            memoryMappedBuffers = null;
            Runtime.getRuntime().gc();
        }

        tracker.close(100, true);
    }

    private IndexAndTaxonomy addDocumentAndAssertCorrectlyAdded(LuceneIndexLanguageTracker tracker) throws IOException {
        Document document = new Document();
        document.add(new IntField("intField1", 1, Field.Store.YES));
        document.add(new IntField("intField2", 2, Field.Store.YES));
        document.add(new IntField("intField1", 3, Field.Store.YES));

        Collection<CategoryPath> categories = Arrays.asList(new CategoryPath("intField1", "1"), new CategoryPath("intField1", "3"));
        tracker.addDocument(LANG, document, categories);

        final IndexAndTaxonomy acquire = tracker.acquire(LANG, -1);
        assertEquals(2, acquire.indexReader.document(0).getValues("intField1").length);
        assertEquals(1, acquire.indexReader.document(0).getValues("intField2").length);
        assertEquals(1, acquire.indexReader.numDocs());
        return acquire;
    }

    /**
     * This test has 3 thread.
     * <ul>
     *     <li>Main thread: responsible for calling reset() on tracker</li>
     *     <li>Open reader thread: Will try to open a new reader AFTER reset() has been called.  It should not be able to until reset finishes</li>
     *     <li>
     *         Close reader thread: Will wait until both reset() has been called and acquire has been called before closing reader.
     *         It checks that both are blocked before closing reader to ensure correct behaviour.
     *     </li>
     * </ul>
     * @throws Exception
     */
    @Test(timeout = 30000)
    public void testCantOpenNewReaderDuringReset() throws Exception {
        GeonetworkDataDirectory datadir = Mockito.mock(GeonetworkDataDirectory.class);
        Mockito.when(datadir.getLuceneDir()).thenReturn(folder.getRoot());
        FSDirectoryFactory directoryFactory = new FSDirectoryFactory();
        directoryFactory.setDataDir(datadir);
        LuceneConfig luceneConfig = Mockito.mock(LuceneConfig.class);
        Mockito.when(luceneConfig.commitInterval()).thenReturn(1L);
        Mockito.when(luceneConfig.useNRTManagerReopenThread()).thenReturn(false);
        final LuceneIndexLanguageTracker tracker = new LuceneIndexLanguageTracker(directoryFactory, luceneConfig);

        final IndexAndTaxonomy acquire = addDocumentAndAssertCorrectlyAdded(tracker);

        final boolean[] tryingToReset = new boolean[1];
        final boolean[] tryingToOpenReader = new boolean[1];
        final boolean[] haveOpenedReader = new boolean[1];
        final boolean[] resetFinished = new boolean[1];
        final boolean[] done = new boolean[1];
        final Exception[] error = new Exception[1];

        startThread("Open New Reader", new Runnable() {
            @Override
            public void run() {
                try {

                    while (!tryingToReset[0]) {
                        Thread.sleep(100);
                    }
                    tryingToOpenReader[0] = true;
                    assertEquals(false, resetFinished[0]);
                    tracker.acquire(LANG, -1);
                    haveOpenedReader[0] = true;
                    assertEquals(true, resetFinished[0]);
                } catch (Exception e) {
                    error[0] = e;
                }
            }
        });

        startThread("Close Reader", new Runnable() {
            @Override
            public void run() {
                try {

                    while (!tryingToOpenReader[0]) {
                        Thread.sleep(100);
                    }
                    Thread.sleep(200);
                    assertEquals(false, haveOpenedReader[0]);
                    assertEquals(false, resetFinished[0]);
                    acquire.indexReader.releaseToNRTManager();
                } catch (Exception e) {
                    error[0] = e;
                }
                done[0] = true;
            }
        });

        tryingToReset[0] = true;
        tracker.reset(TimeUnit.MINUTES.toMillis(10));
        resetFinished[0] = true;

        while(!done[0]) {
            Thread.sleep(100);
        }

        if (error[0] != null) {
            throw error[0];
        }

    }
}
