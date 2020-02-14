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

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

import java.util.ArrayDeque;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.history.RecordValidationTriggeredEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(
    value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("processValidate")
public class ValidateApi {
    private static final int NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP = 1;

    @Autowired
    IProcessingReportRegistry registry;

    @Autowired
    IMetadataValidator validator;

    @Autowired
    AccessManager accessMan;

    @Autowired
    DataManager dataMan;

    @Autowired
    IMetadataUtils metadataRepository;

    @Autowired
    InspireValidatorUtils inspireValidatorUtils;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    MetadataValidationRepository metadataValidationRepository;

    @Autowired
    protected ApplicationContext appContext;

    @Autowired
    MBeanExporter mBeanExporter;

    private ArrayDeque<SelfNaming> mAnalyseProcesses = new ArrayDeque<>(NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP);

    @PostConstruct
    public void iniMBeansSlidingWindowWithEmptySlot() {
        for (int i = 0; i < NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP; i++) {
            EmptySlotBatch emptySlot = new EmptySlotBatch("batch-etf-inspire", i);
            mAnalyseProcesses.addFirst(emptySlot);
            try {
                mBeanExporter.registerManagedResource(emptySlot, emptySlot.getObjectName());
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
    }
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
            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);

            for (String uuid : records) {
                if (!metadataRepository.existsMetadataUuid(uuid)) {
                    report.incrementNullRecords();
                }
                for (AbstractMetadata record : metadataRepository.findAllByUuid(uuid)) {
                    if (!accessMan.canEdit(serviceContext, String.valueOf(record.getId()))) {
                        report.addNotEditableMetadataId(record.getId());
                    } else {
                        boolean isValid = validator.doValidate(record, serviceContext.getLanguage());
                        if (isValid) {
                            report.addMetadataInfos(record.getId(), "Is valid");
                            new RecordValidationTriggeredEvent(record.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), "1").publish(applicationContext);
                        } else {
                            report.addMetadataInfos(record.getId(), "Is invalid");
                            new RecordValidationTriggeredEvent(record.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), "0").publish(applicationContext);
                        }
                        report.addMetadataId(record.getId());
                        report.incrementProcessedRecords();
                    }
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


    @ApiOperation(value = "Validate one or more records in INSPIRE validator",
        nickname = "validateRecordsInspire",
        notes = "Update validation status for all records.")
    @RequestMapping(
        value = "/validate/inspire",
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
    public ResponseEntity validateRecordsInspire(
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
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        MInspireEtfValidateProcess registredMAnalyseProcess = getRegistredMInspireEtfValidateProcess(serviceContext);

        registredMAnalyseProcess.deleteAll();

        UserSession userSession = ApiUtils.getUserSession(session);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);

        registredMAnalyseProcess.processMetadata(records);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private MInspireEtfValidateProcess getRegistredMInspireEtfValidateProcess(ServiceContext serviceContext) {
        String URL = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);

        MInspireEtfValidateProcess mAnalyseProcess = new MInspireEtfValidateProcess(URL, serviceContext, appContext);
        mBeanExporter.registerManagedResource(mAnalyseProcess, mAnalyseProcess.getObjectName());
        try {
            mBeanExporter.unregisterManagedResource(mAnalyseProcesses.removeLast().getObjectName());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        mAnalyseProcesses.addFirst(mAnalyseProcess);
        return mAnalyseProcess;
    }
}
