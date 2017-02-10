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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

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
        value = "Preview process result applied to one or more records",
        nickname = "previewProcessRecordsUsingXslt",
        notes = ApiParams.API_OP_NOTE_PROCESS_PREVIEW)
    @RequestMapping(
        value = "/{process}",
        method = RequestMethod.GET,
        produces = {
            MediaType.ALL_VALUE
        })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Processed records."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public Object previewProcessRecords(
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
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = "Append documents before processing",
            required = false,
            example = "false")
        @RequestParam(required = false, defaultValue = "false")
            boolean appendFirst,
        @ApiIgnore
            HttpSession httpSession,
        @ApiIgnore
            HttpServletRequest request,
        @ApiIgnore
            HttpServletResponse response) throws IllegalArgumentException {
        UserSession session = ApiUtils.getUserSession(httpSession);

        XsltMetadataProcessingReport xslProcessingReport =
            new XsltMetadataProcessingReport(process);

        Element preview = new Element("preview");
        StringBuffer output = new StringBuffer();

        boolean isText = process.endsWith(".csv");

        response.setHeader(CONTENT_TYPE, isText ? MediaType.TEXT_PLAIN_VALUE : MediaType.APPLICATION_XML_VALUE);

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
            ApplicationContext context = ApplicationContextHolder.get();
            DataManager dataMan = context.getBean(DataManager.class);
            SchemaManager schemaMan = context.getBean(SchemaManager.class);

            final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();
            Element mergedDocuments = new Element("records");
            String schema = null;
            for (String uuid : records) {
                String id = dataMan.getMetadataId(uuid);
                Log.info("org.fao.geonet.services.metadata",
                    "Processing metadata for preview with id:" + id);


                if (appendFirst) {
                    // Check that all metadata are in the same schema
                    String currentSchema = dataMan.getMetadataSchema(id);
                    if (schema != null && !currentSchema.equals(schema)) {
                        // We can't append and use a mix of schema.
                        throw new IllegalArgumentException(String.format(
                            "When using append mode, process preview cannot process records with different schemas. " +
                                "Record with uuid '%s' as schema '%s'. Select only records in the same schema (ie. '%s')",
                            uuid, currentSchema, schema));
                    } else {
                        schema = currentSchema;
                    }
                    mergedDocuments.addContent(dataMan.getMetadata(id));
                } else {
                    // Save processed metadata
                    if (isText) {
                        output.append(XslProcessUtils.processAsText(ApiUtils.createServiceContext(request),
                            id, process, true,
                            xslProcessingReport, siteURL, request.getParameterMap())
                        );
                    } else {
                        Element record = XslProcessUtils.process(ApiUtils.createServiceContext(request),
                            id, process, true,
                            xslProcessingReport, siteURL, request.getParameterMap());
                        if (record != null) {
                            preview.addContent(record.detach());
                        }
                    }
                }
            }
            if (appendFirst) {
                Path xslProcessing = schemaMan.getSchemaDir(schema).resolve("process").resolve(process + ".xsl");
                if (process.endsWith(".csv")) {
                    StringWriter sw = new StringWriter();
                    Xml.transform(
                        mergedDocuments, xslProcessing,  new StreamResult(sw), null);
                    return sw.toString();
                } else {
                    return Xml.transform(mergedDocuments, xslProcessing);
                }
            }
        } catch (Exception exception) {
            xslProcessingReport.addError(exception);
        } finally {
            xslProcessingReport.close();
        }

        return isText ? output.toString() : preview;
    }

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
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiIgnore
            HttpSession httpSession,
        @ApiIgnore
            HttpServletRequest request) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);

        XsltMetadataProcessingReport xslProcessingReport =
            new XsltMetadataProcessingReport(process);

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
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
