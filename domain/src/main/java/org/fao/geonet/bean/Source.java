package org.fao.geonet.bean;

/**
 * Entity representing a metadata source.
 * 
 * @author Jesse
 */
public class Source extends GeonetEntity {
	private static final long serialVersionUID = -5810796138593573976L;
	private String _uuid;
	private String _name;

	public String get_uuid() {
		return _uuid;
	}

	public void set_uuid(String _uuid) {
		this._uuid = _uuid;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}
}
