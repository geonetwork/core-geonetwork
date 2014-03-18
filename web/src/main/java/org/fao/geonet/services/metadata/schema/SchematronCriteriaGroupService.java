package org.fao.geonet.services.metadata.schema;

import com.google.common.collect.Lists;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.schema.SchemaDao;
import org.fao.geonet.kernel.schema.SchematronRequirement;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import java.util.List;

/**
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaGroupService implements Service {

    static final String PARAM_SCHEMATRON_ID = "schematronId";
    static final String PARAM_GROUP_NAME = "groupName";
    private static final String PARAM_INCLUDE_CRITERIA = "includeCriteria";
    private static final String PARAM_INCLUDE_SCHEMATRON = "includeSchematron";
    private static final Filter SCHEMATRON_ID_FILTER = new ElementFilter(SchemaDao.COL_GROUP_SCHEMATRON_ID);
    private static final Filter GROUP_NAME_FILTER = new ElementFilter(SchemaDao.COL_GROUP_NAME);
    static final String ACTION_LIST = "list";
    static final String ACTION_ADD = "add";
    static final String ACTION_EDIT = "edit";
    static final String ACTION_DELETE = "delete";
    private boolean _defaultIncludeSchematron;
    private boolean _defaultIncludeCriteria;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        this._defaultIncludeSchematron = Boolean.parseBoolean(params.getValue(PARAM_INCLUDE_SCHEMATRON, "false"));
        this._defaultIncludeCriteria = Boolean.parseBoolean(params.getValue(PARAM_INCLUDE_SCHEMATRON, "false"));
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
            String action = Util.getParam(params, Params.ACTION, ACTION_LIST);
            if (action.equals(ACTION_EDIT)) {
            return edit(params, context);
        } else if (action.equals(ACTION_DELETE)) {
            return delete(params, context);
        } else {
            return list(params, context);
        }
    }

    private Element delete(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        dbms.execute("DELETE FROM "+SchemaDao.TABLE_SCHEMATRON_CRITERIA +
                     " WHERE "+SchemaDao.COL_CRITERIA_GROUP_NAME + "=? and "+SchemaDao.COL_CRITERIA_SCHEMATRON_ID + "=?",
                groupName, schematronId);
        dbms.execute("DELETE FROM "+SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP +
                     " WHERE "+SchemaDao.COL_GROUP_NAME+ "=? and "+SchemaDao.COL_GROUP_SCHEMATRON_ID + "=?",
                groupName, schematronId);

        return new Element("ok");
    }

    private Element list(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME, null);
        String schematronId = Util.getParam(params, PARAM_SCHEMATRON_ID, null);
        boolean includeCriteriaParamPresent = params.getChild(PARAM_INCLUDE_CRITERIA) == null;
        boolean includeCriteria = Util.getParam(params, PARAM_INCLUDE_CRITERIA,
                includeCriteriaParamPresent?_defaultIncludeSchematron:includeCriteriaParamPresent);
        boolean includeSchematronParamPresent = params.getChild(PARAM_INCLUDE_SCHEMATRON) == null;
        boolean includeSchematron = Util.getParam(params, PARAM_INCLUDE_SCHEMATRON,
                includeCriteriaParamPresent?_defaultIncludeCriteria:includeSchematronParamPresent);

        if (schematronId != null && groupName != null) {
            throw new IllegalArgumentException("Only one of " + PARAM_GROUP_NAME + " or " + PARAM_SCHEMATRON_ID + " can be " +
                                               "specified");
        }

        Element groups;
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if (schematronId != null) {
            groups = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP + " WHERE " + SchemaDao
                    .COL_GROUP_SCHEMATRON_ID + "=?", Integer.parseInt(schematronId));
        } else {
            if (groupName == null) {
                groups = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP);
            } else {
                groups = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP + " WHERE " + SchemaDao
                        .COL_GROUP_NAME + "=?", groupName);
            }
        }

        groups.setName(SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP);

        if (includeCriteria) {
            @SuppressWarnings("unchecked")
            List<Element> groupNameElems = Lists.newArrayList(groups.getDescendants(GROUP_NAME_FILTER));
            for (Element groupNameEl : groupNameElems) {
                final String currentSchematronId = groupNameEl.getParentElement().getChildText(SchemaDao.COL_GROUP_SCHEMATRON_ID);
                final String query = "SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON_CRITERIA + " WHERE " + SchemaDao
                        .COL_CRITERIA_GROUP_NAME + "=? and " + SchemaDao.COL_CRITERIA_SCHEMATRON_ID + "=?";
                Element criteria = dbms.select(query,groupNameEl.getText(), Integer.parseInt(currentSchematronId));
                criteria.setName("criteria");

                @SuppressWarnings("unchecked")
                List<Element> records = criteria.getChildren();

                for (Element record : records) {
                    record.setName("criteria");
                }
                groupNameEl.getParentElement().addContent(criteria);
            }
        }

        if (includeSchematron) {
            @SuppressWarnings("unchecked")
            List<Element> groupNameElems = Lists.newArrayList(groups.getDescendants(SCHEMATRON_ID_FILTER));
            for (Element schemaIdEl : groupNameElems) {
                Element schematronResults = dbms.select("SELECT * FROM " + SchemaDao.TABLE_SCHEMATRON + " WHERE " + SchemaDao
                        .COL_SCHEMATRON_ID + "=?", Integer.parseInt(schemaIdEl.getText())).getChild("record");
                schematronResults.setName("schematron");
                schematronResults.addContent(new Element("ruleName").setText(
                        SchemaDao.toRuleName(schematronResults.getChildText(SchemaDao.COL_SCHEMATRON_FILE))));
                schematronResults.detach();

                Element criteriaGroup = schemaIdEl.getParentElement();
                schemaIdEl.detach();
                criteriaGroup.addContent(schematronResults);
            }
        }

        return groups;
    }

    private Element edit(Element params, ServiceContext context) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String groupName = Util.getParam(params, SchemaDao.COL_GROUP_NAME);
        String schematronId = Util.getParam(params, SchemaDao.COL_GROUP_SCHEMATRON_ID);
        String requirement = Util.getParam(params, SchemaDao.COL_GROUP_REQUIREMENT, null);

        if (requirement != null) {

            // Test that the new requirement is valid
            SchematronRequirement.valueOf(requirement.toUpperCase());

            dbms.execute("UPDATE "+SchemaDao.TABLE_SCHEMATRON_CRITERIA_GROUP+" SET "+SchemaDao.COL_GROUP_REQUIREMENT+"=? " +
                         "WHERE "+SchemaDao.COL_GROUP_NAME+"=? AND "+SchemaDao.COL_GROUP_SCHEMATRON_ID+"=?", requirement, groupName,
                    Integer.parseInt(schematronId));
            return new Element("ok");
        }

        return new Element("NoUpdate");

    }
}