package org.fao.geonet.bean;

/**
 * Variable substitutions and extra information that goes in a CSW capabilities
 * document. <br/>
 * Typically each entity represents the translated information for a single
 * language of a single field. This is essentially a map where the key is the
 * language+field and the value is the translated label.
 * 
 * @author Jesse
 */
public class CswCapabilitiesInfoField extends GeonetEntity {
	private static final long serialVersionUID = -4762806655058693446L;
	private static final int ID_COLUMN_LENGTH = 10;
	private static final int LANG_ID_COLUMN_LENGTH = 5;
	private static final int FIELD_NAME_COLUMN_LENGTH = 32;
	private int _id = 0;
	private String _langId;
	private String _fieldName;
	private String _value;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_langId() {
		return _langId;
	}

	public void set_langId(String _langId) {
		this._langId = _langId;
	}

	public String get_fieldName() {
		return _fieldName;
	}

	public void set_fieldName(String _fieldName) {
		this._fieldName = _fieldName;
	}

	public String get_value() {
		return _value;
	}

	public void set_value(String _value) {
		this._value = _value;
	}

	public static int getIdColumnLength() {
		return ID_COLUMN_LENGTH;
	}

	public static int getLangIdColumnLength() {
		return LANG_ID_COLUMN_LENGTH;
	}

	public static int getFieldNameColumnLength() {
		return FIELD_NAME_COLUMN_LENGTH;
	}

}
