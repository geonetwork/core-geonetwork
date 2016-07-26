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
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

/**
 * Process a metadata with an XSL transformation declared for the metadata schema. Parameters sent
 * to the service are forwaded to XSL process.
 *
 * In each xml/schemas/schemaId directory, a process could be added in a directory called process.
 * Then the process could be called using the following URL : http://localhost:8080/geonetwork/srv/eng/md.processing.batch
 * ?process=keywords-comma-exploder&url=http://xyz
 *
 * In that example the process has to be named keywords-comma-exploder.xsl.
 *
 * To retrieve parameters in XSL process use the following: <code> <xsl:param
 * name="url">http://localhost:8080/</xsl:param> </code>
 *
 * @author fxprunayre
 */
@RequestMapping(value = {
    "/api/processes",
    "/api/" + API.VERSION_0_1 +
        "/processes"
})
@Api(value = "processes",
    tags = "processes",
    description = "Processing operations")
@Controller("xslprocess")
@ReadWriteController
public class XslProcessApi {

    @ApiOperation(
        value = "Apply a process to one or more records",
        nickname = "processRecordsUsingXslt",
        notes = ApiParams.API_OP_NOTE_PROCESS)
    @RequestMapping(
        value = "/{process}",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Report about processed records."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public XsltMetadataProcessingReport processRecords(
        @ApiParam(
            value = ApiParams.API_PARAM_PROCESS_ID
        )
        @PathVariable
            String process,
        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiIgnore
            HttpSession httpSession,
        @ApiIgnore
            HttpServletRequest request) throws Exception {

        UserSession session = ApiUtils.getUserSession(httpSession);

        XsltMetadataProcessingReport xslProcessingReport =
            new XsltMetadataProcessingReport(process);

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, session);
            ApplicationContext context = ApplicationContextHolder.get();
            DataManager dataMan = context.getBean(DataManager.class);

            final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();

            xslProcessingReport.setTotalRecords(records.size());

            BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(
                ApiUtils.createServiceContext(request),
                dataMan, records, process, httpSession, siteURL,
                xslProcessingReport, request);
            m.process();

        } catch (Exception exception) {
            xslProcessingReport.addError(exception);
        } finally {
            xslProcessingReport.close();
        }

        return xslProcessingReport;
    }

    static final class BatchXslMetadataReindexer extends
        MetadataIndexerProcessor {
        Set<String> records;
        String process;
        String siteURL;
        HttpSession session;
        XsltMetadataProcessingReport xslProcessingReport;
        HttpServletRequest request;
        ServiceContext context;

        public BatchXslMetadataReindexer(ServiceContext context,
                                         DataManager dm,
                                         Set<String> records,
                                         String process,
                                         HttpSession session,
                                         String siteURL,
                                         XsltMetadataProcessingReport xslProcessingReport,
                                         HttpServletRequest request) {
            super(dm);
            this.records = records;
            this.process = process;
            this.session = session;
            this.siteURL = siteURL;
            this.request = request;
            this.xslProcessingReport = xslProcessingReport;
            this.context = context;
        }

        @Override
        public void process() throws Exception {
            for (String uuid : this.records) {
                String id = getDataManager().getMetadataId(uuid);
                Log.info("org.fao.geonet.services.metadata",
                    "Processing metadata with id:" + id);

                XslProcessUtils.process(context, id, process,
                    true, xslProcessingReport, siteURL, request.getParameterMap());
            }
        }
    }
}
