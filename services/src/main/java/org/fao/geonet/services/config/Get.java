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

import java.util.ArrayList;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.services.config.bean.Setting;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
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
 */
@Controller
public class Get implements Service {

	@Autowired
	private SettingManager sm;

	@RequestMapping(value = "/{lang}/admin.config.list", produces="application/xml")
	public @ResponseBody
	List<Setting> exec(
			@PathVariable String lang,
			@RequestParam(required = false, defaultValue = "false") Boolean asTree)
			throws Exception {

		Element system = sm.getAllAsXML(asTree);
		XMLOutputter outp = new XMLOutputter();
		String s = outp.outputString(system);

		System.out.println("---");
		System.out.println(s);
		System.out.println("---");

		List<Setting> res = new ArrayList<Setting>();
		Setting set = new Setting();
		set.setName("sdf");
		res.add(set);

		return res;
	}

	public SettingManager getSm() {
		return sm;
	}

	public void setSm(SettingManager sm) {
		this.sm = sm;
	}

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		boolean asTree = Util.getParam(params, "asTree", "true").equals("true");

		Element system = gc.getBean(SettingManager.class).getAllAsXML(asTree);
		return system;
	}
}
