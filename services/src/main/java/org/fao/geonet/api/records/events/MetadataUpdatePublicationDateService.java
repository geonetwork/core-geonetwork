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

package org.fao.geonet.api.records.events;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.processing.XslProcessUtils;
import org.fao.geonet.api.processing.report.Report;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetadataUpdatePublicationDateService {
    public void addPublicationDate(AbstractMetadata md, ISODate publicationDate) {

        ServiceContext context = ServiceContext.get();

        String process = "publicationdate-add";
        if (!hasProcess(context, md, process)) {
            // The record schema does not provide the process (only some schemas
            // such as ISO19115-3 do), so publication date management does not apply.
            return;
        }

        XsltMetadataProcessingReport report =
            new XsltMetadataProcessingReport(process);

        Map<String, String[]> params = new HashMap<>();
        params.put("publicationDate", new String[]{publicationDate.toString()});
        try {
            XslProcessUtils.process(context, String.valueOf(md.getId()), process, true, true, true, report, "", params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        checkForErrors(report, md);
    }

    public void removePublicationDate(AbstractMetadata md) {

        ServiceContext context = ServiceContext.get();

        String process = "publicationdate-remove";
        if (!hasProcess(context, md, process)) {
            // The record schema does not provide the process (only some schemas
            // such as ISO19115-3 do), so publication date management does not apply.
            return;
        }

        XsltMetadataProcessingReport report =
            new XsltMetadataProcessingReport(process);

        Map<String, String[]> params = new HashMap<>();

        try {
            XslProcessUtils.process(context, String.valueOf(md.getId()), process, true, true, true, report, "", params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        checkForErrors(report, md);
    }

    /**
     * The publication date processes are only provided by some schema plugins (eg. ISO19115-3).
     * Records whose schema does not provide the process are skipped so that publishing records
     * of other schemas is not affected.
     */
    private boolean hasProcess(ServiceContext context, AbstractMetadata md, String process) {
        SchemaManager schemaManager = context.getBean(SchemaManager.class);
        String schema = md.getDataInfo().getSchemaId();
        Path xslProcessing = schemaManager.getSchemaDir(schema).resolve("process").resolve(process + ".xsl");
        return Files.exists(xslProcessing);
    }

    /**
     * XslProcessUtils.process() catches its own processing exceptions and records them on the
     * report instead of throwing, so a failure to set/remove the publication date would otherwise
     * pass completely unnoticed. Surface it here so the caller can log/handle it.
     */
    private void checkForErrors(XsltMetadataProcessingReport report, AbstractMetadata md) {
        if (report.getNumberOfRecordsWithErrors() > 0) {
            List<Report> errors = report.getMetadataErrors().values().stream()
                .flatMap(List::stream).collect(Collectors.toList());
            String messages = errors.stream().map(Report::getMessage).collect(Collectors.joining("; "));
            throw new RuntimeException("Failed to process publication date XSL for metadata "
                + md.getId() + ": " + messages);
        }
    }
}
