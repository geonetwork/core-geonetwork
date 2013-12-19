package org.fao.geonet.bean;

/**
 * Csw custom element set. This is part of the CSW specification related to what
 * elements are returned by GetRecords and GetRecordById.
 * 
 * @author Jesse
 */
public class CustomElementSet extends GeonetEntity {
	private static final long serialVersionUID = -6426636768954186464L;
	private static final int XPATH_COLUMN_LENGTH = 1000;
	private String _xpath;
	private int _xpathHashcode;

	public String get_xpath() {
		return _xpath;
	}

	public void set_xpath(String _xpath) {
		this._xpath = _xpath;
	}

	public int get_xpathHashcode() {
		return _xpathHashcode;
	}

	public void set_xpathHashcode(int _xpathHashcode) {
		this._xpathHashcode = _xpathHashcode;
	}

	public static int getXpathColumnLength() {
		return XPATH_COLUMN_LENGTH;
	}

}
