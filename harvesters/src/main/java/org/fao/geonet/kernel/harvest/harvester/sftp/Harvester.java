//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
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
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.harvest.harvester.sftp;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Harvester implements IHarvester<HarvestResult> {
    private final AtomicBoolean cancelMonitor;
    private final SftpParams params;
    private final ServiceContext context;
    private Logger log;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private final List<HarvestError> errors;

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, SftpParams params, List<HarvestError> errors) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
        this.errors = errors;
    }

    @Override
    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;

        if (cancelMonitor.get()) {
            return new HarvestResult();
        }

        boolean error = false;
        HarvestResult result = new HarvestResult();
        try {
            Aligner aligner = new Aligner(cancelMonitor, context, params, log);
            result = aligner.align(errors);
        } catch (Exception t) {
            error = true;
            log.error("Unknown error trying to harvest");
            log.error(t.getMessage());
            log.error(t);
            errors.add(new HarvestError(context, t));
        }

        if (error) {
            log.warning("Due to previous errors the align process has not been called");
        }

        return result;
    }
}
