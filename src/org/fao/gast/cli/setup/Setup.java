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

package org.fao.gast.cli.setup;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.utils.Xml;
import org.dlib.tools.TextFileLoader;
import org.fao.gast.boot.Starter;
import org.fao.gast.lib.druid.Import;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.util.McKoiDB;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

//==============================================================================

public class Setup
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

		//--- setup McKoi data file (if the case)

//		McKoiDB mcKoiDB = setupMcKoi(installDir, jdbc);
//
//		TextFileLoader schema = getSchemaFile(installDir, jdbc);
//		Dbms           dbms   = getDbms(jdbc);
//		connect(dbms, jdbc);

		//--- from now, if we exit, we have to close the dbms connection

		try
		{
			log("Creating db schema...");
//			createTables(dbms, schema);

			log("Filling db tables...");
//			fillTables(installDir, dbms, schema);

			log("Almost finished...");

			//--- we don't provide any data by default.
			//--- the GAST tool should be used to populate the db

			//			setupMetadata(installDir, dbms, config);
//			setupSite(installDir, dbms);
//			log("Disconnecting...");
//			dbms.disconnect();
//
//			if (mcKoiDB != null)
//			{
//				log("Stopping McKoi server...");
//				mcKoiDB.stop();
//				log("Adding activator to config.xml...");
//				transformConfig(installDir, config);
//			}

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
//			dbms.disconnect();
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

		if (!url.equals(MCKOI_JDBC))
			return null;
		else
		{
			String dbPath = installDir + MCKOI_CONFIG;

			McKoiDB mcKoi = new McKoiDB();
			mcKoi.setConfigFile(dbPath);

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

	private void connect(Dbms dbms, Element jdbc) throws Exception
	{
		String user   = jdbc.getChildText(Jeeves.Res.Pool.USER);
		String passwd = jdbc.getChildText(Jeeves.Res.Pool.PASSWORD);

		try
		{
			dbms.connect(user, passwd);
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
	//--- Setup other site parameters
	//---
	//---------------------------------------------------------------------------

	private void setupSite(String installDir, Dbms dbms) throws Exception
	{
		String uuid = UUID.randomUUID().toString();

		dbms.execute("UPDATE Settings SET value=? WHERE name='siteId'", uuid);
		dbms.execute("UPDATE Metadata SET source=?", uuid);
		dbms.commit();

		//--- rename site logo to reflect the uuid

		String logoPath = installDir +"/web/images/logos";

		File logoSrc = new File(logoPath, "dummy.png");
		File logoDes = new File(logoPath, uuid +".png");

		logoSrc.renameTo(logoDes);
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

