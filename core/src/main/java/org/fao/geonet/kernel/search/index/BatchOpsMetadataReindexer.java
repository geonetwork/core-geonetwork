/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import com.google.common.collect.Lists;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.search.submission.batch.BatchingIndexSubmitter;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.Log;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.management.ObjectName;
import java.util.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata changed in any of the Batch
 * operation services
 */
@ManagedResource()
public class BatchOpsMetadataReindexer extends MetadataIndexerProcessor implements Runnable {

    /**
     * How long a finished task's probe stays registered, so that the admin
     * dashboard can still display its completed status.
     */
    private static final long PROBE_RETENTION_MINUTES = 1;
    private static final Set<CompletableFuture<Void>> RUNNING_INDEXERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static boolean isIndexing() {
        RUNNING_INDEXERS.removeIf(CompletableFuture::isDone);
        return !RUNNING_INDEXERS.isEmpty();
    }

    private final Collection<Integer> metadata;
    private ExecutorService executor = null;
    private ObjectName probeName;
    private final int toProcessCount;
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger inError = new AtomicInteger();
    private CompletableFuture<Void> allCompleted;
    private final MBeanExporter exporter;
    private final TaskScheduler probeCleaner;

    public BatchOpsMetadataReindexer(DataManager dm, Collection<Integer> metadata) {
        super(dm);
        this.metadata = metadata;
        this.toProcessCount = metadata.size();
        exporter = ApplicationContextHolder.get().getBean(MBeanExporter.class);
        probeCleaner = ApplicationContextHolder.get().getBean("probeCleaner", TaskScheduler.class);
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
        // Unregister the probe on its own, after a short retention delay: the
        // dashboard polls it to show the completed status one last time.
        probeCleaner.schedule(
            () -> exporter.unregisterManagedResource(probeName),
            Instant.now().plus(Duration.ofMinutes(PROBE_RETENTION_MINUTES)));
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
            try (BatchingIndexSubmitter batchingIndexSubmittor = new BatchingIndexSubmitter(count)) {
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

