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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jeeves.utils.BinaryFile;

//=============================================================================

public class IOLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void cleanDir(File dir) throws Exception
	{
		File files[] = dir.listFiles();

		if (files == null)
			return;

		for(File file : files)
			if (file.isDirectory())
			{
				if (!file.getName().equals(".svn"))
					cleanDir(file);
			}
			else if (!file.delete())
				throw new Exception("Cannot delete file : "+ file);
	}

	//---------------------------------------------------------------------------

	public List<File> scanDir(File folder)
	{
		return scanDir(folder, null);
	}

	//---------------------------------------------------------------------------

	public List<File> scanDir(File folder, String extension)
	{
		List<File> alFiles = new ArrayList<File>();

		File files[] = folder.listFiles();

		if (files != null)
			for (File file : files)
				if (extension == null || file.getName().endsWith("."+extension))
					alFiles.add(file);

		return alFiles;
	}

	//--------------------------------------------------------------------------

	public void save(File file, InputStream is) throws IOException
	{
		FileOutputStream os = new FileOutputStream(file);
		BinaryFile.copy(is, os, false, true);
	}
}

//=============================================================================

