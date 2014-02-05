package org.fao.geonet.bean;

import java.io.Serializable;

import org.fao.geonet.domain.OperationAllowed;

/**
 * An entity representing group of users. Groups in conjunction with
 * {@link Operation}s control what operations users can perform on metadata.
 * <p>
 * For example, user userA is in group groupA and userB is in groupB. It could
 * be that groupA is configured with view operation permission (See
 * {@link OperationAllowed}) then userA could view metadata but userB could not.
 * </p>
 * 
 * @author Jesse
 */
public class Group implements Serializable {

	private static final long serialVersionUID = 9120548507075846828L;
	private int _id;
	private String _name;
	private String _description;
	private String _email;
	private Integer _referrer;
	private String logo;
	private String website;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public String get_description() {
		return _description;
	}

	public void set_description(String _description) {
		this._description = _description;
	}

	public String get_email() {
		return _email;
	}

	public void set_email(String _email) {
		this._email = _email;
	}

	public Integer get_referrer() {
		return _referrer;
	}

	public void set_referrer(Integer _referrer) {
		this._referrer = _referrer;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

}
