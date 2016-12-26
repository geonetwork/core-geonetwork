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
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

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
@Controller("tagRecords")
@ReadWriteController
public class MetadataTagApi {

    public static final String API_PARAM_TAG_IDENTIFIER = "Tag identifier";

    @ApiOperation(
        value = "Get record tags",
        notes = "Tags are used to classify information.<br/>" +
            "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/tag-information/tagging-with-categories.html'>More info</a>",
        nickname = "getRecordTags")
    @RequestMapping(
        value = "/{metadataUuid}/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Record tags."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Set<MetadataCategory> getTags(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        Metadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        return metadata.getCategories();
    }


    @ApiOperation(
        value = "Add tags to a record",
        notes = "",
        nickname = "addTagsToRecord")
    @RequestMapping(
        value = "/{metadataUuid}/tags",
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Record tags added."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public void updateTags(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = API_PARAM_TAG_IDENTIFIER,
            required = true
        )
        @RequestParam
            Integer[] id,
        @ApiParam(
            value = ApiParams.API_PARAM_CLEAR_ALL_BEFORE_INSERT,
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean clear,
        HttpServletRequest request
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();

        if (clear) {
            appContext.getBean(MetadataRepository.class).update(
                metadata.getId(), entity -> entity.getCategories().clear());
        }

        DataManager dataManager = appContext.getBean(DataManager.class);
        MetadataCategoryRepository categoryRepository = appContext.getBean(MetadataCategoryRepository.class);
        for (int c : id) {
            final MetadataCategory category = categoryRepository.findOne(c);
            if (category != null) {
                dataManager.setCategory(
                    ApiUtils.createServiceContext(request),
                    String.valueOf(metadata.getId()), String.valueOf(c));
            } else {
                throw new ResourceNotFoundException(String.format(
                    "Can't assign non existing category with id '%d' to record '%s'",
                    c, metadataUuid));
            }
        }

        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);
    }

    @ApiOperation(
        value = "Delete tags of a record",
        notes = "",
        nickname = "deleteRecordTags")
    @RequestMapping(
        value = "/{metadataUuid}/tags",
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Record tags removed."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public void deleteTags(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Tag identifier. If none, all tags are removed.",
            required = false
        )
        @RequestParam(required = false)
            Integer[] id,
        HttpServletRequest request
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();

        if (id == null || id.length == 0) {
            appContext.getBean(MetadataRepository.class).update(
                metadata.getId(), entity -> entity.getCategories().clear());
        }

        DataManager dataManager = appContext.getBean(DataManager.class);
        if (id != null) {
            for (int c : id) {
                dataManager.unsetCategory(
                    ApiUtils.createServiceContext(request),
                    String.valueOf(metadata.getId()), c);
            }
        }

        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);
    }


    @ApiOperation(
        value = "Add tags to one or more records",
        notes = "",
        nickname = "addTagsToRecords")
    @RequestMapping(
        value = "/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Report about updated records."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public MetadataProcessingReport updateTags(
        @ApiParam(
            value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(
            value = API_PARAM_TAG_IDENTIFIER,
            required = true
        )
        @RequestParam
            Integer[] id,
        @ApiParam(
            value = ApiParams.API_PARAM_CLEAR_ALL_BEFORE_INSERT,
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean clear,
        HttpServletRequest request,
        @ApiIgnore
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();
            final DataManager dataMan = context.getBean(DataManager.class);
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
            final AccessManager accessMan = context.getBean(AccessManager.class);
            final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                Metadata info = metadataRepository.findOneByUuid(uuid);
                if (info == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(info.getId()))) {
                    report.addNotEditableMetadataId(info.getId());
                } else {
                    if (clear) {
                        info.getCategories().clear();
                    }

                    if (id != null) {
                        for (int c : id) {
                            final MetadataCategory category = categoryRepository.findOne(c);
                            if (category != null) {
                                info.getCategories().add(category);
                                listOfUpdatedRecords.add(String.valueOf(info.getId()));
                            } else {
                                report.addMetadataInfos(info.getId(), String.format(
                                    "Can't assign non existing category with id '%d' to record '%s'",
                                    c, info.getUuid()
                                ));
                            }
                        }
                        metadataRepository.save(info);
                        report.incrementProcessedRecords();
                    }
                }
            }
            dataMan.flush();
            dataMan.indexMetadata(listOfUpdatedRecords);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }

    @ApiOperation(
        value = "Delete tags to one or more records",
        notes = "",
        nickname = "deleteRecordsTags")
    @RequestMapping(
        value = "/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Report about removed records."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public MetadataProcessingReport updateTags(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(
            value = API_PARAM_TAG_IDENTIFIER
        )
        @RequestParam
            Integer[] id,
        HttpServletRequest request,
        @ApiIgnore
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();
            final DataManager dataMan = context.getBean(DataManager.class);
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
            final AccessManager accessMan = context.getBean(AccessManager.class);
            final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                Metadata info = metadataRepository.findOneByUuid(uuid);
                if (info == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(info.getId()))) {
                    report.addNotEditableMetadataId(info.getId());
                } else {
                    info.getCategories().clear();
                    metadataRepository.save(info);
                    report.incrementProcessedRecords();
                }
            }
            dataMan.flush();
            dataMan.indexMetadata(listOfUpdatedRecords);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }
}
