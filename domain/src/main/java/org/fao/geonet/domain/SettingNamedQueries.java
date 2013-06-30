package org.fao.geonet.domain;

/**
 * Contains constants that describe all the named queries that are defined for the {@link Setting} entity.
 * 
 * @author Jesse
 * 
 */
public interface SettingNamedQueries {
    interface QUERY_FIND_CHILDREN_BY_NAME {
        String NAME = "findChildrenByName";
        String PARAMETER_NAME = "name";
        String PARAMETER_PARENTID = "parentid";
        String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID + " and s.name = :"
                + PARAMETER_NAME;
    }

    interface QUERY_FIND_ALL_CHILDREN {
        String NAME = "findAllChildren";
        String PARAMETER_PARENTID = QUERY_FIND_CHILDREN_BY_NAME.PARAMETER_PARENTID;
        String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID;
    }

    interface QUERY_FIND_ROOT {
        String NAME = "findRoot";
        String QUERY = "select s from Setting s where s.parent.id IS NULL";
    }
}
