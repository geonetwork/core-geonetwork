package org.fao.geonet.bean;

import java.io.Serializable;

import org.fao.geonet.domain.Profile;

/**
 * The id object for {@link UserGroup}
 *
 * @author Jesse
 */
public class UserGroupId implements Serializable {
    private static final long serialVersionUID = 758566280699819800L;

    private int _userId;
    private int _groupId;
    private Profile _profile;
	public int get_userId() {
		return _userId;
	}
	public void set_userId(int _userId) {
		this._userId = _userId;
	}
	public int get_groupId() {
		return _groupId;
	}
	public void set_groupId(int _groupId) {
		this._groupId = _groupId;
	}
	public Profile get_profile() {
		return _profile;
	}
	public void set_profile(Profile _profile) {
		this._profile = _profile;
	}


}
