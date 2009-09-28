//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel;

public class KeywordBean {
	private int id;
	private String value;
	private String lang;
	private String definition;
	private String code;
	private String coordEast;
	private String coordWest;
	private String coordSouth;
	private String coordNorth;	
	private String thesaurus;	
	private boolean selected;
	
	/**
	 * @param id
	 * @param value
	 * @param definition
	 * @param code
	 * @param coordEast
	 * @param coordWest
	 * @param coordSouth
	 * @param coordNorth
	 * @param thesaurus
	 * @param selected
	 */
	public KeywordBean(int id, String value, String definition, String code, 
				String coordEast, String coordWest, 
				String coordSouth, String coordNorth, 
				String thesaurus, boolean selected, String lang) {
		super();
		this.id = id;
		this.value = value;
		this.lang = lang;
		this.definition = definition;
		this.code = code;
		this.coordEast = coordEast;
		this.coordWest = coordWest;
		this.coordSouth = coordSouth;
		this.coordNorth = coordNorth;
		this.thesaurus = thesaurus;
		this.selected = selected;
	}

	/**
	 * @param id
	 * @param value
	 * @param definition
	 * @param thesaurus
	 * @param selected
	 */
	public KeywordBean(int id, String value, String definition, String thesaurus, boolean selected) {
		super();
		this.id = id;
		this.value = value;
		this.definition = definition;
		this.thesaurus = thesaurus;
		this.selected = selected;
	}
	
	/**
	 * @param value
	 * @param definition
	 * @param thesaurus
	 * @param selected
	 */
	public KeywordBean(String value, String definition, String thesaurus, boolean selected) {
		super();
		this.value = value;
		this.definition = definition;
		this.thesaurus = thesaurus;
		this.selected = selected;
	}

	

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(String thesaurus) {
		this.thesaurus = thesaurus;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/*
	 * return the URI of the keyword concept
	 */
	public String getCode() {
		return code;
	}

	public String getRelativeCode() {
		if (code.contains("#"))
		    return code.split("#")[1];
		else
			return code;
	}

	public String getNameSpaceCode() {
		if (code.contains("#"))
			return code.split("#")[0] + "#";
		else
			return "";
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCoordEast() {
		return coordEast;
	}

	public void setCoordEast(String coordEast) {
		this.coordEast = coordEast;
	}

	public String getCoordNorth() {
		return coordNorth;
	}

	public void setCoordNorth(String coordNorth) {
		this.coordNorth = coordNorth;
	}

	public String getCoordWest() {
		return coordWest;
	}

	public void setCoordWest(String coordWest) {
		this.coordWest = coordWest;
	}

	public String getCoordSouth() {
		return coordSouth;
	}

	public void setCoordSouth(String coordSouth) {
		this.coordSouth = coordSouth;
	}
}
