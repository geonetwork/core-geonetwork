//==============================================================================
//===
//===   Setup
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

package org.fao.geonet.apps.setup;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.utils.Xml;
import org.dlib.tools.TextFileLoader;
import org.fao.geonet.apps.common.Starter;
import org.fao.geonet.apps.setup.druid.Import;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.util.McKoiDB;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import java.io.IOException;

//==============================================================================

public class Setup implements Starter
{
	private PrintStream out;

	private static final String MCKOI_JDBC   = "jdbc:mckoi://localhost/";
	private static final String MCKOI_CONFIG = "/web/WEB-INF/db/db.conf";

	//---------------------------------------------------------------------------
	//---
	//--- Starter interface
	//---
	//---------------------------------------------------------------------------

	public void start(String installDir) throws Exception
	{
		//--- create log file

		out = new PrintStream(new FileOutputStream(installDir +"/setup/setup.log"));

		log("=== Setup started ===================================================");

		//--- load configuration file to get some parameters

		Element config = getConfigFile(installDir +"/web/WEB-INF/config.xml");

		Element jdbc   = config .getChild(ConfigFile.Child.RESOURCES)
										.getChild(ConfigFile.Resources.Child.RESOURCE)
										.getChild(ConfigFile.Resource.Child.CONFIG);

		//--- setup McKoi data file (if the case)

		McKoiDB mcKoiDB = setupMcKoi(installDir, jdbc);

		TextFileLoader schema = getSchemaFile(installDir, jdbc);
		Dbms           dbms   = getDbms(jdbc);
		Connection     conn   = getConnection(dbms, jdbc);

		//--- from now, if we exit, we have to close the dbms connection

		try
		{
			log("Creating db schema...");
			createTables(dbms, schema);

			log("Filling db tables...");
			fillTables(installDir, conn, schema);

			log("Almost finished...");
			setupMetadata(installDir, dbms, config);

			log("Disconnecting...");
			dbms.disconnect();

			if (mcKoiDB != null)
			{
				log("Stopping McKoi server...");
				mcKoiDB.stop();
				log("Adding activator to config.xml...");
				transformConfig(installDir, config);
			}

			log("=== Setup ended =====================================================");
		}
		catch(Exception e)
		{
			log("");
			log("=== Raised Exception ================================================");

			Throwable t = e;

			while(t != null)
			{
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				log(sw.toString());
				t = t.getCause();
				log("-------------------------------------------------------------");
			}

			log("Disconnecting...");
			dbms.disconnect();
			log("Disconnected.");
			log("=== Setup ended =====================================================");

			throw e;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Setup McKoi dbms
	//---
	//---------------------------------------------------------------------------

	private McKoiDB setupMcKoi(String installDir, Element jdbc) throws Exception
	{
		String url      = jdbc.getChildText(Jeeves.Res.Pool.URL);
		String user     = jdbc.getChildText(Jeeves.Res.Pool.USER);
		String password = jdbc.getChildText(Jeeves.Res.Pool.PASSWORD);

		if (!url.equals(MCKOI_JDBC))
			return null;
		else
		{
			String dbPath = installDir + MCKOI_CONFIG;

			McKoiDB mcKoi = new McKoiDB();
			mcKoi.setConfigFile(dbPath);

			log("Creating McKoi data files in '"+ dbPath +"' ...");

			mcKoi.create(user, password);

			log("Starting McKoi server...");
			mcKoi.start();
			log("Ok, McKoi started.");

			return mcKoi;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element getConfigFile(String file) throws Exception
	{
		try
		{
			return Xml.loadFile(file);
		}
		catch (Exception e)
		{
			throw new Exception("Cannot load config file : "+file, e);
		}
	}

	//---------------------------------------------------------------------------

	private TextFileLoader getSchemaFile(String installDir, Element jdbc) throws Exception
	{
		String driver = jdbc.getChildText(Jeeves.Res.Pool.DRIVER);

		//--- find out which dbms schema to load

		String file = "create-db-mckoi.sql";

		if (driver.indexOf("oracle") != -1)
			file = "create-db-oracle.sql";

		else if (driver.indexOf("mysql") != -1)
			file = "create-db-mysql.sql";

		//--- load the dbms schema

		String filePath = installDir +"/setup/sql/"+file;

		TextFileLoader schema = new TextFileLoader(filePath);

		if (!schema.isLoaded())
			throw new Exception("Cannot open sql schema file : "+filePath);

		return schema;
	}

	//---------------------------------------------------------------------------

	private Dbms getDbms(Element jdbc) throws Exception
	{
		String driver = jdbc.getChildText(Jeeves.Res.Pool.DRIVER);
		String url    = jdbc.getChildText(Jeeves.Res.Pool.URL);

		try
		{
			return new Dbms(driver, url);
		}
		catch (ClassNotFoundException e)
		{
			throw new Exception("Dbms class not found : "+driver, e);
		}
	}

	//---------------------------------------------------------------------------

	private Connection getConnection(Dbms dbms, Element jdbc) throws Exception
	{
		String user   = jdbc.getChildText(Jeeves.Res.Pool.USER);
		String passwd = jdbc.getChildText(Jeeves.Res.Pool.PASSWORD);

		try
		{
			dbms.connect(user, passwd);

			return dbms.getConnection();
		}
		catch (SQLException e)
		{
			throw new Exception("Unable to connect to the dbms : "+e, e);
		}
	}

	//---------------------------------------------------------------------------

	private void transformConfig(String installDir, Element config) throws Exception
	{
		String styleSheet = installDir +"/setup/mckoi.xsl";

		//--- we don't want to overwrite the config.xml file during the transformation
		//--- because if there is anexception the config file gets corrupted

		config = Xml.transform(config, styleSheet);

		//--- get data to write

		byte[] data = Xml.getString(new Document(config)).getBytes("UTF-8");

		FileOutputStream os = new FileOutputStream(installDir +"/web/WEB-INF/config.xml");

		try
		{
			os.write(data);
		}
		finally
		{
			os.close();
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Schema creation & table filling
	//---
	//---------------------------------------------------------------------------

	private void createTables(Dbms dbms, TextFileLoader schema) throws Exception
	{
		StringBuffer sb = new StringBuffer();

		try
		{
			for (int i=0; i<schema.getRows();i++)
			{
				String row = schema.getRowAt(i);

				if (!row.startsWith("REM") && !row.startsWith("--") && !row.equals(""))
				{
					sb.append(" ");
					sb.append(row);

					if (row.endsWith(";"))
					{
						String sql = sb.toString();

						sql = sql.substring(0, sql.length() -1);
						dbms.execute(sql);
						sb = new StringBuffer();
					}
				}
			}

		}

	  // --- Errors handling ---

		catch (Exception e)
		{
			throw new Exception("Unable to execute sql statement : \n"+sb, e);
		}
	}

	//---------------------------------------------------------------------------

	private void fillTables(String installDir, Connection conn, TextFileLoader schema) throws Exception
	{
		String table=null;

		try
		{
			for(int i=0; i<schema.getRows(); i++)
			{
				String row = schema.getRowAt(i);

				if (row.startsWith("CREATE TABLE "))
				{
					StringTokenizer st = new StringTokenizer(row, " ");

					st.nextToken();
					st.nextToken();

					table = st.nextToken();

					String file  = installDir +"/setup/db/"+ table +".ddf";

					if (!new File(file).exists())
						log("   Skipping table : "+ table);
					else
					{
						log("   Filling table '"+ table +"' with data file '"+ file +"'");
						Import.load(conn, table, file);
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Unable to fill table : "+ table, e);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Setup resources link into metadata
	//---
	//---------------------------------------------------------------------------

	private void setupMetadata(String installDir, Dbms dbms, Element config) throws Exception
	{
		List list = dbms.select("SELECT * FROM Metadata").getChildren();
		dbms.commit();

		String baseURL = getBaseURL(installDir);
		String siteURL = getSiteURL(config, baseURL);

		for(int i=0; i<list.size(); i++)
		{
			Element record = (Element) list.get(i);

			String id     = record.getChildText("id");
			String schema = record.getChildText("schemaid");
			String data   = record.getChildText("data");
			String uuid   = record.getChildText("uuid");
			String date   = record.getChildText("createdate");

			Element md = updateFixedInfo(installDir, id, Xml.loadString(data, false),
												  uuid, date, schema, siteURL);

			XmlSerializer.update(dbms, id, md, date);
			//FIXME: some data changes, we should reindex the metadata
			dbms.commit();
		}
	}

	//--------------------------------------------------------------------------

	private Element updateFixedInfo(String installDir, String id, Element md,
											  String uuid, String date, String schema,
											  String siteURL) throws Exception
	{
		//--- setup environment

		Element env = new Element("env");

		env.addContent(new Element("id")      .setText(id));
		env.addContent(new Element("uuid")    .setText(uuid));
		env.addContent(new Element("currDate").setText(date));
		env.addContent(new Element("siteURL") .setText(siteURL));

		//--- setup root element

		Element root = new Element("root");

		root.addContent(md);
		root.addContent(env);

		//--- do an XSL  transformation

		String styleSheet = installDir +"/web/xml/schemas/"+schema+"/"+ Geonet.File.UPDATE_FIXED_INFO;

		return Xml.transform(root, styleSheet);
	}

	//--------------------------------------------------------------------------

	private String getSiteURL(Element config, String baseURL)
	{
		String defaultLang = config.getChild(ConfigFile.Child.DEFAULT)
											.getChildText(ConfigFile.Default.Child.LANGUAGE);

		ServiceConfig appHand = new ServiceConfig(config.getChild(ConfigFile.Child.APP_HANDLER).getChildren());

		//FIXME: the problem here is to obtain the baseURL

//		String publicHost = appHand.getMandatoryValue(Geonet.Config.PUBLIC_HOST);
//		String publicPort = appHand.getMandatoryValue(Geonet.Config.PUBLIC_PORT);
//		String locService = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/"+ defaultLang;
//
//		String siteURL = "http://" + publicHost + (publicPort == "80" ? "" : ":" + publicPort) + locService;
//
//		return siteURL;

		return "???";
	}

	//--------------------------------------------------------------------------

	private String getBaseURL(String installDir) throws Exception
	{
		Element web = Xml.loadFile(installDir +"/web/WEB-INF/web.xml");

		Namespace ns = Namespace.getNamespace("http://java.sun.com/xml/ns/j2ee");

		//FIXME: non c'è più in web.xml ! occorre prenderlo dal servlet in qualche modo
		return "/"+ web.getChildText("display-name", ns);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Logging facility
	//---
	//---------------------------------------------------------------------------

	private void log(String message)
	{
		out.println(message);
		out.flush();
	}
}

//==============================================================================

