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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import jeeves.resources.dbms.Dbms;
import org.jdom.Element;

//=============================================================================

public class LocalLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public Map<String, String> getLanguages(Dbms dbms) throws SQLException
	{
		HashMap<String, String> hm = new HashMap<String, String>();

		for (Object obj : dbms.select("SELECT * FROM Languages").getChildren())
		{
			Element lang = (Element) obj;
			hm.put(lang.getChildText("id"), lang.getChildText("name"));
		}

		return hm;
	}

	//-----------------------------------------------------------------------------

	public void insert(Dbms dbms, String baseTable, int id, String name,
									  Set<String> languages) throws SQLException
	{
		String query = "INSERT INTO "+ baseTable +"Des(idDes, langId, label) VALUES (?,?,?)";

		for (String langId : languages)
			dbms.execute(query, id, langId, name);
	}

	//-----------------------------------------------------------------------------

	public Element retrieve(Dbms dbms, String table) throws SQLException
	{
		return retrieve(dbms, table, null, null);
	}

	//-----------------------------------------------------------------------------

	public Element retrieve(Dbms dbms, String table, String where) throws SQLException
	{
		return retrieve(dbms, table, null, where);
	}

	//-----------------------------------------------------------------------------

	public Element retrieveById(Dbms dbms, String table, String id) throws SQLException
	{
		return retrieve(dbms, table, id, null);
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//-----------------------------------------------------------------------------

	private Element retrieve(Dbms dbms, String table, String id, String where)
												throws SQLException
	{
		String query1 = "SELECT * FROM "+table;
		String query2 = "SELECT * FROM "+table+"Des";

		if (id == null)
		{
			if (where != null)
				query1 += " WHERE "+ where;
		}
		else
		{
			query1 += " WHERE id="   +id;
			query2 += " WHERE idDes="+id;
		}

		Element result = dbms.select(query1);

		List base = result.getChildren();
		List des  = dbms.select(query2).getChildren();

		Iterator i = base.iterator();

		while (i.hasNext())
		{
			Element record = (Element) i.next();
			Element labels = new Element("label");

			record.addContent(labels);

			id = record.getChildText("id");

			for (int j=0;j<des.size(); j++)
			{
				Element loc = (Element) des.get(j);

				String iddes = loc.getChildText("iddes");
				String lang  = loc.getChildText("langid");
				String label = loc.getChildText("label");

				if (id.equals(iddes))
					labels.addContent(new Element(lang).setText(label));
			}
		}

		return result.setName(table.toLowerCase());
	}
}

//=============================================================================

