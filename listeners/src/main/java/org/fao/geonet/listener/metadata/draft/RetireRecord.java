/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.listener.metadata.draft;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * When a record gets a status change to retired, unpublish it.
 *
 */
@Component
public class RetireRecord implements ApplicationListener<MetadataStatusChanged> {

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    IMetadataOperations metadataOperations;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Override
    public void onApplicationEvent(MetadataStatusChanged event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void doBeforeCommit(MetadataStatusChanged event) {
        try {
            Log.trace(Geonet.DATA_MANAGER, "Status changed for metadata with id " + event.getMd().getId());

            // Handle draft accordingly to the status change
            // If there is no draft involved, these operations do nothing
            StatusValue status = event.getStatus();
            switch (String.valueOf(status.getId())) {
                case StatusValue.Status.RETIRED:
                    try {
                        ServiceContext context = ServiceContext.get();

                        Log.trace(Geonet.DATA_MANAGER,
                            "Unpublishing retired record (ID=" + event.getMd().getId() + ").");

                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.download.getId());
                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.dynamic.getId());
                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.editing.getId());
                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.featured.getId());
                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.view.getId());
                        metadataOperations.forceUnsetOperation(context, event.getMd().getId(), ReservedGroup.all.getId(), ReservedOperation.notify.getId());

                        metadataIndexer.indexMetadata(String.valueOf(event.getMd().getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);

                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

                    }
                    break;
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Error changing workflow status of " + event.getMd(), e);
        }
    }
}
