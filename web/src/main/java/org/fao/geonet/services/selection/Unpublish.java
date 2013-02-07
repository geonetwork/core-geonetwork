package org.fao.geonet.services.selection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

/**
 * Select a list of elements stored in session For all the MD stored in the
 * selectionManager, the service will send an email to the owner of the MD. If
 * the owner hasn't any mail addresse, the email will be sent to ADMIN_MAIL
 * Returns status
 * 
 * @author fgravin
 */

public class Unpublish implements Service {

    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig config) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        UserSession us = context.getUserSession();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(us);

        Element ret = new Element("response");

        Collection<String> selection = sm.getSelection("metadata");
        synchronized (selection) {
            selection = new LinkedList<String>(selection);
        }
        for (Iterator<String> iter = selection.iterator(); iter.hasNext();) {

            String uuid = (String) iter.next();
            String id = dm.getMetadataId(dbms, uuid);

            dbms.execute("DELETE FROM operationallowed WHERE metadataid = ? and (groupid = 1 or groupid = -1)",
                    Integer.valueOf(id));

            dbms.execute(
                    "INSERT INTO publish_tracking (uuid, entity, validated, published, failurerule) VALUES (?,?,?,?,?)",
                    uuid, "Administrator", 'y', 'n', "manual unpublish by administrator");

            Element retchildserv = new Element("unpublished");
            retchildserv.setAttribute("uuid", uuid);
            ret.addContent(retchildserv);
        }

        return ret;
    }
}

// =============================================================================

