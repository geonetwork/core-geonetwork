package org.fao.geonet.services.metadata.schema;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.schema.SchemaDao;
import org.fao.geonet.kernel.schema.SchematronCriteriaType;
import org.fao.geonet.kernel.schema.SchematronRequirement;
import org.jdom.Element;

import static org.fao.geonet.services.metadata.schema.SchematronCriteriaGroupService.*;

/**
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaService implements Service {

    private static final String PARAM_ID = "id";
    private static final String PARAM_GROUP_NAME = "groupName";

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        // init
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        String action = Util.getParam(params, Params.ACTION, ACTION_LIST);
        if (action.equals(ACTION_DELETE)) {
            return delete(params, context);
        } else if (action.equals(ACTION_ADD)) {
            return add(params, context);
        } else {
            return list(params, context);
        }

    }

    private Element add(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, SchematronCriteriaGroupService.PARAM_GROUP_NAME);
        String schema = Util.getParam(params, SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID);

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        final Integer schematronId = Integer.valueOf(schema);
        final Integer id = context.getSerialFactory().getSerial(dbms,
                SchemaDao.TABLE_SCHEMATRON_CRITERIA, "id");
        final SchematronCriteriaType type = SchematronCriteriaType
                .valueOf(Util.getParam(params, "type"));
        final String value = Util.getParam(params, "value");

        org.fao.geonet.kernel.schema.SchematronCriteriaGroup group = SchemaDao.selectCriteriaGroup(dbms, groupName);
        if (group == null) {
            group = new org.fao.geonet.kernel.schema.SchematronCriteriaGroup();
            group.setName(groupName);
            group.setSchematronId(schematronId);
            group.setRequirement(SchematronRequirement.REQUIRED);
            SchemaDao.insertGroup(context, dbms, group);
        }
        SchemaDao.insertCriteria(dbms, groupName, id, type, value);

        return new Element("ok");
    }

    private Element list(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME, null);
        String schematronId = Util.getParam(params, PARAM_ID, null);

        if (schematronId != null && groupName != null) {
            throw new IllegalArgumentException("Only one of " + PARAM_GROUP_NAME + " or " + PARAM_ID + " can be specified");
        }

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if (schematronId != null) {
            return dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA + " WHERE " + SchemaDao.COL_CRITERIA_GROUP_NAME +
                               "=?", Integer.parseInt(schematronId));
        }

        if (groupName == null) {
            return dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA);
        } else {
            return dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA + " WHERE " + SchemaDao.COL_CRITERIA_ID + "=?",
                    groupName);
        }
    }

    private Element delete(Element params, ServiceContext context) throws Exception {
        final Integer id = Integer.valueOf(Util.getParam(params, SchemaDao.COL_CRITERIA_ID));
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        SchemaDao.deleteCriteria(dbms, id);

        return new Element("ok");
    }
}
