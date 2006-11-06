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

package org.fao.geonet.lib;

import java.sql.SQLException;
import java.util.Iterator;
import jeeves.resources.dbms.Dbms;
import org.jdom.Element;

//=============================================================================

public class DbLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public Element select(Dbms dbms, String table, String name) throws SQLException
	{
		return select(dbms, table, name, null);
	}

	//-----------------------------------------------------------------------------

	public Element select(Dbms dbms, String table, String name, String where) throws SQLException
	{
		String query = "SELECT * FROM "+table;

		if (where != null)
			query += " WHERE "+ where;

		Element result = dbms.select(query);

		Iterator i = result.getChildren().iterator();

		while (i.hasNext())
		{
			Element record = (Element) i.next();
			record.setName(name);
		}

		return result.setName(table.toLowerCase());
	}
}

//=============================================================================

