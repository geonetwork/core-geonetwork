package org.fao.geonet.services.config.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="settings")
public class Setting implements Serializable {
	private static final long serialVersionUID = 8015321734202285414L;

	private String name;
	private int position;

	@XmlAttribute(name = "@name") 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "@position")
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}