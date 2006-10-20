//==============================================================================
//===
//===   Boot
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

import org.fao.geonet.apps.common.Util;

//==============================================================================

public class Boot
{
	//---------------------------------------------------------------------------
	//---
	//--- Main method
	//---
	//---------------------------------------------------------------------------

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			Util.showError("Missing installation directory & jdbc url parameters");

			//--- we cannot use 'return' because the previous 'showError' creates an
			//--- hidden frame that prevent the application from being terminated.
			System.exit(-1);
		}

		String jdbcUrl = args[1];

		boolean mckoi  = jdbcUrl.indexOf("mckoi")  != -1;
		boolean mysql  = jdbcUrl.indexOf("mysql")  != -1;
		boolean oracle = jdbcUrl.indexOf("oracle") != -1;

		if (!mckoi && !mysql && !oracle)
			askForDrivers();

		Util.boot(args[0], "org.fao.geonet.apps.setup.Setup");
		System.exit(0);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private static void askForDrivers()
	{

		String msg = "It is time to copy the java jdbc driver of your DBMS\n"+
						 "into the web/WEB-INF/lib directory.\n"+
						 "Press OK  when you have done.";

		Util.showInfo(msg);
	}
}

//==============================================================================


