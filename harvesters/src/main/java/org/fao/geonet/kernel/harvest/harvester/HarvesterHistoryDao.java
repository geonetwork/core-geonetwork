//=============================================================================
//=== Copyright (C) 2001-2011 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
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
package org.fao.geonet.kernel.harvest.harvester;

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.utils.SerialFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import java.sql.SQLException;
import java.util.List;

public class HarvesterHistoryDao {

	//---------------------------------------------------------------------------

	//---------------------------------------------------------------------------

	//---------------------------------------------------------------------------

	/** Deletes harvester history rows from the Harvester History table.
		*
		* @param dbms connection to dbms
		* @param ids list of ids to delete from history table 
		* @throws SQLException
		* @return number of history records deleted
		*/
	public static int deleteHistory(Dbms dbms, List<Element>ids) throws SQLException {

		int nrRecs = 0; 
		for (Element id : ids) {
			dbms.execute("DELETE from HarvestHistory "
		    	+        "WHERE id = ?", Integer.valueOf(id.getText()));
			nrRecs++;
			dbms.commit();
		}
		return nrRecs;
	}

	//---------------------------------------------------------------------------
	//
	// Private methods
	//
	//---------------------------------------------------------------------------

	/** Expands XML elements that are read from the database as strings
		*
		* @param result Query result returned as XML from dbms.select
		* @return
		*/
	private static Element expandRecords(Element result) {
		for (int i = 0; i < result.getContentSize(); i++) {
			Element record = (Element) result.getContent(i);	

			Element info = record.getChild("info");
			Element xml = null;
			try {
				xml = Xml.loadString(info.getValue(), false);
			} catch (Exception e) {
				xml = new Element("error").setText("Invalid XML harvester result: "+e.getMessage());	
				e.printStackTrace();
			}
			info.removeContent();
			info.addContent(xml);

			Element params = record.getChild("params");
			try {
				xml = Xml.loadString(params.getValue(), false);
			} catch (Exception e) {
				xml = new Element("error").setText("Invalid XML harvester params: "+e.getMessage());	
				e.printStackTrace();
			}
			params.removeContent();
			params.addContent(xml);
		}
		return result;
	}

}
