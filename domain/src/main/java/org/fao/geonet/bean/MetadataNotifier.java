package org.fao.geonet.bean;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.domain.Constants;

/**
 * An entity representing a service that desires to be notified when a metadata
 * is modified.
 * 
 * @author Jesse
 */
public class MetadataNotifier extends GeonetEntity {
	private static final long serialVersionUID = 1293846L;
	private int _id;
	private String _name;
	private String _url;
	private char _enabled = Constants.YN_FALSE;
	private String _username;
	private char[] _password;
	private List<MetadataNotification> _notifications = new ArrayList<MetadataNotification>();

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public String get_url() {
		return _url;
	}

	public void set_url(String _url) {
		this._url = _url;
	}

	public char get_enabled() {
		return _enabled;
	}

	public void set_enabled(char _enabled) {
		this._enabled = _enabled;
	}

	public String get_username() {
		return _username;
	}

	public void set_username(String _username) {
		this._username = _username;
	}

	public char[] get_password() {
		return _password;
	}

	public void set_password(char[] _password) {
		this._password = _password;
	}

	public List<MetadataNotification> get_notifications() {
		return _notifications;
	}

	public void set_notifications(List<MetadataNotification> _notifications) {
		this._notifications = _notifications;
	}

}
