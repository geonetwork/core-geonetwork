package org.fao.geonet.services.metadata;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.ISearchManager;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.kernel.search.submission.IIndexSubmittor;
import org.fao.geonet.util.ThreadUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.export.MBeanExporter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ThreadUtils.class, ApplicationContextHolder.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class BatchOpsMetadatReindexerTest {

    @Test
    public void syncMonoThread() throws Exception {
        int numberOfAvailableThreads = 1;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        DataManager mockDataMan = createMockDataManager(usedTread);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
        toTest.process("siteId", false);

        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().collect(Collectors.joining("-")));
        assertEquals(1, usedTread.size());
        assertNotSame(Thread.currentThread(), usedTread.iterator().next());
    }

    @Test
    public void syncMultiThread() throws Exception {
        int numberOfAvailableThreads = 4;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        DataManager mockDataMan = createMockDataManager(usedTread);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
        toTest.process("siteId", false);

        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
        assertEquals(4, usedTread.size());
    }

    @Test
    public void syncManyThreadButRunInCurrent() throws Exception {
        int numberOfAvailableThreads = 4;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        DataManager mockDataMan = createMockDataManager(usedTread);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
        toTest.process("siteId", true);

        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
        assertEquals(1, usedTread.size());
        assertEquals(Thread.currentThread(), usedTread.iterator().next());
    }

    @Test
    public void asyncMonoThread() throws Exception {
        int numberOfAvailableThreads = 1;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        CountDownLatch latch = new CountDownLatch(1);
        DataManager mockDataMan = createBlockingMockDataManager(usedTread, latch);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
        assertEquals(0, toTest.getProcessed());
        assertEquals(4, toTest.getToProcessCount());

        toTest.wrapAsyncProcess("siteId", false);

        latch.countDown();
        Thread.sleep(500);

        assertEquals(4, toTest.getProcessed());
        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().collect(Collectors.joining("-")));
        assertEquals(1, usedTread.size());
        assertNotSame(Thread.currentThread(), usedTread.iterator().next());
    }

    @Test
    public void asyncMultiThread() throws Exception {
        int numberOfAvailableThreads = 4;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        CountDownLatch latch = new CountDownLatch(1);
        DataManager mockDataMan = createBlockingMockDataManager(usedTread, latch);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);

        assertEquals(0, toTest.getProcessed());
        assertEquals(4, toTest.getToProcessCount());
        toTest.wrapAsyncProcess("siteId",false);

        latch.countDown();
        Thread.sleep(500);

        assertEquals(4, toTest.getProcessed());
        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
        assertEquals(4, usedTread.size());
    }

    @Test
    public void asyncManyThreadButRunInCurrent() throws Exception {
        int numberOfAvailableThreads = 4;
        final Set<Thread> usedTread = new HashSet<>();
        prepareEnvMocks(numberOfAvailableThreads);
        CountDownLatch latch = new CountDownLatch(1);
        DataManager mockDataMan = createBlockingMockDataManager(usedTread, latch);
        Set<Integer> toIndex = createMetadataToIndex();

        BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
        Thread currentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    toTest.wrapAsyncProcess("siteId",true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        currentThread.start();

        Thread.sleep(500);
        assertEquals(0, toTest.getProcessed());
        assertEquals(4, toTest.getToProcessCount());

        latch.countDown();
        Thread.sleep(500);

        assertEquals(4, toTest.getProcessed());
        ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
        assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
        assertEquals(1, usedTread.size());
        assertEquals(currentThread, usedTread.iterator().next());
    }

    private void prepareEnvMocks(int numberOfAvailableThreads) {
        MBeanExporter mockExporter = Mockito.mock(MBeanExporter.class);

        ConfigurableApplicationContext mockAppContext = Mockito.mock(ConfigurableApplicationContext.class);
        Mockito.when(mockAppContext.getBean(Mockito.eq((MBeanExporter.class)))).thenReturn(mockExporter);


        PowerMockito.mockStatic(ApplicationContextHolder.class);
        PowerMockito.when(ApplicationContextHolder.get()).thenReturn(mockAppContext);
        EsSearchManager searchManager = Mockito.mock(EsSearchManager.class);
        Mockito.when(mockAppContext.getBean(Mockito.eq((EsSearchManager.class)))).thenReturn(searchManager);

        PowerMockito.mockStatic(ThreadUtils.class);
        PowerMockito.when(ThreadUtils.getNumberOfThreads()).thenReturn(numberOfAvailableThreads);
    }

    private DataManager createMockDataManager(Set<Thread> usedTread) throws Exception {
        DataManager mockDataMan = Mockito.mock(DataManager.class);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                usedTread.add(Thread.currentThread());
                return null;
            }
        }).when(mockDataMan).indexMetadata(Mockito.anyString(), Mockito.any(IIndexSubmittor.class));
        return mockDataMan;
    }

    private DataManager createBlockingMockDataManager(Set<Thread> usedTread, CountDownLatch latch) throws Exception {
        DataManager mockDataMan = Mockito.mock(DataManager.class);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                usedTread.add(Thread.currentThread());
                latch.await();
                return null;
            }
        }).when(mockDataMan).indexMetadata(Mockito.anyString(), Mockito.any(IIndexSubmittor.class));
        return mockDataMan;
    }

    private Set<Integer> createMetadataToIndex() {
        Set<Integer> toIndex = new HashSet<>();
        toIndex.add(1);
        toIndex.add(2);
        toIndex.add(3);
        toIndex.add(4);
        return toIndex;
    }

    private ArgumentCaptor<String> captureIndexationLaunched(DataManager mockDataMan) throws Exception {
        ArgumentCaptor<String> metadataIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<IIndexSubmittor> indexSubmittorCaptor = ArgumentCaptor.forClass(IIndexSubmittor.class);
        ArgumentCaptor<ISearchManager> isearchManagerCaptor = ArgumentCaptor.forClass(ISearchManager.class);

        Mockito.verify(mockDataMan, Mockito.times(4)).indexMetadata(metadataIdCaptor.capture(), indexSubmittorCaptor.capture());
        return metadataIdCaptor;
    }
}
