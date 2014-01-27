//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.bean.Setting;
import org.fao.geonet.bean.SettingChild;
import org.fao.geonet.bean.SettingParent;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.SpringSettingManager;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//=============================================================================

/**
 * Return the list of languages
 * 
 * 
 */
@Controller
public class Get implements Service {

	@Autowired
	private SpringSettingManager sm;

	
	@RequestMapping(value = "/{lang}/admin.config.list@json", produces = "application/json")
	public @ResponseBody
	ReturnType json(
			@PathVariable String lang,
			@RequestParam(required = false, defaultValue = "false") Boolean asTree) {

		return xml(lang, asTree);
	}

	@RequestMapping(value = "/{lang}/admin.config.list", produces = "application/xml")
	public @ResponseBody
	ReturnType xml(
			@PathVariable String lang,
			@RequestParam(required = false, defaultValue = "false") Boolean asTree) {

		// TODO lang internationalization

		List<org.fao.geonet.domain.Setting> res = sm.getAllAsList();

		if (asTree) {
			List<JAXBElement<? extends Setting>> settings = new LinkedList<JAXBElement<? extends Setting>>();
			for (org.fao.geonet.domain.Setting s : res) {

				String name = s.getName();
				String setname = name.substring(name.lastIndexOf("/") + 1);
				name = name.substring(0, name.lastIndexOf("/"));
				String[] path = name.split("/");
				List<JAXBElement<? extends Setting>> previousParentList = settings;
				SettingParent currentParent = null;
				String tmppath = null;

				for (String step : path) {
					if (tmppath == null) {
						tmppath = step;
					} else {
						tmppath = tmppath + "/" + step;
					}
					currentParent = null;
					for (JAXBElement<? extends Setting> setting : previousParentList) {
						if (step.equals(setting.getName().getLocalPart())
								&& setting.getValue() instanceof SettingParent) {
							currentParent = (SettingParent) setting.getValue();
							break;
						}
					}

					if (currentParent == null) {
						currentParent = new SettingParent();
						currentParent.setName(tmppath);
						currentParent.setPosition(s.getPosition());

						JAXBElement<SettingParent> jaxbchild = new JAXBElement<SettingParent>(
								new QName(step), SettingParent.class,
								currentParent);
						previousParentList.add(jaxbchild);
					}
					previousParentList = currentParent.getChildren();
				}

				SettingChild child = new SettingChild();
				child.setValue(s.getValue());
				child.setDataTypeName(s.getDataType().toString());
				child.setDataType(Integer.valueOf(s.getDataType().ordinal())
						.toString());
				child.setName(s.getName());
				child.setPosition(s.getPosition());

				JAXBElement<SettingChild> setting = new JAXBElement<SettingChild>(
						new QName(setname), SettingChild.class, child);

				currentParent.addChild(setting);
			}
			return new TreeSettings(settings);
		} else {
			List<Setting> settings = new LinkedList<Setting>();
			for (org.fao.geonet.domain.Setting s : res) {
				SettingChild setting = new SettingChild();
				setting.setName(s.getName());
				setting.setPosition(s.getPosition());
				setting.setValue(s.getValue());
				setting.setDataType(s.getDataType().toString());
				settings.add(setting);
			}
			return new Settings(settings);
		}
	}

	public SpringSettingManager getSm() {
		return sm;
	}

	public void setSm(SpringSettingManager sm) {
		this.sm = sm;
	}

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	@Override
	@Deprecated
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		boolean asTree = Util.getParam(params, "asTree", "true").equals("true");

		Element system = gc.getBean(SettingManager.class).getAllAsXML(asTree);
		return system;
	}

}

@XmlRootElement(name = "settings")
@XmlSeeAlso({ Setting.class })
class ReturnType {

}

@XmlRootElement(name = "settings")
@XmlSeeAlso({ Setting.class, SettingParent.class, SettingChild.class })
class Settings extends ReturnType {
	protected List<Setting> setting;

	public Settings() {
	}

	public Settings(List<Setting> list) {
		this.setting = list;
	}

	@XmlElement(name = "setting", type = Setting.class)
	public List<Setting> getSetting() {
		return setting;
	}
}

@XmlRootElement(name = "settings")
@XmlSeeAlso({ Setting.class, SettingParent.class, SettingChild.class })
class TreeSettings extends ReturnType {
	protected List<JAXBElement<? extends Setting>> setting;

	public TreeSettings() {
	}

	public TreeSettings(List<JAXBElement<? extends Setting>> list) {
		this.setting = list;
	}

	@XmlAnyElement(lax = true)
	public List<JAXBElement<? extends Setting>> getSetting() {
		return setting;
	}
}
