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


// Author : Pierre Mauduit <pierre.mauduit@camptocamp.com>
//
// This webservice allows retrieval of multiple layers / services from
// a selection set.
//
package org.fao.geonet.services.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;

import org.jdom.Element;
import org.jdom.xpath.XPath;

//=============================================================================

public class ExtractServicesLayers  implements Service {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dm = gc.getDataManager();
		UserSession us = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(us);

		Element ret = new Element("response");
		
		String paramId = Util.getParam(params, "id", null) ;
		ArrayList<String> lst = new ArrayList<String>();
		
		// case #1 : #id parameter is undefined
		if (paramId == null) {
		    synchronized(sm.getSelection("metadata")) {
		        for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
		            String uuid = (String) iter.next();
		            String id   = dm.getMetadataId(dbms, uuid);
		            lst.add(id);
		        }
		    }
		} else { // case #2 : id parameter has been passed
		    lst.add(paramId);
		}
		    

		for (Iterator<String> iter = lst.iterator(); iter.hasNext();) {   
		    String id = iter.next();

		    Element curMd = dm.getMetadata(context, id, false, false, false);

		    XPath xpath = XPath.newInstance("gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");

		    List<Element> elems ;

		    try {
		        elems = xpath.selectNodes(curMd);
		    }  catch (Exception e)  {
		        // Bad XML input ?
		        continue;
		    }

		    for (Iterator itelem = elems.iterator(); itelem.hasNext();) {
		        Element curnode    = (Element) itelem.next();
		        XPath pLinkage     = XPath.newInstance("gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
		        XPath pProtocol    = XPath.newInstance("gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString");
		        XPath pName        = XPath.newInstance("gmd:CI_OnlineResource/gmd:name/gco:CharacterString");
		        XPath pDescription = XPath.newInstance("gmd:CI_OnlineResource/gmd:description/gco:CharacterString");

		        Element eLinkage     = (Element) pLinkage.selectSingleNode(curnode);
		        Element eProtocol    = (Element) pProtocol.selectSingleNode(curnode);
		        Element eName        = (Element) pName.selectSingleNode(curnode);
		        Element eDescription = (Element) pDescription.selectSingleNode(curnode);

		        if (eLinkage == null)  {
		            continue;
		        }
		        if (eProtocol == null) {
		            continue;
		        }
		        if (eName == null)  {
		            continue;
		        }

		        String sLinkage     = eLinkage.getValue();
		        String sProtocol    = eProtocol.getValue();
		        String sName        = eName.getValue();
		        String sDescription = eDescription != null ? eDescription.getValue() : "";

		        if ((sLinkage == null) || (sLinkage.equals(""))) {
		            continue;
		        }
		        if ((sProtocol == null) || (sProtocol.equals(""))) {
		            continue;
		        }

		        String sProto2 = "WMS"; // by default

		        if (sProtocol.contains("OGC:WMS")) {
		            sProto2 = "WMS";
		        }
		        else if (sProtocol.contains("OGC:WFS")) {
		            sProto2 = "WFS";
		        }
		        else if (sProtocol.contains("OGC:WCS")) {
		            sProto2 = "WMS";
		        }
		        else {
		            continue;
		        }

		        // If no name, we are on a service
		        if ((sName == null) || (sName.equals(""))) {
		            Element retchildserv = new Element("service");
		            retchildserv.setAttribute("owsurl", sLinkage);
		            retchildserv.setAttribute("owstype", sProto2);
		            retchildserv.setAttribute("text", sDescription);
		            retchildserv.setAttribute("mdid", id);

		            ret.addContent(retchildserv);
		        }
		        // else it is a Layer
		        else {
		            Element retchildlayer = new Element("layer");
		            retchildlayer.setAttribute("owsurl", sLinkage);
		            retchildlayer.setAttribute("owstype", sProto2);
		            retchildlayer.setAttribute("layername", sName);
		            retchildlayer.setAttribute("title", sDescription);
		            retchildlayer.setAttribute("mdid", id);
		            ret.addContent(retchildlayer);
		        }
		    } // for
		} // iterates
		
		return ret;
	}
}
