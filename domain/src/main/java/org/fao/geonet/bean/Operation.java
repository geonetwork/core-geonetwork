package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * An entity representing an operation that can be performed on a metadata.
 * 
 * @author Jesse
 */
public class Operation implements Serializable {
	private static final long serialVersionUID = -2860995235478408138L;
	private int _id;
	private String _name;

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

}
