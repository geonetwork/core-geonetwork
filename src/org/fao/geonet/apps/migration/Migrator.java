//==============================================================================
//===
//===   Migrator
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

package org.fao.geonet.apps.migration;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import jeeves.utils.Xml;
import org.fao.geonet.apps.common.SimpleLogger;
import org.jdom.Element;

//==============================================================================

public class Migrator extends Thread
{
	private boolean  testOnly;
	private GNDestin destin;

	private SimpleLogger logger;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Migrator(String geonetworkDir, SimpleLogger logger, boolean testOnly)
	{
		this.logger  = logger;
		this.testOnly= testOnly;

		destin = new GNDestin(geonetworkDir, logger);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Migration process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		try
		{
			logger.clear();

			PrintWriter out = new PrintWriter(new FileOutputStream("migrate/log/unmapped.log"));

			//--- open GeoNetwork 2

			logBanner("Opening GeoNetwork 2");
			destin.open();

			//--- migration

			logBanner("Starting migration process");

			Set<String> ids = destin.getAllIsoMetadataId();

			destin.commit();
			logger.logInfo("Obtained "+ ids.size() +" metadata");
			logger.logInfo("");

			for(String id : ids)
			{
				if (testOnly)	logger.logInfo("Analyzing metadata with id : "+ id);
					else 			logger.logInfo("Migrating metadata with id : "+ id);

				Element metadata = destin.getMetadata(id);
				Element unmapped = destin.getUnmappedFields(metadata);

				destin.commit();
				saveUnmapped(out, id, unmapped);

				if (!testOnly)
				{
					destin.upgradeMetadata(id, metadata);
					destin.commit();
				}
			}

			if (!testOnly)
			{
				logger.logInfo("Cleaning lucene indexes");
				destin.removeLuceneFiles();
			}

			logBanner("Ending migration process");
			out.close();
		}
		catch(Exception e)
		{
			logBanner("Aborting");
			destin.abort();
		}
		catch(Throwable t)
		{
			logBanner("Unexpected exception");
			destin.abort();

			logger.logError("Type  : "+ t.getClass().getName());
			logger.logError("Error : "+ t.getMessage());

			StackTraceElement stack[] = t.getStackTrace();

			if (stack != null)
			{
				StackTraceElement ste = stack[0];

				logger.logError("File  : "+ ste.getFileName());
				logger.logError("Line  : "+ ste.getLineNumber());
			}
		}
		finally
		{
			destin.close();

			logger.logInfo("End.");
			logger.finish();
		}
	}

	//---------------------------------------------------------------------------

	private void saveUnmapped(PrintWriter out, String id, Element unmapped)
	{
		List list = unmapped.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element elem = (Element) list.get(i);
			String  clas = elem.getAttributeValue("class");

			logger.logError("   Mapping warning : "+ clas);

			out.println("Mapping warning for metadata with id "+ id +" : "+ clas);
			out.println();
			out.println(Xml.getString(elem));
			out.println("-------------------------------------------------------");
		}
	}

	//---------------------------------------------------------------------------

	private void logBanner(String title)
	{
		logger.logInfo("");
		logger.logInfo("===================================================================");
		logger.logInfo("=== "+title);
		logger.logInfo("===================================================================");
		logger.logInfo("");
	}
}

//==============================================================================

