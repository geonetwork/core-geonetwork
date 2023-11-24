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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.events.history.RecordUpdatedEvent;
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * When a record gets a status change, check if there is a draft associated to
 * it. If there is, act accordingly (replacing record with draft and/or removing
 * draft).
 *
 * @author delawen
 */
@Component
public class ApproveRecord implements ApplicationListener<MetadataStatusChanged> {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private IMetadataStatus metadataStatus;

    @Autowired
    private DraftUtilities draftUtilities;

    @Autowired
    IMetadataUtils metadataUtils;

    @Override
    public void onApplicationEvent(MetadataStatusChanged event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void doAfterCommit(MetadataStatusChanged event) {
        try {
            Log.trace(Geonet.DATA_MANAGER, "Status changed for metadata with id " + event.getMd().getId());

            // Handle draft accordingly to the status change
            // If there is no draft involved, these operations do nothing
            StatusValue status = event.getStatus();
            switch (String.valueOf(status.getId())) {
                case StatusValue.Status.DRAFT:
                case StatusValue.Status.SUBMITTED:
                    if (event.getMd() instanceof Metadata) {
                        Log.trace(Geonet.DATA_MANAGER,
                            "Replacing contents of record (ID=" + event.getMd().getId() + ") with draft, if exists.");
                        draftUtilities.replaceMetadataWithDraft(event.getMd());
                    }
                    break;
                case StatusValue.Status.RETIRED:
//                case StatusValue.Status.REJECTED:
                    try {
                        Log.trace(Geonet.DATA_MANAGER,
                            "Removing draft from record (ID=" + event.getMd().getId() + "), if exists.");
                        removeDraft(event.getMd());
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

                    }
                    break;
                case StatusValue.Status.APPROVED:
                    try {
                        Log.trace(Geonet.DATA_MANAGER, "Replacing contents of approved record (ID=" + event.getMd().getId()
                            + ") with draft, if exists.");
                        approveWithDraft(event);
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

                    }
                    break;
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Error changing workflow status of " + event.getMd(), e);
        }
    }

    private void removeDraft(AbstractMetadata md) throws Exception {

        if (!(md instanceof MetadataDraft)) {
            md = metadataDraftRepository.findOneByUuid(md.getUuid());
        }

        if (md != null) {
            draftUtilities.removeDraft((MetadataDraft) md);
        }
    }

    private AbstractMetadata approveWithDraft(MetadataStatusChanged event) throws NumberFormatException, Exception {
        AbstractMetadata md = event.getMd();
        AbstractMetadata draft = null;

        if (md instanceof MetadataDraft) {
            draft = md;
            md = metadataRepository.findOneByUuid(draft.getUuid());

            // This status should be associated to original record, not draft
            MetadataStatus status = new MetadataStatus();
            status.setChangeMessage(event.getMessage());
            status.setStatusValue(event.getStatus());
            status.setMetadataId(md.getId());
            status.setUuid(md.getUuid());
            status.setTitles(metadataUtils.extractTitles(Integer.toString(md.getId())));
            status.setChangeDate(new ISODate());
            status.setUserId(event.getUser());

            metadataStatus.setStatusExt(status, true);

        } else if (md instanceof Metadata) {
            draft = null; //metadataDraftRepository.findOneByUuid(md.getUuid());
        }

        if (draft != null) {
            Log.trace(Geonet.DATA_MANAGER, "Approving record " + md.getId() + " which has a draft " + draft.getId());

            XMLOutputter outp = new XMLOutputter();
            String xmlBefore = outp.outputString(md.getXmlData(false));

            md = draftUtilities.replaceMetadataWithDraft(md, draft);

            // Throw RecordUpdatedEvent for the published version
            Element afterMetadata = draft.getXmlData(false);
            String xmlAfter = outp.outputString(afterMetadata);
            new RecordUpdatedEvent(md.getId(), event.getUser(), xmlBefore, xmlAfter).publish(ApplicationContextHolder.get());
        }

        return md;
    }
}
