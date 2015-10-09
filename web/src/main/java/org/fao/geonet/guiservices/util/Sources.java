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

package org.fao.geonet.guiservices.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//=============================================================================

public class Sources implements Service
{
	private ServiceConfig _config;
	
	public void init(String appPath, ServiceConfig params) throws Exception {
		this._config = params;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm =gc.getSettingManager();

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		//--- create local node

		String name   = sm.getValue("system/site/name");
		String siteId = sm.getValue("system/site/siteId");

		Element local = new Element("record");

		local.addContent(new Element("name")  .setText(name));
		local.addContent(new Element("siteid").setText(siteId));

		//--- retrieve known nodes

		Element nodes = dbms.select("SELECT uuid as siteId, name FROM Sources");
		nodes.addContent(local);

		Element harvestingNode = sm.get("/harvesting", -1);
		
		String onlyLinkedSources = _config.getValue("onlyLinkedSources");
		if(harvestingNode == null || onlyLinkedSources==null || !onlyLinkedSources.equals("true")){
			return nodes;
		}
		
		// remove from the source list those are in the harvesting/settings table
		XPath xpathExpression = XPath.newInstance("children/node[value/text()='geonetwork']/children/site/children/name/value");
		Collection<Element> geonetworkHarvesting = xpathExpression.selectNodes(harvestingNode);
		List<String> harvestingSources = new LinkedList<String>();
		for(Element el : geonetworkHarvesting){
			harvestingSources.add(el.getText());
		}
		
		Element response = new Element(Jeeves.Elem.RESPONSE);
		String elName = null;
		for(Element el : (List<Element>)nodes.getChildren()){
			elName = el.getChild("name").getText();
			if(!harvestingSources.contains(elName)){
				response.addContent((Element)el.clone());
			}
		}
		return response;
	}
}

//=============================================================================

