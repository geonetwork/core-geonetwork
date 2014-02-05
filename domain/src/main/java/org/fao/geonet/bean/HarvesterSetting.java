package org.fao.geonet.bean;

/**
 * An entity representing a harvester configuration setting.
 * <p/>
 * Harvester settings are represented by a tree. One should use the
 * {@link org.fao.geonet.repository.HarvesterSettingRepository} to traverse the
 * hierarchy.
 * 
 * @author Jesse
 */
public class HarvesterSetting extends GeonetEntity {
	private static final long serialVersionUID = -3709818921970021407L;

	public static final int ROOT_ID = 0;

	private int _id;
	private HarvesterSetting _parent;
	private String _name;
	private String _value;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public HarvesterSetting get_parent() {
		return _parent;
	}

	public void set_parent(HarvesterSetting _parent) {
		this._parent = _parent;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public String get_value() {
		return _value;
	}

	public void set_value(String _value) {
		this._value = _value;
	}

	public static int getRootId() {
		return ROOT_ID;
	}

}
