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

package org.fao.gast.gui.panels.database.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.app.App;
import org.fao.gast.app.Configuration;
import org.fao.gast.lib.Lib;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

//==============================================================================

public class Worker implements Runnable
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Worker(ProgressDialog dlg)
	{
		this.dlg = dlg;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setImportMetadata (boolean yesno) { impMetadata  = yesno; }
	public void setImportTemplates(boolean yesno) { impTemplates = yesno; }

	//---------------------------------------------------------------------------

	public void setImportRuns(int runs)
	{
		this.runs =runs;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Runnable interface
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		try
		{
			executeJob();
		}
		catch(Exception e)
		{
			Lib.gui.showError(dlg, e);
		}
		finally
		{
			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------

	private void executeJob() throws Exception
	{
		Configuration cfg = App.config;

		XmlRequest req = new XmlRequest(cfg.getHost(), cfg.getPort());

		//--- login

		if (cfg.useAccount())
			login(req);

		//--- scan for mef files

		List<File> files = getFileList();

		//--- export

		dlg.reset((files.size() + runs));

		for (int i=0; i<runs; i++)
			for (File file : files)
				send(req, file);

		//--- logout

		if (cfg.useAccount())
			logout(req);
	}

	//---------------------------------------------------------------------------

	private void login(XmlRequest req) throws Exception
	{
		dlg.reset(1);
		dlg.advance("Login into : "+ App.config.getHost());

		Lib.service.login(req);
	}

	//---------------------------------------------------------------------------

	private void logout(XmlRequest req)
	{
		dlg.reset(1);
		dlg.advance("Logout from : "+ App.config.getHost());

		Lib.service.logout(req);
	}

	//---------------------------------------------------------------------------

	private List<File> getFileList()
	{
		ArrayList<File> files = new ArrayList<File>();

		File sampleDir = new File(App.path +"/gast/setup/sample-data");

		if (impMetadata)
			files.addAll(Lib.io.scanDir(new File(sampleDir, "maps"), "mef"));

		if (impTemplates)
			files.addAll(Lib.io.scanDir(new File(sampleDir, "templates"), "mef"));

		return files;
	}

	//---------------------------------------------------------------------------

	private void send(XmlRequest req, File mefFile) throws Exception
	{
		dlg.advance("Importing file : "+ mefFile.getName());

		req.setAddress("/"+ App.config.getServlet() +"/srv/en/"+ Geonet.Service.MEF_IMPORT);

		Element response = req.send("mefFile", mefFile);
		Lib.service.checkError(response);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private boolean impMetadata;
	private boolean impTemplates;

	private int runs;

	private ProgressDialog dlg;
}

//==============================================================================

