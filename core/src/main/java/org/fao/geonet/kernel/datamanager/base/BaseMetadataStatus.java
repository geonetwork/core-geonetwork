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

package org.fao.geonet.kernel.datamanager.base;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.specification.MetadataStatusSpecs;
import org.fao.geonet.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;

import jeeves.server.context.ServiceContext;

public class BaseMetadataStatus implements IMetadataStatus {

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;
    @Autowired
    private IMetadataIndexer metadataIndexer;
    @Autowired
    private StatusValueRepository statusValueRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    @Lazy
    private SettingManager settingManager;
    @Autowired
    IMetadataUtils metadataUtils;

    @Override
    public boolean isUserMetadataStatus(int userId) throws Exception {
        return metadataStatusRepository.count(MetadataStatusSpecs.hasUserId(userId)) > 0;
    }

    @Override
    public void transferMetadataStatusOwnership(int oldUserId, int newUserId) throws Exception {
        List<MetadataStatus> oldUserStatus = metadataStatusRepository.findAll(MetadataStatusSpecs.hasUserId(oldUserId));
        oldUserStatus.stream().forEach(s -> {
            s.setUserId(newUserId);
        });
        metadataStatusRepository.saveAll(oldUserStatus);
    }

    /**
     * Return last workflow status for the metadata id
     */
    @Override
    public MetadataStatus getStatus(int metadataId) throws Exception {
        String sortField = SortUtils.createPath(MetadataStatus_.changeDate);
        List<MetadataStatus> status = metadataStatusRepository.findAllByMetadataIdAndByType(metadataId,
                StatusValueType.workflow, Sort.by(Sort.Direction.DESC, sortField));
        if (status.isEmpty()) {
            return null;
        } else {
            return status.get(0);
        }
    }

    /**
     * Return previous workflow status for the metadata id
     */
    @Override
    public MetadataStatus getPreviousStatus(int metadataId) throws Exception {
        String sortField = SortUtils.createPath(MetadataStatus_.id, MetadataStatus_.changeDate);
        List<MetadataStatus> metadataStatusList = metadataStatusRepository.findAllByMetadataIdAndByType(
            metadataId, StatusValueType.workflow, Sort.by(Sort.Direction.DESC, sortField));
        if (metadataStatusList.isEmpty() || metadataStatusList.size() == 1) {
            return null;
        } else {
            return metadataStatusList.get(1);
        }
    }

    /**
     * Return all status for the metadata id
     */
    @Override
    public List<MetadataStatus> getAllStatus(int metadataId) throws Exception {
        String sortField = SortUtils.createPath(MetadataStatus_.changeDate);
        List<MetadataStatus> status = metadataStatusRepository.findAllByMetadataId(metadataId,
                Sort.by(Sort.Direction.DESC, sortField));
        if (status.isEmpty()) {
            return null;
        } else {
            return status;
        }
    }

    /**
     * Return status of metadata id.
     */
    @Override
    public String getCurrentStatus(int metadataId) throws Exception {
        MetadataStatus status = getStatus(metadataId);
        if (status == null) {
            return StatusValue.Status.DRAFT;
        }

        return String.valueOf(status.getStatusValue().getId());
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Categories API
    // ---
    // --------------------------------------------------------------------------

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    @Override
    @Deprecated
    public MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate,
            String changeMessage) throws Exception {
        MetadataStatus statusObject = setStatusExt(context, id, status, changeDate, changeMessage);
        metadataIndexer.indexMetadata(Integer.toString(id), DirectIndexSubmitter.INSTANCE, IndexingMode.full);
        return statusObject;
    }

    @Override
    public MetadataStatus setStatusExt(MetadataStatus metatatStatus, boolean updateIndex) throws Exception {
        metadataStatusRepository.save(metatatStatus);
        if (updateIndex) {
            metadataIndexer.indexMetadata(metatatStatus.getMetadataId() + "", DirectIndexSubmitter.INSTANCE, IndexingMode.full);
        }
        return metatatStatus;
    }

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    @Override
    public MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate,
            String changeMessage) throws Exception {
        Optional<StatusValue> statusValue = statusValueRepository.findById(status);

        if (!statusValue.isPresent()) {
            throw new IllegalArgumentException("The workflow status change requested is not valid: " + status);
        }

        MetadataStatus metatatStatus = new MetadataStatus();
        metatatStatus.setChangeMessage(changeMessage);

        metatatStatus.setStatusValue(statusValue.get());
        int userId = context.getUserSession().getUserIdAsInt();
        metatatStatus.setMetadataId(id);
        metatatStatus.setChangeDate(changeDate);
        metatatStatus.setUserId(userId);
        metatatStatus.setUuid(metadataUtils.getMetadataUuid(Integer.toString(id)));
        metatatStatus.setTitles(metadataUtils.extractTitles(Integer.toString(id)));

        return metadataStatusRepository.save(metatatStatus);
    }

    /**
     * If groupOwner match regular expression defined in setting
     * metadata/workflow/draftWhenInGroup, then set status to draft to enable
     * workflow.
     */
    @Override
    public void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception {
        if (StringUtils.isEmpty(groupOwner)) {
            return;
        }

        final Optional<Group> group = groupRepository.findById(Integer.valueOf(groupOwner));
        String groupName = "";
        if (group.isPresent()) {
            groupName = group.get().getName();
        }

        if (WorkflowUtil.isGroupWithEnabledWorkflow(groupName)) {
            setStatus(context, Integer.valueOf(newId), Integer.valueOf(StatusValue.Status.DRAFT), new ISODate(),
                    String.format("Workflow automatically enabled for record in group '%s'. Record status is set to %s.",
                            groupName, StatusValue.Status.DRAFT));
        }
    }

    /**
     * Safely change the status if the current is compatible.
     */
    @Override
    public void changeCurrentStatus(Integer userId, Integer metadataId, Integer newStatus) throws Exception {

        Optional<StatusValue> statusValue = statusValueRepository.findById(newStatus);

        if (!statusValue.isPresent()) {
            throw new IllegalArgumentException("The workflow status change requested is not valid: " + newStatus);
        }

        // Check compatible workflow status
        String currentState = this.getCurrentStatus(metadataId);
        String nextStatus = String.valueOf(newStatus);

        if (!verifyAllowedStatusTransition(currentState, nextStatus)) {
            throw new IllegalArgumentException("The workflow status change requested is not allowed");
        }

        MetadataStatus metatatStatus = new MetadataStatus();
        metatatStatus.setChangeMessage("");
        metatatStatus.setStatusValue(statusValue.get());
        metatatStatus.setMetadataId(metadataId);
        metatatStatus.setChangeDate(new ISODate());
        metatatStatus.setUserId(userId);
        metatatStatus.setUuid(metadataUtils.getMetadataUuid(Integer.toString(metadataId)));
        metatatStatus.setTitles(metadataUtils.extractTitles(Integer.toString(metadataId)));

        metadataStatusRepository.save(metatatStatus);
        metadataIndexer.indexMetadata(metadataId + "", DirectIndexSubmitter.INSTANCE, IndexingMode.full);
    }

    // Utility to verify workflow status transitions
    public static boolean verifyAllowedStatusTransition(String currentState, String nextStatus) {

        HashSet<String> draftCompatible = new HashSet<>();
        draftCompatible.add(StatusValue.Status.DRAFT);
        draftCompatible.add(StatusValue.Status.SUBMITTED);
        draftCompatible.add(StatusValue.Status.RETIRED);

        HashSet<String> submittedCompatible = new HashSet<>();
        submittedCompatible.add(StatusValue.Status.DRAFT);
        submittedCompatible.add(StatusValue.Status.SUBMITTED);
        submittedCompatible.add(StatusValue.Status.APPROVED);

        HashSet<String> approvedCompatible = new HashSet<>();
        approvedCompatible.add(StatusValue.Status.SUBMITTED);
        approvedCompatible.add(StatusValue.Status.APPROVED);
        approvedCompatible.add(StatusValue.Status.DRAFT);

        HashSet<String> retiredCompatible = new HashSet<>();
        retiredCompatible.add(StatusValue.Status.DRAFT);
        retiredCompatible.add(StatusValue.Status.SUBMITTED);
        retiredCompatible.add(StatusValue.Status.APPROVED);
        retiredCompatible.add(StatusValue.Status.RETIRED);

        if (StatusValue.Status.DRAFT.equals(currentState) && draftCompatible.contains(nextStatus)) {
            return true;
        } else if (StatusValue.Status.SUBMITTED.equals(currentState) && submittedCompatible.contains(nextStatus)) {
            return true;
        } else if (StatusValue.Status.APPROVED.equals(currentState) && approvedCompatible.contains(nextStatus)) {
            return true;
        } else if (StatusValue.Status.RETIRED.equals(currentState) && retiredCompatible.contains(nextStatus)) {
            return true;
        }

        return false;
    }

    public boolean canEditorEdit(Integer metadataId) throws Exception {

        String currentState = this.getCurrentStatus(metadataId);

        HashSet<String> draftCompatible = new HashSet<>();
        draftCompatible.add(StatusValue.Status.DRAFT);
        draftCompatible.add(StatusValue.Status.APPROVED);
        draftCompatible.add(StatusValue.Status.RETIRED);

        return draftCompatible.contains(currentState);
    }

}
