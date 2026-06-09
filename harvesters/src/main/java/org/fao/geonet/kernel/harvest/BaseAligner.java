/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class helps {@link AbstractHarvester} instances to process all metadata collected on the
 * harvest.
 * <p>
 * Takes care of common properties like categories or privileges.
 * <p>
 * Not all harvesters use this. They should. But don't. //FIXME?
 *
 * @author heikki doeleman
 */
public abstract class BaseAligner<P extends AbstractParams> extends AbstractAligner<P> {

    private static Logger LOGGER = LoggerFactory.getLogger(Geonet.HARVESTER);

    public final AtomicBoolean cancelMonitor;

    protected BaseAligner(AtomicBoolean cancelMonitor) {
        this.cancelMonitor = cancelMonitor;
    }

    public void addCategories(AbstractMetadata metadata, Iterable<String> categories,
                              CategoryMapper localCateg, ServiceContext context,
                              String serverCategory, boolean saveMetadata) {

        MetadataCategoryRepository metadataCategoryRepository = context.getBean(MetadataCategoryRepository.class);
        Map<String, MetadataCategory> nameToCategoryMap = new HashMap<>();
        for (MetadataCategory metadataCategory : metadataCategoryRepository.findAll()) {
            nameToCategoryMap.put("" + metadataCategory.getId(), metadataCategory);
        }
        for (String catId : categories) {
            String name = localCateg.getName(catId);

            if (name == null) {
                LOGGER.debug("    - Skipping removed category with id:{}", catId);
            } else {
                LOGGER.debug("    - Setting category : {}", name);
                final MetadataCategory metadataCategory = nameToCategoryMap.get(catId);
                if (metadataCategory != null) {
                    metadata.getCategories().add(metadataCategory);
                } else {
                    LOGGER.warn("Unable to map category: {} ({}) to a category in Geonetwork", catId, name);
                }
            }
        }

        if (serverCategory != null) {
            String catId = localCateg.getID(serverCategory);
            if (catId == null) {
                LOGGER.debug("    - Skipping removed category :{}", serverCategory);
            } else {
                final MetadataCategory metadataCategory = nameToCategoryMap.get(catId);
                if (metadataCategory != null) {
                    metadata.getCategories().add(metadataCategory);
                } else {
                    LOGGER.warn("Unable to map category: {} to a category in Geonetwork", catId);
                }
            }
        }
        if (saveMetadata) {
            context.getBean(IMetadataManager.class).save(metadata);
        }
    }

    public void addPrivileges(String id, Iterable<Privileges> privilegesIterable, GroupMapper localGroups, ServiceContext context) throws Exception {
        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        DataManager dataManager = context.getBean(DataManager.class);
        if (!params.isIfRecordExistAppendPrivileges()) {
            operationAllowedRepository.deleteAllByMetadataId(Integer.parseInt(id));
        }
        for (Privileges priv : privilegesIterable) {
            String name = localGroups.getName(priv.getGroupId());

            if (name == null) {
                LOGGER.debug("    - Skipping removed group with id: {}", priv.getGroupId());
            } else {
                LOGGER.debug("    - Setting privileges for group: {}", name);
                for (int opId : priv.getOperations()) {
                    name = dataManager.getAccessManager().getPrivilegeName(opId);
                    //--- all existing operation
                    if (name != null) {
                        LOGGER.debug("       --> Operation: {}", name);
                        dataManager.setOperation(context, id, priv.getGroupId(), opId + "");
                    }
                }
            }
        }
    }

    /**
     * Applies a xslt process (schema_folder/process/translate.xsl) to translate create the metadata
     * fields configured in the harvester to the languages configured, using the translation provider
     * configured in the application settings.
     *
     * If no translation provider is configured or if the schema doesn't have the translation xslt,
     * the translation process is not applied to the metadata.
     *
     * @param context
     * @param md
     * @param schema
     * @return
     */
    public Element translateMetadataContent(ServiceContext context,
                                            Element md,
                                            String schema) {

        SettingManager settingManager = context.getBean(SettingManager.class);

        String translationProvider = settingManager.getValue(Settings.SYSTEM_TRANSLATION_PROVIDER);

        if (!StringUtils.hasLength(translationProvider)) {
            LOGGER.warn("     metadata content can't be translated. Translation provider not configured.");
            return md;
        }

        if (!StringUtils.hasLength(params.getTranslateContentLangs()) ||
            !StringUtils.hasLength(params.getTranslateContentFields())) {
            LOGGER.warn("     metadata content can't be translated. No languages or fields provided to translate.");
            return md;
        }

        SchemaManager schemaManager = context.getBean(SchemaManager.class);

        Path filePath = schemaManager.getSchemaDir(schema).resolve("process").resolve( "translate.xsl");

        if (!Files.exists(filePath)) {
            LOGGER.debug(String.format("     metadata content translation process not available for schema %s", schema));
        } else {
            Element processedMetadata;
            try {
                Map<String, Object> processParams = new HashMap<>();
                List<String> langs = Arrays.asList(params.getTranslateContentLangs().split(","));
                processParams.put("languages", langs);

                List<String> fields = Arrays.asList(params.getTranslateContentFields().split("\\n"));
                processParams.put("fieldsToTranslate", fields);

                processedMetadata = Xml.transform(md, filePath, processParams);
                LOGGER.debug("     metadata content translated.");
                md = processedMetadata;
            } catch (Exception e) {
                LOGGER.warn(String.format("     metadata content translated error: %s", e.getMessage()));
            }
        }
        return md;
    }


    /**
     * Filter the metadata if process parameter is set and corresponding XSL transformation
     * exists in xsl/conversion/import.
     *
     * @param context
     * @param md
     * @param processName
     * @param processParams
     * @param log
     * @return
     */
    protected Element applyXSLTProcessToMetadata(ServiceContext context,
                                    Element md,
                                    String processName,
                                    Map<String, Object> processParams,
                                                 org.fao.geonet.Logger log) {
        Path filePath = context.getBean(GeonetworkDataDirectory.class).getXsltConversion(processName);
        if (!Files.exists(filePath)) {
            log.debug("     processing instruction  " + processName + ". Metadata not filtered.");
        } else {
            Element processedMetadata;
            try {
                processedMetadata = Xml.transform(md, filePath, processParams);
                log.debug("     metadata filtered.");
                md = processedMetadata;
            } catch (Exception e) {
                log.warning("     processing error " + processName + ": " + e.getMessage());
            }
        }
        return md;
    }


}
