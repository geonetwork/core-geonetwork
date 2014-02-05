package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * A Metadata category. This is separate from any category listed in the
 * metadata xml itself and is geonetwork specific.
 * 
 * @author Jesse
 */
public class MetadataCategory implements Serializable {
	private static final long serialVersionUID = -48215909798608235L;
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
