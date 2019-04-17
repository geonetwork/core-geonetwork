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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.events.md.MetadataPublished;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import jeeves.server.context.ServiceContext;

/**
 * When a record with workflow enabled gets published, status will automatically
 * change to approved
 *
 * @author delawen
 */
@Component
public class ApprovePublishedRecord implements ApplicationListener<MetadataPublished> {

    @Autowired
    private IMetadataStatus metadataStatus;

    @Autowired
    private DraftUtilities draftUtilities;

    @Autowired
    private StatusValueRepository statusValueRepository;

    @Override
    @Transactional
    public void onApplicationEvent(MetadataPublished event) {
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    @Modifying(clearAutomatically = true)
    public void doAfterCommit(MetadataPublished event) {

        Log.debug(Geonet.DATA_MANAGER, "Metadata with id " + event.getMd().getId() + " published.");

        try {
            // Only do something if the workflow is enabled
            MetadataStatus previousStatus = metadataStatus.getStatus(event.getMd().getId());
            if (previousStatus != null) {
                draftUtilities.replaceMetadataWithDraft(event.getMd());
                if (!Integer.valueOf(StatusValue.Status.APPROVED).equals(previousStatus.getId().getStatusId())) {
                    changeToApproved(event.getMd(), previousStatus);
                }
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Error upgrading workflow of " + event.getMd(), e);
        }
    }

    private void changeToApproved(AbstractMetadata md, MetadataStatus previousStatus)
        throws NumberFormatException, Exception {
        // This status should be associated to original record, not draft
        MetadataStatus status = new MetadataStatus();
        status.setChangeMessage("Record published.");
        status.setPreviousState(previousStatus.getCurrentState());
        status.setStatusValue(statusValueRepository.findOne(Integer.valueOf(StatusValue.Status.APPROVED)));

        MetadataStatusId mdStatusId = new MetadataStatusId();
        mdStatusId.setStatusId(Integer.valueOf(StatusValue.Status.APPROVED));
        mdStatusId.setMetadataId(md.getId());
        mdStatusId.setChangeDate(new ISODate());
        mdStatusId.setUserId(ServiceContext.get().getUserSession().getUserIdAsInt());
        status.setId(mdStatusId);

        metadataStatus.setStatusExt(status);

        Log.trace(Geonet.DATA_MANAGER, "Metadata with id " + md.getId() + " automatically approved due to publishing.");
    }

}
