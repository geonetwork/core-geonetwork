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

package org.fao.geonet.lib;

import jeeves.resources.dbms.Dbms;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================

public class LocalLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------
    public List<String> getLanguagesInspire(Dbms dbms) throws SQLException
	{
		List<String> hm = new ArrayList<String>();

		for (Object obj : dbms.select("SELECT * FROM Languages WHERE isInspire='y'").getChildren())
		{
			Element lang = (Element) obj;
            hm.add(lang.getChildText("id"));
		}

		return hm;
	}

    public String getDefaultLanguage(Dbms dbms) throws SQLException
	{
		for (Object obj : dbms.select("SELECT * FROM Languages WHERE isDefault='y'").getChildren())
		{
			Element lang = (Element) obj;
			return lang.getChildText("id");
		}

		return null;
	}

    public Map<String, String> getLanguagesIso(Dbms dbms) throws SQLException
	{
		Map<String, String> hm = new HashMap<String, String>();

		for (Object obj : dbms.select("SELECT * FROM Languages").getChildren())
		{
			Element lang = (Element) obj;
			hm.put(lang.getChildText("id"), lang.getChildText("isocode"));
		}

		return hm;
	}

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

	public void insert(Dbms dbms, String baseTable, int id, String name) throws SQLException
	{
		Set<String> langs = getLanguages(dbms).keySet();

		String query = "INSERT INTO "+ baseTable +"Des(idDes, langId, label) VALUES (?,?,?)";

		for (String langId : langs)
			dbms.execute(query, id, langId, name);
	}

	//-----------------------------------------------------------------------------

	public void insert(Dbms dbms, String baseTable, int id, Map<String, String> locNames,
							 String defName) throws SQLException
	{
		Set<String> langs = getLanguages(dbms).keySet();

		String query = "INSERT INTO "+ baseTable +"Des(idDes, langId, label) VALUES (?,?,?)";

		for (String langId : langs)
		{
			String name = (locNames == null) ? null : locNames.get(langId);

			//--- check if the local language does not exist in locNames
			//--- this will help to align languages with remote sites

			if (name == null)
				name = defName;

			dbms.execute(query, id, langId, name);
		}
	}

	//-----------------------------------------------------------------------------

	public void update(Dbms dbms, String baseTable, int id, Element label) throws SQLException
	{
		@SuppressWarnings("unchecked")
        List<Element> labels = label.getChildren();

		for (Element locText : labels)
		{
			String langId = locText.getName();
			String value  = locText.getText();

			update(dbms, baseTable, id, langId, value);
		}
	}

	//-----------------------------------------------------------------------------

	public void update(Dbms dbms, String baseTable, int id, String langId,
							 String label) throws SQLException
	{
		String query = "UPDATE "+ baseTable +"Des SET label=? WHERE idDes=? AND langId=?";

		dbms.execute(query, label, id, langId);
	}

	//-----------------------------------------------------------------------------

	public Element retrieve(Dbms dbms, String table) throws SQLException
	{
		return retrieve(dbms, table, null, null, null, (Object[])null);
	}

	//-----------------------------------------------------------------------------

	public Element retrieveWhere(Dbms dbms, String table, String where, Object... args) throws SQLException
	{
		return retrieve(dbms, table, null, where, null, args);
	}

	//-----------------------------------------------------------------------------

	public Element retrieveWhereOrderBy(Dbms dbms, String table, String where, String orderBy, Object... args) throws SQLException
	{
		return retrieve(dbms, table, null, where, orderBy, args);
	}

	//-----------------------------------------------------------------------------

	public Element retrieveById(Dbms dbms, String table, String id) throws SQLException
	{
		return retrieve(dbms, table, id, null, null, Integer.valueOf(id));
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//-----------------------------------------------------------------------------

	private Element retrieve(Dbms dbms, String table, String id, String where, String orderBy, Object... args)
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
			query1 += " WHERE id=?";
			query2 += " WHERE idDes=?";
		}

		if (orderBy != null)
			query1 += " ORDER BY "+ orderBy;

		Element result = dbms.select(query1, args);

		@SuppressWarnings("unchecked")
        List<Element> base = result.getChildren();

		final List<Element> des;
		if (id != null) {
		    @SuppressWarnings("unchecked")
            final List<Element> tmp = dbms.select(query2, args).getChildren();
		    des = tmp;
		}
		else {
		    @SuppressWarnings("unchecked")
            final List<Element> tmp = dbms.select(query2).getChildren();
		    des = tmp;
		}

		//--- preprocess data for faster access

		Map<String, List<String>> langData = new HashMap<String, List<String>>();

		for (Object o : des)
		{
			Element loc = (Element) o;

			String iddes = loc.getChildText("iddes");
			String lang  = loc.getChildText("langid");
			String label = loc.getChildText("label");

			List<String> list = langData.get(iddes);

			if (list == null)
			{
				list = new ArrayList<String>();
				langData.put(iddes, list);
			}

			list.add(lang);
			list.add(label);
		}

		//--- fill results

		for (Object o : base)
		{
			Element record = (Element) o;
			Element labels = new Element("label");

			record.addContent(labels);

			id = record.getChildText("id");

			List<String> list = langData.get(id);
			if(list != null) {
				for (int j=0; j<list.size(); j+=2) {
					labels.addContent(new Element(list.get(j)).setText(list.get(j+1)));
				}
			}
			else {
				System.out.println("WARNING: CORRUPT DATA IN DATABASE: No localization found for record in table " + table + " with id: " + id + ", skipping it.");

			}

		}

		return result.setName(table.toLowerCase());
	}
}

//=============================================================================

