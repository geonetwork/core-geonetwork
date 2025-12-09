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

package org.fao.geonet.api.processing;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.ProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@RequestMapping(value = {
    "/{portal}/api/processes"
})
@Tag(name = "processes",
    description = "Processing operations")
@Controller("process")
public class ProcessApi {

    @Autowired
    IProcessingReportRegistry registry;

    @Autowired
    DataManager dataMan;

    @Autowired
    SettingManager settingManager;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get current process reports",
        description = "When processing, the report is stored in memory and allows to retrieve " +
            "progress repport during processing. Usually, process reports are returned by " +
            "the synchronous processing operation.")
    @RequestMapping(
        path = "/reports",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of reports returned."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_AUTHENTICATED)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public List<ProcessingReport> getProcessReport() throws Exception {
        return registry.get();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Clear process reports list")
    @RequestMapping(
        path = "/reports",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Report registry cleared.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_AUTHENTICATED)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deleteProcessReport() throws Exception {
        registry.clear();
    }


    @Deprecated
    @io.swagger.v3.oas.annotations.Operation(summary = "Search and replace values in one or more ISO19139 records",
        description = "Service to apply replacements to one or more records." +
            "\n" +
            " To define a replacement, send the following parameters:\n" +
            " * mdsection-139815551372=metadata\n" +
            " * mdfield-1398155513728=id.contact.individualName\n" +
            " * replaceValue-1398155513728=Juan\n" +
            " * searchValue-1398155513728=Jose\n\n" +
            "<br/>" +
            "Batch editing can also be used for similar works.")
    @RequestMapping(
        value = "/search-and-replace",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Replacements applied."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public MetadataReplacementProcessingReport searchAndReplace(
        @RequestParam(
            defaultValue = "massive-content-update")
            String process,
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
            description = ApiParams.API_PARAM_PROCESS_TEST_ONLY,
            required = false)
        @RequestParam(defaultValue = "false")
            boolean isTesting,
        @Parameter(description = "Case insensitive search.",
            required = false)
        @RequestParam(defaultValue = "false")
            boolean isCaseInsensitive,
        @RequestParam(defaultValue = "")
        @Parameter(description = "'record' to apply vacuum.xsl, " +
            "'element' to remove empty elements. " +
            "Empty to not affect empty elements.",
            required = false)
            String vacuumMode,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allParams,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        UserSession userSession = ApiUtils.getUserSession(session);

        MetadataReplacementProcessingReport report =
            new MetadataReplacementProcessingReport("massive-content-update");
        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, userSession);

            report.setTotalRecords(records.size());
            MetadataSearchAndReplace m = new MetadataSearchAndReplace(
                dataMan,
                process,
                isTesting, isCaseInsensitive, vacuumMode,
                allParams,
                ApiUtils.createServiceContext(request), records, report);
            m.process(settingManager.getSiteId());
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }
}
