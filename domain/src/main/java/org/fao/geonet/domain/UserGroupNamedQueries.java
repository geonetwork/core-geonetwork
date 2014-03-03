package org.fao.geonet.domain;

/**
 * Contains all named queries for UserGroup entity.
 *
 * @author Jesse
 */
public interface UserGroupNamedQueries {
    /**
     * UserGroups table name
     */
    String TABLE_NAME = "UserGroups";

    /**
     * Constants for the Delete all UserGRoups by userId query.
     *
     * @author Jesse
     */
    interface DeleteAllByUserId {
        /**
         * The userId parameter.
         */
        String PARAM_USERID = "userId";
        /**
         * The query string.
         */
        String QUERY = "delete from UserGroup ug where ug.userId = :" + PARAM_USERID;
    }
}
