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

import jeeves.interfaces.Logger;
import jeeves.server.context.ServiceContext;

import com.k_int.util.LoggingFacade.*;
import com.k_int.z3950.server.*;

import java.util.*;
import java.net.*;
import java.io.*;

//=============================================================================

/** Z3950 server
  */

public class Server
{
	private static ZServer _server;

	//--------------------------------------------------------------------------

	/** initializes the server
	  */
	public static void init(String port, String schemaMappings, ServiceContext srvContext)
		throws Exception
	{
		String evaluator    = "org.fao.geonet.services.util.z3950.GNSearchable";
		String configurator = "com.k_int.IR.Syntaxes.Conversion.XMLConfigurator";

		Properties props = new Properties();
		props.setProperty("port",                              port);
		props.setProperty("evaluator",                         evaluator);
		props.setProperty("XSLConverterConfiguratorClassName", configurator);
		props.setProperty("ConvertorConfigFile",               schemaMappings);
		props.put("srvContext", srvContext);

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
}

