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

package org.fao.geonet.api.processing;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.jdom.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(
    value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("processValidate")
public class ValidateApi {

    @Autowired
    IProcessingReportRegistry registry;


    @ApiOperation(value = "Validate one or more records",
        nickname = "validateRecords",
        notes = "Update validation status for all records.")
    @RequestMapping(
        value = "/validate",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Records validated."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport validateRecords(
        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiIgnore
            HttpSession session,
        @ApiIgnore
            HttpServletRequest request
    ) throws Exception {
        UserSession userSession = ApiUtils.getUserSession(session);

        SimpleMetadataProcessingReport report =
            new SimpleMetadataProcessingReport();
        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            DataManager dataMan = applicationContext.getBean(DataManager.class);
            AccessManager accessMan = applicationContext.getBean(AccessManager.class);
            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);

            final MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);
            for (String uuid : records) {
                Metadata record = metadataRepository.findOneByUuid(uuid);
                if (record == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(serviceContext, String.valueOf(record.getId()))) {
                    report.addNotEditableMetadataId(record.getId());
                } else {
                    String idString = String.valueOf(record.getId());
                    boolean isValid = dataMan.doValidate(record.getDataInfo().getSchemaId(),
                        idString,
                        new Document(record.getXmlData(false)),
                        serviceContext.getLanguage());
                    if (isValid) {
                        report.addMetadataInfos(record.getId(), "Is valid");
                    } else {
                        report.addMetadataInfos(record.getId(), "Is invalid");
                    }
                    report.addMetadataId(record.getId());
                    report.incrementProcessedRecords();
                }
            }

            // index records
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, report.getMetadata());
            r.process();
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }
}
