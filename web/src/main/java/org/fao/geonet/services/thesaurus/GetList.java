//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

//=============================================================================

/**
 * 
 * Retrieve Thesauri list.
 * 
 * @author mcoudert
 *
 */
public class GetList implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		Element response = new Element(Jeeves.Elem.RESPONSE);
		
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		ThesaurusManager th = gc.getThesaurusManager();
		Hashtable<String, Thesaurus> thTable = th.getThesauriTable();
		
		response.addContent(buildResultfromThTable(thTable, dbms));
		
		return response;
	}

	
	/**
	 * @param thTable
	 * @return {@link Element}
	 * @throws SQLException 
	 */
	private Element buildResultfromThTable(Hashtable<String, Thesaurus> thTable, Dbms dbms) throws SQLException {
		
		Element elRoot = new Element("thesauri");
		
		Enumeration<Thesaurus> e = thTable.elements();
		while (e.hasMoreElements()) {
		        Thesaurus currentTh = e.nextElement();
		        Element elLoop = new Element("thesaurus");
			
			Element elKey = new Element("key");
			String key = currentTh.getKey();
			elKey.addContent(key);
			
			Element elDname = new Element("dname");
			String dname = currentTh.getDname();
			elDname.addContent(dname);
			
			Element elFname = new Element("filename");
			String fname = currentTh.getFname();
			elFname.addContent(fname);
			
			Element elType = new Element("type");
			String type = currentTh.getType();
			elType.addContent(type);
			
			Element elActivated= new Element("activated");
			String activated = "y";
                        // Thesaurus are activated by default
                        String checkQuery = "SELECT count(*) as disabled FROM Thesaurus WHERE id = ? and activated = 'n'";
                        Element records = dbms.select(checkQuery, currentTh.getKey());
                        if (records.getChild("record").getChildText("disabled").equals("1")) {
                            activated = "n";
                        }
                        elActivated.setText(activated);
			
			elLoop.addContent(elKey);
			elLoop.addContent(elDname);
			elLoop.addContent(elFname);
			elLoop.addContent(elType);
			elLoop.addContent(elActivated);
			
			elRoot.addContent(elLoop);
		}
		
		return elRoot;
	}
}

// =============================================================================

