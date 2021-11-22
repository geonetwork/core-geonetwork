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
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade with utility methods responsible for workflow transitions.
 */
@Service
public class DraftUtilities {

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private MetadataFileUploadRepository metadataFileUploadRepository;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private XmlSerializer xmlSerializer;

    @Autowired
    private IMetadataOperations metadataOperations;

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Autowired
    private MetadataRatingByIpRepository metadataRatingByIpRepository;

    @Autowired
    private IMetadataUtils draftMetadataUtils;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    @Qualifier("resourceStore")
    private Store store;

    /**
     * Replace the contents of the record with the ones on the draft, if exists, and
     * remove the draft
     *
     * @param md
     * @return
     */
    public AbstractMetadata replaceMetadataWithDraft(AbstractMetadata md) {
        AbstractMetadata draft = metadataDraftRepository.findOneByUuid(md.getUuid());

        if (draft != null) {
            return replaceMetadataWithDraft(md, draft);
        }

        return md;
    }

    /**
     * Replace the contents of the record with the ones on the draft and remove the
     * draft
     *
     * @param md
     * @param draft
     * @return
     */
    public AbstractMetadata replaceMetadataWithDraft(AbstractMetadata md, AbstractMetadata draft) {
        Log.trace(Geonet.DATA_MANAGER, "Found approved record with id " + md.getId());
        Log.trace(Geonet.DATA_MANAGER, "Found draft with id " + draft.getId());
        // Reassign metadata validations
        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(draft.getId());
        metadataValidationRepository.deleteAll(MetadataValidationSpecs.hasMetadataId(md.getId()));
        for (MetadataValidation draftValidation : validations) {
            MetadataValidation metadataValidation = new MetadataValidation()
                .setId(new MetadataValidationId(md.getId(), draftValidation.getId().getValidationType()))
                .setStatus(draftValidation.getStatus()).setRequired(draftValidation.isRequired())
                .setValid(draftValidation.isValid()).setValidationDate(draftValidation.getValidationDate())
                .setNumTests(draftValidation.getNumTests()).setNumFailures(draftValidation.getNumFailures())
                .setReportUrl(draftValidation.getReportUrl()).setReportContent(draftValidation.getReportContent());

            metadataValidationRepository.save(metadataValidation);
            metadataValidationRepository.delete(draftValidation);
        }

        // Reassign metadata workflow statuses
        List<MetadataStatus> statuses = metadataStatusRepository.findAllByMetadataId(draft.getId(),
            SortUtils.createSort(MetadataStatus_.metadataId));
        for (MetadataStatus old : statuses) {
            MetadataStatus st = new MetadataStatus();
            st.setChangeMessage(old.getChangeMessage());
            st.setCloseDate(old.getCloseDate());
            st.setCurrentState(old.getCurrentState());
            st.setOwner(old.getOwner());
            st.setPreviousState(old.getPreviousState());
            st.setStatusValue(old.getStatusValue());
            st.setChangeDate(old.getChangeDate());
            st.setUserId(old.getUserId());
            st.setMetadataId(md.getId());
            st.setUuid(md.getUuid());
            try {
                st.setTitles(metadataUtils.extractTitles(Integer.toString(md.getId())));
            } catch (Exception e) {
                Log.error(Geonet.DATA_MANAGER, String.format(
                        "Error locating titles for metadata id: %d", +md.getId()), e);
            }

            metadataStatusRepository.save(st);
            metadataStatusRepository.delete(old);
        }

        // Reassign file uploads
        draftMetadataUtils.replaceFiles(draft, md);

        metadataFileUploadRepository.deleteAll(MetadataFileUploadSpecs.hasMetadataId(md.getId()));
        List<MetadataFileUpload> fileUploads = metadataFileUploadRepository
            .findAll(MetadataFileUploadSpecs.hasMetadataId(draft.getId()));
        for (MetadataFileUpload fu : fileUploads) {
            fu.setMetadataId(md.getId());
            metadataFileUploadRepository.save(fu);
        }

        try {
            ServiceContext context = ServiceContext.get();
            if( context == null ){
                Log.trace(Geonet.DATA_MANAGER,"context unavailable");
            }
            Element xmlData = draft.getXmlData(false);
            String changeDate = draft.getDataInfo().getChangeDate().getDateAndTime();

            store.delResources(context, draft.getUuid(), false);
            removeDraft((MetadataDraft) draft);

            // Copy contents
            Log.trace(Geonet.DATA_MANAGER, "Update record with id " + md.getId());
            md = metadataManager.updateMetadata(context, String.valueOf(md.getId()),
                xmlData, false, false, true,
                context.getLanguage(), changeDate, true);

            Log.info(Geonet.DATA_MANAGER, "Record updated with draft contents: " + md.getId());

        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Error upgrading from draft record with id " + md.getId(), e);
        }
        return md;
    }

    public void removeDraft(MetadataDraft draft) {

        Integer id = draft.getId();
        if (!metadataDraftRepository.existsById(id)) {
            // We are being called after removing everything related to this record.
            // Nothing to do here
            return;
        }

        Log.trace(Geonet.DATA_MANAGER, "Removing draft " + draft);

        try {
            // Remove related data
            metadataOperations.deleteMetadataOper(String.valueOf(id), false);
            metadataRatingByIpRepository.deleteAllById_MetadataId(id);
            metadataValidationRepository.deleteAllById_MetadataId(id);
            metadataStatusRepository.deleteAllById_MetadataId(id);

            // --- remove metadata
            xmlSerializer.delete(String.valueOf(id), ServiceContext.get());
            searchManager.delete(String.format("+id:%d", id ));
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't cleanup draft " + draft, e);
        }
    }
}
