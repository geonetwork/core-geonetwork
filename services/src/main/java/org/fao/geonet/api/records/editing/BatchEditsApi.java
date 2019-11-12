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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.kernel.BatchEditParameter;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.events.history.RecordUpdatedEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
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
    @ApiOperation(value = "Edit a set of records by XPath expressions. This operations applies the update-fixed-info.xsl "
        + "transformation for the metadata schema and updates the change date if the parameter updateDateStamp is set to true.",
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
    public IProcessingReport batchEdit(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "iso19139")
        @RequestParam(required = false) String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(
            value = ApiParams.API_PARAM_UPDATE_DATESTAMP,
            required = false,
            defaultValue = "false"
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean updateDateStamp,
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
                selectionManager.getSelection(bucket)) {
                final Set<String> selection = selectionManager.getSelection(bucket);
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
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        String changeDate = null;
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
                    boolean metadataChanged = false;

                    Iterator<BatchEditParameter> listOfUpdatesIterator = listOfUpdates.iterator();
                    while (listOfUpdatesIterator.hasNext()) {
                        BatchEditParameter batchEditParameter =
                            listOfUpdatesIterator.next();

                        AddElemValue propertyValue =
                            new AddElemValue(batchEditParameter.getValue());

                        boolean applyEdit = true;
                        if (StringUtils.isNotEmpty(batchEditParameter.getCondition())) {
                            final Object node = Xml.selectSingle(metadata, batchEditParameter.getCondition(), metadataSchema.getNamespaces());
                            applyEdit = (node != null) || (node instanceof Boolean && (Boolean)node != false);
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
                    if (metadataChanged) {
                        boolean validate = false;
                        boolean ufo = true;
                        boolean index = true;
                        boolean uds = updateDateStamp;
                        Element beforeMetadata = dataMan.getMetadata(serviceContext, String.valueOf(record.getId()), false, false, false);

                        dataMan.updateMetadata(
                            serviceContext, record.getId() + "", metadata,
                            validate, ufo, index,
                            "eng", // Not used when validate is false
                            changeDate, uds);
                        report.addMetadataInfos(record.getId(), "Metadata updated.");

                        Element afterMetadata = dataMan.getMetadata(serviceContext, String.valueOf(record.getId()), false, false, false);
                        XMLOutputter outp = new XMLOutputter();
                        String xmlBefore = outp.outputString(beforeMetadata);
                        String xmlAfter = outp.outputString(afterMetadata);
                        new RecordUpdatedEvent(record.getId(), userSession.getUserIdAsInt(), xmlBefore, xmlAfter).publish(appContext);
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
