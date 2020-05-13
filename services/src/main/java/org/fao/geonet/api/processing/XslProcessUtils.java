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

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.transform.stream.StreamResult;

import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

/**
 * Created by francois on 23/05/16.
 */
public class XslProcessUtils {
    private static final String SCHEMA_UPGRADE_PROCESS_SUFFIX = "-schemaupgrade";

    /**
     * Process a metadata record and add information about the processing to one or more sets for
     * reporting.
     *  @param id      The metadata identifier corresponding to the metadata record to process
     * @param process The process name
     * @param updateDateStamp
     */
    public static Element process(ServiceContext context, String id,
                                  String process,
                                  boolean save, boolean index,
                                  boolean updateDateStamp,
                                  XsltMetadataProcessingReport report,
                                  String siteUrl,
                                  Map<String, String[]> params) throws Exception {
        SchemaManager schemaMan = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        DataManager dataMan = context.getBean(DataManager.class);
        SettingManager settingsMan = context.getBean(SettingManager.class);
        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        IMetadataManager metadataManager = context.getBean(IMetadataManager.class);
        IMetadataSchemaUtils metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);

        report.incrementProcessedRecords();

        // When a record is deleted the UUID is in the selection manager
        // and when retrieving id, return null
        if (id == null) {
            report.incrementNullRecords();
            return null;
        }

        int iId = Integer.valueOf(id);
        AbstractMetadata info = metadataRepository.findOne(id);


        if (info == null) {
            report.addNotFoundMetadataId(iId);
        } else if (!accessMan.canEdit(context, id)) {
            report.addNotEditableMetadataId(iId);
        } else {

            // -----------------------------------------------------------------------
            // --- check processing exist for current schema
            String schema = info.getDataInfo().getSchemaId();

            FilePathChecker.verify(process);

            Path xslProcessing = schemaMan.getSchemaDir(schema).resolve("process").resolve(process + ".xsl");
            if (!Files.exists(xslProcessing)) {
                Log.info("org.fao.geonet.services.metadata", "  Processing instruction not found for " + schema +
                    " schema. Looking for " + xslProcessing);
                report.addNoProcessFoundMetadataId(iId);
                return null;
            }

            boolean schemaUpgradeProcess = process.endsWith(SCHEMA_UPGRADE_PROCESS_SUFFIX);

            // --- Process metadata
            Element processedMetadata = null;
            try {
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
                Element md = metadataManager.getMetadata(context, id, forEditing, false, withValidationErrors, keepXlinkAttributes);

                Map<String, Object> xslParameter = new HashMap<>();

                xslParameter.put("guiLang", context.getLanguage());
                xslParameter.put("baseUrl", context.getBaseUrl());
                xslParameter.put("nodeUrl", settingsMan.getNodeURL());
                xslParameter.put("catalogUrl", settingsMan.getSiteURL(context));
                xslParameter.put("nodeId", context.getNodeId());

                for (Map.Entry<String, String[]> parameter : params.entrySet()) {
                    String value = parameter.getValue()[0].trim();
                    String key = parameter.getKey();
                    // Add extra metadata
                    if (key.equals("extra_metadata_uuid")
                        && !value.equals("")) {
                        String extraMetadataId = dataMan.getMetadataId(value);
                        if (extraMetadataId != null) {
                            Element extraMetadata = dataMan.getMetadata(context,
                                extraMetadataId, forEditing,
                                withValidationErrors, keepXlinkAttributes);
                            md.addContent(new Element("extra")
                                .addContent(extraMetadata));
                            xslParameter.put(key, value);
                        }
                    } else {
                        // Or add parameter
                        xslParameter.put(key, value);
                    }
                }


                xslParameter.put("siteUrl", siteUrl);

                processedMetadata = Xml.transform(md, xslProcessing, xslParameter);

                // --- save metadata and return status
                if (save) {
                    Lib.resource.checkEditPrivilege(context, id);

                    boolean validate = false;
                    boolean ufo = true;
                    String language = context.getLanguage();

                    // If it's an upgrade process, update the schema id and remove validation info in the database, .
                    if (schemaUpgradeProcess) {
                        String newSchema = metadataSchemaUtils.autodetectSchema(processedMetadata);

                        if (!newSchema.equalsIgnoreCase(info.getDataInfo().getSchemaId())) {
                            metadataManager.update(info.getId(), new Updater<AbstractMetadata>() {
                                @Override
                                public void apply(@Nonnull AbstractMetadata entity) {
                                    entity.getDataInfo().setSchemaId(newSchema);
                                }
                            });

                            metadataValidationRepository.deleteAll(MetadataValidationSpecs.hasMetadataId(info.getId()));
                        }
                    }

                    dataMan.updateMetadata(context, id, processedMetadata, validate, ufo, index, language, new ISODate().toString(), updateDateStamp);
                    if (index) {
                        dataMan.indexMetadata(id, true, null);
                    }
                }

                report.addMetadataId(iId);
                // TODO : it could be relevant to list at least
                // if there was any change in the record or not.
                // Using hash on processMd and metadata ?
            } catch (Exception e) {
                report.addMetadataError(iId, e);
                context.error("  Processing failed with error " + e.getMessage());
                context.error(e);
            }
            return processedMetadata;
        }
        return null;
    }

    public static String processAsText(ServiceContext context, String id, String process, boolean save,
                                  XsltMetadataProcessingReport report,
                                  String siteUrl,
                                  Map<String, String[]> params) throws Exception {
        SchemaManager schemaMan = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        DataManager dataMan = context.getBean(DataManager.class);
        SettingManager settingsMan = context.getBean(SettingManager.class);
        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);

        report.incrementProcessedRecords();

        // When a record is deleted the UUID is in the selection manager
        // and when retrieving id, return null
        if (id == null) {
            report.incrementNullRecords();
            return null;
        }

        int iId = Integer.valueOf(id);
        AbstractMetadata info = metadataRepository.findOne(id);


        if (info == null) {
            report.addNotFoundMetadataId(iId);
        } else if (!accessMan.canEdit(context, id)) {
            report.addNotEditableMetadataId(iId);
        } else {

            // -----------------------------------------------------------------------
            // --- check processing exist for current schema
            String schema = info.getDataInfo().getSchemaId();

            Path xslProcessing = schemaMan.getSchemaDir(schema).resolve("process").resolve(process + ".xsl");
            if (!Files.exists(xslProcessing)) {
                Log.info("org.fao.geonet.services.metadata", "  Processing instruction not found for " + schema +
                    " schema. Looking for " + xslProcessing);
                report.addNoProcessFoundMetadataId(iId);
                return null;
            }


            // --- Process metadata
            StringWriter sw = new StringWriter();
            try {
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
                Element md = dataMan.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

                Map<String, Object> xslParameter = new HashMap<>();

                xslParameter.put("guiLang", context.getLanguage());
                xslParameter.put("baseUrl", context.getBaseUrl());
                xslParameter.put("nodeUrl", settingsMan.getNodeURL());
                xslParameter.put("catalogUrl", settingsMan.getSiteURL(context));
                xslParameter.put("nodeId", context.getNodeId());
                xslParameter.put("thesauriDir", context.getApplicationContext().getBean(GeonetworkDataDirectory.class).getThesauriDir().toAbsolutePath().toString());

                for (Map.Entry<String, String[]> parameter : params.entrySet()) {
                    String value = parameter.getValue()[0].trim();
                    String key = parameter.getKey();
                    // Add extra metadata
                    if (key.equals("extra_metadata_uuid")
                        && !value.equals("")) {
                        String extraMetadataId = dataMan.getMetadataId(value);
                        if (extraMetadataId != null) {
                            Element extraMetadata = dataMan.getMetadata(context,
                                extraMetadataId, forEditing,
                                withValidationErrors, keepXlinkAttributes);
                            md.addContent(new Element("extra")
                                .addContent(extraMetadata));
                            xslParameter.put(key, value);
                        }
                    } else {
                        // Or add parameter
                        xslParameter.put(key, value);
                    }
                }


                xslParameter.put("siteUrl", siteUrl);

                Xml.transform(
                    md, xslProcessing,  new StreamResult(sw), xslParameter);

                report.addMetadataId(iId);
                // TODO : it could be relevant to list at least
                // if there was any change in the record or not.
                // Using hash on processMd and metadata ?
            } catch (Exception e) {
                report.addMetadataError(iId, e);
                context.error("  Processing failed with error " + e.getMessage());
                context.error(e);
            }
            return sw.toString();
        }
        return null;
    }
}
