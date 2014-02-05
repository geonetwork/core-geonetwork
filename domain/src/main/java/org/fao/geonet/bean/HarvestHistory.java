package org.fao.geonet.bean;

import org.fao.geonet.domain.Constants;

/**
 * An entity representing a harvesting task that may have been completed or
 * possibly ending in error.
 * 
 * @author Jesse
 */
public class HarvestHistory extends GeonetEntity {
	private static final long serialVersionUID = 4543334423903618392L;
	private int _id;
	private ISODate _harvestDate;
	private int _elapsedTime;
	private String _harvesterUuid;
	private String _harvesterName;
	private String _harvesterType;
	private char _deleted = Constants.YN_FALSE;
	private String _info;
	private String _params;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public ISODate get_harvestDate() {
		return _harvestDate;
	}

	public void set_harvestDate(ISODate _harvestDate) {
		this._harvestDate = _harvestDate;
	}

	public int get_elapsedTime() {
		return _elapsedTime;
	}

	public void set_elapsedTime(int _elapsedTime) {
		this._elapsedTime = _elapsedTime;
	}

	public String get_harvesterUuid() {
		return _harvesterUuid;
	}

	public void set_harvesterUuid(String _harvesterUuid) {
		this._harvesterUuid = _harvesterUuid;
	}

	public String get_harvesterName() {
		return _harvesterName;
	}

	public void set_harvesterName(String _harvesterName) {
		this._harvesterName = _harvesterName;
	}

	public String get_harvesterType() {
		return _harvesterType;
	}

	public void set_harvesterType(String _harvesterType) {
		this._harvesterType = _harvesterType;
	}

	public char get_deleted() {
		return _deleted;
	}

	public void set_deleted(char _deleted) {
		this._deleted = _deleted;
	}

	public String get_info() {
		return _info;
	}

	public void set_info(String _info) {
		this._info = _info;
	}

	public String get_params() {
		return _params;
	}

	public void set_params(String _params) {
		this._params = _params;
	}

}
