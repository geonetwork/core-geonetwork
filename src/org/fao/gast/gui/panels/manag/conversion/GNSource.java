//==============================================================================
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

package org.fao.gast.gui.panels.manag.conversion;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;
import org.fao.gast.lib.ConfigLib;
import org.fao.gast.lib.ProfileLib;
import org.fao.geonet.kernel.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;

//==============================================================================

public class GNSource
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GNSource(String oldAppPath) throws JDOMException, IOException
	{
		appPath = oldAppPath;

		config   = new ConfigLib (oldAppPath);
		profiles = new ProfileLib(oldAppPath);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API Methods
	//---
	//---------------------------------------------------------------------------

	public Set<String> getAllIsoMetadataId(Dbms dbms) throws Exception
	{
		String query = "SELECT id FROM Metadata WHERE schemaId='iso19115'";

//		try
//		{
			List list = dbms.select(query).getChildren();

			Set<String> ids = new HashSet<String>();

			for(int i=0; i<list.size(); i++)
			{
				Element rec = (Element) list.get(i);
				ids.add(rec.getChildText("id"));
			}

			return ids;
//		}
//		catch (SQLException e)
//		{
//			throw geoNetExc(e, "Cannot select query : "+ query);
//		}
	}

	//---------------------------------------------------------------------------

	public Element getMetadata(Dbms dbms, String id) throws Exception
	{
//		try
//		{
			return XmlSerializer.select(dbms, "Metadata", id);
//		}
//		catch (Exception e)
//		{
//			throw geoNetExc(e, "Cannot retrieve metadata with id : "+ id);
//		}
	}

	//---------------------------------------------------------------------------

	public Element getUnmappedFields(Element md) throws Exception
	{
//		try
//		{
			return Xml.transform(md, appPath +"/gast/xsl/unmapped.xsl");
//		}
//		catch (Exception e)
//		{
//			throw geoNetExc(e, "Cannot get unmapped fields");
//		}
	}

	//---------------------------------------------------------------------------

	public void upgradeMetadata(Dbms dbms, String id, Element md) throws Exception
	{
		//--- step 1 : convert metadata from ISO19115 to ISO19139

		try
		{
			md = Xml.transform(md, appPath +"/gast/xsl/19115-to-19139.xsl");
		}
		catch (Exception e)
		{
//			throw geoNetExc(e, "Cannot transform metadata to ISO19139");
		}

		//--- step 2 : save new metadata

		try
		{
			String query = "UPDATE Metadata SET schemaId='iso19139', data=? WHERE id=?";

			dbms.execute(query, Xml.getString(md), new Integer(id));
		}
		catch (Exception e)
		{
//			throw geoNetExc(e, "Cannot upgrade metadata with id : "+ id);
		}
	}

	//---------------------------------------------------------------------------

	public void addMetadata(Dbms dbms, List list) throws Exception
	{
		String schema = "iso19115";
		String source = ""; //appHand.getValue("siteId");

		for(int i=0; i<list.size(); i++)
		{
			Element rec = (Element) list.get(i);

			String id   = rec.getChildText("id");
			String data = rec.getChildText("data");
			String date = rec.getChildText("lastchangedate");
			String uuid = UUID.randomUUID().toString();

			try
			{
//				Element md = updateFixedInfo(schema, id, Xml.loadString(data, false), uuid, date);
//
//				XmlSerializer.insert(dbms, schema, md, Integer.parseInt(id), source, uuid, date, date, null);
			}
			catch (Exception e)
			{
//				throw geoNetExc(e, "Cannot migrate metadata with id : "+ id);
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String     appPath;
	public  ConfigLib  config;
	public  ProfileLib profiles;
}

//==============================================================================


