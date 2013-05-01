package org.fao.geonet.kernel.harvest;

import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;

/**
 * @author heikki doeleman
 */
public abstract class BaseAligner {

    protected void addPrivileges(String id, Iterable<Privileges> privilegesIterable, GroupMapper localGroups, DataManager dataMan, ServiceContext context, Dbms dbms, Logger log) throws Exception {
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
                        dataMan.setOperation(context, dbms, id, priv.getGroupId(), opId +"");
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
