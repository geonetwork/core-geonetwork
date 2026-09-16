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

package org.fao.geonet.api.processing.report.registry;

import org.fao.geonet.api.processing.report.ProcessingReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple in memory registry of reports.
 */
public class ProcessingReportRegistry implements IProcessingReportRegistry {
    List<ProcessingReport> reports = Collections.synchronizedList(new ArrayList<ProcessingReport>());

    public ProcessingReportRegistry() {
    }

    @Override
    public void add(ProcessingReport report) {
        reports.add(report);
    }

    @Override
    public List<ProcessingReport> get() {
        return reports;
    }

    @Override
    public ProcessingReport get(String uuid) {
        for (ProcessingReport r : reports) {
            if (r.getUuid().equals(uuid)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public boolean remove(String uuid) {
        synchronized (reports) {
            for (ProcessingReport r : reports) {
                if (r.getUuid().equals(uuid)) {
                    reports.remove(r);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clear() {
        reports.clear();
    }
}
