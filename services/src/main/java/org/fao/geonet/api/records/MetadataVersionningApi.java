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

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordVersionning")
@ReadWriteController
public class MetadataVersionningApi {

    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    DataManager dataManager;
    @Autowired
    AccessManager accessMan;
    @Autowired
    IMetadataUtils metadataRepository;

    @ApiOperation(
        value = "(Experimental) Enable version control",
        notes = "",
        nickname = "enableVersionControl")
    @RequestMapping(
        value = "/{metadataUuid}/versions",
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public ResponseEntity enableVersionControl(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        dataManager.versionMetadata(ApiUtils.createServiceContext(request),
            String.valueOf(metadata.getId()), metadata.getXmlData(false));

        return new ResponseEntity(HttpStatus.CREATED);
    }




    @ApiOperation(
        value = "(Experimental) Enable version control for one or more records",
        notes = "",
        nickname = "enableVersionControlForRecords")
    @RequestMapping(
        value = "/versions",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public MetadataProcessingReport enableVersionControlForRecords(
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
        HttpServletRequest request,
        @ApiIgnore
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            for (String uuid : records) {
            	if(!metadataRepository.existsMetadataUuid(uuid)) {
                    report.incrementNullRecords();            		
            	} else {
	                for(AbstractMetadata metadata : metadataRepository.findAllByUuid(uuid)) {
		                if (!accessMan.canEdit(
		                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
		                    report.addNotEditableMetadataId(metadata.getId());
		                } else {
		                    dataManager.versionMetadata(ApiUtils.createServiceContext(request),
		                        String.valueOf(metadata.getId()), metadata.getXmlData(false));
		                    report.incrementProcessedRecords();
		                }
	                }
            	}
            }
        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }
}
