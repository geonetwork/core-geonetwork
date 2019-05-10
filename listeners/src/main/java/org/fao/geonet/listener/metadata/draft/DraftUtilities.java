package org.fao.geonet.listener.metadata.draft;

import java.util.List;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

@Service
public class DraftUtilities {

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private MetadataFileUploadRepository metadataFileUploadRepository;

    @Autowired
    private SearchManager searchManager;

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
    private DraftMetadataUtils draftMetadataUtils;

    /**
     * Replace the contents of the record with the ones on the draft, if exists, and
     * remove the draft
     *
     * @param md
     * @param draft
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
        for (MetadataValidation mv : validations) {
            mv.getId().setMetadataId(md.getId());
            metadataValidationRepository.save(mv);
        }

        // Reassign metadata workflow statuses
        List<MetadataStatus> statuses = metadataStatusRepository.findAllById_MetadataId(draft.getId(),
            SortUtils.createSort(MetadataStatus_.id, MetadataStatusId_.metadataId));
        for (MetadataStatus old : statuses) {
            MetadataStatus st = new MetadataStatus();
            st.setChangeMessage(old.getChangeMessage());
            st.setCloseDate(old.getCloseDate());
            st.setCurrentState(old.getCurrentState());
            st.setOwner(old.getOwner());
            st.setPreviousState(old.getPreviousState());
            st.setStatusValue(old.getStatusValue());
            MetadataStatusId id = new MetadataStatusId();
            id.setChangeDate(old.getId().getChangeDate());
            id.setStatusId(old.getId().getStatusId());
            id.setUserId(old.getId().getUserId());
            id.setMetadataId(md.getId());
            st.setId(id);
            metadataStatusRepository.save(st);
            metadataStatusRepository.delete(old);
        }

        // Reassign file uploads
        draftMetadataUtils.cloneFiles(draft, md);
        metadataFileUploadRepository.deleteAll(MetadataFileUploadSpecs.hasMetadataId(md.getId()));
        List<MetadataFileUpload> fileUploads = metadataFileUploadRepository
            .findAll(MetadataFileUploadSpecs.hasMetadataId(draft.getId()));
        for (MetadataFileUpload fu : fileUploads) {
            fu.setMetadataId(md.getId());
            metadataFileUploadRepository.save(fu);
        }

        try {
            ServiceContext context = ServiceContext.get();
            Element xmlData = draft.getXmlData(false);
            String changeDate = draft.getDataInfo().getChangeDate().getDateAndTime();

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
        if (!metadataDraftRepository.exists(id)) {
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
            searchManager.delete(id + "");
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't cleanup draft " + draft, e);
        }
    }
}
