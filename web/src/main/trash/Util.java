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

package org.fao.geonet.apps.trash;

import jeeves.constants.ConfigFile;
import jeeves.resources.dbms.DbmsPool;
import jeeves.utils.Xml;
import org.jdom.Element;

import java.util.List;

public class Util {
    /**
     * TODO javadoc.
     *
     * @param configFile
     * @return
     * @throws Exception
     */
	public static DbmsPool getDbmsPool(String configFile) throws Exception {
		Element configRoot    = Xml.loadFile(configFile);
		Element resourcesRoot = configRoot.getChild(ConfigFile.Child.RESOURCES);
		List resList = resourcesRoot.getChildren(ConfigFile.Resources.Child.RESOURCE);

		for(int i=0; i<resList.size(); i++) {
			Element res = (Element) resList.get(i);
			String  name      = res.getChildText(ConfigFile.Resource.Child.NAME);

			if (name.equals("main-db")) {
				Element config    = res.getChild(ConfigFile.Resource.Child.CONFIG);
				DbmsPool pool = new DbmsPool();
				pool.init(name, config);
				return pool;
			}
		}
		throw new Exception("resource 'main-db' not found");
	}

    /**
     * TODO javadoc.
     *
     * @param configFile
     * @param pack
     * @return
     * @throws Exception
     */
	public static Element getServices(String configFile, String pack) throws Exception {
		Element configRoot = Xml.loadFile(configFile);
		List services = configRoot.getChildren(ConfigFile.Child.SERVICES);
		for (int i = 0; i < services.size(); i++) {
			Element ss = (Element)services.get(i);
			String p = ss.getAttributeValue(ConfigFile.Services.Attr.PACKAGE);
			if (p != null && p.equals(pack)) return ss;
		}
		return null;
	}
}