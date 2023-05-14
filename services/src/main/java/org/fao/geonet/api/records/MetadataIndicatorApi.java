package org.fao.geonet.api.records;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataIndicator;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataIndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordIndicators")
@ReadWriteController
public class MetadataIndicatorApi {
    public static final String API_PARAM_INDICATORS = "Indicators";

    @Autowired
    MetadataIndicatorRepository metadataIndicatorRepository;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    IMetadataIndexer metadataIndexer;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record indicators",
        description = "Indicators are used to add additiona information to the metadata like quality information to generate statistics.")
    @RequestMapping(
        value = "/{metadataUuid}/indicators",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record indicators."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Set<MetadataIndicator> getRecordIndicators(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        return metadata.getIndicators();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add indicators to a record",
        description = "")
    @RequestMapping(
        value = "/{metadataUuid}/indicators",
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Record indicators added."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public void addRecordIndicators(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = API_PARAM_INDICATORS,
            required = true
        )
        @RequestParam
            Map<String, String> indicators,
        @Parameter(
            description = ApiParams.API_PARAM_CLEAR_ALL_BEFORE_INSERT,
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean clear,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        if (clear) {
            metadataManager.update(
                metadata.getId(), entity -> entity.getIndicators().clear());
        }

        indicators.forEach((key, value) -> {
            Optional<MetadataIndicator> indicator = metadata.getIndicators().stream().filter(i -> i.getName().equals(key)).findFirst();

            if (indicator.isPresent()) {
                indicator.get().setValue(value);
            } else {
                MetadataIndicator newIndicator = new MetadataIndicator().setName(key).setValue(value);
                metadata.getIndicators().add(newIndicator);

            }

        });

        metadataManager.save(metadata);
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), true, IndexingMode.full);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete indicators of a record",
        description = "")
    @RequestMapping(
        value = "/{metadataUuid}/indicators",
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record indicators removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public void deleteIndicators(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Indicator identifiers. If none, all indicators are removed.",
            required = false
        )
        @RequestParam(required = false)
            String[] indicators,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        if (indicators == null || indicators.length == 0) {
            metadataManager.update(
                metadata.getId(), entity -> entity.getIndicators().clear());
        }

        for (String indicator : indicators) {
            Optional<MetadataIndicator> mdIndicator = metadata.getIndicators().stream().filter(i -> i.getName().equals(indicator)).findFirst();

            if (mdIndicator.isPresent()) {
                metadata.getIndicators().remove(mdIndicator);
            }
        }

        metadataManager.save(metadata);
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), true, IndexingMode.full);
    }
}
