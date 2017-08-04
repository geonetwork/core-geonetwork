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

package org.fao.geonet.services.metadata;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.util.ThreadUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata changed in any of the Batch
 * operation services
 */
public class BatchOpsMetadataReindexer extends MetadataIndexerProcessor {

    Set<Integer> metadata;

    public BatchOpsMetadataReindexer(DataManager dm, Set<Integer> metadata) {
        super(dm);
        this.metadata = metadata;
    }

    public void process() throws Exception {
        process(false);
    }

    public void process(boolean runInCurrentThread) throws Exception {
        int threadCount = ThreadUtils.getNumberOfThreads();
        ExecutorService executor = null;
        try {
            if (runInCurrentThread) {
                executor = MoreExecutors.sameThreadExecutor();
            } else {
                executor = Executors.newFixedThreadPool(threadCount);
            }
            int[] ids = new int[metadata.size()];
            int i = 0;
            for (Integer id : metadata) ids[i++] = id;

            int perThread;
            if (ids.length < threadCount) perThread = ids.length;
            else perThread = ids.length / threadCount;
            int index = 0;

            List<BatchOpsCallable> jobs = Lists.newArrayList();
            while (index < ids.length) {
                int start = index;
                int count = Math.min(perThread, ids.length - start);
                // create threads to process this chunk of ids
                jobs.add(createCallable(ids, start, count));

                index += count;
            }
            List<Future<Void>> submitList = Lists.newArrayList();
            for (i = 1; i < jobs.size(); i++) {
                Future<Void> submit = executor.submit(jobs.get(i));
                submitList.add(submit);
            }

            if (jobs.size() > 0) {
                jobs.get(0).call();
                for (Future<Void> future : submitList) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    protected BatchOpsCallable createCallable(int[] ids, int start, int count) {
        return new BatchOpsCallable(ids, start, count, getDataManager());
    }

    public static final class BatchOpsCallable implements Callable<Void> {
        private final int ids[];
        private final int beginIndex, count;
        private final DataManager dm;

        BatchOpsCallable(int ids[], int beginIndex, int count, DataManager dm) {
            this.ids = ids;
            this.beginIndex = beginIndex;
            this.count = count;
            this.dm = dm;
        }

        public Void call() throws Exception {
            for (int i = beginIndex; i < beginIndex + count; i++) {
                boolean doIndex = beginIndex + count - 1 == i;
                dm.indexMetadata(ids[i] + "", doIndex, null);
            }

            return null;
        }
    }
}

