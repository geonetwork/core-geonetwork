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
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HarvesterHistoryDao {

	//---------------------------------------------------------------------------

	/** Write the harvester history Dao to the database
		* 
		* @param dbms The database connection to use
		* @param sf The serial factory to get the next available row id from
		* @param type The harvester type
		* @param name The name of the harvester
		* @param uuid The uuid of the harvester (allocated on creation)
		* @param runDate The date the harvester was run
		* @param node XML describing the harvester parameters (info node removed)
		* @param result XML describing the result that the harvester produced
		*/
	public static void write(Dbms dbms, SerialFactory sf, String type, String name, String uuid, long elapsedTime, String runDate, Element node, Element result) {
		try {
			//--- generate a new id
			int hhId = sf.getSerial(dbms, "HarvestHistory");
			
			String query = "INSERT INTO HarvestHistory (id,harvestDate,harvesterUuid,"
						+        "harvesterName,harvesterType,deleted,info,params,elapsedTime)";
      query +=       "VALUES (?,?,?,?,?,?,?,?,?)";
      int res = dbms.execute(
          query,
          hhId,
          runDate,
          uuid,
          name,
          type,
          "n",
          Xml.getString(result),
          Xml.getString(node), elapsedTime);
				dbms.commit();
    } catch (SQLException sqle) {
      	dbms.abort();
      	Log.warning(Geonet.HARVESTER, "Error occured when writing HarvestHistory. Aborting :" + sqle.getMessage());
		}
	}

	//---------------------------------------------------------------------------

	/** Retrieves the harvester history for all harvesters
		*
		* @param dbms connection to dbms
		* @param sortCriteria which field to sort on, has values 'date' or 'type'
		* @return
		* @throws SQLException
		*/
	public static Element retrieveSort(Dbms dbms, String sortCriteria) throws SQLException {
		String query = "SELECT * FROM HarvestHistory ";
		if (sortCriteria.equals("date")) {
			query += "ORDER BY harvestDate DESC, harvesterUuid";
		} else {
			query += "ORDER BY harvesterType, harvestDate DESC";
		}
		Element result = dbms.select(query);
		return expandRecords(result);
	}

	//---------------------------------------------------------------------------

	/** Retrieves the harvester history for a specific harvester uuid
		*
		* @param dbms connection to dbms
		* @param uuid uuid of harvester to retrieve history for
		* @return
		* @throws SQLException
		*/
	public static Element retrieve(Dbms dbms, String uuid) throws SQLException {

		String query = "SELECT * FROM HarvestHistory WHERE deleted = 'n' "
		      +        "AND harvesterUuid = ? "
					+				 "ORDER BY harvestDate DESC, harvesterUuid";

		Element result = dbms.select(query, uuid);
		return expandRecords(result);
	}

	//---------------------------------------------------------------------------

	/** Sets the deleted status in the harvester history for a harvester node.
	  * Should be called when the harvester is deleted in the HarvestManager.
		*
		* @param dbms connection to dbms
		* @param uuid uuid of harvester to set deleted status for
		* @throws SQLException
		*/
	public static void setDeleted(Dbms dbms, String uuid) throws SQLException {

		dbms.execute("UPDATE HarvestHistory SET deleted = 'y' "
		    +        "WHERE harvesterUuid = ?", uuid);
	}

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
		    	+        "WHERE id = ?", new Integer(id.getText()));
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
				xml.addContent(new Element("error").setText("Invalid XML harvester result: "+e.getMessage()));	
				e.printStackTrace();
			}
			info.removeContent();
			info.addContent(xml);

			Element params = record.getChild("params");
			try {
				xml = Xml.loadString(params.getValue(), false);
			} catch (Exception e) {
				xml.addContent(new Element("error").setText("Invalid XML harvester params: "+e.getMessage()));	
				e.printStackTrace();
			}
			params.removeContent();
			params.addContent(xml);
		}
		return result;
	}

}
