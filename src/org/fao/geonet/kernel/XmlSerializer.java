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

package org.fao.geonet.kernel;

import java.sql.SQLException;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

/** This class is responsible of reading and writing xml on the database. It
  * works on tables like (id, data, lastChangeDate)
  */

public class XmlSerializer
{
	//--------------------------------------------------------------------------
	//---
	//--- API
	//---
	//--------------------------------------------------------------------------

	/** Retrieve the xml element which id matches the given one. The element is
	  * read from 'table' and the string read is converted into xml
	  */

	public static Element select(Dbms dbms, String table, String id) throws Exception
	{
		String query = "SELECT * FROM " + table + " WHERE id=?";

		Element rec = dbms.select(query, id).getChild(Jeeves.Elem.RECORD);

		if (rec == null)
			return null;

		Element xmlField = rec.getChild("data");
		String  xmlData  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xmlField.getText();

		return Xml.loadString(xmlData, false);
	}

	//--------------------------------------------------------------------------

	public static String insert(Dbms dbms, String schema, Element xml, int serial,
										 String source, String uuid) throws SQLException
	{
		return insert(dbms, schema, xml, serial, source, uuid, null, null, null);
	}

	//--------------------------------------------------------------------------

	public static String insert(Dbms dbms, String schema, Element xml, int serial,
										 String source, String uuid, String createDate,
										 String changeDate, String sourceUri) throws SQLException
	{
		String date = new ISODate().toString();

		if (createDate == null)
			createDate = date;

		if (changeDate == null)
			changeDate = date;

		String query = "INSERT INTO Metadata (id, schemaId, data, createDate, "+
							"changeDate, source, uuid) VALUES(?,?,?,?,?,?,?)";

		if (sourceUri != null)
			query = "INSERT INTO Metadata (id, schemaId, data, createDate, "+
					  "changeDate, source, uuid, sourceUri) VALUES(?,?,?,?,?,?,?,?)";

		Vector args = new Vector();

		args.add(new Integer(serial));
		args.add(schema);
		args.add(Xml.getString(xml));
		args.add(createDate);
		args.add(changeDate);
		args.add(source);
		args.add(uuid);

		if (sourceUri != null)
			args.add(sourceUri);

		dbms.execute(query, args.toArray());

		return Integer.toString(serial);
	}

	//--------------------------------------------------------------------------
	/** Updates an xml element into the database. The new data replaces the old one
	  */

	public static void update(Dbms dbms, String id, Element xml) throws SQLException
	{
		update(dbms, id, xml, null);
	}

	//--------------------------------------------------------------------------

	public static void update(Dbms dbms, String id, Element xml, String changeDate) throws SQLException
	{
		String query = "UPDATE Metadata SET data=?, changeDate=? WHERE id=?";

		Vector args = new Vector();

		args.add(Xml.getString(xml));

		if (changeDate == null)		args.add(new ISODate().toString());
			else							args.add(changeDate);

		args.add(new Integer(id));

		dbms.execute(query, args);
	}

	//--------------------------------------------------------------------------
	/** Deletes an xml element given its id
	  */

	public static void delete(Dbms dbms, String table, String id) throws SQLException
	{
		String query = "DELETE FROM " + table + " WHERE id=?";

		Vector args = new Vector();

		args.add(new Integer(id));

		dbms.execute(query, args);
	}
}

//=============================================================================

