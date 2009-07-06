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

package org.fao.gast.gui.panels.manag.mefexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.utils.BinaryFile;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.app.App;
import org.fao.gast.app.Configuration;
import org.fao.gast.lib.Lib;
import org.fao.gast.localization.Messages;
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

	public void setOutDir  (String  dir)    { this.outDir   = dir;    }
	public void setFormat  (String  format) { this.format   = format; }
	public void setSkipUuid(boolean yesno)  { this.skipUuid = yesno;  }

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
		dlg.reset(1);
		dlg.advance(MessageFormat.format(Messages.getString("Worker.loginto"), App.config.getHost()));

		Lib.service.login(req);
	}

	//---------------------------------------------------------------------------

	private void logout(XmlRequest req)
	{
		dlg.reset(1);
		dlg.advance(MessageFormat.format(Messages.getString("Worker.logoutFrom"), App.config.getHost()));

		Lib.service.logout(req);
	}

	//---------------------------------------------------------------------------

	private List search(XmlRequest req) throws Exception
	{
		Configuration cfg = App.config;

		dlg.reset(1);
		dlg.advance(MessageFormat.format(Messages.getString("Worker.search"), cfg.getHost()));

		req.setAddress("/"+ cfg.getServlet() +"/srv/en/"+ Geonet.Service.XML_SEARCH);

		return req.execute(panSearch.createRequest()).getChildren("metadata");
	}

	//---------------------------------------------------------------------------

	private File retrieveMEF(XmlRequest req, String uuid) throws Exception
	{
		dlg.advance(MessageFormat.format(Messages.getString("Worker.exportUUID"),uuid));

		req.clearParams();
		req.addParam("uuid",     uuid);
		req.addParam("format",   format);
		req.addParam("skipUuid", skipUuid);

		req.setAddress("/"+ App.config.getServlet() +"/srv/en/"+ Geonet.Service.MEF_EXPORT);

		File tempFile = File.createTempFile("temp-", ".dat");
		req.executeLarge(tempFile);

		Element root = loadMefFile(tempFile);

		if (root != null)
		{
			String id = root.getAttributeValue("id");

			if ("operation-not-allowed".equals(id))
				throw new OperationNotAllowedEx();

			throw new Exception(MessageFormat.format(Messages.getString("Worker.errorServer"),Xml.getString(root)));
		}

		return tempFile;
	}

	//---------------------------------------------------------------------------

	private void save(File mefFile, String uuid) throws IOException
	{
		// sanitize the uuid to remove and characters that might cause issues
		// when we write the mef file out
		uuid = simplifyName(uuid);
		File outFile = new File(outDir, uuid + ".mef");

		FileInputStream  is = new FileInputStream (mefFile);
		FileOutputStream os = new FileOutputStream(outFile);
		BinaryFile.copy(is, os, true, true);

		mefFile.delete();
	}

	//---------------------------------------------------------------------------
	/** Try to load a MEF file as an XML document. If the user does not have
	  * privileges to download the MEF, an error in XML format is returned
	  */

	private Element loadMefFile(File file)
	{
		try
		{
			return Xml.loadFile(file);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------

	private String simplifyName(String file)
	{

		//--- we need to sanitize the filename here - make it UTF8, no ctrl
		//--- characters and only containing [A-Z][a-z][0-9],_.-

		//--- start by converting to UTF-8
		try {
			byte[] utf8Bytes = file.getBytes("UTF8");
			file = new String(utf8Bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		//--- replace whitespace with underscore
		file = file.replaceAll("\\s","_");

		//--- remove everything that isn't [0-9][a-z][A-Z],#_.-
		file = file.replaceAll("[^\\w&&[^,_.-]]","");
		return file;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String  outDir;
	private String  format;
	private boolean skipUuid;

	private ProgressDialog dlg;
	private SearchPanel    panSearch;
}

//==============================================================================

