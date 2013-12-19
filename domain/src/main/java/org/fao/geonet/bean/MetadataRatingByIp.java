package org.fao.geonet.bean;

/**
 * An entity that tracks which users have rated a metadata. It currently tracks
 * by Ip address so that each IP address can only rate a given metadata once.
 * 
 * @author Jesse
 */
public class MetadataRatingByIp extends GeonetEntity {
	private static final long serialVersionUID = 84720751487108559L;
	private MetadataRatingByIpId _id;
	private int _rating;

	public MetadataRatingByIpId get_id() {
		return _id;
	}

	public void set_id(MetadataRatingByIpId _id) {
		this._id = _id;
	}

	public int get_rating() {
		return _rating;
	}

	public void set_rating(int _rating) {
		this._rating = _rating;
	}
}
