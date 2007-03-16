//==============================================================================
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

package org.fao.gast.gui.panels.manag.conversion;


import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.jdom.Element;

//==============================================================================

public class Worker implements Runnable
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Worker(ProgressDialog d)
	{
		dlg = d;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setOldDir(String dir)
	{
		appPath = dir;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Migration process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		if (!openSource())
			return;

		Resource oldRes = null;
		Resource newRes = null;

		try
		{
			oldRes = source.config.createResource();
			newRes = Lib.config.createResource();

			executeJob((Dbms) oldRes.open(), (Dbms) newRes.open());
		}
		catch(Throwable t)
		{
			Lib.gui.showError(dlg, t);
		}
		finally
		{
			if (oldRes != null)
				oldRes.close();

			if (newRes != null)
				newRes.close();

			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------

	private boolean openSource()
	{
		try
		{
			source = new GNSource(appPath);

			return true;
		}
		catch (Exception e)
		{
			Lib.gui.showError(dlg, 	"It seems that the specified folder does not \n"+
											"contain an old GeoNetwork installation");

			return false;
		}
	}

	//---------------------------------------------------------------------------

	private void executeJob(Dbms oldDbms, Dbms newDbms) throws Exception
	{
		String      log = Lib.server.getAppPath() +"/gast/log/unmapped.log";
		PrintWriter out = new PrintWriter(new FileOutputStream(log));

		//--- open GeoNetwork 2

		dlg.reset(2);

//		Set<String> ids = destin.getAllIsoMetadataId();

//		destin.commit();

//		dlg.reset(ids.size());

//		for(String id : ids)
//		{
//			if (testOnly)	dlg.advance("Analyzing metadata with id : "+ id);
//				else 			dlg.advance("Migrating metadata with id : "+ id);
//
//			Element metadata = destin.getMetadata(id);
//			Element unmapped = destin.getUnmappedFields(metadata);

//			destin.commit();
//			saveUnmapped(out, id, unmapped);
//
//			if (!testOnly)
//			{
//				destin.upgradeMetadata(id, metadata);
//				destin.commit();
//			}
//		}
//
//		if (!testOnly)
//		{
//			logger.logInfo("Cleaning lucene indexes");
//			destin.removeLuceneFiles();
//		}

		out.close();
	}

	//---------------------------------------------------------------------------

	private void saveUnmapped(PrintWriter out, String id, Element unmapped)
	{
		List list = unmapped.getChildren();

		for(Object e : list)
		{
			Element elem = (Element) e;
			String  clas = elem.getAttributeValue("class");

			out.println("Mapping warning for metadata with id "+ id +" : "+ clas);
			out.println();
			out.println(Xml.getString(elem));
			out.println("-------------------------------------------------------");
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String   appPath;
	private GNSource source;

	private ProgressDialog dlg;
}

//==============================================================================

