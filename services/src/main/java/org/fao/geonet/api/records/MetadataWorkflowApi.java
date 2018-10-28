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
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.MetadataStatusParameter;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordWorkflow")
@ReadWriteController
public class MetadataWorkflowApi {

    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(
        value = "Get record status",
        notes = "",
        nickname = "getStatus")
    @RequestMapping(value = "/{metadataUuid}/status/workflow/last",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Record status."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataStatusResponse getStatus(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language());

        AccessManager am = appContext.getBean(AccessManager.class);
        //--- only allow the owner of the record to set its status
        if (!am.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "Only the owner of the metadata can get the status. User is not the owner of the metadata"
            ));
        }

        IMetadataStatus metadataStatus = context.getBean(IMetadataStatus.class);
        MetadataStatus recordStatus = metadataStatus.getStatus(metadata.getId());

//        List<StatusValue> elStatus = context.getBean(StatusValueRepository.class).findAll();
        List<StatusValue> elStatus = context.getBean(StatusValueRepository.class).findAllByType(StatusValueType.workflow);

        //--- get the list of content reviewers for this metadata record
        Set<Integer> ids = new HashSet<Integer>();
        ids.add(Integer.valueOf(metadata.getId()));
        List<Pair<Integer, User>> reviewers = context.getBean(UserRepository.class).findAllByGroupOwnerNameAndProfile(ids,
            Profile.Reviewer, SortUtils.createSort(User_.name));
        List<User> listOfReviewers = new ArrayList<>();
        for (Pair<Integer, User> reviewer : reviewers) {
            listOfReviewers.add(reviewer.two());
        }
        return new MetadataStatusResponse(
            recordStatus,
            listOfReviewers,
            am.hasEditPermission(context, metadata.getId() + ""),
            elStatus);

    }


    @ApiOperation(
        value = "Set record status",
        notes = "",
        nickname = "setStatus")
    @RequestMapping(value = "/{metadataUuid}/status",
        method = RequestMethod.PUT
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Status updated."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setStatus(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Metadata status",
            required = true
        )
        @RequestBody(
            required = true
        )
            MetadataStatusParameter status,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ServiceContext context = ApiUtils.createServiceContext(request, locale.getISO3Language());


        AccessManager am = appContext.getBean(AccessManager.class);
        //--- only allow the owner of the record to set its status
        if (!am.isOwner(context, String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "Only the owner of the metadata can set the status. User is not the owner of the metadata"
            ));
        }


        //--- use StatusActionsFactory and StatusActions class to
        //--- change status and carry out behaviours for status changes
        StatusActionsFactory saf = appContext.getBean(StatusActionsFactory.class);

        StatusActions sa = saf.createStatusActions(context);

        int author = context.getUserSession().getUserIdAsInt();
        MetadataStatus metadataStatus = convertParameter(metadata.getId(), status, author);
        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
        listOfStatusChange.add(metadataStatus);
        sa.statusChange(listOfStatusChange);

        //--- reindex metadata
        DataManager dataManager = appContext.getBean(DataManager.class);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true, null);
    }


    public MetadataStatus convertParameter(int id, MetadataStatusParameter parameter, int author) throws Exception {
        StatusValueRepository statusValueRepository = ApplicationContextHolder.get().getBean(StatusValueRepository.class);
        StatusValue statusValue = statusValueRepository.findOne(parameter.getStatus());

        MetadataStatus metadataStatus = new MetadataStatus();

        MetadataStatusId mdStatusId = new MetadataStatusId()
            .setStatusId(parameter.getStatus())
            .setMetadataId(id)
            .setChangeDate(new ISODate())
            .setUserId(author);

        metadataStatus.setId(mdStatusId);
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
}
