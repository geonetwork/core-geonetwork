package org.fao.geonet.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * One of the entities responsible for dynamic service configuration. Entity
 * representing a {@link jeeves.interfaces.Service}. Originally they were for
 * CSW virtual services but are generic and could in theory be any arbitrary
 * service.
 * 
 * @author Jesse
 */
public class Service extends GeonetEntity {
	private static final long serialVersionUID = 9183533103467569829L;
	private int _id;
	private String _name;
	private String _className;
	private String description;
	private Map<String, String> _parameters = new HashMap<String, String>();

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

	public String get_className() {
		return _className;
	}

	public void set_className(String _className) {
		this._className = _className;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> get_parameters() {
		return _parameters;
	}

	public void set_parameters(Map<String, String> _parameters) {
		this._parameters = _parameters;
	}

}
