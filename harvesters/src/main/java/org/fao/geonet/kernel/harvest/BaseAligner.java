package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;

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

    /**
     * TODO Javadoc.
     *
     * @param id
     * @param categories
     * @param localCateg
     * @param dataMan
     * @param context
     * @param log
     * @throws Exception
     */
    public void addCategories(String id, Iterable<String> categories, CategoryMapper localCateg, DataManager dataMan, ServiceContext context, Logger log, String serverCategory) throws Exception {
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
                dataMan.setCategory(context, id, catId);
            }
        }

        if (serverCategory != null) {
            String catId = localCateg.getID(serverCategory);
            if (catId == null) {
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category :" + serverCategory);
            } else {
                dataMan.setCategory(context, id, catId);
            }
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

                    //--- allow only: view, dynamic, featured
                    if (opId == 0 || opId == 5 || opId == 6) {
                        if(log.isDebugEnabled()) {
                            log.debug("       --> "+ name);
                        }
                        dataMan.setOperation(context, id, priv.getGroupId(), opId +"");
                    }
                    else {
                        if(log.isDebugEnabled()) {
                            log.debug("       --> "+ name +" (skipped)");
                        }
                    }
                }
            }
        }
    }
}
