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

package org.fao.geonet.kernel.mef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jeeves.exceptions.BadFormatEx;
import jeeves.utils.Xml;
import org.jdom.Element;

import static org.fao.geonet.kernel.mef.MEFConstants.*;

//=============================================================================

public class MEFFileVisitor implements FileVisitor
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public void visit(File mefFile, MEFVisitor v) throws Exception
	{
		Element info = handleXml(mefFile, v);
		handleBin(mefFile, v, info);
	}

	//--------------------------------------------------------------------------

	public Element handleXml(File mefFile, MEFVisitor v) throws Exception
	{
		ZipInputStream    zis = new ZipInputStream(new FileInputStream(mefFile));
		InputStreamBridge isb = new InputStreamBridge(zis);

		ZipEntry entry;

		Element md   = null;
		Element info = null;

		try
		{
			while ((entry=zis.getNextEntry()) != null)
			{
				String name = entry.getName();

				if (name.equals(FILE_METADATA))
					md = Xml.loadStream(isb);

				else if (name.equals(FILE_INFO))
					info = Xml.loadStream(isb);

				zis.closeEntry();
			}
		}
		finally
		{
			safeClose(zis);
		}

		if (md == null)
			throw new BadFormatEx("Missing metadata file : "+ FILE_METADATA);

		if (info == null)
			throw new BadFormatEx("Missing info file : "+ FILE_INFO);

		v.handleMetadata(md);
		v.handleInfo(info);

		return info;
	}

	//--------------------------------------------------------------------------

	public void handleBin(File mefFile, MEFVisitor v, Element info) throws Exception
	{
		ZipInputStream    zis = new ZipInputStream(new FileInputStream(mefFile));
		InputStreamBridge isb = new InputStreamBridge(zis);

		List pubFiles = info.getChild("public") .getChildren();
		List prvFiles = info.getChild("private").getChildren();

		ZipEntry entry;

		try
		{
			while ((entry=zis.getNextEntry()) != null)
			{
				String fullName   = entry.getName();
				String simpleName = new File(fullName).getName();

				if (fullName.equals(DIR_PUBLIC) || fullName.equals(DIR_PRIVATE))
					continue;

				if (fullName.startsWith(DIR_PUBLIC))
					v.handlePublicFile(simpleName, getChangeDate(pubFiles, simpleName), isb);

				else if (fullName.startsWith(DIR_PRIVATE))
					v.handlePrivateFile(simpleName, getChangeDate(prvFiles, simpleName), isb);

				zis.closeEntry();
			}
		}
		finally
		{
			safeClose(zis);
		}
	}

	//--------------------------------------------------------------------------

	private static String getChangeDate(List files, String fileName) throws Exception
	{
		for (Object f : files)
		{
			Element file = (Element) f;
			String  name = file.getAttributeValue("name");
			String  date = file.getAttributeValue("changeDate");

			if (name.equals(fileName))
				return date;
		}

		throw new Exception("File not found in info.xml : "+ fileName);
	}

	//--------------------------------------------------------------------------

	private static void safeClose(ZipInputStream zis)
	{
		try
		{
			zis.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}

//=============================================================================

