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

package org.fao.gast.cli.setup;

import java.io.File;
import java.util.List;
import jeeves.resources.dbms.Dbms;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;

//==============================================================================

public class Setup
{
	public void exec(String appPath, List<String> args) throws Exception
	{
		//--- this line saves the 'gast/data/index.html' file into 'web'
		//--- substituting the $SERVLET variable

		Lib.embeddedSC.save();

		//--- now we create the embedded database...

		Lib.embeddedDB.createDB();

		//--- ... and save the account into the config.xml file

		Lib.config.setDbmsUser    (Lib.embeddedDB.getUser());
		Lib.config.setDbmsPassword(Lib.embeddedDB.getPassword());
		Lib.config.addActivator();
		Lib.config.save();

		//--- proper setup: open a database connection and setup data

		Resource resource  = Lib.config.createResource();
		Lib.database.setup(resource, null);

		//--- ask for and install sample metadata

		if (Lib.gui.confirm(null, SAMPLE_MSG))
			addSampleData(appPath);
	}

	//---------------------------------------------------------------------------

	private void addSampleData(String appPath) throws Exception
	{
		Lib.log.info("Adding sample metadata");

		Resource resource = Lib.config.createResource();
		Dbms     dbms     = (Dbms) resource.open();

		try
		{
			int serial = Lib.database.getNextSerial(dbms, "Metadata");
			dbms.commit();

			File   sampleDir = new File(appPath, SAMPLE_PATH);
			File[] samples   = sampleDir.listFiles();

			if (samples == null)
				Lib.log.warning("Cannot scan directory : "+ sampleDir.getAbsolutePath());
			else
			{
				for (File sample : samples)
					if (sample.getName().endsWith(".mef"))
					{
						Lib.mef.doImport(dbms, serial++, sample);
						dbms.commit();
					}
			}

			Lib.log.info("Synchronizing metadata");
			Lib.metadata.sync(dbms);
			resource.close();
			Lib.log.info("Done");
		}
		catch(Exception e)
		{
			Lib.log.fatal("Raised exception : "+ e.getMessage());
			resource.abort();
			throw e;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static final String SAMPLE_MSG  = "Do you want to install sample metadata ?";
	private static final String SAMPLE_PATH = "gast/setup/sample-data";
}

//==============================================================================

