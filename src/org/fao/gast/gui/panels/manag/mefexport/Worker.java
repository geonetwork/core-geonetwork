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

package org.fao.gast.gui.panels.manag.mefexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.utils.BinaryFile;
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

	public Worker(ProgressDialog dlg, SearchPanel sp)
	{
		this.dlg       = dlg;
		this.panSearch = sp;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setOutDir(String dir)    { this.outDir = dir;    }
	public void setFormat(String format) { this.format = format; }

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

		//--- search

		List result = search(req);

		//--- export

		dlg.reset(result.size());

		for (Object r : result)
		{
			Element rec = (Element) r;

			//--- go to 'geonet.info element'
			rec = (Element) rec.getChildren().get(0);

			String uuid = rec.getChildText("uuid");

			File file = retrieveMEF(req, uuid);
			save(file, uuid);
		}

		//--- logout

		if (cfg.useAccount())
			logout(req);
	}

	//---------------------------------------------------------------------------

	private void login(XmlRequest req) throws Exception
	{
		Configuration cfg = App.config;

		dlg.reset(1);
		dlg.advance("Login into : "+ cfg.getHost());

		req.setAddress("/"+ cfg.getServlet() +"/srv/en/"+ Geonet.Service.XML_LOGIN);
		req.clearParams();
		req.addParam("username", cfg.getUsername());
		req.addParam("password", cfg.getPassword());

		Element response = req.execute();
		Lib.service.checkError(response);
	}

	//---------------------------------------------------------------------------

	private void logout(XmlRequest req)
	{
		Configuration cfg = App.config;

		dlg.reset(1);
		dlg.advance("Logout from : "+ cfg.getHost());

		req.clearParams();
		req.setAddress("/"+ cfg.getServlet() +"/srv/en/"+ Geonet.Service.XML_LOGOUT);
	}

	//---------------------------------------------------------------------------

	private List search(XmlRequest req) throws Exception
	{
		Configuration cfg = App.config;

		dlg.reset(1);
		dlg.advance("Searching on : "+ cfg.getHost());

		req.setAddress("/"+ cfg.getServlet() +"/srv/en/"+ Geonet.Service.XML_SEARCH);

		return req.execute(panSearch.createRequest()).getChildren("metadata");
	}

	//---------------------------------------------------------------------------

	private File retrieveMEF(XmlRequest req, String uuid) throws IOException
	{
		dlg.advance("Exporting uuid : "+uuid);

		req.clearParams();
		req.addParam("uuid",   uuid);
		req.addParam("format", format);

		req.setAddress("/"+ App.config.getServlet() +"/srv/en/"+ Geonet.Service.MEF_EXPORT);

		File tempFile = File.createTempFile("temp-", ".dat");
		req.executeLarge(tempFile);

		return tempFile;
	}

	//---------------------------------------------------------------------------

	private void save(File mefFile, String uuid) throws IOException
	{
		File outFile = new File(outDir, uuid + ".mef");

		FileInputStream  is = new FileInputStream (mefFile);
		FileOutputStream os = new FileOutputStream(outFile);
		BinaryFile.copy(is, os, true, true);

		mefFile.delete();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String outDir;
	private String format;

	private ProgressDialog dlg;
	private SearchPanel    panSearch;
}

//==============================================================================

