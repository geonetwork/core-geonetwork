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

package org.fao.geonet.api.harvesting;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.HarvestHistorySpecs;
import org.fao.geonet.services.harvesting.Util;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RequestMapping(value = {
    "/{portal}/api/harvesters"
})
@Tag(name = "harvesters",
    description = "Harvester operations")
@Controller("harvesters")
public class HarvestersApi {

    @Autowired
    HarvestHistoryRepository historyRepository;
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
    private SettingManager settingManager;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Assign harvester records to a new source",
        description = ""
    )
    @PostMapping(
        value = "/{harvesterUuid}/assign",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester records transfered to new source.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public HttpEntity<HttpStatus> assignHarvestedRecordToSource(
        @Parameter(
            description = "The harvester UUID"
        )
        @PathVariable
        String harvesterUuid,
        @Parameter(
            description = "The target source UUID"
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

        if (allHarvestedRecords.isEmpty()) {
            throw new NoResultsFoundException(String.format(
                "Harvester with UUID '%s' has no record to assign to source '%s'.",
                harvesterUuid,
                source));
        }

        for (AbstractMetadata metadataRecord : allHarvestedRecords) {
            metadataRecord.getSourceInfo().setSourceId(source);
            metadataRecord.getHarvestInfo().setHarvested(false)
                .setUri(null)
                .setUuid(null);
            metadataManager.save(metadataRecord);
            records.add(metadataRecord.getId() + "");
        }

        dataManager.indexMetadata(records);

        // Add an harvester history step
        Element historyEl = new Element("result");
        historyEl.addContent(new Element("cleared").
            setAttribute("recordsTransfered", records.size() + ""));
        final ISODate lastRunDate = new ISODate();

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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check if a harvester name or host already exist",
        description = ""
    )
    @GetMapping(
        value = "/properties/{property}"
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Property does not exist."),
        @ApiResponse(responseCode = "404", description = "A property with that value already exist."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<HttpStatus> checkHarvesterPropertyExist(
        @Parameter(
            description = "The harvester property to check"
        )
        @PathVariable
        String property,
        @Parameter(
            description = "The value to search"
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a harvester",
        description = "")
    @DeleteMapping(
        value = "/{harvesterIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> deleteHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element params = new Element("params");
        params.addContent(new Element("id").setText(harvesterIdentifier.toString()));

        Util.exec(params, context, (hm, id) -> hm.remove(id));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Removes the harvester metadata",
        description = "")
    @PutMapping(
        value = "/{harvesterIdentifier}/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester metadata removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> clearHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element params = new Element("params");
        params.addContent(new Element("id").setText(harvesterIdentifier.toString()));

        Util.exec(params, context, (hm, id) -> hm.clearBatch(id));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Activate a harvester",
        description = "")
    @PutMapping(
        value = "/{harvesterIdentifier}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester activated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> startHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element params = new Element("params");
        params.addContent(new Element("id").setText(harvesterIdentifier.toString()));

        Util.exec(params, context, (hm, id) -> hm.start(id));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Deactivate a harvester",
        description = "")
    @PutMapping(
        value = "/{harvesterIdentifier}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester activated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> stopHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        @Parameter(
            description = "Harvester status"
        )
        @RequestParam(required = false)
        String status,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element params = new Element("params");
        params.addContent(new Element("id").setText(harvesterIdentifier.toString()));

        if (StringUtils.isEmpty(status)) {
            status = Common.Status.INACTIVE.toString();
        }

        final Common.Status newStatus = Common.Status.parse(status);
        Util.exec(params, context, (hm, id) -> hm.stop(id, newStatus));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Activate and run a harvester",
        description = "")
    @PutMapping(
        value = "/{harvesterIdentifier}/run")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester executed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> runHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element params = new Element("params");
        params.addContent(new Element("id").setText(harvesterIdentifier.toString()));

        Util.exec(params, context, (hm, id) -> hm.run(id));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create a clone of a harvester",
        description = "")
    @PutMapping(
        value = "/{harvesterIdentifier}/clone")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Harvester cloned."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ResponseEntity<Integer> cloneHarvester(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterIdentifier,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        String newId = context.getBean(HarvestManager.class).createClone(harvesterIdentifier.toString(), context.getUserSession().getUserId(), context);

        if (newId != null) {
            return new ResponseEntity<>(Integer.parseInt(newId), HttpStatus.CREATED);
        } else {
            //--- we get here only if the 'id' was not present or node was not found
            throw new ResourceNotFoundException(String.format(
                "Harvester with identifier '%d' not found. Cannot clone the harvester.",
                harvesterIdentifier));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a harvester history",
        description = "")
    @DeleteMapping(
        value = "/{harvesterUuid}/history")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester history removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<HttpStatus> deleteHarvesterHistory(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        String harvesterUuid
    ) {
        final Specification<HarvestHistory> hasHarvesterUuid = HarvestHistorySpecs.hasHarvesterUuid(harvesterUuid);
        historyRepository.deleteAll(hasHarvesterUuid);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Download a logfile from harvesting",
        description = "")
    @GetMapping(
        value = "/{harvesterHistoryIdentifier}/log",
        produces = {
            "text/plain"
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Log file returned."),
        @ApiResponse(responseCode = "400", description = "Bad parameters."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void getLog(
        @Parameter(
            description = "Harvester identifier",
            required = true
        )
        @PathVariable
        Integer harvesterHistoryIdentifier,
        HttpServletResponse response) throws IOException, JDOMException, ResourceNotFoundException {

        Optional<HarvestHistory> harvestHistoryOptional = historyRepository.findById(harvesterHistoryIdentifier);
        String logFile = "";

        if (harvestHistoryOptional.isPresent()) {
            Element info = harvestHistoryOptional.get().getInfoAsXml();
            logFile = info.getChildText("logfile");
        }

        if (StringUtils.isEmpty(logFile)) {
            throw new ResourceNotFoundException(
                "Couldn't find or read the logfile in catalogue log directory for the harvester history entry. Check log file configuration.");
        }

        File mainLogFile = GeonetworkDataDirectory.getLogfile();
        Path pathLogFile;
        if (mainLogFile != null) {
            pathLogFile = mainLogFile.toPath().getParent().resolve(logFile);
        }
        else {
            pathLogFile = Paths.get(logFile);
        }

        if (!Files.exists(pathLogFile) || !Files.isReadable(pathLogFile)) {
            throw new ResourceNotFoundException(String.format(
                "Couldn't find or read the logfile %s in catalogue log directory. Check log file configuration.",
                logFile));
        }

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment;filename=" + logFile);
        ServletOutputStream out = response.getOutputStream();
        try (BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(pathLogFile.toFile()), StandardCharsets.UTF_8))) {
            IOUtils.copy(reader1, out, StandardCharsets.UTF_8);
        } finally {
            out.flush();
            out.close();
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "List harvesters",
        description = ""
    )
    @GetMapping(
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of harvesters."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public String list(
        @Parameter(
            description = "The harvesters identifiers"
        )
        @RequestParam(name = "id", required = false)
        Integer[] harvesterIds,
        @Parameter(
            description = "Return only information"
        )
        @RequestParam(required = false, defaultValue = "false")
        Boolean onlyInfo,
        @RequestParam(required = false, defaultValue = "site[1]/name[1]")
        String sortField,
        HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);

        //--- if 'id' is null all entries are returned
        String[] disabledTypes = StringUtils.split(
            StringUtils.defaultIfBlank(
                settingManager.getValue(Settings.SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES),
                "").toLowerCase().replace(',', ' '),
            " ");

        List<Integer> ids;
        if (harvesterIds.length == 0) {
            ids = Collections.singletonList(-1);
        } else {
            ids = Arrays.asList(harvesterIds);
        }
        Element result = new Element("nodes");
        for (Integer id : ids) {
            Element node = harvestManager.get(String.valueOf(id), context, sortField);

            if (node != null) {
                if (id == -1) {
                    List<Element> childNodes = node.getChildren();
                    for (Element childNode : childNodes) {
                        String harvesterType = childNode.getAttributeValue("type");
                        if (Arrays.stream(disabledTypes).noneMatch(disabledType -> disabledType.equalsIgnoreCase(harvesterType))) {
                            result.addContent((Content) childNode.clone());
                        }
                    }
                } else {
                    String harvesterType = node.getAttributeValue("type");
                    if (Arrays.stream(disabledTypes).noneMatch(disabledType -> disabledType.equalsIgnoreCase(harvesterType))) {
                        result.addContent(node.detach());
                    }
                }
            } else {
                throw new ObjectNotFoundEx("No Harvester found with id: " + id);
            }
        }

        if (onlyInfo) {
            removeAllDataExceptInfo(result);
        }

        return Xml.getJSON(result);
    }

    private void removeAllDataExceptInfo(Element node) {
        final List<Element> toRemove = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final List<Element> children = node.getChildren();

        for (Element harvesters : children) {
            @SuppressWarnings("unchecked")
            final List<Element> harvesterInfo = harvesters.getChildren();
            for (Element element : harvesterInfo) {
                if (!element.getName().equalsIgnoreCase("info") && !element.getName().equalsIgnoreCase("error")) {
                    toRemove.add(element);
                }
            }
        }

        for (Element element : toRemove) {
            element.detach();
        }
    }
}
