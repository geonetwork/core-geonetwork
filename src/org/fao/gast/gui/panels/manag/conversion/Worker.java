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
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.fao.gast.localization.Messages;
import org.jdom.Element;
import org.jdom.Namespace;

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
	//--- Conversion process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		Resource res = null;

		try
		{
			res = Lib.config.createResource();
			executeJob((Dbms) res.open());
		}
		catch(Exception e)
		{
			Lib.gui.showError(dlg, e);
		}
		finally
		{
			if (res != null)
				res.close();

			Lib.gui.showInfo(dlg, 	Messages.getString("Worker.conversionTerm"));
			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------

	private void executeJob(Dbms dbms) throws Exception
	{
		String      log = Lib.server.getAppPath() +"/gast/logs/unmapped.log";
		PrintWriter out = new PrintWriter(new FileOutputStream(log));

		dlg.reset(1);
		dlg.advance(Messages.getString("Worker.retrieveIds"));

		List ids = getAllIsoMetadata(dbms);
		dbms.commit();

		dlg.reset(ids.size());

		for(Object o : ids)
		{
			Element el = (Element) o;
			String  id = el.getChildText("id");

			dlg.advance(MessageFormat.format(Messages.getString("Worker.convertMetadata"), id));

			Element md    = Lib.metadata.getMetadata(dbms, id);
			Element res   = Lib.metadata.convert(md, "iso19115", "iso19139");

			Element metadata = (Element) res.getChild("metadata").getChildren().get(0);
			Element unmapped = res.getChild("unmapped");

			updateMetadata(dbms, id, metadata);
			dbms.commit();
			saveUnmapped(out, id, unmapped);
		}

//		logger.logInfo("Cleaning lucene indexes");
//		removeLuceneFiles();

		out.close();
	}

	//---------------------------------------------------------------------------

	private List getAllIsoMetadata(Dbms dbms) throws SQLException
	{
		String query = "SELECT id FROM Metadata WHERE schemaId = ?";

		return dbms.select(query, "iso19115").getChildren();
	}

	//---------------------------------------------------------------------------

	private void updateMetadata(Dbms dbms, String id, Element md) throws Exception
	{
		Namespace ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
		fixNamespace(md, ns);

		String query = "UPDATE Metadata SET schemaId='iso19139', data=?, root=? WHERE id=?";

		dbms.execute(query, Xml.getString(md), "gmd:"+ md.getName(), new Integer(id));
	}

	//---------------------------------------------------------------------------

	private void fixNamespace(Element md, Namespace ns)
	{
		if (md.getNamespaceURI().equals(ns.getURI()))
			md.setNamespace(ns);

		for (Object o : md.getChildren())
			fixNamespace((Element) o, ns);
	}

	//---------------------------------------------------------------------------

	private void saveUnmapped(PrintWriter out, String id, Element unmapped)
	{
		List list = unmapped.getChildren();

		for(Object e : list)
		{
			Element elem = (Element) e;
			String  clas = elem.getAttributeValue("class");

			out.println(MessageFormat.format(Messages.getString("Worker.mapWarning"),id, clas));
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

	private ProgressDialog dlg;
}

//==============================================================================

