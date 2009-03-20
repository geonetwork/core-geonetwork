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

import org.jdom.Element;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.server.context.ServiceContext;

//=============================================================================

public class MEFLib
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public static int doImport(Element params, ServiceContext context, File mefFile, String stylePath) throws Exception
	{
		return Importer.doImport(params, context, mefFile, stylePath);
	}

	//--------------------------------------------------------------------------

	public static String doExport(ServiceContext context, String uuid, String format,
											boolean skipUUID) throws Exception
	{
		return Exporter.doExport(context, uuid, Format.parse(format), skipUUID);
	}

	//--------------------------------------------------------------------------

	public static void visit(File mefFile, FileVisitor fvisitor, MEFVisitor v) throws Exception
	{
		fvisitor.visit(mefFile, v);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Types
	//---
	//--------------------------------------------------------------------------

	enum Format
	{
		SIMPLE, PARTIAL, FULL;

		//------------------------------------------------------------------------

		public static Format parse(String format) throws BadInputEx
		{
			if (format == null)
				throw new MissingParameterEx("format");

			if (format.equals("simple"))		return SIMPLE;
			if (format.equals("partial"))		return PARTIAL;
			if (format.equals("full")) 		return FULL;

			throw new BadParameterEx("format", format);
		}

		//------------------------------------------------------------------------

		public String toString() { return super.toString().toLowerCase(); }
	};
}

//=============================================================================


