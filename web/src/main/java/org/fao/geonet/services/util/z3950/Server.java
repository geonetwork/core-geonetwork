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

import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.jzkit.z3950.server.Z3950Listener;
import org.springframework.context.ApplicationContext;

//=============================================================================

/** Z3950 server
  */

public class Server
{
	private static Z3950Listener _listener;

	//--------------------------------------------------------------------------

	/** initializes the server
	  */
	public static void init(String port, ApplicationContext app_context)
	{
		try
		{
			//--- normal processing

			_listener  = (Z3950Listener)app_context.getBean("Z3950Listener", Z3950Listener.class);
			_listener.setPort(Integer.parseInt(port));
			_listener.start();
		}
		catch (Exception e)
		{
			//--- Z39.50 must not stop Geonetwork starting even if there are problems
			Log.warning(Geonet.Z3950_SERVER, "Cannot start Z39.50 server : "+ e.getMessage());
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------

	/** ends the server
	  */
	public static void end()
	{
		if (_listener != null)
			_listener.shutdown(0); // shutdown type is not used in current implementation
	}

}

//=============================================================================

