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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.ProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.APIPARAM_RECORD_UUIDS_OR_SELECTION;

@RequestMapping(value = {
    "/api/processes",
    "/api/" + API.VERSION_0_1 +
        "/processes"
})
@Api(value = "processes",
    tags = "processes",
    description = "Processing operations")
@Controller("process")
public class ProcessApi {

    @Autowired
    IProcessingReportRegistry registry;

    @ApiOperation(value = "Get current process reports",
        nickname = "getProcess")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<ProcessingReport> get() throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();
        if (profile == null) {
            throw new SecurityException(
                "You are not allowed to retrieve processing reports.");
        }

        return registry.get();
    }

    @ApiOperation(value = "Clear process reports list",
        nickname = "deleteProcess")
    @RequestMapping(
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    void delete() throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();
        if (profile == null) {
            throw new SecurityException(
                "You are not allowed to retrieve processing reports.");
        }

        registry.clear();
    }


    @ApiOperation(value = "Search and replace process",
        nickname = "searchAndReplace",
        notes = "Service to apply replacements to one or more records." +
            "\n" +
            " To define a replacement, send the following parameters:\n" +
            " * mdsection-139815551372=metadata\n" +
            " * mdfield-1398155513728=id.contact.individualName\n" +
            " * replaceValue-1398155513728=Juan\n" +
            " * searchValue-1398155513728=Jose\n\n" +
            "TODO: Would be good to provide a simple object to define list of changes " +
            "instead of group of parameters.")
    @RequestMapping(
        value = "/search-and-replace",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public MetadataReplacementProcessingReport searchAndReplace(
        @RequestParam(defaultValue = "massive-content-update")
            String process,
        @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(value = "Test only (ie. metadata are not saved). Return the report only.",
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
            Map<String, String> allParams
    ) throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();
        if (profile == null) {
            throw new SecurityException(
                "You are not allowed to run a search and replace process.");
        }

        MetadataReplacementProcessingReport report =
            new MetadataReplacementProcessingReport("massive-content-update");
        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            DataManager dataMan = applicationContext.getBean(DataManager.class);

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, context.getUserSession());

            report.setTotalRecords(records.size());
            MetadataSearchAndReplace m = new MetadataSearchAndReplace(
                dataMan,
                process,
                isTesting, isCaseInsensitive, vacuumMode,
                allParams,
                context, records, report);
            m.process();
        } catch (Exception e) {
            throw e;
        } finally {
            report.close();
        }
        return report;
    }
}
