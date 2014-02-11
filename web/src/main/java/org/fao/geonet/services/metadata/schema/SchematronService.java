package org.fao.geonet.services.metadata.schema;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.SchemaDao;
import org.jdom.Element;

/**
 * List all schematrons.
 *
 * Created by Jesse on 2/7/14.
 */
public class SchematronService implements Service{

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        // init
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String id = Util.getParam(params, "id", null);

        Element result;
        if (id == null) {
            result = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON);
        } else {
            result = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON + " WHERE id=?", Integer.parseInt(id));
        }

        result.setName("schematron");
        return result;
    }
}
