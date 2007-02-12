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

package org.fao.gast.gui.panels.migration.oldinst;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
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
//		destin = new GNDestin(dir, logger);
	}

	//---------------------------------------------------------------------------

	public void setTestOnly(boolean yesno)
	{
		testOnly = yesno;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Migration process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		Resource resource = null;

		try
		{
			resource  = Lib.config.createResource();
//			executeJob((Dbms) resource.open());
		}
		catch(Throwable t)
		{
//			destin.abort();
//			Lib.gui.showError(dlg, e);
		}
		finally
		{
			if (resource != null)
				resource.close();

			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------

	public void executeJob() throws Exception
	{
		String      log = Lib.server.getAppPath() +"/gast/log/unmapped.log";
		PrintWriter out = new PrintWriter(new FileOutputStream(log));

		//--- open GeoNetwork 2

		dlg.reset(2);
		dlg.advance("Opening old GeoNetwork");
//		destin.open();

		//--- migration

		dlg.advance("Starting migration process");

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

	private boolean  testOnly;
//	private GNDestin destin;

	private ProgressDialog dlg;
}

//==============================================================================

