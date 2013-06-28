package org.fao.geonet.domain;

/**
 * Contains all named queries for UserGroup entity.
 *
 * @author Jesse
 */
public interface UserGroupNamedQueries {
    String TABLE_NAME="usergroups";

    interface DeleteAllByUserId {
        String PARAM_USERID = "userId";
        String QUERY = "delete from UserGroup ug where ug.userId = :"+PARAM_USERID;
    }
}
