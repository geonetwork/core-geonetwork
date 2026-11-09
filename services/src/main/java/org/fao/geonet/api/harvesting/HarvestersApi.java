/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.LogoUtils;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.specification.HarvestHistorySpecs;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.fao.oaipmh.exceptions.NoSetHierarchyException;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.fao.oaipmh.requests.ListSetsRequest;
import org.fao.oaipmh.responses.ListSetsResponse;
import org.fao.geonet.constants.Geonet;
import org.fao.oaipmh.responses.SetInfo;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SourceRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequestMapping(value = {
    "/{portal}/api/harvesters"
})
@Tag(name = "harvesters",
    description = "Harvester operations")
@Controller("harvesters")
@ReadWriteController
public class HarvestersApi {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern HARVESTER_LOG_FILE = Pattern.compile("harvester_[\\w.-]+\\.log");
    private static final List<String> ICON_EXTENSIONS = Arrays.asList(".gif", ".png", ".jpg", ".jpeg");

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
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private GeonetHttpRequestFactory requestFactory;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Assign harvester records to a new source",
        description = ""
//        authorizations = {
//            @Authorization(value = "basicAuth")
//        })
    )
    @RequestMapping(
        value = "/{harvesterUuid}/assign",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Harvester records transfered to new source.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity<Void> assignHarvestedRecordToSource(
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

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get harvester logo image.",
        description = LogoUtils.API_GET_LOGO_NOTE
    )
    @GetMapping(
        value = "/{harvesterUuid}/logo"
    )
    public void getHarvesterLogo(
        @Parameter(
            description = "The harvester UUID"
        )
        @PathVariable
        String harvesterUuid,
        @Parameter(hidden = true) WebRequest webRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ResourceNotFoundException, IOException {
        Source source = sourceRepository.findOneByUuid(harvesterUuid);
        if (source == null || source.getType() != SourceType.harvester) {
            throw new ResourceNotFoundException(String.format(
                "Harvester with UUID '%s' not found.",
                harvesterUuid));
        }

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        Resources resources = serviceContext.getBean(Resources.class);
        try (Resources.ResourceHolder image = LogoUtils.getImage(resources, serviceContext, source.getLogo())) {
            LogoUtils.writeImageOrTransparentLogo(webRequest, response, image);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
            summary = "Reindexes all records of an harvester",
            description = ""
    )
    @RequestMapping(
            value = "/{harvesterUuid}/reindex",
            method = RequestMethod.POST
    )
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reindexing was successful"),
            @ApiResponse(responseCode = "409", description = "Reindexing already running"),
            @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<Void> reindexHarvester(
            @Parameter(
                    description = "The harvester UUID"
            )
            @PathVariable
            String harvesterUuid) throws Exception {
        final AbstractHarvester<?, ?> harvester = harvestManager.getHarvester(harvesterUuid);
        if (harvester == null) {
            throw new ResourceNotFoundException(String.format(
                    "Harvester with UUID '%s' not found. Cannot reindex harvester.",
                    harvesterUuid));
        }
        var successful = harvester.reindex();
        if (successful) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check if a harvester name or host already exist",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/properties/{property}",
        method = RequestMethod.GET
    )
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Property does not exist."),
        @ApiResponse(responseCode = "404", description = "A property with that value already exist."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<Void> checkHarvesterPropertyExist(
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
        summary = "Get harvesters",
        description = "Summary of the harvesters visible to the user. Harvester definitions are returned " +
            "as converted from their XML configuration.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ArrayNode getHarvesters(HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        ArrayNode harvesters = JSON.createArrayNode();
        for (Element node : getChildren(harvestManager.get("-1", context, "site[1]/name[1]"))) {
            if (!isDisabledType(node.getAttributeValue("type"))) {
                harvesters.add(toJson(node));
            }
        }
        return harvesters;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get the status of harvesters",
        description = "Returns only the run information and the last error of each harvester. Unknown harvesters are skipped.")
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ArrayNode getHarvestersStatus(
        @Parameter(description = "The harvester UUIDs") @RequestParam List<String> uuid,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        ArrayNode status = JSON.createArrayNode();
        for (String harvesterUuid : uuid) {
            AbstractHarvester<?, ?> harvester = harvestManager.getHarvester(harvesterUuid);
            Element node = harvester == null ? null : harvestManager.get(harvester.getID(), context, null);
            if (node != null) {
                getChildren(node).stream()
                    .filter(child -> !child.getName().equals("info") && !child.getName().equals("error"))
                    .collect(Collectors.toList())
                    .forEach(Element::detach);
                status.add(toJson(node));
            }
        }
        return status;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a harvester definition")
    @GetMapping(value = "/{harvesterUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public JsonNode getHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element node = harvestManager.get(getVisibleHarvesterId(harvesterUuid, context), context, null);
        if (isDisabledType(node.getAttributeValue("type"))) {
            throw new ResourceNotFoundException(String.format("Harvester with UUID '%s' not found.", harvesterUuid));
        }
        return toJson(node);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a harvester",
        description = "The harvester definition is the XML configuration of the harvester type.")
    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Map<String, String> addHarvester(
        @RequestBody String definition,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        String id = harvestManager.addHarvesterReturnId(Xml.loadString(definition, false), context.getUserSession().getUserId());
        return harvesterIdentifiers(id, context);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Update a harvester")
    @PutMapping(value = "/{harvesterUuid}", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        @RequestBody String definition,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Element node = Xml.loadString(definition, false);
        node.setAttribute("id", getVisibleHarvesterId(harvesterUuid, context));
        if (!harvestManager.update(node, context.getUserSession().getUserId())) {
            throw new ResourceNotFoundException(String.format("Harvester with UUID '%s' not found.", harvesterUuid));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Clone a harvester")
    @PostMapping(value = "/{harvesterUuid}/clone", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Map<String, String> cloneHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        String id = harvestManager.createClone(getVisibleHarvesterId(harvesterUuid, context),
            context.getUserSession().getUserId(), context);
        if (id == null) {
            throw new ResourceNotFoundException(String.format("Harvester with UUID '%s' not found.", harvesterUuid));
        }
        return harvesterIdentifiers(id, context);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Remove a harvester")
    @DeleteMapping(value = "/{harvesterUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Map<String, String>> removeHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        return operationResult(harvestManager.remove(getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request))));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Remove the records harvested by a harvester")
    @DeleteMapping(value = "/{harvesterUuid}/records", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Map<String, String>> clearHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        return operationResult(harvestManager.clearBatch(getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request))));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Activate the harvester schedule")
    @PostMapping(value = "/{harvesterUuid}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Map<String, String>> startHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        return operationResult(harvestManager.start(getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request))));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Stop a harvester",
        description = "Stops the running harvest and sets the harvester status (inactive by default).")
    @PostMapping(value = "/{harvesterUuid}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Map<String, String>> stopHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        @Parameter(description = "active or inactive") @RequestParam(defaultValue = "inactive") String status,
        HttpServletRequest request) throws Exception {
        return operationResult(harvestManager.stop(getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request)),
            Common.Status.parse(status)));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Run a harvester now")
    @PostMapping(value = "/{harvesterUuid}/run", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Map<String, String>> runHarvester(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        return operationResult(harvestManager.run(getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request))));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get the harvest history of a harvester",
        description = "Most recent first. History entries are returned as converted from their XML.")
    @GetMapping(value = "/{harvesterUuid}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public Map<String, Object> getHarvesterHistory(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        @Parameter(description = "Page number, from 0") @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request) throws Exception {
        getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request));
        Specification<HarvestHistory> spec = HarvestHistorySpecs.hasHarvesterUuid(harvesterUuid);
        Sort sort = Sort.by(Sort.Order.desc(HarvestHistory_.harvestDate.getName()), Sort.Order.asc(HarvestHistory_.harvesterUuid.getName()));
        long total = historyRepository.count(spec);

        ArrayNode history = JSON.createArrayNode();
        for (Element record : getChildren(historyRepository.findAllAsXml(spec, PageRequest.of(page, size, sort)))) {
            history.add(toJson(record));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        result.put("pages", (total + size - 1) / size);
        result.put("history", history);
        return result;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete the harvest history of a harvester")
    @DeleteMapping(value = "/{harvesterUuid}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public Map<String, Long> deleteHarvesterHistory(
        @Parameter(description = "The harvester UUID") @PathVariable String harvesterUuid,
        HttpServletRequest request) throws Exception {
        getVisibleHarvesterId(harvesterUuid, ApiUtils.createServiceContext(request));
        Specification<HarvestHistory> spec = HarvestHistorySpecs.hasHarvesterUuid(harvesterUuid);
        long deleted = historyRepository.count(spec);
        historyRepository.deleteAll(spec);
        return Map.of("deleted", deleted);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a harvester log file",
        description = "Log files written by harvesters (harvester_*.log) in the catalogue log directory or one of its subdirectories.")
    @GetMapping(value = "/logs")
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<FileSystemResource> getHarvesterLog(
        @Parameter(description = "The log file, relative to the catalogue log directory", example = "harvester_csw_mycatalogue_20261031120000.log")
        @RequestParam String file) throws Exception {
        java.io.File mainLogFile = GeonetworkDataDirectory.getLogfile();
        if (mainLogFile == null) {
            throw new ResourceNotFoundException(String.format("Harvester log file '%s' not found.", file));
        }
        Path logDir = mainLogFile.toPath().getParent().toAbsolutePath().normalize();
        Path log = logDir.resolve(file).normalize();
        if (!log.startsWith(logDir) || !HARVESTER_LOG_FILE.matcher(log.getFileName().toString()).matches()
            || !Files.isReadable(log)) {
            throw new ResourceNotFoundException(String.format("Harvester log file '%s' not found.", file));
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + log.getFileName() + "\"")
            .contentType(MediaType.TEXT_PLAIN)
            .body(new FileSystemResource(log));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the harvester types that can be used")
    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public List<String> getHarvesterTypes(HttpServletRequest request) {
        return Arrays.asList(AbstractHarvester.getNonDisabledHarvesterTypes(ApiUtils.createServiceContext(request)));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the icons that can be used for harvesters")
    @GetMapping(value = "/icons", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public List<String> getHarvesterIcons(HttpServletRequest request) {
        ServiceContext context = ApiUtils.createServiceContext(request);
        DirectoryStream.Filter<Path> isIcon = icon -> Files.isRegularFile(icon)
            && ICON_EXTENSIONS.stream().anyMatch(ext -> icon.getFileName().toString().endsWith(ext));
        return context.getBean(Resources.class).listFiles(context, "harvesting", isIcon).stream()
            .map(icon -> icon.getFileName().toString())
            .sorted()
            .collect(Collectors.toList());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the validation options for harvested records")
    @GetMapping(value = "/validation-options", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public List<String> getValidationOptions() {
        return Arrays.stream(HarvestValidationEnum.values()).map(Enum::name).collect(Collectors.toList());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get the metadata formats and sets of an OAI-PMH server",
        description = "Used to configure an OAI-PMH harvester.")
    @GetMapping(value = "/oaipmh/server", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public Map<String, Object> getOaiPmhServerInfo(
        @Parameter(description = "The OAI-PMH server URL") @RequestParam String url,
        HttpServletRequest request) throws Exception {
        if (!Lib.net.isUrlValid(url)) {
            throw new IllegalArgumentException(String.format("Invalid OAI-PMH server URL '%s'.", url));
        }
        ServiceContext context = ApiUtils.createServiceContext(request);
        Path oaiSchema = dataDirectory.getWebappDir().resolve("xml/validation/oai/OAI-PMH.xsd");

        ListMetadataFormatsRequest formatsRequest = new ListMetadataFormatsRequest(requestFactory);
        formatsRequest.setSchemaPath(oaiSchema);
        setupOaiRequest(formatsRequest.getTransport(), url, context);
        List<String> formats = new ArrayList<>();
        formatsRequest.execute().getFormats().forEach(format -> formats.add(format.prefix));

        List<Map<String, String>> sets = new ArrayList<>();
        try {
            ListSetsRequest setsRequest = new ListSetsRequest(requestFactory);
            setsRequest.setSchemaPath(oaiSchema);
            setupOaiRequest(setsRequest.getTransport(), url, context);
            ListSetsResponse response = setsRequest.execute();
            while (response.hasNext()) {
                SetInfo set = response.next();
                Map<String, String> item = new LinkedHashMap<>();
                item.put("name", set.getSpec());
                item.put("label", set.getName());
                sets.add(item);
            }
        } catch (NoSetHierarchyException e) {
            // The server does not support sets
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formats", formats);
        result.put("sets", sets);
        return result;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get the stylesheets converting harvested fragments",
        description = "Stylesheets provided by the standards for the WFS GetFeature or THREDDS harvesters.")
    @GetMapping(value = "/stylesheets/fragments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public List<Map<String, String>> getFragmentStylesheets(
        @Parameter(description = "wfs or thredds") @RequestParam String type,
        @Parameter(description = "Only for this standard") @RequestParam(required = false) String schema) throws Exception {
        String folder;
        if ("wfs".equals(type)) {
            folder = Geonet.Path.WFS_STYLESHEETS;
        } else if ("thredds".equals(type)) {
            folder = Geonet.Path.TDS_STYLESHEETS;
        } else {
            throw new IllegalArgumentException(String.format("Unknown type '%s'. Use wfs or thredds.", type));
        }

        List<Map<String, String>> stylesheets = new ArrayList<>();
        for (String schemaName : schemaManager.getSchemas()) {
            Path xslDir = schemaManager.getSchemaDir(schemaName).resolve(folder);
            if ((schema != null && !schema.isEmpty() && !schema.equals(schemaName)) || !Files.isDirectory(xslDir)) {
                continue;
            }
            try (DirectoryStream<Path> sheets = Files.newDirectoryStream(xslDir, "*.xsl")) {
                for (Path sheet : sheets) {
                    String id = sheet.getFileName().toString();
                    Map<String, String> stylesheet = new LinkedHashMap<>();
                    stylesheet.put("id", id);
                    stylesheet.put("name", id.substring(0, id.length() - ".xsl".length()));
                    stylesheet.put("schema", schemaName);
                    stylesheets.add(stylesheet);
                }
            }
        }
        return stylesheets;
    }

    /**
     * @return the harvester id, if the harvester exists and the user can see it (owner group).
     */
    private String getVisibleHarvesterId(String harvesterUuid, ServiceContext context) throws Exception {
        AbstractHarvester<?, ?> harvester = harvestManager.getHarvester(harvesterUuid);
        if (harvester == null || harvestManager.get(harvester.getID(), context, null) == null) {
            throw new ResourceNotFoundException(String.format("Harvester with UUID '%s' not found.", harvesterUuid));
        }
        return harvester.getID();
    }

    private Map<String, String> harvesterIdentifiers(String id, ServiceContext context) throws Exception {
        Map<String, String> identifiers = new LinkedHashMap<>();
        identifiers.put("id", id);
        Element node = harvestManager.get(id, context, null);
        identifiers.put("uuid", node == null ? null : node.getChild("site").getChildText("uuid"));
        return identifiers;
    }

    private ResponseEntity<Map<String, String>> operationResult(Common.OperResult result) {
        HttpStatus status = result == Common.OperResult.NOT_FOUND ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(Map.of("status", result.toString()));
    }

    private boolean isDisabledType(String type) {
        String disabledTypes = settingManager.getValue(Settings.SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES);
        return disabledTypes != null && Arrays.stream(disabledTypes.toLowerCase().split("[,\\s]+"))
            .anyMatch(disabled -> disabled.equalsIgnoreCase(type));
    }

    private void setupOaiRequest(XmlRequest transport, String url, ServiceContext context) throws Exception {
        transport.setUrl(new URL(url));
        Lib.net.setupProxy(context, transport);
    }

    @SuppressWarnings("unchecked")
    private static List<Element> getChildren(Element element) {
        return new ArrayList<>(element.getChildren());
    }

    private static JsonNode toJson(Element element) throws IOException {
        return JSON.readTree(Xml.getJSON(element));
    }
}
