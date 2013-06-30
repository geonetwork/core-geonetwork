package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * The id object for {@link UserGroup}
 *
 * @author Jesse
 */
@Embeddable
public class UserGroupId implements Serializable {
    private static final long serialVersionUID = 758566280699819800L;

    private int _userId;
    private int _groupId;
    public int getUserId() {
        return _userId;
    }
    public UserGroupId setUserId(int userId) {
        this._userId = userId;
        return this;
    }
    public int getGroupId() {
        return _groupId;
    }
    public UserGroupId setGroupId(int groupId) {
        this._groupId = groupId;
        return this;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _groupId;
        result = prime * result + _userId;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserGroupId other = (UserGroupId) obj;
        if (_groupId != other._groupId)
            return false;
        if (_userId != other._userId)
            return false;
        return true;
    }
}
