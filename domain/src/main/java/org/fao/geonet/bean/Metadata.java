package org.fao.geonet.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * An entity representing a metadata object in the database. The xml, groups and
 * operations are lazily loaded so accessing then will need to be done in a
 * thread that has a bound EntityManager. Also they can trigger database access
 * if they have not been cached and therefore can cause slowdowns so they should
 * only be accessed in need.
 * 
 * @author Jesse
 */
public class Metadata extends GeonetEntity {

	private static final long serialVersionUID = 6886741395209400709L;
	public static final String METADATA_CATEG_JOIN_TABLE_NAME = "MetadataCateg";
	public static final String METADATA_CATEG_JOIN_TABLE_CATEGORY_ID = "categoryId";
	private int _id;
	private String _uuid;
	private String _data;
	private MetadataDataInfo _dataInfo = new MetadataDataInfo();
	private MetadataSourceInfo _sourceInfo = new MetadataSourceInfo();
	private MetadataHarvestInfo _harvestInfo = new MetadataHarvestInfo();
	private Set<MetadataCategory> _metadataCategories = new HashSet<MetadataCategory>();

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_uuid() {
		return _uuid;
	}

	public void set_uuid(String _uuid) {
		this._uuid = _uuid;
	}

	public String get_data() {
		return _data;
	}

	public void set_data(String _data) {
		this._data = _data;
	}

	public MetadataDataInfo get_dataInfo() {
		return _dataInfo;
	}

	public void set_dataInfo(MetadataDataInfo _dataInfo) {
		this._dataInfo = _dataInfo;
	}

	public MetadataSourceInfo get_sourceInfo() {
		return _sourceInfo;
	}

	public void set_sourceInfo(MetadataSourceInfo _sourceInfo) {
		this._sourceInfo = _sourceInfo;
	}

	public MetadataHarvestInfo get_harvestInfo() {
		return _harvestInfo;
	}

	public void set_harvestInfo(MetadataHarvestInfo _harvestInfo) {
		this._harvestInfo = _harvestInfo;
	}

	public Set<MetadataCategory> get_metadataCategories() {
		return _metadataCategories;
	}

	public void set_metadataCategories(Set<MetadataCategory> _metadataCategories) {
		this._metadataCategories = _metadataCategories;
	}

	public static String getMetadataCategJoinTableName() {
		return METADATA_CATEG_JOIN_TABLE_NAME;
	}

	public static String getMetadataCategJoinTableCategoryId() {
		return METADATA_CATEG_JOIN_TABLE_CATEGORY_ID;
	}

}
