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

//==============================================================================
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.index;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.search.submission.BatchingIndexSubmittor;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.Log;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.management.ObjectName;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata changed in any of the Batch
 * operation services
 */
@ManagedResource()
public class BatchOpsMetadataReindexer extends MetadataIndexerProcessor implements Runnable {

    private static final JmxRemovalListener REMOVAL_LISTENER = new JmxRemovalListener();
    private static final Cache<ObjectName, ObjectName> PROBE_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .removalListener(REMOVAL_LISTENER)
        .build();
    private static final Set<CompletableFuture<Void>> RUNNING_INDEXERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static boolean isIndexing() {
        RUNNING_INDEXERS.removeIf(CompletableFuture::isDone);
        return !RUNNING_INDEXERS.isEmpty();
    }

    private final Set<Integer> metadata;
    private ExecutorService executor = null;
    private ObjectName probeName;
    private final int toProcessCount;
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger inError = new AtomicInteger();
    private CompletableFuture<Void> allCompleted;
    private final MBeanExporter exporter;

    public BatchOpsMetadataReindexer(DataManager dm, Set<Integer> metadata) {
        super(dm);
        this.metadata = metadata;
        this.toProcessCount = metadata.size();
        exporter = ApplicationContextHolder.get().getBean(MBeanExporter.class);
        REMOVAL_LISTENER.setExporter(exporter);
    }

    @ManagedAttribute
    public int getToProcessCount() {
        return toProcessCount;
    }

    @ManagedAttribute
    public int getProcessed() {
        return processed.intValue();
    }

    @ManagedAttribute
    public int getInError() {
        return inError.intValue();
    }

    public void process(String catalogueId, boolean runInCurrentThread) throws Exception {
        wrapAsyncProcess(catalogueId, runInCurrentThread);
        allCompleted.get();
    }

    public void process(String catalogueId) throws Exception {
        process(catalogueId, false);
    }

    public String wrapAsyncProcess(String catalogueId, boolean runInCurrentThread) throws Exception {
        probeName = new ObjectName(String.format(
            "geonetwork-%s:name=indexing-task,idx=%s",
            catalogueId, this.hashCode()));
        exporter.registerManagedResource(this, probeName);
        return processAsync(runInCurrentThread);
    }

    private String processAsync(boolean runInCurrentThread) {
        int threadCount = ThreadUtils.getNumberOfThreads();

        int[] ids = metadata.stream().mapToInt(i -> i).toArray();

        int perThread;
        if (ids.length < threadCount) perThread = ids.length;
        else perThread = ids.length / threadCount;

        int index = 0;

        Log.warning(Geonet.INDEX_ENGINE, String.format(
            "Indexing %d records with %d threads.",
            ids.length, threadCount));

        List<BatchOpsCallable> jobs = Lists.newArrayList();
        while (index < ids.length) {
            int start = index;
            int count = Math.min(perThread, ids.length - start);
            // create threads to process this chunk of ids
            BatchOpsCallable task = new BatchOpsCallable(ids, start, count);
            jobs.add(task);

            index += count;
        }

        if (runInCurrentThread) {
            allCompleted = new CompletableFuture<>();
            try {
                RUNNING_INDEXERS.add(allCompleted);
                for (BatchOpsCallable job : jobs) {
                    job.run();
                }
            }
            finally {
                allCompleted.complete(null);
            }
        } else {
            List<CompletableFuture<Void>> submitList = Lists.newArrayList();
            executor = Executors.newFixedThreadPool(threadCount);

            for (BatchOpsCallable job : jobs) {
                CompletableFuture<Void> completed = CompletableFuture.runAsync(job, executor);
                submitList.add(completed);
            }

            allCompleted = CompletableFuture.allOf(submitList.toArray(new CompletableFuture[0]));
            RUNNING_INDEXERS.add(allCompleted);
        }
        allCompleted.thenRun(this);
        return probeName.toString();
    }

    @Override
    public void run() {
        if (executor != null) {
            executor.shutdown();
        }
        PROBE_CACHE.cleanUp();
        PROBE_CACHE.put(probeName, probeName);
    }

    private static class JmxRemovalListener implements com.google.common.cache.RemovalListener<ObjectName, ObjectName> {
        private MBeanExporter exporter;

        @Override
        public void onRemoval(RemovalNotification<ObjectName, ObjectName> removalNotification) {
            exporter.unregisterManagedResource(removalNotification.getValue());
        }

        public void setExporter(MBeanExporter exporter) {
            this.exporter = exporter;
        }
    }

    private final class BatchOpsCallable implements Runnable {
        private final int[] ids;
        private final int beginIndex, count;

        BatchOpsCallable(int[] ids, int beginIndex, int count) {
            this.ids = ids;
            this.beginIndex = beginIndex;
            this.count = count;
        }

        @Override
        public void run() {
            Log.warning(Geonet.INDEX_ENGINE, String.format(
                "Indexing range [%d-%d]/%d by threads %s.",
                beginIndex, beginIndex + count, ids.length, Thread.currentThread().getId()));
            long start = System.currentTimeMillis();
            try (BatchingIndexSubmittor batchingIndexSubmittor = new BatchingIndexSubmittor(count)) {
                for (int i = beginIndex; i < beginIndex + count; i++) {
                    try {
                        dm.indexMetadata(ids[i] + "", batchingIndexSubmittor);
                        processed.incrementAndGet();
                    } catch (Exception e) {
                        inError.incrementAndGet();
                    }
                }
                Log.warning(Geonet.INDEX_ENGINE, String.format(
                    "Indexing range [%d-%d]/%d completed in %dms by threads %s.",
                    beginIndex, beginIndex + count, ids.length,
                    System.currentTimeMillis() - start,
                    Thread.currentThread().getId()));
            }
        }
    }
}

