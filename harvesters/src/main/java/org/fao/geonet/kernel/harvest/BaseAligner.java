package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * This class helps {@link AbstractHarvester} instances to
 * process all metadata collected on the harvest.
 * 
 * Takes care of common properties like categories or privileges.
 * 
 * Not all harvesters use this. They should. But don't. //FIXME?
 * 
 * @author heikki doeleman
 */
public abstract class BaseAligner {

    public final AtomicBoolean cancelMonitor;

    public BaseAligner(AtomicBoolean cancelMonitor) {
        this.cancelMonitor = cancelMonitor;
    }


    /**
     * TODO Javadoc.
     *
     * @param categories
     * @param localCateg
     * @param log
     * @param saveMetadata
     * @throws Exception
     */
    public void addCategories(Metadata metadata, Iterable<String> categories, CategoryMapper localCateg, ServiceContext context,
                              Logger log, String serverCategory, boolean saveMetadata) {

        final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
        Map<String, MetadataCategory> nameToCategoryMap = new HashMap<String, MetadataCategory>();
        for (MetadataCategory metadataCategory : categoryRepository.findAll()) {
            nameToCategoryMap.put(""+metadataCategory.getId(), metadataCategory);
        }
        for(String catId : categories)  {
            String name = localCateg.getName(catId);

            if (name == null) {
                if(log.isDebugEnabled()) {
                    log.debug("    - Skipping removed category with id:"+ catId);
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("    - Setting category : "+ name);
                }
                final MetadataCategory metadataCategory = nameToCategoryMap.get(catId);
                if (metadataCategory != null) {
                    metadata.getCategories().add(metadataCategory);
                } else {
                    log.warning("Unable to map category: "+catId+" ("+name+") to a category in Geonetwork");
                }
            }
        }

        if (serverCategory != null) {
            String catId = localCateg.getID(serverCategory);
            if (catId == null) {
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category :" + serverCategory);
            } else {
                final MetadataCategory metadataCategory = nameToCategoryMap.get(catId);
                if (metadataCategory != null) {
                    metadata.getCategories().add(metadataCategory);
                } else {
                    log.warning("Unable to map category: "+catId+" to a category in Geonetwork");
                }
            }
        }
        if (saveMetadata) {
            context.getBean(MetadataRepository.class).save(metadata);
        }
    }

    /**
     *
     * @param id
     * @param privilegesIterable
     * @param localGroups
     * @param dataMan
     * @param context
     * @param log
     * @throws Exception
     */
    public void addPrivileges(String id, Iterable<Privileges> privilegesIterable, GroupMapper localGroups, DataManager dataMan, ServiceContext context, Logger log) throws Exception {
        for (Privileges priv : privilegesIterable) {
            String name = localGroups.getName(priv.getGroupId());

            if (name == null) {
                if(log.isDebugEnabled()) {
                    log.debug("    - Skipping removed group with id:"+ priv.getGroupId());
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("    - Setting privileges for group : "+ name);
                }

                for (int opId: priv.getOperations()) {
                    name = dataMan.getAccessManager().getPrivilegeName(opId);

                    //--- all existing operation
                    if (name != null) {
                        if(log.isDebugEnabled()) {
                            log.debug("       --> Operation: "+ name);
                        }
                        dataMan.setOperation(context, id, priv.getGroupId(), opId +"");
                    }
                }
            }
        }
    }
}
