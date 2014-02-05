package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * Information about the source and owner of the metadata document. This is a
 * JPA Embeddable object that is embedded into a {@link Metadata} Entity
 * 
 * @author Jesse
 */
public class MetadataSourceInfo implements Serializable {
	private static final long serialVersionUID = -6032075543035787580L;
	private String _sourceId;
	private Integer _groupOwner;
	private int _owner;

	public String get_sourceId() {
		return _sourceId;
	}

	public void set_sourceId(String _sourceId) {
		this._sourceId = _sourceId;
	}

	public Integer get_groupOwner() {
		return _groupOwner;
	}

	public void set_groupOwner(Integer _groupOwner) {
		this._groupOwner = _groupOwner;
	}

	public int get_owner() {
		return _owner;
	}

	public void set_owner(int _owner) {
		this._owner = _owner;
	}

}
