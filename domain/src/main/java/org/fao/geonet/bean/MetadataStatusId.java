package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * The id object of {@link MetadataStatus}.
 * 
 * @author Jesse
 */
public class MetadataStatusId implements Serializable {
	private static final long serialVersionUID = -4395314364468537427L;
	private ISODate _changedate;
	private int _metadataId;
	private int _statusId;
	private int _userId;

	public ISODate get_changedate() {
		return _changedate;
	}

	public void set_changedate(ISODate _changedate) {
		this._changedate = _changedate;
	}

	public int get_metadataId() {
		return _metadataId;
	}

	public void set_metadataId(int _metadataId) {
		this._metadataId = _metadataId;
	}

	public int get_statusId() {
		return _statusId;
	}

	public void set_statusId(int _statusId) {
		this._statusId = _statusId;
	}

	public int get_userId() {
		return _userId;
	}

	public void set_userId(int _userId) {
		this._userId = _userId;
	}

}
