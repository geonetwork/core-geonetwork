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

package org.wfp.vam.intermap.kernel;



public class GlobalTempFiles extends TempFiles
{
//	private final File _dir;
	private static GlobalTempFiles _instance = null;

	public static GlobalTempFiles getInstance()
	{
		return _instance;
	}

	public static synchronized void init(String servletEnginePath, String path, int minutes) throws Exception
	{
		if( _instance != null)
			throw new IllegalStateException("This singleton has already been initialized.");

		_instance = new GlobalTempFiles(servletEnginePath, path, minutes);
	}
	
	private GlobalTempFiles(String servletEnginePath, String path, int minutes) throws Exception
	{
		super(servletEnginePath, path, minutes);
	}

}

