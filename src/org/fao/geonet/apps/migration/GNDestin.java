//==============================================================================
//===
//===   GNDestin
//===
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

package org.fao.geonet.apps.migration;

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
import org.fao.geonet.exceptions.GeoNetException;
import org.fao.geonet.kernel.XmlSerializer;
import org.jdom.Element;
import org.jdom.Namespace;

//==============================================================================

public class GNDestin
{
	private String  geonetDir;
	private Element config;
	private Element dbRes;
	private Element jdbc;
	private Element profiles;

	private ServiceConfig appHand;
	private Activator     activ;
	private Dbms          dbms;
	private Connection    conn;
	private SimpleLogger  logger;

	private String baseURL;
	private String siteURL;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GNDestin(String dir, SimpleLogger l)
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

	public void open() throws GeoNetException
	{
		//--- load config file
		config = Util.getConfigFile(logger, geonetDir +"web/WEB-INF/config.xml");

		dbRes  = Util.getDBResource(config);
		activ  = Util.getActivator(logger, geonetDir, dbRes);
		jdbc   = dbRes.getChild(ConfigFile.Resource.Child.CONFIG);

		appHand = new ServiceConfig(config.getChild(ConfigFile.Child.APP_HANDLER).getChildren());

		dbms    = Util.getDbms(logger, jdbc);
		conn    = Util.getConnection(logger, dbms, jdbc);
		profiles= Util.getUserProfiles(logger, geonetDir +"web/xml/user-profiles.xml");

		baseURL = getBaseURL();
		siteURL = getSiteURL();
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

	public void commit() throws GeoNetException
	{
		try
		{
			dbms.commit();
		}
		catch (SQLException e)
		{
			throw geoNetExc(e, "Cannot commit");
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

	public Set<String> getAllIsoMetadataId() throws GeoNetException
	{
		String query = "SELECT id FROM Metadata WHERE schemaId='iso19115'";

		try
		{
			List list = dbms.select(query).getChildren();

			Set<String> ids = new HashSet<String>();

			for(int i=0; i<list.size(); i++)
			{
				Element rec = (Element) list.get(i);
				ids.add(rec.getChildText("id"));
			}

			return ids;
		}
		catch (SQLException e)
		{
			throw geoNetExc(e, "Cannot select query : "+ query);
		}
	}

	//---------------------------------------------------------------------------

	public Element getMetadata(String id) throws GeoNetException
	{
		try
		{
			Element md = XmlSerializer.select(dbms, "Metadata", id);

			if (md == null)
				return null;

			md.detach();

			return md;
		}
		catch (Exception e)
		{
			throw geoNetExc(e, "Cannot retrieve metadata with id : "+ id);
		}
	}

	//---------------------------------------------------------------------------

	public Element getUnmappedFields(Element md) throws GeoNetException
	{
		try
		{
			return Xml.transform(md, geonetDir +"migrate/xsl/unmapped.xsl");
		}
		catch (Exception e)
		{
			throw geoNetExc(e, "Cannot get unmapped fields");
		}
	}

	//---------------------------------------------------------------------------

	public void upgradeMetadata(String id, Element md) throws GeoNetException
	{
		//--- step 1 : convert metadata from ISO19115 to ISO19139

		try
		{
			md = Xml.transform(md, geonetDir +"migrate/xsl/19115-to-19139.xsl");
		}
		catch (Exception e)
		{
			throw geoNetExc(e, "Cannot transform metadata to ISO19139");
		}

		//--- step 2 : save new metadata

		try
		{
			String query = "UPDATE Metadata SET schemaId='iso19139', data=? WHERE id=?";

			Vector args = new Vector();

			args.add(Xml.getString(md));
			args.add(new Integer(id));

			dbms.execute(query, args);
		}
		catch (Exception e)
		{
			throw geoNetExc(e, "Cannot upgrade metadata with id : "+ id);
		}
	}

	//---------------------------------------------------------------------------

	public void removeLuceneFiles() throws GeoNetException
	{
		String luceneDir = geonetDir +"web/"+ appHand.getValue(Geonet.Config.LUCENE_DIR);

		cleanDir(new File(luceneDir));
	}

	//---------------------------------------------------------------------------

	public void addMetadata(List list) throws GeoNetException
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
				Element md = updateFixedInfo(schema, id, Xml.loadString(data, false), uuid, date);

				XmlSerializer.insert(dbms, schema, md, Integer.parseInt(id), source, uuid, date, date, null);
			}
			catch (Exception e)
			{
				throw geoNetExc(e, "Cannot migrate metadata with id : "+ id);
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void cleanDir(File dir) throws GeoNetException
	{
		File files[] = dir.listFiles();

		for(int i=0; i<files.length; i++)
			if (files[i].isDirectory())
			{
				if (!files[i].getName().equals("CVS"))
					cleanDir(files[i]);
			}
			else if (!files[i].delete())
			{
				throw geoNetExc(null, "Cannot delete file : "+files[i]);
			}
	}

	//---------------------------------------------------------------------------

	private void insertRecords(String table, List records, String fields[],
										Mapper mapper) throws GeoNetException
	{
		for(int i=0; i<records.size(); i++)
		{
			Element rec = (Element) records.get(i);

			insertRecord(table, rec, fields, mapper);
		}
	}

	//---------------------------------------------------------------------------

	private void insertRecord(String table, Element rec, String fields[],
									  Mapper mapper) throws GeoNetException
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
			dbms.execute(query, values);
		}
		catch (SQLException e)
		{
			logger.logError("Cannot add a new record in table : "+ table);
			logger.logError("Error : "+ e.getMessage());
			logger.logError("Query : "+ query);

			for(int i=0; i<values.size(); i++)
				logger.logError("   - Value : "+ values.get(i));

			throw new GeoNetException("");
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

	private Element updateFixedInfo(String schema, String id, Element md, String uuid, String date) throws Exception
	{
		//--- setup environment

		Element env = new Element("env");

		env.addContent(new Element("id")      .setText(id));
		env.addContent(new Element("uuid")    .setText(uuid));
		env.addContent(new Element("currDate").setText(date));
		env.addContent(new Element("siteURL").setText(siteURL));

		//--- setup root element

		Element root = new Element("root");

		root.addContent(md);
		root.addContent(env);

		//--- do an XSL  transformation

		String styleSheet = geonetDir +"web/xml/schemas/"+ schema +"/"+ Geonet.File.UPDATE_FIXED_INFO;

		return Xml.transform(root, styleSheet);
	}

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

	//--------------------------------------------------------------------------

	private String getBaseURL() throws GeoNetException
	{
		Element web = Util.getConfigFile(logger, geonetDir +"web/WEB-INF/web.xml");

		if (web == null)
			throw new GeoNetException("");

		Namespace ns = Namespace.getNamespace("http://java.sun.com/xml/ns/j2ee");

		return "/"+ web.getChildText("display-name", ns);
	}

	//--------------------------------------------------------------------------

	private String getSiteURL()
	{
		String defaultLang = config.getChild(ConfigFile.Child.DEFAULT)
											.getChildText(ConfigFile.Default.Child.LANGUAGE);

		String publicHost = appHand.getMandatoryValue(Geonet.Config.PUBLIC_HOST);
		String publicPort = appHand.getMandatoryValue(Geonet.Config.PUBLIC_PORT);
		String locService = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/"+ defaultLang;

		String siteURL = "http://" + publicHost + (publicPort == "80" ? "" : ":" + publicPort) + locService;

		return siteURL;
	}

	//--------------------------------------------------------------------------

	private GeoNetException geoNetExc(Exception e, String message)
	{
		logger.logError(message);

		if (e != null)
			logger.logError("Error : "+ e.getMessage());

		return new GeoNetException("");
	}
}

//==============================================================================

interface Mapper
{
	public String map(String field, String value);
}

//==============================================================================

