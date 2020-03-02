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
package org.fao.geonet.api.records.editing;

import com.google.common.collect.Sets;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.BatchEditParameter;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = "records",
    tags = "records",
    description = "Metadata record editing operations")
@Controller("records/edit")
@ReadWriteController
public class BatchEditsApi implements ApplicationContextAware {
    @Autowired
    SchemaManager _schemaManager;
    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    /**
     * The service edits to the current selection or a set of uuids.
     */
    @ApiOperation(value = "Edit a set of records by XPath expressions",
        nickname = "batchEdit")
    @RequestMapping(value = "/batchediting",
        method = RequestMethod.PUT,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Return a report of what has been done."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public
    IProcessingReport batchEdit(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "iso19139")
        @RequestParam(required = false) String[] uuids,
        @RequestBody BatchEditParameter[] edits,
        HttpServletRequest request)
        throws Exception {

        List<BatchEditParameter> listOfUpdates = Arrays.asList(edits);
        if (listOfUpdates.size() == 0) {
            throw new IllegalArgumentException("At least one edit must be defined.");
        }


        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            SelectionManager selectionManager =
                SelectionManager.getManager(serviceContext.getUserSession());

            synchronized (
                selectionManager.getSelection(
                    SelectionManager.SELECTION_METADATA)) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                setOfUuidsToEdit = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToEdit = Sets.newHashSet(Arrays.asList(uuids));
        }

        if (setOfUuidsToEdit.size() == 0) {
            throw new IllegalArgumentException("At least one record should be defined or selected for updates.");
        }

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataMan = appContext.getBean(DataManager.class);
        SchemaManager _schemaManager = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        final String settingId = Settings.SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS;
        boolean createXpathNodeIfNotExists =
            context.getBean(SettingManager.class).getValueAsBool(settingId);


        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        report.setTotalRecords(setOfUuidsToEdit.size());

        String changeDate = null;
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
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
                    boolean metadataChanged = false;

                    Iterator<BatchEditParameter> listOfUpdatesIterator = listOfUpdates.iterator();
                    while (listOfUpdatesIterator.hasNext()) {
                        BatchEditParameter batchEditParameter =
                            listOfUpdatesIterator.next();

                        AddElemValue propertyValue =
                            new AddElemValue(batchEditParameter.getValue());

                        metadataChanged = editLib.addElementOrFragmentFromXpath(
                            metadata,
                            metadataSchema,
                            batchEditParameter.getXpath(),
                            propertyValue,
                            createXpathNodeIfNotExists
                        );
                    }
                    if (metadataChanged) {
                        boolean validate = false;
                        boolean ufo = false;
                        boolean index = true;
                        dataMan.updateMetadata(
                            serviceContext, record.getId() + "", metadata,
                            validate, ufo, index,
                            "eng", // Not used when validate is false
                            changeDate, false);
                        report.addMetadataInfos(record.getId(), "Metadata updated.");
                    }
                } catch (Exception e) {
                    report.addMetadataError(record.getId(), e);
                }
                report.incrementProcessedRecords();
            }
        }
        report.close();
        return report;
    }
}
