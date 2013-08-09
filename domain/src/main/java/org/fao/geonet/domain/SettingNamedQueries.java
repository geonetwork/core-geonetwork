package org.fao.geonet.domain;

/**
 * Contains constants that describe all the named queries that are defined for the {@link Setting} entity.
 * 
 * @author Jesse
 * 
 */
public interface SettingNamedQueries {
    /**
     * The constants for the "Find Children by Name" stored query.
     *
     * @author Jesse
     */
    interface QUERY_FIND_CHILDREN_BY_NAME {
        /**
         * Name of the query.
         */
        String NAME = "findChildrenByName";
        /**
         * The setting name parameter.
         */
        String PARAMETER_NAME = "name";
        /**
         * The parent id parameter
         */
        String PARAMETER_PARENTID = "parentid";
        /**
         * The Query string
         */
        String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID + " and s.name = :" + PARAMETER_NAME;
    }

    /**
     * The constants for the "Find All Children" stored query.
     *
     * @author Jesse
     */
    interface QUERY_FIND_ALL_CHILDREN {
        /**
         * Name of the query.
         */
        String NAME = "findAllChildren";
        /**
         * The parent id parameter
         */
        String PARAMETER_PARENTID = QUERY_FIND_CHILDREN_BY_NAME.PARAMETER_PARENTID;
        /**
         * The Query string
         */
        String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID;
    }

    /**
     * The constants for the "Find Root" stored query.
     *
     * @author Jesse
     */
    interface QUERY_FIND_ROOT {
        /**
         * Name of the query.
         */
        String NAME = "findRoot";
        /**
         * The Query string
         */
        String QUERY = "select s from Setting s where s.parent.id IS NULL";
    }
}
