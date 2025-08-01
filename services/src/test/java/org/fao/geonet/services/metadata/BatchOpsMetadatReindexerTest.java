package org.fao.geonet.services.metadata;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.ISearchManager;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.util.ThreadUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.export.MBeanExporter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;

public class BatchOpsMetadatReindexerTest {

    EsSearchManager searchManager;

    ConfigurableApplicationContext mockAppContext;

    MBeanExporter mockExporter;

    @Before
    public void setUpMocks() {
        mockAppContext = Mockito.mock(ConfigurableApplicationContext.class);
        mockExporter = Mockito.mock(MBeanExporter.class);
        Mockito.when(mockAppContext.getBean(Mockito.eq(MBeanExporter.class))).thenReturn(mockExporter);
        searchManager = Mockito.mock(EsSearchManager.class);
        Mockito.when(mockAppContext.getBean(Mockito.eq(EsSearchManager.class))).thenReturn(searchManager);
    }

    @Test
    public void syncMonoThread() throws Exception {
        int numberOfAvailableThreads = 1;
        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
            DataManager mockDataMan = createMockDataManager(usedTread);
            Set<Integer> toIndex = createMetadataToIndex();

            BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
            toTest.process("siteId", false);

            ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
            assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().collect(Collectors.joining("-")));
            assertEquals(1, usedTread.size());
            assertNotSame(Thread.currentThread(), usedTread.iterator().next());
        }
    }

    @Test
    public void syncMultiThread() throws Exception {
        int numberOfAvailableThreads = 4;
        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
            DataManager mockDataMan = createMockDataManager(usedTread);
            Set<Integer> toIndex = createMetadataToIndex();

            BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
            toTest.process("siteId", false);

            ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
            assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
            assertEquals(4, usedTread.size());
        }
    }

    @Test
    public void syncManyThreadButRunInCurrent() throws Exception {
        int numberOfAvailableThreads = 4;

        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
            DataManager mockDataMan = createMockDataManager(usedTread);
            Set<Integer> toIndex = createMetadataToIndex();

            BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
            toTest.process("siteId", true);

            ArgumentCaptor<String> metadataIdCaptor = captureIndexationLaunched(mockDataMan);
            assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
            assertEquals(1, usedTread.size());
            assertEquals(Thread.currentThread(), usedTread.iterator().next());
        }
    }

    @Test
    public void asyncMonoThread() throws Exception {
        int numberOfAvailableThreads = 1;

        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
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
    }

    @Test
    public void asyncMultiThread() throws Exception {
        int numberOfAvailableThreads = 4;

        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
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
            assertEquals("1-2-3-4", metadataIdCaptor.getAllValues().stream().sorted().collect(Collectors.joining("-")));
            assertEquals(4, usedTread.size());
        }
    }

    @Test
    public void asyncManyThreadButRunInCurrent() throws Exception {
        int numberOfAvailableThreads = 4;

        try (
            MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class);
            MockedStatic<ApplicationContextHolder> applicationContextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
        ) {
            applicationContextHolderMockedStatic.when(ApplicationContextHolder::get).thenReturn(mockAppContext);
            threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);

            final Set<Thread> usedTread = new HashSet<>();
            CountDownLatch latch = new CountDownLatch(1);
            DataManager mockDataMan = createBlockingMockDataManager(usedTread, latch);
            Set<Integer> toIndex = createMetadataToIndex();

            BatchOpsMetadataReindexer toTest = new BatchOpsMetadataReindexer(mockDataMan, toIndex);
            Thread currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (MockedStatic<ThreadUtils> threadUtilsMockedStatic = Mockito.mockStatic(ThreadUtils.class)) {
                        threadUtilsMockedStatic.when(ThreadUtils::getNumberOfThreads).thenReturn(numberOfAvailableThreads);
                        toTest.wrapAsyncProcess("siteId", true);
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
    }

    private DataManager createMockDataManager(Set<Thread> usedTread) throws Exception {
        DataManager mockDataMan = Mockito.mock(DataManager.class);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                usedTread.add(Thread.currentThread());
                return null;
            }
        }).when(mockDataMan).indexMetadata(Mockito.anyString(), Mockito.anyBoolean());
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
        }).when(mockDataMan).indexMetadata(Mockito.anyString(), Mockito.anyBoolean());
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
        ArgumentCaptor<Boolean> forceRefreshCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<ISearchManager> isearchManagerCaptor = ArgumentCaptor.forClass(ISearchManager.class);

        Mockito.verify(mockDataMan, Mockito.times(4)).indexMetadata(metadataIdCaptor.capture(), forceRefreshCaptor.capture());
        return metadataIdCaptor;
    }
}
