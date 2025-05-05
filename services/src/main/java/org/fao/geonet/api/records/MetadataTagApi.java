/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordCategoryChangeEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("tagRecords")
@ReadWriteController
public class MetadataTagApi {

    public static final String API_PARAM_TAG_IDENTIFIER = "Tag identifier";

    @Autowired
    MetadataCategoryRepository categoryRepository;

    @Autowired
    DataManager dataManager;

    @Autowired
    EsSearchManager searchManager;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    AccessManager accessMan;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record tags",
        description = "Tags are used to classify information.<br/>" +
            "<a href='https://docs.geonetwork-opensource.org/latest/user-guide/tag-information/tagging-with-categories/'>More info</a>")
    @GetMapping(
        value = "/{metadataUuid:.+}/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record tags."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Set<MetadataCategory> getRecordTags(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        return metadata.getCategories();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add tags to a record",
        description = "")
    @PutMapping(value = "/{metadataUuid:.+}/tags")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Record tags added."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public void tagRecord(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = API_PARAM_TAG_IDENTIFIER,
            required = true
        )
        @RequestParam
            Integer[] id,
        @Parameter(
            description = ApiParams.API_PARAM_CLEAR_ALL_BEFORE_INSERT,
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean clear,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Set<MetadataCategory> before = metadata.getCategories();

        if (clear) {
            metadataManager.update(
                metadata.getId(), entity -> entity.getCategories().clear());
        }

        for (int c : id) {
            final Optional<MetadataCategory> category = categoryRepository.findById(c);
            if (category.isPresent()) {
                dataManager.setCategory(
                    ApiUtils.createServiceContext(request),
                    String.valueOf(metadata.getId()), String.valueOf(c));
            } else {
                throw new ResourceNotFoundException(String.format(
                    "Can't assign non existing category with id '%d' to record '%s'",
                    c, metadataUuid));
            }
        }

        indexTags(metadata);

        metadata = ApiUtils.canEditRecord(metadataUuid, request);
        Set<MetadataCategory> after = metadata.getCategories();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        new RecordCategoryChangeEvent(metadata.getId(), userSession.getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(before, RecordCategoryChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(after, RecordCategoryChangeEvent.FIELD)).publish(appContext);

    }

    private void indexTags(AbstractMetadata metadata) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        if (!metadata.getCategories().isEmpty()) {
            List<String> categories = new ArrayList<>();
            for (MetadataCategory category : metadata.getCategories()) {
                categories.add(category.getName());
            }
            fields.put(Geonet.IndexFieldNames.CAT, categories);
        }

        if (metadata instanceof MetadataDraft) {
            searchManager.updateFields(metadata.getUuid() + "-draft", fields,
                Sets.newHashSet(new String[] {Geonet.IndexFieldNames.CAT}));
        } else {
            searchManager.updateFields(metadata.getUuid(), fields,
                Sets.newHashSet(new String[] {Geonet.IndexFieldNames.CAT}));
        }


    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete tags of a record",
        description = "")
    @DeleteMapping(value = "/{metadataUuid:.+}/tags")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record tags removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public void deleteTags(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Tag identifier. If none, all tags are removed.",
            required = false
        )
        @RequestParam(required = false)
            Integer[] id,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        Set<MetadataCategory> before = metadata.getCategories();

        if (id == null || id.length == 0) {
            metadataManager.update(
                metadata.getId(), entity -> entity.getCategories().clear());
        }

        if (id != null) {
            for (int c : id) {
                dataManager.unsetCategory(
                    ApiUtils.createServiceContext(request),
                    String.valueOf(metadata.getId()), c);
            }
        }

        indexTags(metadata);

        metadata = ApiUtils.canEditRecord(metadataUuid, request);
        Set<MetadataCategory> after = metadata.getCategories();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        new RecordCategoryChangeEvent(metadata.getId(), userSession.getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(before, RecordCategoryChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(after, RecordCategoryChangeEvent.FIELD)).publish(appContext);

    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add or remove tags to one or more records",
        description = "")
    @PutMapping(
        value = "/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about updated records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public MetadataProcessingReport tagRecords(
        @Parameter(
            description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = API_PARAM_TAG_IDENTIFIER,
            required = false
        )
        @RequestParam(required = false)
            Integer[] id,
        @Parameter(
            description = API_PARAM_TAG_IDENTIFIER + " to remove.",
            required = false
        )
        @RequestParam(required = false)
            Integer[] removeId,
        @Parameter(
            description = ApiParams.API_PARAM_CLEAR_ALL_BEFORE_INSERT,
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean clear,
        HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                AbstractMetadata info = metadataRepository.findOneByUuid(uuid);
                Set<MetadataCategory> before = info.getCategories();
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
                            final Optional<MetadataCategory> category = categoryRepository.findById(c);
                            if (category.isPresent()) {
                                info.getCategories().add(category.get());
                                listOfUpdatedRecords.add(String.valueOf(info.getId()));
                            } else {
                                report.addMetadataInfos(info, String.format(
                                    "Can't assign non existing category with id '%d' to record '%s'",
                                    c, info.getUuid()
                                ));
                            }
                        }
                    }
                    if (removeId != null) {
                        for (int c : removeId) {
                            final Optional<MetadataCategory> category = categoryRepository.findById(c);
                            if (category.isPresent()) {
                                info.getCategories().remove(category.get());
                                listOfUpdatedRecords.add(String.valueOf(info.getId()));
                            } else {
                                report.addMetadataInfos(info, String.format(
                                    "Can't remove non existing category with id '%d' to record '%s'",
                                    c, info.getUuid()
                                ));
                            }
                        }
                        metadataManager.save(info);
                        report.incrementProcessedRecords();
                    }
                }

                info = metadataRepository.findOneByUuid(uuid);
                Set<MetadataCategory> after = info.getCategories();
                indexTags(info);
                UserSession userSession = ApiUtils.getUserSession(request.getSession());
                new RecordCategoryChangeEvent(info.getId(), userSession.getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(before, RecordCategoryChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(after, RecordCategoryChangeEvent.FIELD)).publish(context);

            }
            dataManager.flush();
        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete tags to one or more records",
        description = "")
    @DeleteMapping(
        value = "/tags",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report about removed records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public MetadataProcessingReport deleteTagForRecords(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = API_PARAM_TAG_IDENTIFIER
        )
        @RequestParam
            Integer[] id,
        HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();
            for (String uuid : records) {
                AbstractMetadata info = metadataRepository.findOneByUuid(uuid);
                Set<MetadataCategory> before = info.getCategories();
                if (info == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(info.getId()))) {
                    report.addNotEditableMetadataId(info.getId());
                } else {
                    info.getCategories().clear();
                    metadataManager.save(info);
                    report.incrementProcessedRecords();
                }

                info = metadataRepository.findOneByUuid(uuid);
                Set<MetadataCategory> after = info.getCategories();
                indexTags(info);
                UserSession userSession = ApiUtils.getUserSession(request.getSession());
                new RecordCategoryChangeEvent(info.getId(), userSession.getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(before, RecordCategoryChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(after, RecordCategoryChangeEvent.FIELD)).publish(context);
            }
            dataManager.flush();
        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }
}
