//==============================================================================
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

package org.fao.gast.gui.panels.migration.oldinst;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Activator;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.utils.Xml;
import org.fao.geonet.apps.common.SimpleLogger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.XmlSerializer;
import org.jdom.Element;
import org.jdom.Namespace;

//==============================================================================

public class GNSource
{
	private String  geonetDir;
	private Element config;
	private Element dbRes;
	private Element jdbc;
	private Element profiles;

	private ServiceConfig appHand;
	private Activator     activ;
	private Dbms          dbms;
	private SimpleLogger  logger;

	private String baseURL;
	private String siteURL;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GNSource(String dir, SimpleLogger l)
	{
		if (!dir.endsWith(File.separator))
			dir += File.separator;

		geonetDir = dir;
		logger    = l;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API Methods
	//---
	//---------------------------------------------------------------------------

	public void open() throws Exception
	{
		//--- load config file
		config = Util.getConfigFile(logger, geonetDir +"web/WEB-INF/config.xml");

		dbRes  = Util.getDBResource(config);
//		activ  = Util.getActivator(logger, geonetDir, dbRes);
		jdbc   = dbRes.getChild(ConfigFile.Resource.Child.CONFIG);

		appHand = new ServiceConfig(config.getChild(ConfigFile.Child.APP_HANDLER).getChildren());

//		dbms    = Util.getDbms(logger, jdbc);
//		Util.connect(logger, dbms, jdbc);
		profiles= Util.getUserProfiles(logger, geonetDir +"web/xml/user-profiles.xml");

//		baseURL = getBaseURL();
//		siteURL = getSiteURL();
	}

	//---------------------------------------------------------------------------

	public void close()
	{
		if (dbms != null)
		{
			logger.logInfo("Disconnecting from DBMS...");
			dbms.disconnect();
		}

		if (activ != null)
		{
			logger.logInfo("Stopping activator...");
			activ.shutdown();
		}
	}

	//---------------------------------------------------------------------------

	public void commit() throws Exception
	{
		try
		{
			dbms.commit();
		}
		catch (SQLException e)
		{
//			throw geoNetExc(e, "Cannot commit");
		}
	}

	//---------------------------------------------------------------------------

	public void abort()
	{
		try
		{
			dbms.commit();
		}
		catch (SQLException e)
		{
			logger.logError("Cannot abort");
			logger.logError("Error : "+ e.getMessage());
		}
	}

	//---------------------------------------------------------------------------

	public Set<String> getAllIsoMetadataId() throws Exception
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

	public Element getMetadata(String id) throws Exception
	{
//		try
//		{
			Element md = XmlSerializer.select(dbms, "Metadata", id);

			if (md == null)
				return null;

			md.detach();

			return md;
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
			return Xml.transform(md, geonetDir +"migrate/xsl/unmapped.xsl");
//		}
//		catch (Exception e)
//		{
//			throw geoNetExc(e, "Cannot get unmapped fields");
//		}
	}

	//---------------------------------------------------------------------------

	public void upgradeMetadata(String id, Element md) throws Exception
	{
		//--- step 1 : convert metadata from ISO19115 to ISO19139

		try
		{
			md = Xml.transform(md, geonetDir +"migrate/xsl/19115-to-19139.xsl");
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

	public void removeLuceneFiles() throws Exception
	{
		String luceneDir = geonetDir +"web/"+ appHand.getValue(Geonet.Config.LUCENE_DIR);

//		cleanDir(new File(luceneDir));
	}

	//---------------------------------------------------------------------------

	public void addMetadata(List list) throws Exception
	{
		logger.logInfo("Migrating "+ list.size() +" metadata");

		String schema = "iso19115";
		String source = appHand.getValue("siteId");

		for(int i=0; i<list.size(); i++)
		{
			Element rec = (Element) list.get(i);

			String id   = rec.getChildText("id");
			String data = rec.getChildText("data");
			String date = rec.getChildText("lastchangedate");
			String uuid = UUID.randomUUID().toString();

			if (i % 100 == 0)
				logger.logInfo("   - Migrated "+ i +" metadata");

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

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void insertRecords(String table, List records, String fields[],
										Mapper mapper) throws Exception
	{
		for(int i=0; i<records.size(); i++)
		{
			Element rec = (Element) records.get(i);

			insertRecord(table, rec, fields, mapper);
		}
	}

	//---------------------------------------------------------------------------

	private void insertRecord(String table, Element rec, String fields[],
									  Mapper mapper) throws Exception
	{
		StringBuffer names = new StringBuffer();
		StringBuffer marks = new StringBuffer();

		Vector values = new Vector();

		for(int i=0; i<fields.length; i++)
		{
			names.append(fields[i]);
			marks.append("?");

			String oldValue = rec.getChildText(fields[i].toLowerCase());
			String newValue = (mapper == null)
										? oldValue
										: mapper.map(fields[i], oldValue);

			values.add(newValue);

			if (i <fields.length -1)
			{
				names.append(", ");
				marks.append(", ");
			}
		}

		String query = "INSERT INTO "+ table +"("+ names +") VALUES ("+ marks +")";

		try
		{
			dbms.execute(query, values.toArray());
		}
		catch (SQLException e)
		{
			logger.logError("Cannot add a new record in table : "+ table);
			logger.logError("Error : "+ e.getMessage());
			logger.logError("Query : "+ query);

			for(int i=0; i<values.size(); i++)
				logger.logError("   - Value : "+ values.get(i));

			throw new Exception("");
		}
	}

	//---------------------------------------------------------------------------

	private boolean existsProfile(String name)
	{
		List list = profiles.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

			if (name.equals(el.getAttributeValue("name")))
				return true;
		}

		return false;
	}

	//---------------------------------------------------------------------------

	private String getDefaultProfile()
	{
		List list = profiles.getChildren();

		Element last = (Element) list.get(list.size() -1);

		return last.getAttributeValue("name");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata specific private methods
	//---
	//--------------------------------------------------------------------------

	private Element getThumbnails(String schema, String id) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		if (md == null)
			return null;

		md.detach();

		//--- do an XSL  transformation

		String styleSheet = geonetDir +"web/xml/schemas/"+ schema +"/"+ Geonet.File.EXTRACT_THUMBNAILS;

		Element result = Xml.transform(md, styleSheet);

		return result;
	}
}

//==============================================================================

interface Mapper
{
	public String map(String field, String value);
}

//==============================================================================

