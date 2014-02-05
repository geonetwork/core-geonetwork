package org.fao.geonet.bean;

import org.fao.geonet.domain.MetadataValidationStatus;

/**
 * Entity representing metadata validation reports.
 * 
 * @author Jesse
 */
public class MetadataValidation extends GeonetEntity {
	private static final long serialVersionUID = 6980849290000321963L;
	private MetadataValidationId _id;
	private MetadataValidationStatus _status;
	private int _tested;
	private int _failed;
	private ISODate _validationDate;

	public MetadataValidationId get_id() {
		return _id;
	}

	public void set_id(MetadataValidationId _id) {
		this._id = _id;
	}

	public MetadataValidationStatus get_status() {
		return _status;
	}

	public void set_status(MetadataValidationStatus _status) {
		this._status = _status;
	}

	public int get_tested() {
		return _tested;
	}

	public void set_tested(int _tested) {
		this._tested = _tested;
	}

	public int get_failed() {
		return _failed;
	}

	public void set_failed(int _failed) {
		this._failed = _failed;
	}

	public ISODate get_validationDate() {
		return _validationDate;
	}

	public void set_validationDate(ISODate _validationDate) {
		this._validationDate = _validationDate;
	}

}
