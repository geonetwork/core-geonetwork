package org.fao.geonet.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.fao.geonet.domain.SettingDataType;

/**
 * An entity representing a system configuration setting.
 * <p/>
 * Settings are represented by a tree. One should use the
 * {@link org.fao.geonet.repository.HarvesterSettingRepository} to traverse the
 * hierarchy.
 * 
 * @author delawen
 */
@XmlRootElement(name = "setting")
public class SettingChild extends Setting {

	private static final long serialVersionUID = 3427895789235891L;
	private String value;
	private String dataTypeName;
	private String dataType;
	
	@XmlAttribute(name = "datatype")
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@XmlAttribute(name = "datatypeName")
	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	@XmlValue
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
