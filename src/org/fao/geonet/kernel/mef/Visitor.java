//=============================================================================
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

package org.fao.geonet.kernel.mef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jeeves.exceptions.BadFormatEx;
import jeeves.utils.Xml;
import org.jdom.Element;

import static org.fao.geonet.kernel.mef.MEFConstants.*;

//=============================================================================

public class Visitor
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public static void visit(File mefFile, MEFVisitor v) throws Exception
	{
		handleXml(mefFile, v);
		handleBin(mefFile, v);
	}

	//--------------------------------------------------------------------------

	private static void handleXml(File mefFile, MEFVisitor v) throws Exception
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
	}

	//--------------------------------------------------------------------------

	private static void handleBin(File mefFile, MEFVisitor v) throws IOException
	{
		ZipInputStream    zis = new ZipInputStream(new FileInputStream(mefFile));
		InputStreamBridge isb = new InputStreamBridge(zis);

		ZipEntry entry;

		try
		{
			while ((entry=zis.getNextEntry()) != null)
			{
				String fullName   = entry.getName();
				String simpleName = new File(fullName).getName();

				if (fullName.startsWith(DIR_PUBLIC) && !fullName.equals(DIR_PUBLIC))
					v.handlePublicFile(simpleName, isb);

				else if (fullName.equals(DIR_PRIVATE) && !fullName.equals(DIR_PRIVATE))
					v.handlePrivateFile(simpleName, isb);

				zis.closeEntry();
			}
		}
		finally
		{
			safeClose(zis);
		}
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

class InputStreamBridge extends InputStream
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public InputStreamBridge(InputStream is)
	{
		this.is = is;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Bridging methods
	//---
	//--------------------------------------------------------------------------

	public int read() throws IOException { return is.read(); }

	public int available() throws IOException { return is.available(); }

	//--- this *must* be empty to work with zip files
	public void close() throws IOException {}

	public synchronized void mark(int readlimit) { is.mark(readlimit); }

	public synchronized void reset() throws IOException { is.reset(); }

	public boolean markSupported() {	return is.markSupported(); }

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private InputStream is;
}

//=============================================================================

