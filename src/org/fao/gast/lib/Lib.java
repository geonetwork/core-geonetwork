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

package org.fao.gast.lib;

import org.fao.geonet.lib.TypeLib;

//=============================================================================

public class Lib
{
	public static XMLLib        xml    = new XMLLib();
	public static TextLib       text   = new TextLib();
	public static TypeLib       type   = new TypeLib();
	public static GuiLib        gui    = new GuiLib();
	public static IOLib         io     = new IOLib();
	public static ServiceLib    service= new ServiceLib();
	public static ConfigLib     config;
	public static EmbeddedSCLib embeddedSC;
	public static EmbeddedDBLib embeddedDB;
	public static DatabaseLib   database;
	public static MetadataLib   metadata;
	public static ServerLib     server;

	//---------------------------------------------------------------------------
	//---
	//--- Initialization
	//---
	//---------------------------------------------------------------------------

	public static void init(String appPath) throws Exception
	{
		config     = new ConfigLib    (appPath);
		embeddedSC = new EmbeddedSCLib(appPath);
		embeddedDB = new EmbeddedDBLib(appPath);
		database   = new DatabaseLib  (appPath);
		metadata   = new MetadataLib  (appPath);
		server     = new ServerLib    (appPath);
	}
}

//=============================================================================

