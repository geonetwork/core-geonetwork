package org.fao.geonet.bean;

import java.io.Serializable;

import org.fao.geonet.domain.Constants;

/**
 * Encapsulates the harvest data related to a metadata document. Like whether
 * the metadata was harvested, the uuid of the harvester, etc... This is a JPA
 * Embeddable object that is embedded into a {@link Metadata} Entity
 * 
 * @author Jesse
 */
public class MetadataHarvestInfo implements Serializable {
	private static final long serialVersionUID = 7935704119616189903L;
	private char _harvested = Constants.YN_FALSE;
	private String _uuid;
	private String _uri;

	public char get_harvested() {
		return _harvested;
	}

	public void set_harvested(char _harvested) {
		this._harvested = _harvested;
	}

	public String get_uuid() {
		return _uuid;
	}

	public void set_uuid(String _uuid) {
		this._uuid = _uuid;
	}

	public String get_uri() {
		return _uri;
	}

	public void set_uri(String _uri) {
		this._uri = _uri;
	}
}
