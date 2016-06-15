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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.ProcessingReport;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.APIPARAM_RECORD_UUIDS_OR_SELECTION;

@RequestMapping(value = {
    "/api/processes",
    "/api/" + API.VERSION_0_1 +
        "/processes"
})
@Api(value = "processes",
    tags = "processes",
    description = "Processing operations")
@Controller("processValidate")
public class ValidateApi {

    @Autowired
    IProcessingReportRegistry registry;


    @ApiOperation(value = "Validate records",
        nickname = "validateRecords",
        notes = "")
    @RequestMapping(
        value = "/validate",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public SimpleMetadataProcessingReport searchAndReplace(
        @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
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

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, userSession);

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
