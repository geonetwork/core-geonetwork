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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.events.history.RecordProcessingChangeEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
 * <p>
 * In each xml/schemas/schemaId directory, a process could be added in a directory called process.
 * Then the process could be called using the following URL : http://localhost:8080/geonetwork/srv/eng/md.processing.batch
 * ?process=keywords-comma-exploder&url=http://xyz
 * <p>
 * In that example the process has to be named keywords-comma-exploder.xsl.
 * <p>
 * To retrieve parameters in XSL process use the following: <code> <xsl:param
 * name="url">http://localhost:8080/</xsl:param> </code>
 *
 * @author fxprunayre
 */
@RequestMapping(value = {
    "/{portal}/api/processes",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/processes"
})
@Tag(name = "processes",
    description = "Processing operations")
@Controller("xslprocess")
@ReadWriteController
public class XslProcessApi {

    @Autowired
    DataManager dataMan;

    @Autowired
    SchemaManager schemaMan;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Preview process result applied to one or more records",
        description = ApiParams.API_OP_NOTE_PROCESS_PREVIEW +
            " When errors occur during processing, the processing report is returned in JSON format.")
    @RequestMapping(
        value = "/{process}",
        method = RequestMethod.GET,
        consumes = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.ALL_VALUE
        })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processed records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public Object previewProcessRecords(
        @Parameter(
            description = ApiParams.API_PARAM_PROCESS_ID
        )
        @PathVariable
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
        @Parameter(description = "Append documents before processing",
            required = false,
            example = "false")
        @RequestParam(required = false, defaultValue = "false")
            boolean appendFirst,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
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
                            id, process, false,
                            xslProcessingReport, siteURL, request.getParameterMap())
                        );
                    } else {
                        Element record = XslProcessUtils.process(ApiUtils.createServiceContext(request),
                            id, process, false, false,
                            false, xslProcessingReport, siteURL, request.getParameterMap());
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
                        mergedDocuments, xslProcessing, new StreamResult(sw), null);
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

        // In case of errors during processing return report.
        if (xslProcessingReport.getErrors().size() > 0) {
            response.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(xslProcessingReport);
            } catch (JsonProcessingException errorReportException) {
                return String.format("Failed to generate error report due to '%s'.",
                    errorReportException.getMessage());
            }
        }

        return isText ? output.toString() : preview;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Apply a process to one or more records",
        description = ApiParams.API_OP_NOTE_PROCESS)
    @RequestMapping(
        value = "/{process}",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about processed records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public XsltMetadataProcessingReport processRecords(
        @Parameter(
            description = ApiParams.API_PARAM_PROCESS_ID
        )
        @PathVariable
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
            description = ApiParams.API_PARAM_UPDATE_DATESTAMP,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "true"
        )
            boolean updateDateStamp,
        @Parameter(description = "Index after processing",
            required = false,
            example = "false")
        @RequestParam(required = false, defaultValue = "true")
            boolean index,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);

        XsltMetadataProcessingReport xslProcessingReport =
            new XsltMetadataProcessingReport(process);

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
            UserSession userSession = ApiUtils.getUserSession(httpSession);

            final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();

            xslProcessingReport.setTotalRecords(records.size());

            BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(
                ApiUtils.createServiceContext(request),
                dataMan, records, process, httpSession, siteURL,
                xslProcessingReport, request, index, updateDateStamp, userSession.getUserIdAsInt());
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
        private final boolean index;
        private final boolean updateDateStamp;
        Set<String> records;
        String process;
        String siteURL;
        HttpSession session;
        XsltMetadataProcessingReport xslProcessingReport;
        HttpServletRequest request;
        ServiceContext context;
        int userId;

        public BatchXslMetadataReindexer(ServiceContext context,
                                         DataManager dm,
                                         Set<String> records,
                                         String process,
                                         HttpSession session,
                                         String siteURL,
                                         XsltMetadataProcessingReport xslProcessingReport,
                                         HttpServletRequest request, boolean index,
                                         boolean updateDateStamp, int userId) {
            super(dm);
            this.records = records;
            this.process = process;
            this.session = session;
            this.index = index;
            this.updateDateStamp = updateDateStamp;
            this.siteURL = siteURL;
            this.request = request;
            this.xslProcessingReport = xslProcessingReport;
            this.context = context;
            this.userId = userId;
        }

        @Override
        public void process() throws Exception {
            DataManager dataMan = context.getBean(DataManager.class);
            ApplicationContext appContext = ApplicationContextHolder.get();
            for (String uuid : this.records) {
                String id = getDataManager().getMetadataId(uuid);
                Log.info("org.fao.geonet.services.metadata",
                    "Processing metadata with id:" + id);

                Element beforeMetadata = dataMan.getMetadata(context, id, false, false, false);

                XslProcessUtils.process(context, id, process,
                    true, index, updateDateStamp, xslProcessingReport,
                    siteURL, request.getParameterMap());

                Element afterMetadata = dataMan.getMetadata(context, id, false, false, false);

                XMLOutputter outp = new XMLOutputter();
                String xmlAfter = outp.outputString(afterMetadata);
                String xmlBefore = outp.outputString(beforeMetadata);
                new RecordProcessingChangeEvent(Long.parseLong(id), this.userId, xmlBefore, xmlAfter, process).publish(appContext);
            }
        }
    }
}
