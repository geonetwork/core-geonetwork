package org.fao.geonet.bean;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
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
@XmlRootElement(name = "setting")
@XmlType(propOrder = { "children" })
public class SettingParent extends Setting {

	private static final long serialVersionUID = -9076754640371279891L;
	private List<JAXBElement<? extends Setting>> children = null;

	@XmlAnyElement(lax = true)
	public List<JAXBElement<? extends Setting>> getChildren() {
		if (this.children == null) {
			this.children = new LinkedList<JAXBElement<? extends Setting>>();
		}		
		return children;
	}

	public void setChildren(List<JAXBElement<? extends Setting>> children) {
		this.children = children;
	}

	public void addChild(JAXBElement<? extends Setting> child) {
		this.getChildren().add(child);
	}

}
