package org.fao.geonet.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An entity representing a system configuration setting.
 * <p/>
 * Settings are represented by a tree. One should use the
 * {@link org.fao.geonet.repository.HarvesterSettingRepository} to traverse the
 * hierarchy.
 * 
 * @author delawen
 */
@XmlRootElement(name="setting")
@XmlType(propOrder={"name", "position"})
@javax.xml.bind.annotation.XmlTransient
public abstract class Setting implements Serializable {

	private static final long serialVersionUID = 5431195166983620627L;
	private String name;
	private int position;

	@XmlAttribute(name = "name") 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "position")
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
