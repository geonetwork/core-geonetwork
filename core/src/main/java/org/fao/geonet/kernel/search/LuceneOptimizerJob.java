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

/**
 *
 */
package org.fao.geonet.kernel.search;

import org.fao.geonet.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Performs the LuceneOptimization task.
 *
 * @author jeichar
 */
@DisallowConcurrentExecution
public class LuceneOptimizerJob implements Job {

    LuceneIndexLanguageTracker indexTracker;

    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        try {
            indexTracker.optimize();
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE,
                "Optimize task failed: " + e.getMessage(), e);
        }
    }

    public void setIndexTracker(LuceneIndexLanguageTracker indexTracker) {
        this.indexTracker = indexTracker;
    }

}
