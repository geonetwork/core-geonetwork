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

package org.fao.geonet.services.metadata.replace;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Class to apply replacements to a metadata selection.
 *
 *  @author Jose García
 */
public class MassiveXslMetadataReindexer extends MetadataIndexerProcessor {
    Iterator<String> iter;
    String process;
    Element params;
    ServiceContext context;
    Set<Integer> metadata;
    MassiveReplaceReport report;

    public MassiveXslMetadataReindexer(DataManager dm, Iterator<String> iter,
                                       String process,
                                       Element params,
                                       ServiceContext context,
                                       Set<Integer> metadata,
                                       MassiveReplaceReport report) {
        super(dm);
        this.iter = iter;
        this.process = process;
        this.params = params;
        this.context = context;
        this.metadata = metadata;
        this.report = report;
    }

    @Override
    public void process() throws Exception {
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        // Build replacements parameter for xslt process
        Element replacements = new Element("replacements");

        if (!StringUtils.isEmpty(params.getChildText("caseinsensitive"))) {
            Element caseInsensitiveEl = new Element("caseInsensitive").setText(params.getChildText("caseinsensitive"));
            replacements.addContent(caseInsensitiveEl);
        }

        List<Element> paramsList = params.getChildren();
        for (Element p: paramsList) {
            if (p.getName().startsWith("mdfield-")) {
                String key = p.getName().split("-")[1];

                String searchValue = params.getChildText("searchValue-" + key);
                String replaceValue = params.getChildText("replaceValue-" + key);

                Element replacement = new Element("replacement");
                replacement.addContent(new Element("field").setText(p.getText()));
                replacement.addContent(new Element("searchValue").setText(searchValue));
                replacement.addContent(new Element("replaceValue").setText(replaceValue));

                replacements.addContent(replacement);

            }
        }

        String replacementsString = Xml.getString(replacements);
        //replacementsString = replacementsString.replaceAll("\\s","");

        while (iter.hasNext()) {
            String uuid = iter.next();
            String id = dm.getMetadataId(uuid);
            context.info("Processing metadata with id:" + id);

            processInternal(id, process, "replacements", replacementsString, context, metadata);
        }
    }

    /**
     * Applies xslt to replace content in metadata record.
     *
     * @param id
     * @param process
     * @param paramNameXml
     * @param paramXml
     * @param context
     * @param metadata
     * @return
     * @throws Exception
     */
    private Element processInternal(String id, String process,
                                    String paramNameXml, String paramXml,
                                    ServiceContext context,
                                    Set<Integer> metadata) throws Exception {

        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        SchemaManager schemaMan = gc.getBean(SchemaManager.class);
        AccessManager accessMan = gc.getBean(AccessManager.class);

        report.incrementProcessedRecords();

        // When a record is deleted the UUID is in the selection manager
        // and when retrieving id, return null
        if (id == null) {
            report.incrementNullRecords();
            return null;
        }

        int iId = Integer.valueOf(id);

        Metadata metadataEntity =  context.getBean(MetadataRepository.class).findOne(iId);
        MetadataDataInfo info = metadataEntity.getDataInfo();

        // Get metadata title from the index
        String metadataTitle = LuceneSearcher.getMetadataFromIndexById(context.getLanguage(), id, "title");
        if (StringUtils.isEmpty(metadataTitle)) metadataTitle = metadataEntity.getUuid();

        if (info == null) {
            MassiveReplaceReportEntry notFoundEntry =
                    new MassiveReplaceReportEntry(id, "", null);
            report.addNotFound(notFoundEntry);

        } else if (!accessMan.isOwner(context, id)) {
            MassiveReplaceReportEntry notOwnerEntry =
                    new MassiveReplaceReportEntry(metadataEntity.getUuid(), metadataTitle, null);
            report.addNotEditable(notOwnerEntry);
        } else {

            // -----------------------------------------------------------------------
            // --- check processing exist for current schema
            String schema = info.getSchemaId();
            String filePath = schemaMan.getSchemaDir(schema) + "/process/" + process + ".xsl";
            File xslProcessing = new File(filePath);
            if (!xslProcessing.exists()) {
                context.info("  Processing instruction not found for " + schema
                        + " schema.");

                MassiveReplaceReportEntry notOwnerEntry =
                        new MassiveReplaceReportEntry(metadataEntity.getUuid(), metadataTitle, null);
                report.addNoProcessFound(notOwnerEntry);

                return null;
            }
            // --- Process metadata
            Element processedMetadata = null;

            try {
                Element md = dataMan.getMetadataNoInfo(context, id);

                // -- here we send parameters set by user from
                // URL if needed.
                List<Element> children = params.getChildren();
                Map<String, Object> xslParameter = new HashMap<String, Object>();
                for (Element param : children) {
                    if (param.getChildren().size() > 0) {
                        xslParameter.put(param.getName(), param);
                    } else {
                        xslParameter.put(param.getName(), param.getTextTrim());

                    }
                }

                processedMetadata = Xml.transformWithXmlParam(md, filePath, paramNameXml, paramXml);

                // Get changes
                Path filePath2 = schemaMan.getSchemaDir(schema).resolve("process/massive-content-update-extract-changes.xsl");
                List<Element> changesEl = Xml.transform(processedMetadata, filePath2).getChildren("change");

                boolean hasChanges = (changesEl.size() > 0);


                MassiveReplaceReportEntry mdEntry = new MassiveReplaceReportEntry(metadataEntity.getUuid(),
                        metadataTitle,
                        changesEl);

                if (hasChanges) {
                    report.addChanged(mdEntry);
                } else {
                    report.addNotChanged(mdEntry);
                }

                // --- save metadata and return status
                if ((changesEl.size() > 0) && (!params.getChildText("test").equalsIgnoreCase("true"))) {
                    Path filePath3 = schemaMan.getSchemaDir(schema).resolve("process/massive-content-update-clean-changes.xsl");

                    // Remove empty elements or vacuum record
                    if (!StringUtils.isEmpty(params.getChildText("vacuum-mode"))) {
                        String mode = params.getChildText("vacuum-mode");
                        // Search and replace, then vacuum record
                        if ("record".equals(mode)) {
                            processedMetadata = Xml.transform(processedMetadata, filePath3);

                            Path vacuumXsltPath = schemaMan.getSchemaDir(schema).resolve("process/vacuum.xsl");
                            if (vacuumXsltPath.toFile().exists()) {
                                processedMetadata = Xml.transform(processedMetadata, vacuumXsltPath);
                            }
                        } else {
                            // Clean geonet:changes elements and remove
                            // elements having an empty new value.
                            Map<String, Object> params = new HashMap<>(1);
                            params.put("removeEmptyElement", "true");
                            processedMetadata = Xml.transform(processedMetadata, filePath3, params);
                        }
                    } else {
                        // Clean geonet:changes elements
                        processedMetadata = Xml.transform(processedMetadata, filePath3);
                    }


                    dataMan.updateMetadata(context, id, processedMetadata,
                            false, true, true,
                            context.getLanguage(),
                            new ISODate().toString(), true);
                }


                metadata.add(new Integer(id));
            } catch (Exception e) {
                report.addMetadataError(iId, e);
                context.error("  Processing failed with error " + e.getMessage());
                e.printStackTrace();
            }

            return processedMetadata;
        }

        return null;
    }
}