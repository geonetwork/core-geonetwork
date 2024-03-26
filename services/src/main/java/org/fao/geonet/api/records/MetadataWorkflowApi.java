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

package org.fao.geonet.api.records;

import io.swagger.annotations.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.FeatureNotEnabledException;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.*;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordRestoredEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.util.MetadataPublicationMailNotifier;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL;

@RequestMapping(value = { "/{portal}/api/records", "/{portal}/api/" + API.VERSION_0_1 + "/records" })
@Api(value = API_CLASS_RECORD_TAG, tags = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordWorkflow")
@ReadWriteController
public class MetadataWorkflowApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    MetadataStatusRepository metadataStatusRepository;

    @Autowired
    MetadataDraftRepository metadatadraftRepository;

    @Autowired
    StatusValueRepository statusValueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    IMetadataStatus metadataStatus;

    @Autowired
    AccessManager accessManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    DataManager dataManager;

    @Autowired
    IMetadataIndexer metadataIndexer;

    @Autowired
    StatusActionsFactory statusActionFactory;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private MetadataCategoryRepository metadataCategoryRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    @Autowired
    private MetadataCategoryRepository categoryRepository;

    @Autowired
    MetadataPublicationMailNotifier metadataPublicationMailNotifier;

    // The restore function currently supports these states
    static final Integer[] supportedRestoreStatuses = {
            Integer.parseInt(StatusValue.Events.RECORDUPDATED),
            Integer.parseInt(StatusValue.Events.RECORDPROCESSINGCHANGE),
            Integer.parseInt(StatusValue.Events.RECORDDELETED),
            Integer.parseInt(StatusValue.Events.RECORDRESTORED)};

    private enum State {
        BEFORE, AFTER
    }

    @ApiOperation(value = "Get record status history", notes = "", nickname = "getRecordStatusHistory")
    @RequestMapping(value = "/{metadataUuid}/status", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusResponse> getRecordStatusHistory(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @RequestParam(required = false) boolean details,
            @ApiParam(value = "Sort direction", required = false) @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder,
            HttpServletRequest request) throws Exception {
      try (ServiceContext context = ApiUtils.createServiceContext(request)) {
          AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, context);

          String sortField = SortUtils.createPath(MetadataStatus_.changeDate);

          List<MetadataStatus> listOfStatus = metadataStatusRepository.findAllByMetadataId(metadata.getId(),
              new Sort(sortOrder, sortField));

          // TODO: Add paging
          return buildMetadataStatusResponses(listOfStatus, details,
              context.getLanguage());
      }
    }

    @ApiOperation(value = "Get record status history by type", notes = "", nickname = "getRecordStatusHistoryByType")
    @RequestMapping(value = "/{metadataUuid}/status/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusResponse> getRecordStatusHistoryByType(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Type", required = true) @PathVariable StatusValueType type,
            @RequestParam(required = false) boolean details,
            @ApiParam(value = "Sort direction", required = false) @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder,
            HttpServletRequest request) throws Exception {
      try (ServiceContext context = ApiUtils.createServiceContext(request)) {
          AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, context);

          String sortField = SortUtils.createPath(MetadataStatus_.changeDate);

          List<MetadataStatus> listOfStatus = metadataStatusRepository.findAllByMetadataIdAndByType(metadata.getId(), type,
              new Sort(sortOrder, sortField));

          // TODO: Add paging
          return buildMetadataStatusResponses(listOfStatus, details,
              context.getLanguage());
      }
    }

    @ApiOperation(value = "Get last workflow status for a record", notes = "", nickname = "getStatus")
    @RequestMapping(value = "/{metadataUuid}/status/workflow/last", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Record status."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataWorkflowStatusResponse getStatus(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            HttpServletRequest request) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
      try (ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language())) {
          AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, context);

          // --- only allow the owner of the record to set its status
          if (!accessManager.isOwner(context, String.valueOf(metadata.getId()))) {
              throw new SecurityException(
                  "Only the owner of the metadata can get the status. User is not the owner of the metadata");
          }

          MetadataStatus recordStatus = metadataStatus.getStatus(metadata.getId());

          List<StatusValue> elStatus = statusValueRepository.findAllByType(StatusValueType.workflow);

          // --- get the list of content reviewers for this metadata record
          Set<Integer> ids = new HashSet<>();
          ids.add(metadata.getId());
          List<Pair<Integer, User>> reviewers = userRepository.findAllByGroupOwnerNameAndProfile(ids, Profile.Reviewer);
          reviewers.sort(Comparator.comparing(s -> s.two().getName()));

          List<User> listOfReviewers = new ArrayList<>();
          for (Pair<Integer, User> reviewer : reviewers) {
              listOfReviewers.add(reviewer.two());
          }
          return new MetadataWorkflowStatusResponse(recordStatus, listOfReviewers,
              accessManager.hasEditPermission(context, String.valueOf(metadata.getId())), elStatus);
      }
    }

    @ApiOperation(value = "Set the records status to approved", notes = "", nickname = "setRecordsStatus")
    @RequestMapping(value = "/approve", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Metadata approved ."),
        @ApiResponse(code = 400, message = "Metadata workflow not enabled.")})
    @ResponseBody
    MetadataProcessingReport approve(@RequestBody MetadataBatchApproveParameter approveParameter,
                                     @ApiIgnore
                                     @ApiParam(hidden = true) HttpSession session,
                                     @ApiIgnore
                                     @ApiParam(hidden = true) HttpServletRequest request) throws Exception {

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        checkWorkflowEnabled();

        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        try (ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language())) {
            List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
            boolean notifyByEmail = StringUtils.isNoneEmpty(settingManager.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(approveParameter.getUuids(),
                approveParameter.getBucket(), ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext appContext = ApplicationContextHolder.get();
            final AccessManager accessMan = appContext.getBean(AccessManager.class);


            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.isOwner(
                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    if (!isAllowedMetadataStatusChange(context, metadata, report)) {
                        continue;
                    }

                    MetadataStatus currentStatus = metadataStatus.getStatus(metadata.getId());

                    if (currentStatus == null) {
                        // Metadata not in the workflow
                        report.addMetadataInfos(metadata.getId(), metadata.getUuid(),
                            true, false, "Metadata workflow is not enabled");
                        continue;
                    } else {
                        if (!approveParameter.isDirectApproval()) {
                            if (currentStatus.getStatusValue().getId() != Integer.parseInt(StatusValue.Status.SUBMITTED)) {
                                report.addMetadataInfos(metadata.getId(), metadata.getUuid(),
                                    this.metadataUtils.isMetadataDraft(metadata.getId()),
                                    this.metadataUtils.isMetadataApproved(metadata.getId()),
                                    "Metadata is not in submitted status.");
                                continue;
                            }
                        }
                    }

                    // Change the metadata status to approved
                    changeMetadataStatus(context, metadata, currentStatus.getCurrentState(),
                        StatusValue.Status.APPROVED, approveParameter.getMessage());

                    report.incrementProcessedRecords();
                    listOfUpdatedRecords.add(String.valueOf(metadata.getId()));

                    // Check if it's published to send a mail notification
                    if (notifyByEmail) {
                        int metadataIdApproved = metadata.getId();
                        if (metadata instanceof MetadataDraft) {
                            Metadata metadataApproved = metadataRepository.findOneByUuid(metadata.getUuid());

                            if (metadataApproved != null) {
                                metadataIdApproved = metadataApproved.getId();
                            }
                        }
                        // Status has change to APPROVED and the metadata is published
                        if ((currentStatus.getStatusValue().getId() != Integer.parseInt(StatusValue.Status.APPROVED)) &&
                            this.metadataUtils.isMetadataPublished(metadataIdApproved)) {
                            MetadataPublicationNotificationInfo metadataNotificationInfo = new MetadataPublicationNotificationInfo();
                            metadataNotificationInfo.setMetadataUuid(metadata.getUuid());
                            metadataNotificationInfo.setMetadataId(metadataIdApproved);
                            metadataNotificationInfo.setGroupId(metadata.getSourceInfo().getGroupOwner());
                            metadataNotificationInfo.setPublished(true);
                            metadataNotificationInfo.setPublicationDateStamp(new ISODate());
                            metadataNotificationInfo.setReapproval(true);

                            metadataListToNotifyPublication.add(metadataNotificationInfo);
                        }
                    }
                }
            }
            dataManager.flush();
            metadataIndexer.indexMetadata(listOfUpdatedRecords);

            if (notifyByEmail && !metadataListToNotifyPublication.isEmpty()) {
                metadataPublicationMailNotifier.notifyPublication(messages, context.getLanguage(), metadataListToNotifyPublication);
            }

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }

    @ApiOperation(value = "Set the records status to submitted", notes = "", nickname = "submit")
    @RequestMapping(value = "/submit", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Metadata submitted ."),
        @ApiResponse(code = 400, message = "Metadata workflow not enabled.")})
    @ResponseBody
    MetadataProcessingReport submit(@RequestBody MetadataBatchSubmitParameter submitParameter,
                                     @ApiParam(hidden = true) HttpSession session,
                                     @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request,
            languageUtils.getIso3langCode(request.getLocales()));

        checkWorkflowEnabled();

        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(submitParameter.getUuids(),
                submitParameter.getBucket(), ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessManager.isOwner(
                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    if (!isAllowedMetadataStatusChange(context, metadata, report)) {
                        continue;
                    }

                    MetadataStatus currentStatus = metadataStatus.getStatus(metadata.getId());

                    if (currentStatus == null) {
                        // Metadata not in the workflow
                        report.addMetadataInfos(metadata.getId(), metadata.getUuid(),
                            true, false, "Metadata workflow is not enabled");
                        continue;
                    } else if (currentStatus.getStatusValue().getId() != Integer.parseInt(StatusValue.Status.DRAFT)) {
                        // Metadata not in draft status
                        report.addMetadataInfos(metadata.getId(), metadata.getUuid(),
                            this.metadataUtils.isMetadataDraft(metadata.getId()),
                            this.metadataUtils.isMetadataApproved(metadata.getId()),
                            "Metadata is not in draft status.");
                        continue;
                    }

                    // Change the metadata status to submitted
                    changeMetadataStatus(context, metadata, currentStatus.getCurrentState(),
                        StatusValue.Status.SUBMITTED, submitParameter.getMessage());

                    // Reindex the metadata table record to update the field _statusWorkflow that contains the composite
                    // status of the published and draft versions
                    if (metadata instanceof MetadataDraft) {
                        Metadata metadataApproved = metadataRepository.findOneByUuid(metadata.getUuid());

                        if (metadataApproved != null) {
                            listOfUpdatedRecords.add(String.valueOf(metadataApproved.getId()));
                        }
                    }

                    report.incrementProcessedRecords();
                    listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                }
            }
            dataManager.flush();
            metadataIndexer.indexMetadata(listOfUpdatedRecords);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }

    @ApiOperation(value = "Set the record status", notes = "", nickname = "setStatus")
    @RequestMapping(value = "/{metadataUuid}/status", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Status updated."),
            @ApiResponse(code = 400, message = "Metadata workflow not enabled."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setStatus(@ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Metadata status", required = true) @RequestBody(required = true) MetadataStatusParameter status,
            HttpServletRequest request) throws Exception {
      try (ServiceContext context = ApiUtils.createServiceContext(request, languageUtils.iso3code(request.getLocales()))) {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, context);
        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
        boolean notifyByEmail = StringUtils.isNoneEmpty(settingManager.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        int author = context.getUserSession().getUserIdAsInt();
        MetadataStatus metadataStatusValue = convertParameter(metadata.getId(), metadata.getUuid(), status, author);

        if (metadataStatusValue.getStatusValue().getType() == StatusValueType.workflow
          && !isMdWorkflowEnable) {
            throw new FeatureNotEnabledException(
                "Metadata workflow is disabled, can not be set the status of metadata")
                .withMessageKey("exception.resourceNotEnabled.workflow")
                .withDescriptionKey("exception.resourceNotEnabled.workflow.description");
        }

        // --- only allow the owner of the record to set its status
        if (!accessManager.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(
                    "Only the owner of the metadata can set the status of this record. User is not the owner of the metadata.");
        }

        boolean isAllowedSubmitApproveInvalidMd = settingManager
                .getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD);
        if (((status.getStatus() == Integer.parseInt(StatusValue.Status.SUBMITTED))
                || (status.getStatus() == Integer.parseInt(StatusValue.Status.APPROVED)))
                && !isAllowedSubmitApproveInvalidMd) {

            boolean isInvalid = MetadataUtils.retrieveMetadataValidationStatus(metadata, context);

            if (isInvalid) {
                throw new NotAllowedException("Metadata is invalid: can't be submitted or approved")
                    .withMessageKey("exception.resourceInvalid.metadata")
                    .withDescriptionKey("exception.resourceInvalid.metadata.description");
            }
        }

        // --- use StatusActionsFactory and StatusActions class to
        // --- change status and carry out behaviours for status changes
        StatusActions sa = statusActionFactory.createStatusActions(context);

        String metadataCurrentStatus = dataManager.getCurrentStatus(metadata.getId());
        metadataStatusValue.setPreviousState(metadataCurrentStatus);

        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
        listOfStatusChange.add(metadataStatusValue);
        sa.onStatusChange(listOfStatusChange, false);

          int metadataIdApproved = metadata.getId();

        // --- reindex metadata
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), true, null);

        // Reindex the metadata table record to update the field _statusWorkflow that contains the composite
        // status of the published and draft versions
        if (metadata instanceof MetadataDraft) {
            Metadata metadataApproved = metadataRepository.findOneByUuid(metadata.getUuid());

            if (metadataApproved != null) {
                metadataIdApproved = metadataApproved.getId();
                metadataIndexer.indexMetadata(String.valueOf(metadataApproved.getId()), true, null);
            }
        }

          if ((status.getStatus() == Integer.parseInt(StatusValue.Status.APPROVED) && notifyByEmail)
              && (this.metadataUtils.isMetadataPublished(metadataIdApproved))) {
              MetadataPublicationNotificationInfo metadataNotificationInfo = new MetadataPublicationNotificationInfo();
              metadataNotificationInfo.setMetadataUuid(metadata.getUuid());
              metadataNotificationInfo.setMetadataId(metadataIdApproved);
              metadataNotificationInfo.setGroupId(metadata.getSourceInfo().getGroupOwner());
              metadataNotificationInfo.setPublished(true);
              metadataNotificationInfo.setPublicationDateStamp(new ISODate());
              metadataNotificationInfo.setReapproval(metadataIdApproved != metadata.getId());

              metadataListToNotifyPublication.add(metadataNotificationInfo);

              metadataPublicationMailNotifier.notifyPublication(messages, context.getLanguage(), metadataListToNotifyPublication);
          }
      }
    }

    @ApiOperation(value = "Close a record task", notes = "", nickname = "closeTask")
    @RequestMapping(value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/close", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Task closed."),
            @ApiResponse(code = 404, message = "Status not found."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeTask(@ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Status identifier", required = true) @PathVariable int statusId,
            @ApiParam(value = "User identifier", required = true) @PathVariable int userId,
            @ApiParam(value = "Change date", required = true) @PathVariable String changeDate,
            @ApiParam(value = "Close date", required = true) @RequestParam String closeDate, HttpServletRequest request)
            throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        MetadataStatus metadataStatusValue = metadataStatusRepository
                .findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(metadata.getId(), statusId, userId, new ISODate(changeDate));

        if (metadataStatusValue != null) {
            metadataStatusRepository.update(metadataStatusValue.getId(),
                    entity -> entity.setCloseDate(new ISODate(closeDate)));
        } else {
            throw new ResourceNotFoundException(
                String.format("Can't find metadata status for record '%s', user '%d' at date '%s'", metadataUuid,
                    userId, changeDate));
        }
    }

    @ApiOperation(value = "Delete a record status", notes = "", nickname = "deleteStatus")
    @RequestMapping(value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Status removed."),
            @ApiResponse(code = 404, message = "Status not found."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecordStatus(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Status identifier", required = true) @PathVariable int statusId,
            @ApiParam(value = "User identifier", required = true) @PathVariable int userId,
            @ApiParam(value = "Change date", required = true) @PathVariable String changeDate,
            HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        MetadataStatus metadataStatusValue = metadataStatusRepository
            .findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(metadata.getId(), statusId, userId, new ISODate(changeDate));
        if (metadataStatusValue != null) {
            metadataStatusRepository.delete(metadataStatusValue);
            // TODO: Reindex record ?
        } else {
            throw new ResourceNotFoundException(
                String.format("Can't find metadata status for record '%s', user '%d' at date '%s'", metadataUuid,
                    userId, changeDate));
        }
    }

    @ApiOperation(value = "Delete all record status", notes = "", nickname = "deleteAllRecordStatus")
    @RequestMapping(value = "/{metadataUuid}/status", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Status removed."),
            @ApiResponse(code = 404, message = "Status not found."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllRecordStatus(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        metadataStatusRepository.deleteAllById_MetadataId(metadata.getId());
    }

    @ApiOperation(value = "Search status", notes = "", nickname = "searchStatusByType")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, path = "/status/search")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public List<MetadataStatusResponse> getStatusByType(
        @ApiParam(value = "One or more types to retrieve (ie. worflow, event, task). Default is all.", required = false) @RequestParam(required = false) List<StatusValueType> type,
        @ApiParam(value = "All event details including XML changes. Responses are bigger. Default is false", required = false) @RequestParam(required = false) boolean details,
        @ApiParam(value = "Sort Order (ie. DESC or ASC). Default is none.", required = false) @RequestParam(required = false) Sort.Direction sortOrder,
        @ApiParam(value = "One or more event author. Default is all.", required = false) @RequestParam(required = false) List<Integer> author,
        @ApiParam(value = "One or more event owners. Default is all.", required = false) @RequestParam(required = false) List<Integer> owner,
        @ApiParam(value = "One or more record identifier. Default is all.", required = false) @RequestParam(required = false) List<Integer> id,
        @ApiParam(value = "One or more metadata record identifier. Default is all.", required = false) @RequestParam(required = false) List<Integer> recordIdentifier,
        @ApiParam(value = "One or more metadata uuid. Default is all.", required = false) @RequestParam(required = false) List<String> uuid,
        @ApiParam(value = "One or more status id. Default is all.", required = false) @RequestParam(required = false) List<String> statusIds,
        @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String dateFrom,
        @ApiParam(value = "End date", required = false) @RequestParam(required = false) String dateTo,
        @ApiParam(value = "From page", required = false) @RequestParam(required = false, defaultValue = "0") Integer from,
        @ApiParam(value = "Number of records to return", required = false) @RequestParam(required = false, defaultValue = "100") Integer size,
        HttpServletRequest request) throws Exception {
      try (ServiceContext context = ApiUtils.createServiceContext(request)) {

        Profile profile = context.getUserSession().getProfile();
        if (profile != Profile.Administrator) {
            if (CollectionUtils.isEmpty(recordIdentifier) &&
                CollectionUtils.isEmpty(uuid)) {
                throw new NotAllowedException(
                    "Non administrator user must use a id or uuid parameter to search for status.");
            }

            if (!CollectionUtils.isEmpty(recordIdentifier)) {
                for (Integer recordId : recordIdentifier) {
                    try {
                        ApiUtils.canEditRecord(String.valueOf(recordId), request);
                    } catch (SecurityException e) {
                        throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(uuid)) {
                for (String recordId : uuid) {
                    try {
                        ApiUtils.canEditRecord(recordId, request);
                    } catch (SecurityException e) {
                        throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT);
                    }
                }
            }
        }
        PageRequest pageRequest;
        if (sortOrder != null) {
            Sort sortByStatusChangeDate = SortUtils.createSort(sortOrder, MetadataStatus_.changeDate).and(SortUtils.createSort(sortOrder, MetadataStatus_.id));
            pageRequest = new PageRequest(from, size, sortByStatusChangeDate);
        } else {
            // Default sort order
            Sort sortByStatusChangeDate = SortUtils.createSort(Sort.Direction.DESC, MetadataStatus_.changeDate).and(SortUtils.createSort(Sort.Direction.DESC, MetadataStatus_.id));
            pageRequest = new PageRequest(from, size, sortByStatusChangeDate);
        }

        List<MetadataStatus> metadataStatuses;
        if (CollectionUtils.isNotEmpty(id) ||
            CollectionUtils.isNotEmpty(uuid) ||
            CollectionUtils.isNotEmpty(type) ||
            CollectionUtils.isNotEmpty(author) ||
            CollectionUtils.isNotEmpty(owner) ||
            CollectionUtils.isNotEmpty(recordIdentifier) ||
            CollectionUtils.isNotEmpty(statusIds)) {
            metadataStatuses = metadataStatusRepository.searchStatus(
                id, uuid, type, author, owner, recordIdentifier, statusIds,
                dateFrom, dateTo, pageRequest);
        } else {
            metadataStatuses = metadataStatusRepository.findAll(pageRequest).getContent();
        }

        return buildMetadataStatusResponses(metadataStatuses, details, context.getLanguage());
      }
    }

    /**
     * Convert request parameter to a metadata status.
     */
    private MetadataStatus convertParameter(int id, String uuid, MetadataStatusParameter parameter, int author) throws Exception {
        StatusValue statusValue = statusValueRepository.findOne(parameter.getStatus());

        MetadataStatus metadataStatusValue = new MetadataStatus();

        metadataStatusValue.setMetadataId(id);
        metadataStatusValue.setUuid(uuid);
        metadataStatusValue.setChangeDate(new ISODate());
        metadataStatusValue.setUserId(author);
        metadataStatusValue.setStatusValue(statusValue);

        if (parameter.getChangeMessage() != null) {
            metadataStatusValue.setChangeMessage(parameter.getChangeMessage());
        }
        if (StringUtils.isNotEmpty(parameter.getDueDate())) {
            metadataStatusValue.setDueDate(new ISODate(parameter.getDueDate()));
        }
        if (StringUtils.isNotEmpty(parameter.getCloseDate())) {
            metadataStatusValue.setCloseDate(new ISODate(parameter.getCloseDate()));
        }
        if (parameter.getOwner() != null) {
            metadataStatusValue.setOwner(parameter.getOwner());
        }
        return metadataStatusValue;
    }

    @ApiOperation(
            value = "Get saved content from the status record before changes",
            notes = "",
            nickname = "showStatusBefore")
    @RequestMapping(
            value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/before",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            })

    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Previous version of the record."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String showStatusBefore(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Status identifier", required = true) @PathVariable int statusId,
            @ApiParam(value = "User identifier", required = true) @PathVariable int userId,
            @ApiParam(value = "Change date", required = true) @PathVariable String changeDate,
            @ApiIgnore @ApiParam(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        MetadataStatus metadataStatusValue = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        return getValidatedStateText(metadataStatusValue, State.BEFORE, request, httpSession);

    }

    @ApiOperation(
            value = "Get saved content from the status record after changes",
            notes = "",
            nickname = "showStatusAfter")
    @RequestMapping(value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/after",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            })

    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Version of the record after changes."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String showStatusAfter(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Status identifier", required = true) @PathVariable int statusId,
            @ApiParam(value = "User identifier", required = true) @PathVariable int userId,
            @ApiParam(value = "Change date", required = true) @PathVariable String changeDate,
            @ApiIgnore @ApiParam(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        MetadataStatus metadataStatusValue = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        return getValidatedStateText(metadataStatusValue, State.AFTER, request, httpSession);
    }

    @ApiOperation(
            value = "Restore saved content from a status record",
            notes = "",
            nickname = "restoreAtStatusSave")
    @RequestMapping(
            value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/restore",
            method = RequestMethod.POST
            )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Record restored."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void restoreAtStatusSave(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @ApiParam(value = "Status identifier", required = true) @PathVariable int statusId,
            @ApiParam(value = "User identifier", required = true) @PathVariable int userId,
            @ApiParam(value = "Change date", required = true) @PathVariable String changeDate,
            @ApiIgnore @ApiParam(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        ApplicationContext applicationContext = ApplicationContextHolder.get();

        MetadataStatus metadataStatusValue = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        // Try to get previous state text this will also check to ensure that user is allowed to access the data.
        String previousStateText = getValidatedStateText(metadataStatusValue, State.BEFORE, request, httpSession);

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
      try (ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language())) {

        // For cases where the records was not deleted, we will attempt to get the metadata record.
        // If it remains as null then the record did not exists and this is a recovery.
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canEditRecord(metadataStatusValue.getUuid(), request);
        } catch (ResourceNotFoundException e) {
            // resource not found so lets set it to null;
            metadata = null;
        }

        // Begin the recovery
        // If not a recovery from delete then get the before state
        Element beforeMetadata = null;
        String xmlBefore = null;
        if (metadata != null) {
            beforeMetadata = dataManager.getMetadata(context, String.valueOf(metadata.getId()), false, false, false);

            XMLOutputter outp = new XMLOutputter();
            if (beforeMetadata != null) {
                xmlBefore = outp.outputString(beforeMetadata);
            }

            if (xmlBefore.equals(previousStateText)) {
                throw new NotAllowedException("Error recovering metadata id " + metadataUuid + ". Cannot recover record which are identical. Possibly already recovered.");
            }
        }

        // Now begin the recovery
        Integer recoveredMetadataId = null;
        if (metadata != null) {
            Element md = Xml.loadString(previousStateText, false);
            Element mdNoGeonetInfo = metadataUtils.removeMetadataInfo(md);

           metadataManager.updateMetadata(context, String.valueOf(metadata.getId()), mdNoGeonetInfo, false, true, true, context.getLanguage(),
                    null, false);
            recoveredMetadataId = metadata.getId();
        } else {
            // Recover from delete
            Element element = null;
            try {
                element = Xml.loadString(previousStateText, false);
            } catch (JDOMParseException ex) {
                throw new IllegalArgumentException(
                        String.format("XML fragment is invalid. Error is %s", ex.getMessage()));
            }
            recoveredMetadataId = reloadRecord(element, httpSession, request);
        }

          metadataIndexer.indexMetadata(String.valueOf(recoveredMetadataId), true, null);

        UserSession session = ApiUtils.getUserSession(request.getSession());
        if (session != null) {
            // Create a new event
            Element afterMetadata = dataManager.getMetadata(context, String.valueOf(recoveredMetadataId), false, false, false);
            XMLOutputter outp = new XMLOutputter();
            String xmlAfter = outp.outputString(afterMetadata);
            new RecordRestoredEvent(recoveredMetadataId, metadataStatusValue.getUuid(), session.getUserIdAsInt(), xmlBefore, xmlAfter, metadataStatusValue).publish(applicationContext);
        }
      }
    }

    /**
     * Build a list of status with additional information about users (author and
     * owner of the status change).
     */
    private List<MetadataStatusResponse> buildMetadataStatusResponses(List<MetadataStatus> listOfStatus,
                                                                      boolean details, String language) {
        List<MetadataStatusResponse> response = new ArrayList<>();

        // Add all user info in response
        Map<Integer, User> listOfUsers = new HashMap<>();

        // Collect all user info
        for (MetadataStatus s : listOfStatus) {
            if (listOfUsers.get(s.getUserId()) == null) {
                listOfUsers.put(s.getUserId(), userRepository.findOne(s.getUserId()));
            }
            if (s.getOwner() != null && listOfUsers.get(s.getOwner()) == null) {
                listOfUsers.put(s.getOwner(), userRepository.findOne(s.getOwner()));
            }
        }

        Map<Integer, String> titles = new HashMap<>();

        // Add all user info and record title to response
        for (MetadataStatus s : listOfStatus) {
            MetadataStatusResponse status = new MetadataStatusResponse(s, details);

            User author = listOfUsers.get(status.getUserId());
            if (author != null) {
                status.setAuthorName(author.getName() + " " + author.getSurname());
                status.setAuthorEmail(author.getEmail());
            }
            if (s.getOwner() != null) {
                User owner = listOfUsers.get(status.getOwner());
                if (owner != null) {
                    status.setOwnerName(owner.getName() + " " + owner.getSurname());
                    status.setOwnerEmail(owner.getEmail());
                }
            }

            status.setDateChange(s.getChangeDate().getDateAndTime());

            if (s.getStatusValue().getType().equals(StatusValueType.event)) {
                status.setCurrentStatus(extractCurrentStatus(s));
                status.setPreviousStatus(extractPreviousStatus(s));
            } else if (s.getStatusValue().getType().equals(StatusValueType.task)) {
                if (s.getDueDate() != null) {
                    status.setDateDue(s.getDueDate().getDateAndTime());
                }
                if (s.getCloseDate() != null) {
                    status.setDateClose(s.getCloseDate().getDateAndTime());
                }
            }


            if (s.getTitles() != null && s.getTitles().size() > 0) {
                // Locate language title based on language which is a 3 char code
                // First look for exact match. otherwise look for 2 char code and if still not found then default to first occurrence
                status.setTitle(
                        s.getTitles().getOrDefault(language,
                                s.getTitles().getOrDefault(language.substring(0, 2),
                                        s.getTitles().entrySet().iterator().next().getValue())));
            }
            // If title was not stored in database then try to get it from the index.
            // Titles may be missing in database if it is older data or if the extract-titles.xsl does not exists/fails for schema plugin
            if (status.getTitle() == null || status.getTitle().length() == 0) {
                String title = titles.get(s.getMetadataId());
                if (title == null) {
                    try {
                        // Collect metadata titles. For now we use Lucene
                        title = LuceneSearcher.getMetadataFromIndexById(language, s.getMetadataId() + "", "title");
                        titles.put(s.getMetadataId(), title);
                    } catch (Exception e1) {
                    }
                }
                status.setTitle(title);
            }

            status.setUuid(s.getUuid());

            response.add(status);
        }

        return response;
    }

    private String extractCurrentStatus(MetadataStatus s) {
        switch (Integer.toString(s.getStatusValue().getId())) {
            case StatusValue.Events.ATTACHMENTADDED:
                return s.getCurrentState();
            case StatusValue.Events.RECORDOWNERCHANGE:
                return ObjectJSONUtils.extractFieldFromJSONString(s.getCurrentState(), "owner", "name");
            case StatusValue.Events.RECORDGROUPOWNERCHANGE:
                return ObjectJSONUtils.extractFieldFromJSONString(s.getCurrentState(), "owner", "name");
            case StatusValue.Events.RECORDPROCESSINGCHANGE:
                return ObjectJSONUtils.extractFieldFromJSONString(s.getCurrentState(), "process");
            case StatusValue.Events.RECORDCATEGORYCHANGE:
                List<String> categories = ObjectJSONUtils.extractListOfFieldFromJSONString(s.getCurrentState(), "category",
                        "name");
                StringBuffer categoriesAsString = new StringBuffer("[ ");
                for (String categoryName : categories) {
                    categoriesAsString.append(categoryName + " ");
                }
                categoriesAsString.append("]");
                return categoriesAsString.toString();
            case StatusValue.Events.RECORDVALIDATIONTRIGGERED:
                return s.getCurrentState().equals("1") ? "OK" : "KO";
            default:
                return "";
        }
    }

    private String extractPreviousStatus(MetadataStatus s) {
        switch (Integer.toString(s.getStatusValue().getId())) {
            case StatusValue.Events.ATTACHMENTDELETED:
                return s.getPreviousState();
            case StatusValue.Events.RECORDOWNERCHANGE:
                return ObjectJSONUtils.extractFieldFromJSONString(s.getPreviousState(), "owner", "name");
            case StatusValue.Events.RECORDGROUPOWNERCHANGE:
                return ObjectJSONUtils.extractFieldFromJSONString(s.getPreviousState(), "owner", "name");
            default:
                return "";
        }
    }

    private void checkCanViewStatus(String metadata, HttpSession httpSession) throws Exception {
        Element xmlElement = null;
        try {
            xmlElement = Xml.loadString(metadata, false);
        } catch (JDOMParseException ex) {
            throw new IllegalArgumentException(
                    String.format("XML fragment is invalid. Error is %s", ex.getMessage()));
        }

        Element info = xmlElement.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        if (info == null) {
            throw new IllegalArgumentException("Can't locate required geonet:info which is required for the recovery. May need to manually re-import the data");
        }

        String groupOwnerName = info.getChildText(Edit.Info.Elem.GROUPOWNERNAME);

        String groupId = null;
        if (groupOwnerName != null) {
            Group groupEntity = groupRepository.findByName(groupOwnerName);
            if (groupEntity != null) {
                groupId = String.valueOf(groupEntity.getId());
            }
        }

        UserSession userSession = ApiUtils.getUserSession(httpSession);
        if (userSession.getProfile() != Profile.Administrator) {
            if (groupId != null) {
                final List<Integer> editingGroupList = AccessManager.getGroups(userSession, Profile.Editor);
                if (!editingGroupList.contains(Integer.valueOf(groupId))) {
                    throw new SecurityException(
                        String.format("You can't view history from this group (%s). User MUST be an Editor in that group", groupOwnerName));
                }
            } else {
                throw new SecurityException(
                        String.format("Error identify group where this metadata belong to. Only administrator can view this record"));
            }
        }
    }

    private int reloadRecord(Element md, HttpSession httpSession, HttpServletRequest request) throws Exception {

        Element info = md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        if (info == null) {
            throw new IllegalArgumentException("Can't location geonet:info which is required for the recovery. May need to manually re-import the data");
        }

        md = metadataUtils.removeMetadataInfo(md);

        String groupOwnerName = info.getChildText(Edit.Info.Elem.GROUPOWNERNAME);

        String groupId = null;
        if (groupOwnerName != null) {
            Group groupEntity = groupRepository.findByName(groupOwnerName);
            if (groupEntity != null) {
                groupId = String.valueOf(groupEntity.getId());
            }
        }

        UserSession userSession = ApiUtils.getUserSession(httpSession);
        if (userSession.getProfile() != Profile.Administrator) {
            if (groupId != null) {
                final List<Integer> editingGroupList = AccessManager.getGroups(userSession, Profile.Editor);
                if (!editingGroupList.contains(Integer.valueOf(groupId))) {
                    throw new SecurityException(
                        String.format("You can't create a record in this group (%s). User MUST be an Editor in that group", groupOwnerName));
                }
            }
        }

      try (ServiceContext context = ApiUtils.createServiceContext(request)) {

        String schema = info.getChildText(Edit.Info.Elem.SCHEMA);
        if (schema == null) {
            try {
                schema = dataManager.autodetectSchema(md);
            } catch (Exception e) {
                throw new IllegalArgumentException("Can't detect schema for metadata automatically. "
                    + "You could try to force the schema with the schema parameter.");
            }
        }

        String uuid = info.getChildText(Edit.Info.Elem.UUID);
        if (uuid == null) {
            // --- if the uuid does not exist we generate it for metadata and templates
            uuid = metadataUtils.extractUUID(schema, md);
            if (uuid.length() == 0) {
                throw new IllegalArgumentException("Could not locate the UUID for the document being restored.");
            }
        }

        if (metadataRepository.findOneByUuid(uuid) != null) {
            throw new IllegalArgumentException(
                    String.format("A record with UUID '%s' already exist", uuid));
        }

        String date = new ISODate().toString();

        // insert record
        boolean ufo = false, indexImmediate = false;
        String metadataId = metadataManager.insertMetadata(context, schema, md, uuid,
                context.getUserSession().getUserIdAsInt(), groupId, settingManager.getSiteId(), MetadataType.METADATA.codeString
                , null, null, date, date, ufo, indexImmediate);

        int id = Integer.parseInt(metadataId);

        List<Element> categoryList = info.getChildren(Edit.Info.Elem.CATEGORY);
        if (categoryList != null && !categoryList.isEmpty()) {
            for (Element cat : categoryList) {
                String catName = cat.getText();
                final MetadataCategory metadataCategory = categoryRepository.findOneByName(catName);
                if (metadataCategory != null) {
                    dataManager.setCategory(context, metadataId, String.valueOf(metadataCategory.getId()));
                }
            }
        }

        return id;
      }
    }

    private MetadataStatus getMetadataStatus(String uuidOrInternalId, int statusId, int userId, String changeDate) throws ResourceNotFoundException {
        MetadataStatus metadataStatusValue;
        if (uuidOrInternalId.matches("\\d+")) {
            metadataStatusValue = metadataStatusRepository.findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(Integer.valueOf(uuidOrInternalId), statusId, userId, new ISODate(changeDate));
        } else {
            metadataStatusValue = metadataStatusRepository.findOneByUuidAndStatusValue_IdAndUserIdAndChangeDate(uuidOrInternalId, statusId, userId, new ISODate(changeDate));
        }

        if (metadataStatusValue == null) {
            throw new ResourceNotFoundException(
                    String.format("Can't find metadata status for record '%s', user '%d', status, '%d' at date '%s'", uuidOrInternalId,
                            userId, statusId, changeDate));
        }

        return metadataStatusValue;
    }

    private String getValidatedStateText(MetadataStatus metadataStatus, State state, HttpServletRequest request, HttpSession httpSession) throws Exception {

        if (!StatusValueType.event.equals(metadataStatus.getStatusValue().getType())
            || !ArrayUtils.contains(supportedRestoreStatuses, metadataStatus.getStatusValue().getId())) {
            throw new NotAllowedException("Unsupported action on status type '" + metadataStatus.getStatusValue().getType()
                + "' for metadata '" + metadataStatus.getUuid() + "'. Supports status type '"
                + StatusValueType.event + "' with the status id '" + Arrays.toString(supportedRestoreStatuses) + "'.");
        }

        String stateText;
        if (state.equals(State.AFTER)) {
            stateText = metadataStatus.getCurrentState();
        } else {
            stateText = metadataStatus.getPreviousState();
        }

        if (stateText == null) {
            throw new ResourceNotFoundException(
                String.format("No data exists for previous state on metadata record '%s', user '%d' at date '%s'",
                    metadataStatus.getUuid(), metadataStatus.getUserId(), metadataStatus.getChangeDate()));
        }

        // If record exists then check if user has access.
        try {
            ApiUtils.canEditRecord(metadataStatus.getUuid(), request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        } catch (ResourceNotFoundException e) {
            // If metadata record does not exists then it was deleted so
            // we will only allow the administrator, owner to view the contents
            checkCanViewStatus(stateText, httpSession);
        }

        return stateText;
    }

    /**
     * Checks if the metadata workflow is enabled.
     *
     * @throws FeatureNotEnabledException
     */
    private void checkWorkflowEnabled() throws FeatureNotEnabledException {
        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);

        if (!isMdWorkflowEnable) {
            throw new FeatureNotEnabledException(
                "Metadata workflow is disabled, can not be set the status of metadata")
                .withMessageKey("exception.resourceNotEnabled.workflow")
                .withDescriptionKey("exception.resourceNotEnabled.workflow.description");
        }
    }

    /**
     * Checks if the metadata status can be changed.
     *
     * If the setting to allow only to submit / approve valid metadata only is enabled,
     * the metadata should be valid, to allow the status change.
     *
     * @param context
     * @param metadata
     * @param report
     * @throws Exception
     */
    private boolean isAllowedMetadataStatusChange(ServiceContext context, AbstractMetadata metadata,
                                                  MetadataProcessingReport report) throws Exception {
        boolean isAllowedSubmitApproveInvalidMd = settingManager
            .getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD);
        if (!isAllowedSubmitApproveInvalidMd) {
            boolean isInvalid = MetadataUtils.retrieveMetadataValidationStatus(metadata, context);

            if (isInvalid) {
                report.addMetadataInfos(metadata.getId(), metadata.getUuid(), true, false, "Metadata is invalid: can't be approved");
                return false;
            }
        }

        return true;
    }

    /**
     * Change the status of a metadata.
     *
     * @param context
     * @param metadata
     * @param previousStatus
     * @param newStatus
     * @param changeMessage
     * @throws Exception
     */
    private void changeMetadataStatus(ServiceContext context, AbstractMetadata metadata,
                                      String previousStatus, String newStatus, String changeMessage)
        throws Exception {
        // --- use StatusActionsFactory and StatusActions class to
        // --- change status and carry out behaviours for status changes
        StatusActions sa = statusActionFactory.createStatusActions(context);

        MetadataStatusParameter status = new MetadataStatusParameter();
        status.setStatus(Integer.parseInt(newStatus));
        status.setChangeMessage(changeMessage);

        int author = context.getUserSession().getUserIdAsInt();
        MetadataStatus metadataStatusValue = convertParameter(metadata.getId(), metadata.getUuid(), status, author);
        metadataStatusValue.setPreviousState(previousStatus);
        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
        listOfStatusChange.add(metadataStatusValue);
        sa.onStatusChange(listOfStatusChange, true);
    }
}
