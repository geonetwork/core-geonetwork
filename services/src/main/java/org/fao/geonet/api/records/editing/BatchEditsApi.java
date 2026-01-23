/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.api.records.editing;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.events.history.RecordUpdatedEvent;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.utils.Diff;
import org.fao.geonet.utils.DiffType;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordsBatchEdit")
@ReadWriteController
public class BatchEditsApi implements ApplicationContextAware {
    @Autowired
    SchemaManager _schemaManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    RoleHierarchy roleHierarchy;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Preview edits made by XPath expressions.")
    @RequestMapping(value = "/batchediting/preview",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processed records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object previewBatchEdit(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION)
        @RequestParam(required = false) String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME)
        @RequestParam(
            required = false
        )
        String bucket,
        @Parameter(
            description = "Return differences with diff, diffhtml or patch"
        )
        @RequestParam(
            required = false
        )
        DiffType diffType,
        @RequestBody BatchEditParameter[] edits,
        HttpServletRequest request)
        throws Exception {
        boolean previewOnly = true;
        return applyBatchEdits(uuids, bucket, false, edits, request, previewOnly, diffType).two();
    }

    /**
     * The service edits to the current selection or a set of uuids.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Edit a set of records by XPath expressions. This operations applies the update-fixed-info.xsl "
        + "transformation for the metadata schema and updates the change date if the parameter updateDateStamp is set to true.")
    @RequestMapping(value = "/batchediting",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Return a report of what has been done."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public IProcessingReport batchEdit(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION)
        @RequestParam(required = false) String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME)
        @RequestParam(
            required = false
        )
        String bucket,
        @Parameter(
            description = ApiParams.API_PARAM_UPDATE_DATESTAMP
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        boolean updateDateStamp,
        @RequestBody BatchEditParameter[] edits,
        HttpServletRequest request)
        throws Exception {

        return applyBatchEdits(uuids, bucket, updateDateStamp, edits, request, false, null).one();
    }

    private Pair<SimpleMetadataProcessingReport, Element> applyBatchEdits(
        String[] uuids, String bucket,
        boolean updateDateStamp, BatchEditParameter[] edits,
        HttpServletRequest request,
        boolean previewOnly, DiffType diffType) throws Exception {
        List<BatchEditParameter> listOfUpdates = Arrays.asList(edits);
        if (listOfUpdates.isEmpty()) {
            throw new IllegalArgumentException("At least one edit must be defined.");
        }


        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserUtil.checkUserProfileLevel(serviceContext.getUserSession(), settingManager, roleHierarchy, Settings.METADATA_BATCH_EDITING_ACCESS_LEVEL, Profile.Editor, "batch edit metadata");
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            SelectionManager selectionManager =
                SelectionManager.getManager(serviceContext.getUserSession());

            synchronized (
                selectionManager.getSelection(bucket)) {
                final Set<String> selection = selectionManager.getSelection(bucket);
                setOfUuidsToEdit = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToEdit = Sets.newHashSet(Arrays.asList(uuids));
        }

        if (setOfUuidsToEdit.isEmpty()) {
            throw new IllegalArgumentException("At least one record should be defined or selected for updates.");
        }

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataMan = appContext.getBean(DataManager.class);
        SchemaManager _schemaManager = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        // This value is used in replace mode to create the node if it doesn't exist.
        // We don't want to create a node in replace mode, just replace the element if it exists, otherwise ignore it.
        boolean createXpathNodeIfNotExists = false;

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        report.setTotalRecords(setOfUuidsToEdit.size());
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        String changeDate = null;
        Element preview = new Element("preview");

        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        for (String recordUuid : setOfUuidsToEdit) {
            AbstractMetadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessMan.isOwner(serviceContext, String.valueOf(record.getId()))) {
                report.addNotEditableMetadataId(record.getId());
            } else {
                // Processing
                try {
                    EditLib editLib = new EditLib(_schemaManager);
                    MetadataSchema metadataSchema = _schemaManager.getSchema(record.getDataInfo().getSchemaId());
                    Element metadata = record.getXmlData(false);
                    String original = Xml.getString(metadata);
                    boolean metadataChanged = false;

                    Iterator<BatchEditParameter> listOfUpdatesIterator = listOfUpdates.iterator();
                    while (listOfUpdatesIterator.hasNext()) {
                        BatchEditParameter batchEditParameter =
                            listOfUpdatesIterator.next();

                        AddElemValue propertyValue =
                            new AddElemValue(batchEditParameter.getValue());

                        boolean applyEdit = true;
                        if (StringUtils.isNotEmpty(batchEditParameter.getCondition())) {
                            applyEdit = false;
                            final Object node = Xml.selectSingle(metadata, batchEditParameter.getCondition(), metadataSchema.getNamespaces());
                            if (node != null && node instanceof Boolean && (Boolean) node == true) {
                                applyEdit = true;
                            }
                        }
                        if (applyEdit) {
                            metadataChanged = editLib.addElementOrFragmentFromXpath(
                                metadata,
                                metadataSchema,
                                batchEditParameter.getXpath(),
                                propertyValue,
                                createXpathNodeIfNotExists
                            ) || metadataChanged;
                        }
                    }
                    if (previewOnly) {
                        if (diffType == null) {
                            preview.addContent(metadata);
                        } else {
                            preview.addContent(
                                Diff.diff(original, Xml.getString(metadata), diffType)
                            );
                        }
                    } else if (metadataChanged) {
                        boolean validate = false;
                        boolean ufo = true;
                        boolean uds = updateDateStamp;
                        Element beforeMetadata = dataMan.getMetadata(serviceContext, String.valueOf(record.getId()), false, false, false);

                        dataMan.updateMetadata(
                            serviceContext, record.getId() + "", metadata,
                            validate, ufo,
                            "eng", // Not used when validate is false
                            changeDate, uds, IndexingMode.full);
                        report.addMetadataInfos(record, "Metadata updated.");

                        Element afterMetadata = dataMan.getMetadata(serviceContext, String.valueOf(record.getId()), false, false, false);
                        XMLOutputter outp = new XMLOutputter();
                        String xmlBefore = outp.outputString(beforeMetadata);
                        String xmlAfter = outp.outputString(afterMetadata);
                        new RecordUpdatedEvent(record.getId(), userSession.getUserIdAsInt(), xmlBefore, xmlAfter).publish(appContext);
                    } else {
                        report.incrementUnchangedRecords();
                    }
                } catch (Exception e) {
                    report.addMetadataError(record, e);
                }
                report.incrementProcessedRecords();
            }
        }
        report.close();
        return Pair.write(report, preview);
    }

}
