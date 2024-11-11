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
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectDeletionSubmittor;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Log.info(Geonet.DATA_MANAGER, String.format("Replacing metadata approved record (%d) with draft record (%d)", md.getId(), draft.getId()));
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
        Log.info(Geonet.DATA_MANAGER, String.format("Copying draft record '%d' resources to approved record '%d'", draft.getId(), md.getId()));
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
            Element xmlData = draft.getXmlData(false);
            String changeDate = draft.getDataInfo().getChangeDate().getDateAndTime();

            removeDraft((MetadataDraft) draft);

            // Copy contents
            Log.trace(Geonet.DATA_MANAGER, "Update record with id " + md.getId());
            md = metadataManager.updateMetadata(context, String.valueOf(md.getId()),
                xmlData, false, false,
                context.getLanguage(), changeDate, true, IndexingMode.full);

            Log.info(Geonet.DATA_MANAGER, "Record '" + md.getUuid() + "(" +md.getId() +")' update with draft contents from metadata id '" + draft.getId() +"'.");

            Log.info(Geonet.DATA_MANAGER, "Cleaning up draft record resources for metadata '" + draft.getUuid() + "(" +draft.getId() +")'");
            store.delResources(context, draft.getId());
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
            searchManager.deleteByUuid(draft.getUuid(), DirectDeletionSubmittor.INSTANCE);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't cleanup draft " + draft, e);
        }
    }
}
