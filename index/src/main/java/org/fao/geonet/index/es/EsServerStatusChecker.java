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
package org.fao.geonet.index.es;

import org.fao.geonet.index.IServerStatusChecker;
import org.fao.geonet.index.State;
import org.fao.geonet.index.Status;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class EsServerStatusChecker
    extends QuartzJobBean
    implements IServerStatusChecker {

    @Autowired
    private Status status;

    private boolean indexChecked = false;

    public EsServerStatusChecker() {
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        checkState();
    }

    @Autowired
    EsRestClient client;

    @Override
    public Status checkState() {
        // Ping ES and return status
        // Check all index are created and available
        // Check all index are in latest version ?
        // Check db record count match index count
        // Check db records date match index records date
        String status = null;
        try {
            status = client.getServerStatus();
            if ("green".equalsIgnoreCase(status)) {
                this.status.setState(State.GREEN, "Index up and running. All green.");
                checkIndexState();
            } else if ("yellow".equalsIgnoreCase(status)) {
                this.status.setState(State.YELLOW, "Index status is yellow. Check index server log.");
                checkIndexState();
            } else {
                this.status.setState(State.RED, "Index is down");
            }
        } catch (Exception e) {
            this.status.setState(State.UNINITIALIZED, String.format(
                "Unable to revive connection to %s. Error is %s", client.getServerUrl(), e.getMessage()));
        }
        return this.status;
    }

    @Override
    public Status checkIndexState() {
        if (!indexChecked) {

            indexChecked = true;
        }
        return null;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
