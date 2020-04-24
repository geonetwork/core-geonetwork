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

package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
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

    public BaseAligner(AtomicBoolean cancelMonitor) {
        this.cancelMonitor = cancelMonitor;
    }

    public void addCategories(AbstractMetadata metadata, Iterable<String> categories,
                              CategoryMapper localCateg, ServiceContext context,
                              String serverCategory, boolean saveMetadata) {

        MetadataCategoryRepository metadataCategoryRepository = context.getBean(MetadataCategoryRepository.class);
        Map<String, MetadataCategory> nameToCategoryMap = new HashMap<String, MetadataCategory>();
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
                LOGGER.debug("    - Skipping removed group with id:{}", priv.getGroupId());
            } else {
                LOGGER.debug("    - Setting privileges for group : {}", name);
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
}
