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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.FeatureNotEnabledException;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.model.MetadataStatusParameter;
import org.fao.geonet.api.records.model.MetadataStatusResponse;
import org.fao.geonet.api.records.model.MetadataWorkflowStatusResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
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
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {"/{portal}/api/records"})
@Tag(name = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
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
    private EsSearchManager searchManager;

    @Autowired
    private MetadataRepository metadataRepository;

    // The restore function currently supports these states
    static final Integer[] supportedRestoreStatuses = {
        Integer.parseInt(StatusValue.Events.RECORDUPDATED),
        Integer.parseInt(StatusValue.Events.RECORDPROCESSINGCHANGE),
        Integer.parseInt(StatusValue.Events.RECORDDELETED),
        Integer.parseInt(StatusValue.Events.RECORDRESTORED)};

    private enum State {
        BEFORE, AFTER
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get record status history", description = "")
    @RequestMapping(value = "/{metadataUuid}/status", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusResponse> getRecordStatusHistory(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @RequestParam(required = false) boolean details,
        @Parameter(description = "Sort direction", required = false) @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);

        String sortField = SortUtils.createPath(MetadataStatus_.changeDate);

        List<MetadataStatus> listOfStatus = metadataStatusRepository.findAllByMetadataId(metadata.getId(),
            Sort.by(sortOrder, sortField));

        List<MetadataStatusResponse> response = buildMetadataStatusResponses(listOfStatus, details,
            context.getLanguage());

        // TODO: Add paging
        return response;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get record status history by type", description = "")
    @RequestMapping(value = "/{metadataUuid}/status/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusResponse> getRecordStatusHistoryByType(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Type", required = true) @PathVariable StatusValueType type,
        @RequestParam(required = false) boolean details,
        @Parameter(description = "Sort direction", required = false) @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);

        String sortField = SortUtils.createPath(MetadataStatus_.changeDate);

        List<MetadataStatus> listOfStatus = metadataStatusRepository.findAllByMetadataIdAndByType(metadata.getId(), type,
            Sort.by(sortOrder, sortField));

        List<MetadataStatusResponse> response = buildMetadataStatusResponses(listOfStatus, details,
            context.getLanguage());

        // TODO: Add paging
        return response;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get last workflow status for a record", description = "")
    @RequestMapping(value = "/{metadataUuid}/status/workflow/last", method = RequestMethod.GET, produces = {
        MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Record status."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataWorkflowStatusResponse getStatus(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language());

        // --- only allow the owner of the record to set its status
        if (!accessManager.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "Only the owner of the metadata can get the status. User is not the owner of the metadata"));
        }

        MetadataStatus recordStatus = metadataStatus.getStatus(metadata.getId());

        List<StatusValue> elStatus = statusValueRepository.findAllByType(StatusValueType.workflow);

        // --- get the list of content reviewers for this metadata record
        Set<Integer> ids = new HashSet<Integer>();
        ids.add(Integer.valueOf(metadata.getId()));
        List<Pair<Integer, User>> reviewers = userRepository.findAllByGroupOwnerNameAndProfile(ids, Profile.Reviewer,
            SortUtils.createSort(User_.name));
        List<User> listOfReviewers = new ArrayList<>();
        for (Pair<Integer, User> reviewer : reviewers) {
            listOfReviewers.add(reviewer.two());
        }
        return new MetadataWorkflowStatusResponse(recordStatus, listOfReviewers,
            accessManager.hasEditPermission(context, metadata.getId() + ""), elStatus);

    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Set the record status", description = "")
    @RequestMapping(value = "/{metadataUuid}/status", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Status updated."),
        @ApiResponse(responseCode = "400", description = "Metadata workflow not enabled."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setStatus(@Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
                          @Parameter(description = "Metadata status", required = true) @RequestBody(required = true) MetadataStatusParameter status,
                          HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request,
            languageUtils.getIso3langCode(request.getLocales()));

        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);

        if (!isMdWorkflowEnable) {
            throw new FeatureNotEnabledException(
                "Metadata workflow is disabled, can not be set the status of metadata");
        }

        // --- only allow the owner of the record to set its status
        if (!accessManager.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "Only the owner of the metadata can set the status of this record. User is not the owner of the metadata."));
        }

        boolean isAllowedSubmitApproveInvalidMd = settingManager
            .getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD);
        if (((status.getStatus() == Integer.parseInt(StatusValue.Status.SUBMITTED))
            || (status.getStatus() == Integer.parseInt(StatusValue.Status.APPROVED)))
            && !isAllowedSubmitApproveInvalidMd) {

            boolean isInvalid = MetadataUtils.retrieveMetadataValidationStatus(metadata, context);

            if (isInvalid) {
                throw new Exception("Metadata is invalid: can't be submitted or approved");
            }
        }

        // --- use StatusActionsFactory and StatusActions class to
        // --- change status and carry out behaviours for status changes
        StatusActions sa = statusActionFactory.createStatusActions(context);

        int author = context.getUserSession().getUserIdAsInt();
        MetadataStatus metadataStatus = convertParameter(metadata.getId(), metadata.getUuid(), status, author);
        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
        listOfStatusChange.add(metadataStatus);
        sa.onStatusChange(listOfStatusChange);

        //--- reindex metadata
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), true);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Close a record task",
        description = "")
    @RequestMapping(
        value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/close",
        method = RequestMethod.PUT
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Task closed."),
        @ApiResponse(responseCode = "404", description = "Status not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeTask(@Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
                          @Parameter(description = "Status identifier", required = true) @PathVariable int statusId,
                          @Parameter(description = "User identifier", required = true) @PathVariable int userId,
                          @Parameter(description = "Change date", required = true) @PathVariable String changeDate,
                          @Parameter(description = "Close date", required = true) @RequestParam String closeDate, HttpServletRequest request)
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        MetadataStatus metadataStatus = metadataStatusRepository
                .findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(metadata.getId(), statusId, userId, new ISODate(changeDate));

        if (metadataStatus != null) {
            metadataStatusRepository.update(metadataStatus.getId(),
                entity -> entity.setCloseDate(new ISODate(closeDate)));
        } else {
            throw new ResourceNotFoundException(
                String.format("Can't find metadata status for record '%d', user '%s' at date '%s'", metadataUuid,
                    userId, changeDate));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a record status", description = "")
    @RequestMapping(value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Status removed."),
        @ApiResponse(responseCode = "404", description = "Status not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecordStatus(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Status identifier", required = true) @PathVariable int statusId,
        @Parameter(description = "User identifier", required = true) @PathVariable int userId,
        @Parameter(description = "Change date", required = true) @PathVariable String changeDate,
        HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        MetadataStatus metadataStatus = metadataStatusRepository
            .findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(metadata.getId(), statusId, userId, new ISODate(changeDate));
        if (metadataStatus != null) {
            metadataStatusRepository.delete(metadataStatus);
            // TODO: Reindex record ?
        } else {
            throw new ResourceNotFoundException(
                String.format("Can't find metadata status for record '%d', user '%s' at date '%s'", metadataUuid,
                    userId, changeDate));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete all record status", description = "")
    @RequestMapping(value = "/{metadataUuid}/status", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Status removed."),
        @ApiResponse(responseCode = "404", description = "Status not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllRecordStatus(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        metadataStatusRepository.deleteAllById_MetadataId(metadata.getId());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Search status", description = "")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, path = "/status/search")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<MetadataStatusResponse> getWorkflowStatusByType(
        @Parameter(description = "One or more types to retrieve (ie. worflow, event, task). Default is all.",
            required = false)
        @RequestParam(required = false)
            StatusValueType[] type,
        @Parameter(description = "All event details including XML changes. Responses are bigger. Default is false",
            required = false)
        @RequestParam(required = false)
            boolean details,
        @Parameter(description = "One or more event author. Default is all.",
            required = false)
        @RequestParam(required = false)
            Integer[] author,
        @Parameter(description = "One or more event owners. Default is all.",
            required = false)
        @RequestParam(required = false)
            Integer[] owner,
        @Parameter(description = "One or more record identifier. Default is all.",
            required = false)
        @RequestParam(required = false)
            Integer[] id,
        @Parameter(description = "One or more metadata record identifier. Default is all.",
            required = false)
        @RequestParam(required = false)
            Integer[] record,
        @Parameter(description = "One or more metadata uuid. Default is all.",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @Parameter(description = "Start date",
            required = false)
        @RequestParam(required = false)
            String dateFrom,
        @Parameter(description = "End date",
            required = false)
        @RequestParam(required = false)
            String dateTo,
        @Parameter(description = "From page",
            required = false)
        @RequestParam(required = false, defaultValue = "0")
            Integer from,
        @Parameter(description = "Number of records to return",
            required = false)
        @RequestParam(required = false, defaultValue = "100")
            Integer size,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        Sort sortByStatusChangeDate = SortUtils.createSort(Sort.Direction.DESC, MetadataStatus_.changeDate).and(SortUtils.createSort(Sort.Direction.DESC, MetadataStatus_.id));

        final PageRequest pageRequest = PageRequest.of(from, size, sortByStatusChangeDate);

        List<MetadataStatus> metadataStatuses;
        if ((id != null && id.length > 0) ||
                (uuid != null && uuid.length > 0) ||
                (type != null && type.length > 0) ||
                (author != null && author.length > 0) ||
                (owner != null && owner.length > 0) ||
                (record != null && record.length > 0)) {
            metadataStatuses = metadataStatusRepository.searchStatus(
                    id != null && id.length > 0 ? Arrays.asList(id) : null,
                    uuid != null && uuid.length > 0 ? Arrays.asList(uuid) : null,
                    type != null && type.length > 0 ? Arrays.asList(type) : null,
                    author != null && author.length > 0 ? Arrays.asList(author) : null,
                    owner != null && owner.length > 0 ? Arrays.asList(owner) : null,
                    record != null && record.length > 0 ? Arrays.asList(record) : null, dateFrom, dateTo, pageRequest);
        } else {
            metadataStatuses = metadataStatusRepository.findAll(pageRequest).getContent();
        }

        return buildMetadataStatusResponses(metadataStatuses, details, context.getLanguage());
    }

    /**
     * Convert request parameter to a metadata status.
     */
    private MetadataStatus convertParameter(int id, String uuid, MetadataStatusParameter parameter, int author) throws Exception {
        StatusValue statusValue = statusValueRepository.findById(parameter.getStatus()).get();

        MetadataStatus metadataStatus = new MetadataStatus();

        metadataStatus.setMetadataId(id);
        metadataStatus.setUuid(uuid);
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(author);
        metadataStatus.setStatusValue(statusValue);

        if (parameter.getChangeMessage() != null) {
            metadataStatus.setChangeMessage(parameter.getChangeMessage());
        }
        if (StringUtils.isNotEmpty(parameter.getDueDate())) {
            metadataStatus.setDueDate(new ISODate(parameter.getDueDate()));
        }
        if (StringUtils.isNotEmpty(parameter.getCloseDate())) {
            metadataStatus.setCloseDate(new ISODate(parameter.getCloseDate()));
        }
        if (parameter.getOwner() != null) {
            metadataStatus.setOwner(parameter.getOwner());
        }
        return metadataStatus;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get saved content from the status record before changes",
        description = "")
    @RequestMapping(
            value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/before",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            })
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Previous version of the record."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String showStatusBefore(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Status identifier", required = true) @PathVariable int statusId,
        @Parameter(description = "User identifier", required = true) @PathVariable int userId,
        @Parameter(description = "Change date", required = true) @PathVariable String changeDate,
        @Parameter(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        MetadataStatus metadataStatus = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        return getValidatedStateText(metadataStatus, State.BEFORE, request, httpSession);

    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get saved content from the status record after changes"
    )
    @RequestMapping(value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/after",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            })

    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Version of the record after changes."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String showStatusAfter(
            @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @Parameter(description = "Status identifier", required = true) @PathVariable int statusId,
            @Parameter(description = "User identifier", required = true) @PathVariable int userId,
            @Parameter(description = "Change date", required = true) @PathVariable String changeDate,
            @Parameter(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        MetadataStatus metadataStatus = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        return getValidatedStateText(metadataStatus, State.AFTER, request, httpSession);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Restore saved content from a status record")
    @RequestMapping(
            value = "/{metadataUuid}/status/{statusId:[0-9]+}.{userId:[0-9]+}.{changeDate}/restore",
            method = RequestMethod.POST
            )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Record restored."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void restoreAtStatusSave(
            @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            @Parameter(description = "Status identifier", required = true) @PathVariable int statusId,
            @Parameter(description = "User identifier", required = true) @PathVariable int userId,
            @Parameter(description = "Change date", required = true) @PathVariable String changeDate,
            @Parameter(hidden = true) HttpSession httpSession, HttpServletRequest request
    )
            throws Exception {

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataMan = applicationContext.getBean(DataManager.class);

        MetadataStatus metadataStatus = getMetadataStatus(metadataUuid, statusId, userId, changeDate);

        // Try to get previous state text this will also check to ensure that user is allowed to access the data.
        String previousStateText = getValidatedStateText(metadataStatus, State.BEFORE, request, httpSession);

        // For cases where the records was not deleted, we will attempt to get the metadata record.
        // If it remains as null then the record did not exists and this is a recovery.
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canEditRecord(metadataStatus.getUuid(), request);
        } catch (ResourceNotFoundException e) {
            // resource not found so lets set it to null;
            metadata = null;
        }

        // Begin the recovery
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language());

        // If not a recovery from delete then get the before state
        Element beforeMetadata = null;
        String xmlBefore = null;
        if (metadata != null) {
            beforeMetadata = dataMan.getMetadata(context, String.valueOf(metadata.getId()), false, false, false);

            XMLOutputter outp = new XMLOutputter();
            if (beforeMetadata != null) {
                xmlBefore = outp.outputString(beforeMetadata);
            }

            if (xmlBefore.equals(previousStateText)) {
                throw new NotAllowedException("Error recovering metadata id " + metadataUuid + ". Cannot recover record which are identical. Possibly already recovered.");
            }
        }

        // Now begin the recovery
        IMetadataManager iMetadataManager = context.getBean(IMetadataManager.class);
        Integer recoveredMetadataId = null;
        if (metadata != null) {
            Element md = Xml.loadString(previousStateText, false);
            Element mdNoGeonetInfo = metadataUtils.removeMetadataInfo(md);

            iMetadataManager.updateMetadata(context, String.valueOf(metadata.getId()), mdNoGeonetInfo, false, true, true, context.getLanguage(),
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
            recoveredMetadataId = reloadRecord(metadataStatus, element, iMetadataManager, httpSession, request);
        }

        dataManager.indexMetadata(String.valueOf(recoveredMetadataId), true);

        UserSession session = ApiUtils.getUserSession(request.getSession());
        if (session != null) {
            // Create a new event
            Element afterMetadata = dataMan.getMetadata(context, String.valueOf(recoveredMetadataId), false, false, false);
            XMLOutputter outp = new XMLOutputter();
            String xmlAfter = outp.outputString(afterMetadata);
            new RecordRestoredEvent(recoveredMetadataId, metadataStatus.getUuid(), session.getUserIdAsInt(), xmlBefore, xmlAfter, metadataStatus).publish(applicationContext);
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
                listOfUsers.put(s.getUserId(), userRepository.findById(s.getUserId()).get());
            }
            if (s.getOwner() != null && listOfUsers.get(s.getOwner()) == null) {
                Optional<User> user = userRepository.findById(s.getOwner());
                if (user.isPresent()) {
                    listOfUsers.put(s.getOwner(), user.get());
                }
            }
        }

        Map<Integer, String> titles = new HashMap<>();
        Map<Integer, String> uuids = new HashMap<>();

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

            if (s.getStatusValue().getType().equals(StatusValueType.event)) {
                status.setCurrentStatus(extractCurrentStatus(s));
                status.setPreviousStatus(extractPreviousStatus(s));
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
                        Set<String> fields = new HashSet<>();
                        String titleField = "resourceTitleObject";
                        fields.add(titleField);
                        Optional<Metadata> metadata = metadataRepository.findById(s.getMetadataId());
                        final Map<String, String> values =
                            searchManager.getFieldsValues(metadata.get().getUuid(), fields);
                        title = values.get(titleField);
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

    private void checkCanViewStatus(String metadata, MetadataStatus metadataStatus, HttpSession httpSession, HttpServletRequest request) throws Exception {
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
                            String.format("You can't view history from this group (" + groupOwnerName + "). User MUST be an Editor in that group"));
                }
            } else {
                throw new SecurityException(
                        String.format("Error identify group where this metadata belong to. Only administrator can view this record"));
            }
        }
    }

    private int reloadRecord(MetadataStatus metadataStatus, Element md, IMetadataManager iMetadataManager, HttpSession httpSession, HttpServletRequest request) throws Exception {

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
                            String.format("You can't create a record in this group (" + groupOwnerName + "). User MUST be an Editor in that group"));
                }
            }
        }

        ServiceContext context = ApiUtils.createServiceContext(request);

        String schema = info.getChildText(Edit.Info.Elem.SCHEMA);
        if (schema == null) {
            schema = dataManager.autodetectSchema(md);
            throw new IllegalArgumentException("Can't detect schema for metadata automatically. "
                    + "You could try to force the schema with the schema parameter.");
        }

        String uuid = info.getChildText(Edit.Info.Elem.UUID);
        if (uuid == null) {
            // --- if the uuid does not exist we generate it for metadata and templates
            uuid = dataManager.extractUUID(schema, md);
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
        String metadataId = iMetadataManager.insertMetadata(context, schema, md, uuid,
                context.getUserSession().getUserIdAsInt(), groupId, settingManager.getSiteId(), MetadataType.METADATA.codeString
                , null, null, date, date, ufo, indexImmediate);

        int id = Integer.parseInt(metadataId);

        List<Element> categoryList = info.getChildren(Edit.Info.Elem.CATEGORY);
        if (categoryList != null && categoryList.size() > 0) {
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
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

    private MetadataStatus getMetadataStatus(String uuidOrInternalId, int statusId, int userId, String changeDate) throws ResourceNotFoundException {
        MetadataStatus metadataStatus;
        if (uuidOrInternalId.matches("\\d+")) {
            metadataStatus = metadataStatusRepository.findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(Integer.valueOf(uuidOrInternalId), statusId, userId, new ISODate(changeDate));
        } else {
            metadataStatus = metadataStatusRepository.findOneByUuidAndStatusValue_IdAndUserIdAndChangeDate(uuidOrInternalId, statusId, userId, new ISODate(changeDate));
        }

        if (metadataStatus == null) {
            throw new ResourceNotFoundException(
                    String.format("Can't find metadata status for record '%s', user '%d', status, '%d' at date '%s'", uuidOrInternalId,
                            userId, statusId, changeDate));
        }

        return metadataStatus;
    }

    private String getValidatedStateText(MetadataStatus metadataStatus, State state, HttpServletRequest request, HttpSession httpSession) throws Exception {

        if (!StatusValueType.event.equals(metadataStatus.getStatusValue().getType()) || !ArrayUtils.contains(supportedRestoreStatuses, metadataStatus.getStatusValue().getId())) {
            throw new NotAllowedException("Unsupported action on status type '" + metadataStatus.getStatusValue().getType() + "' for metadata '" + metadataStatus.getUuid() + "'. Supports status type '" +
                    StatusValueType.event + "' with the status id '" + Arrays.toString(supportedRestoreStatuses) + "'.");
        }

        String StateText;
        if (state.equals(State.AFTER)) {
            StateText = metadataStatus.getCurrentState();
        } else {
            StateText = metadataStatus.getPreviousState();
        }

        if (StateText == null) {
            throw new ResourceNotFoundException(
                    String.format("No data exists for previous state on metadata record '%d', user '%s' at date '%s'", metadataStatus.getUuid(),
                            metadataStatus.getUserId(), metadataStatus.getChangeDate()));
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
            checkCanViewStatus(StateText, metadataStatus, httpSession, request);
        }

        return StateText;
    }
}
