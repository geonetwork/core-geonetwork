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

package org.fao.geonet.listener.metadata.draft;

import java.util.Arrays;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.events.md.sharing.MetadataShare;
import org.fao.geonet.events.md.sharing.MetadataShare.Type;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jeeves.server.context.ServiceContext;

/**
 * When a record modify the privileges, cascade to draft
 *
 * @author delawen
 */
@Component
public class UpdateOperations implements ApplicationListener<MetadataShare> {

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Autowired
    private IMetadataOperations metadataOperations;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Override
    public void onApplicationEvent(MetadataShare event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void doAfterCommit(MetadataShare event) {

        try {

            Log.trace(Geonet.DATA_MANAGER, "UpdateOperationsListener: " + event.getRecord() + " op: "
                + event.getOp().getId().getOperationId());
            AbstractMetadata md = metadataUtils.findOne(event.getRecord());

            if (md == null) {
                // The metadata is still being created, no need to check for draft.
                // If we try to update now, it could lead us to concurrency issues
                return;
            }

            if (md instanceof MetadataDraft) {
                Log.trace(Geonet.DATA_MANAGER, "Draft privileges are handled on approved record: " + event.getOp());
            } else {
                MetadataDraft draft = metadataDraftRepository.findOneByUuid(md.getUuid());

                if (draft != null) {
                    // Copy privileges from original metadata
                    OperationAllowed op = event.getOp();
                    ServiceContext context = ServiceContext.get();

                    // Only interested in editing and reviewing privileges
                    // No one else should be able to see it
                    if (op.getId().getOperationId() == ReservedOperation.editing.getId()) {
                        Log.trace(Geonet.DATA_MANAGER, "Updating privileges on draft " + draft.getId());

                        // except for reserved groups
                        Group g = groupRepository.findOne(op.getId().getGroupId());
                        if (!g.isReserved()) {
                            try {
                                if (event.getType() == Type.REMOVE) {
                                    Log.trace(Geonet.DATA_MANAGER, "Removing editing on group "
                                        + op.getId().getGroupId() + " for draft " + draft.getId());

                                    metadataOperations.forceUnsetOperation(context, draft.getId(),
                                        op.getId().getGroupId(), op.getId().getOperationId());
                                } else {
                                    Log.trace(Geonet.DATA_MANAGER, "Adding editing on group " + op.getId().getGroupId()
                                        + " for draft " + draft.getId());

                                    metadataOperations.forceSetOperation(context, draft.getId(),
                                        op.getId().getGroupId(), op.getId().getOperationId());
                                }
                                metadataIndexer.indexMetadata(Arrays.asList(String.valueOf(draft.getId())));
                            } catch (Exception e) {
                                Log.error(Geonet.DATA_MANAGER, "Error cascading operation to draft", e);
                            }
                        }
                    }

                }
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't update the operations of the draft " + event.getRecord(), e);
        }
    }
}
