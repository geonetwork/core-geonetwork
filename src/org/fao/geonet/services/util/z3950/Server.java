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

package org.fao.geonet.services.util.z3950;

import com.k_int.z3950.server.ZServer;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Document;
import org.jdom.Element;

//=============================================================================

/** Z3950 server
  */

public class Server
{
	private static ZServer _server;

	//--------------------------------------------------------------------------

	/** initializes the server
	  */
	public static void init(String host, String port, String appPath,
									String schemaMappings, ServiceContext context) throws Exception
	{
		//--- fix schema-mappings.xml file

		String tempSchema = appPath + Jeeves.Path.XML + schemaMappings +".tem";
		String realSchema = appPath + Jeeves.Path.XML + schemaMappings;

		fixSchemaFile(tempSchema, realSchema);

		//--- fix repositories.xml file

		String tempRepo = appPath + Jeeves.Path.XML + "repositories.xml" +".tem";
		String realRepo = appPath + Jeeves.Path.XML + "repositories.xml";

		fixRepositoriesFile(tempRepo, realRepo, host, port);

		//--- normal processing

		String evaluator    = "org.fao.geonet.services.util.z3950.GNSearchable";
		String configurator = "com.k_int.IR.Syntaxes.Conversion.XMLConfigurator";

		Properties props = new Properties();
		props.setProperty("port",                              port);
		props.setProperty("evaluator",                         evaluator);
		props.setProperty("XSLConverterConfiguratorClassName", configurator);
		props.setProperty("ConvertorConfigFile",               realSchema);
		props.put("srvContext", context);

		_server = new ZServer(props);
		_server.start();
	}

	//--------------------------------------------------------------------------

	/** ends the server
	  */
	public static void end()
	{
		if (_server != null)
			_server.shutdown(0); // shutdown type is not used in current implementation
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private static void fixSchemaFile(String src, String des) throws Exception
	{
		Element root  = Xml.loadFile(src);
		Element temSrc= root.getChild("templatesource");

		String dir = new File(src).getParentFile().getAbsolutePath() +"/mappings";

		temSrc.setAttribute("directory", dir);

		FileOutputStream os = new FileOutputStream(des);
		Xml.writeResponse(new Document(root), os);
		os.close();
	}

	//--------------------------------------------------------------------------

	private static void fixRepositoriesFile(String src, String des, String host,
														 String z3950port) throws Exception
	{
		Element root = Xml.loadFile(src);
		Element repo = root.getChild("Repository");

		for (Object o : repo.getChildren("RepositoryProperty"))
		{
			Element rp = (Element) o;
			String  name = rp.getAttributeValue("name");

			if ("ServiceHost".equals(name))
				rp.setAttribute("value", host);

			else if ("ServicePort".equals(name))
				rp.setAttribute("value", z3950port);
		}

		FileOutputStream os = new FileOutputStream(des);
		Xml.writeResponse(new Document(root), os);
		os.close();
	}
}

//=============================================================================

