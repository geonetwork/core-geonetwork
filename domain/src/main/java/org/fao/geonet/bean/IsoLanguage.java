package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * An entity representing the bi-directional mapping between the different iso
 * language codes (de -> ger) and translations of the languages. (German,
 * Deutsch, etc...)
 * 
 * @author Jesse
 */
public class IsoLanguage implements Serializable {

	private static final long serialVersionUID = 1870845042572690280L;
	private int id;
	private String code;
	private String shortCode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

}
