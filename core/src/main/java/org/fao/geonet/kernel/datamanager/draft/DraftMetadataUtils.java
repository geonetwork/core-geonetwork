//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.draft;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.eclipse.jetty.io.RuntimeIOException;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.attachments.StoreUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;

public class DraftMetadataUtils extends BaseMetadataUtils {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;
    @Autowired
    private IMetadataOperations metadataOperations;
    @Autowired
    private IMetadataStatus metadataStatus;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private StatusValueRepository statusValueRepository;
    @Autowired
    private MetadataStatusRepository metadataStatusRepository;
    @Autowired
    private MetadataRatingByIpRepository metadataRatingByIpRepository;
    @Autowired
    private MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private AccessManager am;
    @Autowired
    IMetadataUtils metadataUtils;

    private ServiceContext context;
    private Set<String> listOfStatusToTriggerDraftCreation = Sets.newHashSet(StatusValue.Status.APPROVED);

    public void init(ServiceContext context, Boolean force) throws Exception {
        this.context = context;
        super.init(context, force);
    }

    @Override
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(metadataType);
                }
            });
        } else {
            super.setTemplateExt(id, metadataType);
        }
    }

    /**
     * Set metadata type to subtemplate and set the title. Only subtemplates need to
     * persist the title as it is used to give a meaningful title for use when
     * offering the subtemplate to users in the editor.
     *
     * @param id    Metadata id to set to type subtemplate
     * @param title Title of metadata of subtemplate/fragment
     */
    @Override
    public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(MetadataType.SUB_TEMPLATE);
                    if (title != null) {
                        dataInfo.setTitle(title);
                    }
                }
            });

        } else {
            super.setSubtemplateTypeAndTitleExt(id, title);
        }
    }

    @Override
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri)
        throws Exception {
        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft metadata) {
                    MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
                    harvestInfo.setUuid(harvestUuid);
                    harvestInfo.setHarvested(harvestUuid != null);
                    harvestInfo.setUri(harvestUri.orNull());
                }
            });
        } else {
            super.setHarvestedExt(id, harvestUuid, harvestUri);
        }
    }

    /**
     * @param idString
     * @param displayOrder
     * @throws Exception
     */
    @Override
    public void updateDisplayOrder(final String idString, final String displayOrder) throws Exception {
        Integer id = Integer.valueOf(idString);
        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
                }
            });
        } else {
            super.updateDisplayOrder(idString, displayOrder);
        }
    }

    /**
     * Rates a metadata.
     *
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating    range should be 1..5
     * @throws Exception hmm
     */
    @Override
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        // Save rating for this IP
        MetadataRatingByIp ratingEntity = new MetadataRatingByIp();
        ratingEntity.setRating(rating);
        ratingEntity.setId(new MetadataRatingByIpId(metadataId, ipAddress));

        ratingByIpRepository.save(ratingEntity);

        final int newRating = ratingByIpRepository.averageRating(metadataId);

        if (metadataDraftRepository.existsById(metadataId)) {
            metadataDraftRepository.update(metadataId, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setRating(newRating);
                }
            });
        } else {
            return super.rateMetadata(metadataId, ipAddress, rating);
        }
        return rating;
    }

    @SuppressWarnings("unchecked")
    @Override
    public long count(Specification<? extends AbstractMetadata> specs) {
        long tmp = 0;
        try {
            tmp += super.count((Specification<Metadata>) specs);
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<Metadata>
        }
        try {
            tmp += metadataDraftRepository.count((Specification<MetadataDraft>) specs);
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return tmp;
    }

    @Override
    public long count() {
        return super.count() + metadataDraftRepository.count();
    }

    @Override
    public AbstractMetadata findOne(int id) {
        if (super.exists(id)) {
            return super.findOne(id);
        }

        java.util.Optional<MetadataDraft> md = metadataDraftRepository.findById(id);

        return md.isPresent() ? md.get() : null;
    }

    @Override
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return super.existsMetadataUuid(uuid) || !findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
    }

    /**
     * If the user has permission to see the draft, draft goes first
     */
    @Override
    public AbstractMetadata findOneByUuid(String uuid) {
        AbstractMetadata md = super.findOneByUuid(uuid);
        try {
            if (md != null && am.canEdit(context, Integer.toString(md.getId()))) {
                AbstractMetadata tmp = metadataDraftRepository.findOneByUuid(uuid);
                if (tmp != null) {
                    md = tmp;
                }
            } else if (md == null) {
                // A draft without an approved md
                md = metadataDraftRepository.findOneByUuid(uuid);
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, e, e);
        }
        return md;
    }

    /**
     * Return all records, including drafts.
     */
    @Override
    public List<? extends AbstractMetadata> findAllByUuid(String uuid) {
        List<AbstractMetadata> res = new LinkedList<AbstractMetadata>();
        res.addAll(super.findAllByUuid(uuid));
        res.addAll(metadataDraftRepository.findAllByUuid(uuid));
        return res;
    }

    @Override
    public AbstractMetadata findOne(Specification<? extends AbstractMetadata> spec) {
        AbstractMetadata md = null;

        try {
            md = super.findOne(spec);
        } catch (ClassCastException t) {
            // That's fine, it can be a draft specification
        }

        if (md == null) {
            try {
                java.util.Optional<MetadataDraft> mdDraft = metadataDraftRepository.findOne((Specification<MetadataDraft>) spec);

                if (mdDraft.isPresent()) {
                    md = mdDraft.get();
                }
            } catch (ClassCastException t) {
                throw new ClassCastException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
            }
        }

        return md;
    }

    @Override
    public AbstractMetadata findOne(String id) {
        AbstractMetadata md = super.findOne(id);
        if (md == null) {
            java.util.Optional<MetadataDraft> draft = metadataDraftRepository.findById(Integer.parseInt(id));
            return draft.isPresent() ? draft.get() : null;
        }
        return md;
    }

    @Override
    public List<? extends AbstractMetadata> findAllByHarvestInfo_Uuid(String uuid) {
        List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
        list.addAll(metadataDraftRepository.findAllByHarvestInfo_Uuid(uuid));
        list.addAll(super.findAllByHarvestInfo_Uuid(uuid));
        return list;
    }

    @Override
    public Iterable<? extends AbstractMetadata> findAll(Set<Integer> keySet) {
        List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
        for (AbstractMetadata md : super.findAll(keySet)) {
            list.add(md);
        }
        list.addAll(metadataDraftRepository.findAllById(keySet));
        return list;
    }

    @Override
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> specs) {
        List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
        try {
            list.addAll(super.findAll(specs));
        } catch (ClassCastException t) {
            // That's fine, maybe it is a draft specification
        }
        try {
            list.addAll(metadataDraftRepository.findAll((Specification<MetadataDraft>) specs));
        } catch (ClassCastException t) {
            // That's fine, maybe it is a metadata specification
        }
        return list;
    }

    @Override
    public List<SimpleMetadata> findAllSimple(String harvestUuid) {
        List<SimpleMetadata> list = super.findAllSimple(harvestUuid);
        list.addAll(metadataDraftRepository.findSimple(harvestUuid));
        return list;
    }

    @Override
    public boolean exists(Integer iId) {
        return super.exists(iId) || metadataDraftRepository.existsById(iId);
    }

    @Override
    public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
        List<Pair<Integer, ISODate>> list = new LinkedList<Pair<Integer, ISODate>>();

        list.addAll(super.findAllIdsAndChangeDates(pageable).getContent());
        list.addAll(metadataDraftRepository.findIdsAndChangeDates(pageable).getContent());

        Page<Pair<Integer, ISODate>> res = new PageImpl<Pair<Integer, ISODate>>(list, pageable, list.size());
        return res;
    }

    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> spec) {
        Map<Integer, MetadataSourceInfo> map = new LinkedHashMap<Integer, MetadataSourceInfo>();
        try {
            map.putAll(super.findAllSourceInfo(spec));
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<Metadata>
        }

        try {
            map.putAll(metadataDraftRepository.findSourceInfo((Specification<MetadataDraft>) spec));
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return map;
    }

    @Override
    public List<Integer> findAllIdsBy(Specification<? extends AbstractMetadata> specs) {
        List<Integer> res = new LinkedList<Integer>();

        try {
            res.addAll(super.findAllIdsBy(specs));
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<Metadata>
        }

        try {
            res.addAll(metadataDraftRepository.findIdsBy((Specification<MetadataDraft>) specs));
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<MetadataDraft>
        }

        return res;
    }

    /**
     * Start an editing session. This will record the original metadata record in
     * the session under the
     * {@link Geonet.Session#METADATA_BEFORE_ANY_CHANGES} +
     * id session property.
     * <p>
     * The record contains geonet:info element.
     * <p>
     * Note: Only the metadata record is stored in session. If the editing session
     * upload new documents or thumbnails, those documents will not be cancelled.
     * This needs improvements.
     */
    @Override
    public Integer startEditingSession(ServiceContext context, String id) throws Exception {
        // Check id
        AbstractMetadata md = findOne(Integer.valueOf(id));

        if (md == null) {
            throw new EntityNotFoundException("We couldn't find the metadata to edit");
        }
        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);

        // Do we have a metadata draft already?
        if (metadataDraftRepository.findOneByUuid(md.getUuid()) != null) {
            id = Integer.toString(metadataDraftRepository.findOneByUuid(md.getUuid()).getId());

            Log.trace(Geonet.DATA_MANAGER, "Editing draft with id " + id);
        } else if (isMdWorkflowEnable
            && (context.getBean(IMetadataManager.class) instanceof DraftMetadataManager)
            && listOfStatusToTriggerDraftCreation.contains(
                metadataStatus.getCurrentStatus(Integer.parseInt(id)))
        ) {
            id = createDraft(context, id, md);

            Log.trace(Geonet.DATA_MANAGER, "Creating draft with id " + id + " to edit.");
        }

        if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
            Log.trace(Geonet.DATA_MANAGER, "Editing record with id = " + id);
            Log.trace(Geonet.DATA_MANAGER, "Status of record: " + metadataStatus.getCurrentStatus(Integer.valueOf(id)));
        }
        return super.startEditingSession(context, id);
    }

    private String createDraft(ServiceContext context, String id, AbstractMetadata md)
        throws Exception, IOException, JDOMException {
        // We have to create the draft using the metadata information
        String parentUuid = null;

        String groupOwner = null;
        String source = null;
        Integer owner = 1;

        if (md.getSourceInfo() != null) {
            if (md.getSourceInfo().getSourceId() != null) {
                source = md.getSourceInfo().getSourceId().toString();
            }
            if (md.getSourceInfo().getGroupOwner() != null) {
                groupOwner = md.getSourceInfo().getGroupOwner().toString();
            }
            owner = md.getSourceInfo().getOwner();
        }

        id = createDraft(context, id, groupOwner, source, owner, parentUuid, md.getDataInfo().getType().codeString,
            md.getUuid());
        return id;
    }

    protected String createDraft(ServiceContext context, String templateId, String groupOwner, String source, int owner,
                                 String parentUuid, String isTemplate, String uuid) throws Exception {

        Log.trace(Geonet.DATA_MANAGER, "createDraft(" + templateId + "," + groupOwner + "," + source + "," + owner + ","
            + parentUuid + "," + isTemplate + "," + uuid + ")");

        java.util.Optional<Metadata> templateMetadataOpt = getMetadataRepository().findById(Integer.valueOf(templateId));
        if (!templateMetadataOpt.isPresent()) {
            throw new IllegalArgumentException("Template id not found : " + templateId);
        }
        Metadata templateMetadata = templateMetadataOpt.get();

        String schema = templateMetadata.getDataInfo().getSchemaId();
        String data = templateMetadata.getData();
        Element xml = Xml.loadString(data, false);
        if (templateMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            xml = metadataManager.updateFixedInfo(schema, Optional.<Integer>absent(), uuid, xml, parentUuid,
                UpdateDatestamp.NO, context);
        }
        MetadataDraft newMetadata = new MetadataDraft();
        newMetadata.setUuid(uuid);
        newMetadata.setApprovedVersion(templateMetadata);
        newMetadata.getDataInfo().setChangeDate(new ISODate()).setCreateDate(new ISODate()).setSchemaId(schema)
            .setType(MetadataType.lookup(isTemplate));
        if (groupOwner != null) {
            newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
        }
        newMetadata.getSourceInfo().setOwner(owner);

        if (source != null) {
            newMetadata.getSourceInfo().setSourceId(source);
        }
        // If there is a default category for the group, use it:
        if (groupOwner != null) {
            java.util.Optional<Group> group = groupRepository.findById(Integer.valueOf(groupOwner));

            if (group.isPresent() && (group.get().getDefaultCategory() != null)) {
                newMetadata.getCategories().add(group.get().getDefaultCategory());
            }
        }

        for (MetadataCategory mc : templateMetadata.getCategories()) {
            newMetadata.getCategories().add(mc);
        }

        try {
            newMetadata = (MetadataDraft) metadataManager.insertMetadata(context, newMetadata, xml, IndexingMode.full, true,
                UpdateDatestamp.YES, false, true);

            Integer finalId = newMetadata.getId();

            // Remove all default privileges:
            metadataOperations.deleteMetadataOper(String.valueOf(finalId), false);

            // Copy privileges from original metadata
            for (OperationAllowed op : metadataOperations.getAllOperations(templateMetadata.getId())) {

                // Only interested in editing and reviewing privileges and group owner privileges
                // No one else should be able to see it
                if (op.getId().getOperationId() == ReservedOperation.editing.getId() ||
                    ((groupOwner != null) && (op.getId().getGroupId() == Integer.parseInt(groupOwner)))) {
                    Log.trace(Geonet.DATA_MANAGER, "Assign operation: " + op);
                    metadataOperations.forceSetOperation(context, finalId, op.getId().getGroupId(),
                        op.getId().getOperationId());
                } else {
                    Log.trace(Geonet.DATA_MANAGER, "Skipping operation: " + op);
                }
            }

            cloneFiles(templateMetadata, newMetadata);

            // Copy validation status from original metadata
            List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(templateMetadata.getId());
            for (MetadataValidation mv : validations) {
                MetadataValidation metadataValidation = new MetadataValidation()
                    .setId(new MetadataValidationId(finalId, mv.getId().getValidationType()))
                    .setStatus(mv.getStatus()).setRequired(mv.isRequired())
                    .setValid(mv.isValid()).setValidationDate(mv.getValidationDate())
                    .setNumTests(mv.getNumTests()).setNumFailures(mv.getNumFailures())
                    .setReportUrl(mv.getReportUrl()).setReportContent(mv.getReportContent());

                metadataValidationRepository.save(metadataValidation);
            }

            // Enable workflow on draft and make sure original record has also the workflow
            // enabled
            Set<Integer> metadataIds = new HashSet<Integer>();
            metadataIds.add(finalId);

            // --- use StatusActionsFactory and StatusActions class to
            // --- change status and carry out behaviours for status changes
            StatusActionsFactory saf = context.getBean(StatusActionsFactory.class);
            StatusActions sa = saf.createStatusActions(context);

            int author = context.getUserSession().getUserIdAsInt();
            Integer status = Integer.valueOf(StatusValue.Status.DRAFT);
            java.util.Optional<StatusValue> statusValue = statusValueRepository.findById(status);

            String lang = context.getLanguage();
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
                new Locale(lang));

            if (statusValue.isPresent()) {
                for (Integer mdId : metadataIds) {
                    MetadataStatus metadataStatus = new MetadataStatus();
                    metadataStatus.setMetadataId(mdId);
                    metadataStatus.setUuid(uuid);
                    metadataStatus.setChangeDate(new ISODate());
                    metadataStatus.setUserId(author);
                    metadataStatus.setStatusValue(statusValue.get());
                    metadataStatus.setChangeMessage(messages.getString("metadata_status_editing_instance_created_text"));
                    metadataStatus.setTitles(metadataUtils.extractTitles(newMetadata.getDataInfo().getSchemaId(), xml));

                    List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
                    listOfStatusChange.add(metadataStatus);
                    sa.onStatusChange(listOfStatusChange, true);
                }
            }

            return String.valueOf(finalId);
        } catch (Throwable t) {
            Log.error(Geonet.DATA_MANAGER, "Editing instance creation failed", t);
        }
        return templateId;
    }

    @Override
    public void cloneFiles(AbstractMetadata original, AbstractMetadata dest) {
        try {
            StoreUtils.copyDataDir(context, original.getUuid(), dest.getUuid(), false);
            cloneStoreFileUploadRequests(original, dest);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "Failed copy of resources: " + ex.getMessage(), ex);
            throw new RuntimeIOException(ex);
        }
    }

    @Override
    public void replaceFiles(AbstractMetadata original, AbstractMetadata dest) {
        try {
            boolean oldApproved = true;
            boolean newApproved = false;

            // If destination is approved then this is a working copy so the original will not be approved.
            if (metadataUtils.isMetadataApproved(dest.getId())) {
                oldApproved = false;
                newApproved = true;
            }
            StoreUtils.replaceDataDir(context, original.getUuid(), dest.getUuid(), oldApproved, newApproved);
            cloneStoreFileUploadRequests(original, dest);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "Failed copy of resources: " + ex.getMessage(), ex);
            throw new RuntimeIOException(ex);
        }
    }


    @Override
    public void cancelEditingSession(ServiceContext context, String id) throws Exception {
        // Restore the draft to the state it was in before editing
        super.cancelEditingSession(context, id);

        int intId = Integer.parseInt(id);

        // Remove the draft copy if the metadata was edited and the user cancels the editor, without saving any change
        if (metadataDraftRepository.existsById(intId) &&
            (context.getUserSession().getProperty(Geonet.Session.METADATA_EDITING_CREATED_DRAFT) == Boolean.TRUE)) {
            try {
                // Remove related data
                metadataOperations.deleteMetadataOper(id, false);
                metadataRatingByIpRepository.deleteAllById_MetadataId(intId);
                metadataValidationRepository.deleteAllById_MetadataId(intId);
                metadataStatusRepository.deleteAllById_MetadataId(intId);

                // --- remove metadata
                xmlSerializer.delete(id, ServiceContext.get());
                searchManager.delete(String.format("+id:%s", id));

                // Unset METADATA_EDITING_CREATED_DRAFT flag
                context.getUserSession().removeProperty(Geonet.Session.METADATA_EDITING_CREATED_DRAFT);

                // Get the resource store
                Store store = context.getBean("resourceStore", Store.class);

                // Register synchronization to delete resources after commit
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            // Delete resources from the store
                            store.delResources(context, intId);
                        } catch (Exception e) {
                            Log.error(Geonet.DATA_MANAGER, "Couldn't delete resources for draft " + id, e);
                        }
                    }
                });

            } catch (Exception e) {
                Log.error(Geonet.DATA_MANAGER, "Couldn't cleanup draft " + id, e);
            }
        }
    }

    /**
     * Stores a file upload request in the MetadataFileUploads table.
     */
    private void cloneStoreFileUploadRequests(AbstractMetadata original, AbstractMetadata copy) {
        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        repo.deleteAll(MetadataFileUploadSpecs.hasMetadataId(copy.getId()));

        for (MetadataFileUpload mfu : repo.findAll(MetadataFileUploadSpecs.hasMetadataId(original.getId()))) {
            MetadataFileUpload metadataFileUpload = new MetadataFileUpload();

            metadataFileUpload.setMetadataId(copy.getId());
            metadataFileUpload.setFileName(mfu.getFileName());
            metadataFileUpload.setFileSize(mfu.getFileSize());
            metadataFileUpload.setUploadDate(mfu.getUploadDate());
            metadataFileUpload.setUserName(mfu.getUserName());
            metadataFileUpload.setDeletedDate(mfu.getDeletedDate());

            repo.save(metadataFileUpload);
        }
    }

    public void setListOfStatusCreatingDraft(Set<String> listOfStatusCreatingDraft) {
        this.listOfStatusToTriggerDraftCreation = listOfStatusCreatingDraft;
    }

    public Set<String> getListOfStatusCreatingDraft() {
        return listOfStatusToTriggerDraftCreation;
    }
}
