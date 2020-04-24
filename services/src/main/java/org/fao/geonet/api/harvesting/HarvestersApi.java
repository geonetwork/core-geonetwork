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

package org.fao.geonet.api.harvesting;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SourceRepository;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@RequestMapping(value = {
    "/{portal}/api/harvesters",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/harvesters"
})
@Api(value = "harvesters",
    tags = "harvesters",
    description = "Harvester operations")
@Controller("harvesters")
public class HarvestersApi {

    @Autowired
    private HarvestManager harvestManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private IMetadataUtils metadataRepository;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private DataManager dataManager;

    @Autowired
    HarvestHistoryRepository historyRepository;

    @ApiOperation(
        value = "Assign harvester records to a new source",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "assignHarvestedRecordToSource")
    @RequestMapping(
        value = "/{harvesterUuid}/assign",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Harvester records transfered to new source."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public HttpEntity<HttpStatus> assignHarvestedRecordToSource(
        @ApiParam(
            value = "The harvester UUID"
        )
        @PathVariable
            String harvesterUuid,
        @ApiParam(
            value = "The target source UUID"
        )
        @RequestParam
            String source) throws Exception {
        final long elapsedTime = System.currentTimeMillis();
        final AbstractHarvester harvester = harvestManager.getHarvester(harvesterUuid);
        if (harvester == null) {
            throw new ResourceNotFoundException(String.format(
                "Harvester with UUID '%s' not found. Cannot assign new source.",
                harvesterUuid));
        }

        final Source sourceNode = sourceRepository.findOneByUuid(source);
        if (sourceNode == null) {
            throw new ResourceNotFoundException(String.format(
                "Source with UUID '%s' not found. Cannot assign source to harvester records.",
                source));
        }

        final List<? extends AbstractMetadata> allHarvestedRecords = metadataRepository.findAllByHarvestInfo_Uuid(harvesterUuid);
        List<String> records = new ArrayList<>(allHarvestedRecords.size());

        if (allHarvestedRecords.size() < 1) {
            throw new NoResultsFoundException(String.format(
                "Harvester with UUID '%s' has no record to assign to source '%s'.",
                harvesterUuid,
                source));
        }

        for (AbstractMetadata record : allHarvestedRecords) {
            record.getSourceInfo().setSourceId(source);
            record.getHarvestInfo().setHarvested(false)
                .setUri(null)
                .setUuid(null);
            metadataManager.save(record);
            records.add(record.getId() + "");
        }

        dataManager.indexMetadata(records);

        // Add an harvester history step
        Element historyEl = new Element("result");
        historyEl.addContent(new Element("cleared").
            setAttribute("recordsTransfered", records.size() + ""));
        final String lastRun = new DateTime().withZone(DateTimeZone.forID("UTC")).toString();
        final ISODate lastRunDate = new ISODate(lastRun);

        HarvestHistory history = new HarvestHistory();
        history.setDeleted(true);
        history.setElapsedTime((int) elapsedTime);
        history.setHarvestDate(lastRunDate);
        history.setHarvesterName(harvester.getParams().getName());
        history.setHarvesterType(harvester.getType());
        history.setHarvesterUuid(harvester.getParams().getUuid());
        history.setInfo(historyEl);
        history.setParams(harvester.getParams().getNodeElement());
        historyRepository.save(history);

        return new HttpEntity<>(HttpStatus.NO_CONTENT);
    }



    @ApiOperation(
        value = "Check if a harvester name or host already exist",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "checkHarvesterPropertyExist")
    @RequestMapping(
        value = "/properties/{property}",
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Property does not exist."),
        @ApiResponse(code = 404, message = "A property with that value already exist."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<HttpStatus> checkHarvesterPropertyExist(
        @ApiParam(
            value = "The harvester property to check"
        )
        @PathVariable
            String property,
        @ApiParam(
            value = "The value to search"
        )
        @RequestParam
            String exist,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        final Element list = harvestManager.get(null, context, "site[1]/name[1]");
        if (list.getChildren().stream()
                .filter(h -> h instanceof Element)
                    .map(h -> ((Element) h).getChild("site").getChild(property).getTextTrim())
                    .anyMatch(name -> ((String) name).equalsIgnoreCase(exist))) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
