package org.fao.geonet.bean;

import java.util.HashSet;
import java.util.Set;

import org.fao.geonet.domain.Profile;

/**
 * A user entity. A user is used in spring security, controlling access to
 * metadata as well as in the {@link jeeves.server.UserSession}.
 * 
 * @author Jesse
 */
public class User extends GeonetEntity {
	private static final long serialVersionUID = 2589607276443866650L;

	private int _id;
	private String _username;
	private String _surname;
	private String _name;
	private Set<String> _email = new HashSet<String>();
	private Set<Address> _addresses = new HashSet<Address>();
	private String _organisation;
	private String _kind;
	private Profile _profile = Profile.RegisteredUser;
	private UserSecurity _security = new UserSecurity();

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_username() {
		return _username;
	}

	public void set_username(String _username) {
		this._username = _username;
	}

	public String get_surname() {
		return _surname;
	}

	public void set_surname(String _surname) {
		this._surname = _surname;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public Set<String> get_email() {
		return _email;
	}

	public void set_email(Set<String> _email) {
		this._email = _email;
	}

	public Set<Address> get_addresses() {
		return _addresses;
	}

	public void set_addresses(Set<Address> _addresses) {
		this._addresses = _addresses;
	}

	public String get_organisation() {
		return _organisation;
	}

	public void set_organisation(String _organisation) {
		this._organisation = _organisation;
	}

	public String get_kind() {
		return _kind;
	}

	public void set_kind(String _kind) {
		this._kind = _kind;
	}

	public Profile get_profile() {
		return _profile;
	}

	public void set_profile(Profile _profile) {
		this._profile = _profile;
	}

	public UserSecurity get_security() {
		return _security;
	}

	public void set_security(UserSecurity _security) {
		this._security = _security;
	}

}
