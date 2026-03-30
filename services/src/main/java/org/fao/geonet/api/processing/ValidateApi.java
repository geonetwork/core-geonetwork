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

package org.fao.geonet.api.processing;

import java.util.*;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataValidationProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.events.history.RecordValidationTriggeredEvent;
import org.fao.geonet.inspire.validator.MInspireEtfValidateProcess;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.api.records.InspireValidationApi.API_PARAM_INSPIRE_VALIDATION_MODE;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(
    name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("processValidate")
public class ValidateApi {
    private static final int NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP = 1;

    /**
     * XML element name for the active pattern in schematron reports
     */
    public static final String EL_ACTIVE_PATTERN = "active-pattern";

    /**
     * XML element name for fired rules in schematron reports
     */
    public static final String EL_FIRED_RULE = "fired-rule";

    /**
     * XML attribute name for context in schematron reports
     */
    public static final String ATT_CONTEXT = "context";

    /**
     * Default context value when none is provided in schematron reports
     */
    public static final String DEFAULT_CONTEXT = "??";

    @Autowired
    protected ApplicationContext appContext;
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
    SchemaManager schemaManager;
    @Autowired
    SettingManager settingManager;
    @Autowired
    MetadataValidationRepository metadataValidationRepository;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    MBeanExporter mBeanExporter;
    @Autowired
    protected XmlSerializer xmlSerializer;

    private final ArrayDeque<SelfNaming> mAnalyseProcesses = new ArrayDeque<>(NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP);

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

    @io.swagger.v3.oas.annotations.Operation(summary = "Validate one or more records",
        description = "Update validation status for all records.")
    @RequestMapping(
        value = "/validate",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Records validated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public MetadataValidationProcessingReport validateRecords(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(
            required = false,
            defaultValue = "")
            Boolean approved,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        UserSession userSession = ApiUtils.getUserSession(session);

        MetadataValidationProcessingReport report =
            new MetadataValidationProcessingReport();
        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);
            report.setTotalRecords(records.size());

            for (String uuid : records) {
                int loopConditionCount = 0;
                for (AbstractMetadata record : metadataRepository.findAllByUuid(uuid)) {
                    //determine if this is an approved record.
                    Boolean isMetadataApproved = metadataUtils.isMetadataApproved(record.getId());

                    if (approved == null ||
                        (approved == true && isMetadataApproved) ||
                        (approved == false && !isMetadataApproved)) {
                        loopConditionCount++;
                        // If we processed more than one record in this loop then we will also increase the total records
                        // as it means that we are processing both and approved and draft and that was not calculated in the original total.
                        if (loopConditionCount > 1) {
                            report.setTotalRecords(report.getNumberOfRecords() + 1);
                        }
                        if (!accessMan.canEdit(serviceContext, String.valueOf(record.getId()))) {
                            report.addNotEditableMetadataId(record.getId());
                        } else {
                            Pair<Element, String> validationPair = validator.doValidate(userSession, record.getDataInfo().getSchemaId(), Integer.toString(record.getId()), xmlSerializer.select(serviceContext, String.valueOf(record.getId())), serviceContext.getLanguage(), false);
                            Element schemaTronReport = validationPair.one();
                            if (schemaTronReport != null) {
                                restructureReportToHavePatternRuleHierarchy(schemaTronReport);
                                report.addAllReportsMatchingRequirement(record, schemaTronReport, SchematronRequirement.REPORT_ONLY);
                                report.addAllReportsMatchingRequirement(record, schemaTronReport, SchematronRequirement.REQUIRED);
                                if (!report.getMetadataErrors().containsKey(record.getId())) {
                                    report.addValidMetadata(record);
                                    new RecordValidationTriggeredEvent(record.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), "1").publish(applicationContext);
                                } else {
                                    if (!report.getInvalidMetadata().containsKey(record.getId())) {
                                        report.addInvalidMetadata(record);
                                    }
                                    new RecordValidationTriggeredEvent(record.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), "0").publish(applicationContext);
                                }
                            }
                            report.addMetadataId(record.getId());
                            report.incrementProcessedRecords();
                        }
                    }
                }
                // If loopConditionCount is 0 then no data was identified for that uuid.
                if (loopConditionCount == 0) {
                    report.incrementNullRecords();
                }
            }

            // index records
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, report.getMetadata());
            r.process(settingManager.getSiteId(), true);
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Clear validation status of one or more records",
        description = "")
    @RequestMapping(
        value = "/validate",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Records validation status cleared."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport cleanValidationStatus(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        UserSession userSession = ApiUtils.getUserSession(session);

        SimpleMetadataProcessingReport report =
            new SimpleMetadataProcessingReport();
        try {
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
                        List<MetadataValidation> validationStatus = metadataValidationRepository.findAllById_MetadataId(record.getId());
                        metadataValidationRepository.deleteAll(validationStatus);
                        report.addMetadataId(record.getId());
                        report.incrementProcessedRecords();
                    }
                }
            }

            // index records
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, report.getMetadata());
            r.process(settingManager.getSiteId(), true);
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Validate one or more records in INSPIRE validator",
        description = "Update validation status for all records.")
    @RequestMapping(
        value = "/validate/inspire",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Records validated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity validateRecordsInspire(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = API_PARAM_INSPIRE_VALIDATION_MODE,
            required = false)
        @RequestParam(required = false)
            String mode,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        MInspireEtfValidateProcess registredMAnalyseProcess = getRegistredMInspireEtfValidateProcess(serviceContext);

        registredMAnalyseProcess.deleteAll();

        UserSession userSession = ApiUtils.getUserSession(session);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);

        registredMAnalyseProcess.processMetadata(records, mode);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private MInspireEtfValidateProcess getRegistredMInspireEtfValidateProcess(ServiceContext serviceContext) {
        String URL = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);
        String URL_QUERY = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL_QUERY);
        if (StringUtils.isEmpty(URL_QUERY)) {
            URL_QUERY = URL;
        }

        MInspireEtfValidateProcess mAnalyseProcess = new MInspireEtfValidateProcess(settingManager.getSiteId(), URL, URL_QUERY, serviceContext, appContext);
        mBeanExporter.registerManagedResource(mAnalyseProcess, mAnalyseProcess.getObjectName());
        try {
            mBeanExporter.unregisterManagedResource(mAnalyseProcesses.removeLast().getObjectName());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        mAnalyseProcesses.addFirst(mAnalyseProcess);
        return mAnalyseProcess;
    }

    public static final Filter ErrorFinder = new Filter() {
        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("error")) {
                    return true;
                } else if (name.equals("failed-assert")) {
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * Restructures a schematron validation report to create a hierarchical structure.
     *
     * <p>Schematron report has an odd structure where pattern elements, fired rules,
     * and assertions/reports are all siblings. This method restructures the XML to create
     * a more logical hierarchy where patterns contain fired rules, which in turn contain
     * assertions and reports.</p>
     *
     * <p>The input structure looks like:
     * <pre>
     * <code>
     * &lt;svrl:active-pattern  ... />
     * &lt;svrl:fired-rule  ... />
     * &lt;svrl:failed-assert ... />
     * &lt;svrl:successful-report ... />
     * </code>
     * </pre>
     * </p>
     *
     * <p>The output structure looks like:
     * <pre>
     * <code>
     * &lt;svrl:active-pattern  ... >
     *     &lt;svrl:fired-rule  ... >
     *         &lt;svrl:failed-assert ... />
     *         &lt;svrl:successful-report ... />
     *     &lt;svrl:fired-rule  ... >
     * &lt;svrl:active-pattern>
     * </code>
     * </pre>
     * </p>
     *
     * @param errorReport The schematron validation report element to restructure
     */
    private static void restructureReportToHavePatternRuleHierarchy(Element errorReport) {
        final Iterator patternFilter = errorReport
            .getDescendants(new ElementFilter(EL_ACTIVE_PATTERN, Geonet.Namespaces.SVRL));
        @SuppressWarnings("unchecked")
        List<Element> patterns = Lists.newArrayList(patternFilter);
        for (Element pattern : patterns) {
            final Element parentElement = pattern.getParentElement();
            Element currentRule = null;
            @SuppressWarnings("unchecked") final List<Element> children = parentElement.getChildren();

            int index = children.indexOf(pattern) + 1;
            while (index < children.size() && !children.get(index).getName().equals(EL_ACTIVE_PATTERN)) {
                Element next = children.get(index);
                if (EL_FIRED_RULE.equals(next.getName())) {
                    currentRule = next;
                    next.detach();
                    pattern.addContent(next);
                } else {
                    if (currentRule == null) {
                        // odd but could happen I suppose
                        currentRule = new Element(EL_FIRED_RULE, Geonet.Namespaces.SVRL).setAttribute(ATT_CONTEXT,
                            DEFAULT_CONTEXT);
                        pattern.addContent(currentRule);
                    }

                    next.detach();
                    currentRule.addContent(next);

                }
            }
            if (pattern.getChildren().isEmpty()) {
                pattern.detach();
            }
        }
    }
}
