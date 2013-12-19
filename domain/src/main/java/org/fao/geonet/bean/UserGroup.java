package org.fao.geonet.bean;

/**
 * The mapping between user, the groups a user is a part of and the profiles the
 * user has for each group.
 * 
 * @author Jesse
 */
public class UserGroup extends GeonetEntity {
	private static final long serialVersionUID = 2373187130402366299L;
	private UserGroupId _id = new UserGroupId();
	private Group _group;
	private User _user;

	public UserGroupId get_id() {
		return _id;
	}

	public void set_id(UserGroupId _id) {
		this._id = _id;
	}

	public Group get_group() {
		return _group;
	}

	public void set_group(Group _group) {
		this._group = _group;
	}

	public User get_user() {
		return _user;
	}

	public void set_user(User _user) {
		this._user = _user;
	}

}
