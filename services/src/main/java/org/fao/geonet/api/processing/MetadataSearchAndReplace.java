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

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.events.history.RecordProcessingChangeEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Class to apply replacements to a metadata selection.
 *
 * @author Jose García
 */
public class MetadataSearchAndReplace extends MetadataIndexerProcessor {
    String process;
    Map<String, String> params;
    ServiceContext context;
    Set<String> metadata;
    MetadataReplacementProcessingReport report;
    boolean isTesting;
    boolean isCaseInsensitive;
    String vacuumMode;

    public MetadataSearchAndReplace(DataManager dm,
                                    String process,
                                    boolean isTesting,
                                    boolean isCaseInsensitive,
                                    String vacuumMode, Map<String, String> params,
                                    ServiceContext context,
                                    Set<String> records,
                                    MetadataReplacementProcessingReport report) {
        super(dm);
        this.process = process;
        this.params = params;
        this.context = context;
        this.metadata = records;
        this.report = report;
        this.isTesting = isTesting;
        this.isCaseInsensitive = isCaseInsensitive;
        this.vacuumMode = vacuumMode;
    }

    @Override
    public void process() throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        // Build replacements parameter for xslt process
        Element replacements = new Element("replacements");

        if (isCaseInsensitive) {
            Element caseInsensitiveEl =
                new Element("caseInsensitive").setText("true");
            replacements.addContent(caseInsensitiveEl);
        }

        Iterator iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            if (entry.getKey().startsWith("mdfield-")) {
                String key = entry.getKey().split("-")[1];

                String searchValue = params.get("searchValue-" + key);
                String replaceValue = params.get("replaceValue-" + key);

                Element replacement = new Element("replacement");
                replacement.addContent(new Element("field").setText(entry.getValue()));
                replacement.addContent(new Element("searchValue").setText(searchValue));
                replacement.addContent(new Element("replaceValue").setText(replaceValue));

                replacements.addContent(replacement);
            }
        }

        String replacementsString = Xml.getString(replacements);
        //replacementsString = replacementsString.replaceAll("\\s","");

        for (String uuid : this.metadata) {
            String id = dm.getMetadataId(uuid);
            context.info("Processing metadata with id:" + id);
            processInternal(id, process, "replacements", replacementsString, context);
        }
    }

    /**
     * Applies xslt to replace content in metadata record.
     */
    private Element processInternal(String id,
                                    String process,
                                    String paramNameXml,
                                    String paramXml,
                                    ServiceContext context) throws Exception {

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

        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        AbstractMetadata metadataEntity = metadataRepository.findOne(iId);
        MetadataDataInfo info = metadataEntity.getDataInfo();

        // Get metadata title from the index
        String metadataTitle = LuceneSearcher.getMetadataFromIndexById(context.getLanguage(), id, "title");
        if (StringUtils.isEmpty(metadataTitle)) metadataTitle = metadataEntity.getUuid();

        if (info == null) {
            report.incrementNullRecords();
        } else if (!accessMan.isOwner(context, id)) {
            report.addNotEditableMetadataId(iId);
        } else {

            // -----------------------------------------------------------------------
            // --- check processing exist for current schema
            String schema = info.getSchemaId();
            String filePath = schemaMan.getSchemaDir(schema) + "/process/" + process + ".xsl";
            File xslProcessing = new File(filePath);
            if (!xslProcessing.exists()) {
                context.info("  Processing instruction not found for " + schema
                    + " schema.");

                report.addNoProcessFoundMetadataId(iId);
                return null;
            }
            // --- Process metadata
            Element processedMetadata = null;

            try {
                Element md = dataMan.getMetadataNoInfo(context, id);
                processedMetadata = Xml.transformWithXmlParam(md, filePath, paramNameXml, paramXml);

                // Get changes
                Path filePath2 = schemaMan.getSchemaDir(schema).resolve("process/massive-content-update-extract-changes.xsl");
                List<Element> changesEl = Xml.transform(processedMetadata, filePath2).getChildren("change");

                boolean hasChanges = (changesEl.size() > 0);


                if (hasChanges) {
                    report.addMetadataChanges(iId, changesEl);
                } else {
                    report.addMetadataInfos(iId, "No changes.");
                }

                // --- save metadata and return status
                if (changesEl.size() > 0 && !isTesting) {
                    Path filePath3 = schemaMan.getSchemaDir(schema).resolve("process/massive-content-update-clean-changes.xsl");

                    // Remove empty elements or vacuum record
                    if (!StringUtils.isEmpty(this.vacuumMode)) {
                        // Search and replace, then vacuum record
                        if ("record".equals(this.vacuumMode)) {
                            processedMetadata = Xml.transform(processedMetadata, filePath3);

                            Path vacuumXsltPath = schemaMan.getSchemaDir(schema).resolve("process/vacuum.xsl");
                            if (vacuumXsltPath.toFile().exists()) {
                                processedMetadata = Xml.transform(processedMetadata, vacuumXsltPath);
                            }
                        } else if ("element".equals(this.vacuumMode)) {
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

                    UserSession userSession = context.getUserSession();
                    if(userSession != null) {
                        XMLOutputter outp = new XMLOutputter();
                        String xmlAfter = outp.outputString(processedMetadata);
                        String xmlBefore = outp.outputString(md);
                        new RecordProcessingChangeEvent(Long.parseLong(id), Integer.parseInt(userSession.getUserId()), xmlBefore, xmlAfter, process).publish(ApplicationContextHolder.get());
                    }
                }
            } catch (Exception e) {
                report.addMetadataError(iId, e);
            }
            return processedMetadata;
        }
        return null;
    }
}
