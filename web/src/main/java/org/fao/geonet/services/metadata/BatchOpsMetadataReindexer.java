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

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata
 * changed in any of the Batch operation services
 */
public class BatchOpsMetadataReindexer extends MetadataIndexerProcessor {
	
	public class BatchOpsCallable implements Callable<Void> {
		private final int ids[];
		private final int beginIndex, count;
		private final DataManager dm;
		private final Dbms dbms;
	
		BatchOpsCallable(int ids[], int beginIndex, int count, DataManager dm, Dbms dbms) {
			this.ids = ids;
			this.beginIndex = beginIndex;
			this.count = count;
			this.dm = dm;
			this.dbms = dbms;
		}
		
		public Void call() throws Exception {
			for(int i=beginIndex; i<beginIndex+count; i++) {
				dm.indexMetadata(dbms, ids[i]+"");
			}
			return null;
		}
	}
	
  Set<Integer> metadata;
	Dbms dbms;

  public BatchOpsMetadataReindexer(DataManager dm, Dbms dbms, Set<Integer> metadata) {
      super(dm);
			this.dbms = dbms;
      this.metadata = metadata;
  }

	public void process() throws Exception {
		int threadCount = ThreadUtils.getNumberOfThreads();

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		int[] ids = new int[metadata.size()];
		int i = 0; for (Integer id : metadata) ids[i++] = id;

		int perThread;
		if (ids.length < threadCount) perThread = ids.length;
		else perThread = ids.length / threadCount;
		int index = 0;

		List<Future<Void>> submitList = new ArrayList<Future<Void>>();
		while(index < ids.length) {
			int start = index;
			int count = Math.min(perThread,ids.length-start);
			// create threads to process this chunk of ids
			Callable<Void> worker = new BatchOpsCallable(ids, start, count, dm, dbms);
			Future<Void> submit = executor.submit(worker);
			submitList.add(submit);
			index += count;
		}

		for (Future<Void> future : submitList) {
			try {
				Void o = future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}
}

