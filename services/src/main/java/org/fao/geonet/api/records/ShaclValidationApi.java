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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.ApiParams;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping(value = {"/{portal}/api/records"})
@Tag(name = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("shaclValidationApi")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class ShaclValidationApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    ShaclValidationService shaclValidationService;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Validate a record using [SHACL](https://www.w3.org/TR/shacl/)",
        description = "User MUST be able to edit the record to validate it.\n" +
            "\n" +
            "Use a testsuite (preferred) OR define one or more SHACL shapes to validate the record.\n" +
            "Validation is done using the [JENA library](https://jena.apache.org/documentation/shacl/)."
    )
    @RequestMapping(
        value = "/{metadataUuid}/validate/shacl",
        method = {
            RequestMethod.GET,
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            "application/ld+json",
            "text/turtle",
            "application/rdf+xml"
        }
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Validation report."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public @ResponseBody
    String validateRecordUsingShacl(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Formatter to validate",
            examples = {
                @ExampleObject(
                    name = "DCAT",
                    value = "dcat"
                ),
                @ExampleObject(
                    name = "EU DCAT AP",
                    value = "eu-dcat-ap"
                ),
                @ExampleObject(
                    name = "EU DCAT HVD",
                    value = "eu-dcat-ap-hvd"
                )
            },
            required = false)
        @RequestParam(required = false, defaultValue = "dcat")
        String formatter,
        @Parameter(description = "SHACL testsuite to use",
            examples = {
                @ExampleObject(
                    name = "EU DCAT AP 3.0.1 - Base Zero (no background knowledge)",
                    value = "EU DCAT AP 3.0.1 - Base Zero (no background knowledge)"
                ),
                @ExampleObject(
                    name = "EU DCAT AP 3.0.1 - Ranges Zero (no background knowledge)",
                    value = "EU DCAT AP 3.0.1 - Ranges Zero (no background knowledge)"
                ),
                @ExampleObject(
                    name = "EU DCAT AP 3.0.1 - Full (no background knowledge)",
                    value = "EU DCAT AP 3.0.1 - Full (no background knowledge)"
                )
            },
            required = false)
        @RequestParam(required = false) String testsuite,
        @Parameter(description = "SHACL shapes to use", required = false)
        @RequestParam(required = false) List<String> shapeModel,
        @Parameter(description = "Save validation status. When set to true, the validation status will be saved in the database with a validation type set as `shacl-{formatter}-{testsuite_or_shapeshash}`.",
            required = false)
        @RequestParam(required = false, defaultValue = "false") boolean isSavingValidationStatus,
        HttpServletRequest request,
        @Parameter(hidden = true)
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = MediaType.APPLICATION_XML_VALUE)
        String acceptHeader) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        ServiceContext context = ApiUtils.createServiceContext(request);
        return shaclValidationService.validate(formatter, metadata, testsuite, shapeModel, context, acceptHeader, isSavingValidationStatus);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get available SHACL testsuites",
        description = "Returns a list of available SHACL testsuites (configured in `config-shacl-validator.xml`). " +
            "A testsuite is a set of SHACL shapes. " +
            "Rules are common for all schemas and a proper formatter MUST be used to validate metadata (eg. use `eu-dcat-ap` formatter to apply `eu-dcat-ap-300` testsuite). "
    )
    @RequestMapping(
        value = "/{metadataUuid}/validate/shacl/testsuites",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of SHACL testsuites."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public @ResponseBody
    List<String> getShaclTestsuites() throws Exception {
        return shaclValidationService.getShaclValidationTestsuites();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get available SHACL shapes",
        description = "Returns a list of available SHACL shapes (files with .ttl extension in the shacl directory). " +
            "Rules are common for all schemas and a proper formatter MUST be used to validate metadata (eg. use `eu-dcat-ap` formatter to apply `eu-dcat-ap-300` testsuite). "
    )
    @RequestMapping(
        value = "/{metadataUuid}/validate/shacl/testsuites/shapes",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of SHACL rules."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public @ResponseBody
    List<String> getShaclShapes() throws Exception {
        return shaclValidationService.getShaclValidationFiles();
    }
}
