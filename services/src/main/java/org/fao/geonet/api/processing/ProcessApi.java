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

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.ProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.kernel.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@RequestMapping(value = {
    "/{portal}/api/processes",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/processes"
})
@Api(value = "processes",
    tags = "processes",
    description = "Processing operations")
@Controller("process")
public class ProcessApi {

    @Autowired
    IProcessingReportRegistry registry;

    @Autowired
    DataManager dataMan;

    @ApiOperation(
        value = "Get current process reports",
        notes = "When processing, the report is stored in memory and allows to retrieve " +
            "progress repport during processing. Usually, process reports are returned by " +
            "the synchronous processing operation.",
        nickname = "getProcessReport")
    @RequestMapping(
        path = "/reports",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of reports returned."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_AUTHENTICATED)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public List<ProcessingReport> getProcessReport() throws Exception {
        return registry.get();
    }

    @ApiOperation(
        value = "Clear process reports list",
        nickname = "deleteProcess")
    @RequestMapping(
        path = "/reports",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Report registry cleared."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_AUTHENTICATED)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete() throws Exception {
        registry.clear();
    }


    @ApiOperation(value = "Search and replace values in one or more records",
        nickname = "searchAndReplace",
        notes = "Service to apply replacements to one or more records." +
            "\n" +
            " To define a replacement, send the following parameters:\n" +
            " * mdsection-139815551372=metadata\n" +
            " * mdfield-1398155513728=id.contact.individualName\n" +
            " * replaceValue-1398155513728=Juan\n" +
            " * searchValue-1398155513728=Jose\n\n" +
            "TODO: Would be good to provide a simple object to define list of changes " +
            "instead of group of parameters.<br/>" +
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
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Replacements applied."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public MetadataReplacementProcessingReport searchAndReplace(
        @RequestParam(
            defaultValue = "massive-content-update")
            String process,
        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(
            value = ApiParams.API_PARAM_PROCESS_TEST_ONLY,
            required = false)
        @RequestParam(defaultValue = "false")
            boolean isTesting,
        @ApiParam(value = "Case insensitive search.",
            required = false)
        @RequestParam(defaultValue = "false")
            boolean isCaseInsensitive,
        @RequestParam(defaultValue = "")
        @ApiParam(value = "'record' to apply vacuum.xsl, " +
            "'element' to remove empty elements. " +
            "Empty to not affect empty elements.",
            required = false)
            String vacuumMode,
        @ApiIgnore
        @RequestParam
            Map<String, String> allParams,
        @ApiIgnore
            HttpSession session,
        @ApiIgnore
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
            m.process();
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }
}
