package org.fao.geonet.bean;

import java.io.Serializable;

/**
 * The id object of {@link OperationAllowed}
 * 
 * @author Jesse
 */
public class OperationAllowedId implements Serializable {
	private static final long serialVersionUID = -5759713154514715316L;

	private int _metadataId;
	private int _groupId;
	private int _operationId;

	public int get_metadataId() {
		return _metadataId;
	}

	public void set_metadataId(int _metadataId) {
		this._metadataId = _metadataId;
	}

	public int get_groupId() {
		return _groupId;
	}

	public void set_groupId(int _groupId) {
		this._groupId = _groupId;
	}

	public int get_operationId() {
		return _operationId;
	}

	public void set_operationId(int _operationId) {
		this._operationId = _operationId;
	}

}
