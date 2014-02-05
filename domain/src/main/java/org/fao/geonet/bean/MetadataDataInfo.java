package org.fao.geonet.bean;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

import org.fao.geonet.domain.Constants;

/**
 * Encapsulates the metadata about a metadata document. (title, rating, schema
 * etc...) This is a JPA Embeddable object that is embedded into a
 * {@link Metadata} Entity
 * 
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataDataInfo implements Serializable {
	private static final long serialVersionUID = 8049813754167665960L;
	private String _title;
	private ISODate _changeDate = new ISODate();
	private ISODate _createDate = new ISODate();
	private String _schemaId;
	private char _template = Constants.YN_FALSE;
	private String _root;
	private String _doctype;
	private Integer _displayOrder;
	private int _rating;
	private int _popularity;

	public String get_title() {
		return _title;
	}

	public void set_title(String _title) {
		this._title = _title;
	}

	public ISODate get_changeDate() {
		return _changeDate;
	}

	public void set_changeDate(ISODate _changeDate) {
		this._changeDate = _changeDate;
	}

	public ISODate get_createDate() {
		return _createDate;
	}

	public void set_createDate(ISODate _createDate) {
		this._createDate = _createDate;
	}

	public String get_schemaId() {
		return _schemaId;
	}

	public void set_schemaId(String _schemaId) {
		this._schemaId = _schemaId;
	}

	public char get_template() {
		return _template;
	}

	public void set_template(char _template) {
		this._template = _template;
	}

	public String get_root() {
		return _root;
	}

	public void set_root(String _root) {
		this._root = _root;
	}

	public String get_doctype() {
		return _doctype;
	}

	public void set_doctype(String _doctype) {
		this._doctype = _doctype;
	}

	public Integer get_displayOrder() {
		return _displayOrder;
	}

	public void set_displayOrder(Integer _displayOrder) {
		this._displayOrder = _displayOrder;
	}

	public int get_rating() {
		return _rating;
	}

	public void set_rating(int _rating) {
		this._rating = _rating;
	}

	public int get_popularity() {
		return _popularity;
	}

	public void set_popularity(int _popularity) {
		this._popularity = _popularity;
	}

}
