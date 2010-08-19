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

package org.fao.geonet.services.thesaurus;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;

//=============================================================================

/** 
 *  
 *  Upload one thesaurus file
 */

public class Upload implements Service
{
	static String FS = System.getProperty("file.separator", "/");
	static int inc = 0;
	
	private String stylePath;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		this.stylePath = appPath + FS + Geonet.Path.STYLESHEETS + FS;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		long start = System.currentTimeMillis();
		Element uploadResult;

		uploadResult = upload(params, context);
		
		long end = System.currentTimeMillis();
		long duration = (end - start) / 1000;

		Log.debug("Thesaurus","Uploaded in " + duration + " s.");
		
		Element response =  new Element("response");
		if (uploadResult != null)
			response.addContent(uploadResult);
		return response; 
		
	}


	/**
	 * 
	 * @param params
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private Element upload(Element params, ServiceContext context) throws Exception
	{
		String uploadDir = context.getUploadDir();
		Element uploadResult = null; 
		
		// RDF file
		String fname = null;
		Element param = params.getChild(Params.FNAME);
		if (!(param == null || param.getTextTrim().length() == 0)) {
			fname = param.getTextTrim();
		}

		// Thesaurus Type (local, external)
		String type = Util.getParam(params, Params.TYPE, "external");
		
		// Thesaurus directory - one of the ISO theme (Discipline, Place, Stratum, Temporal, Theme)
		String dir = Util.getParam(params, Params.DIR);

		// no XSL to be applied
		String style    = Util.getParam(params, Params.STYLESHEET, "_none_");
		
		// Validation or not
		boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");

		if ((fname != null) && !fname.equals("")) {
			
			Element eTSResult;
			
			File oldFile = new File(uploadDir, fname);
			String extension = fname.substring(fname.lastIndexOf('.')).toLowerCase();

			if (extension.equals(".rdf")) {

					Log.debug("Thesaurus","Uploading thesaurus: "+fname);
					eTSResult = UploadThesaurus(oldFile, style, context, validate, fname, type, dir);
				}
				else {
					Log.debug("Thesaurus","Incorrect extension for thesaurus file name : "+fname);
					throw new Exception("Incorrect extension for thesaurus file name : " + fname);
				}
			
			uploadResult = new Element("record").setText("Thesaurus uploaded");
			uploadResult.addContent(eTSResult);
		}

		return uploadResult;
	}


	/**
	 * Upload one Thesaurus
	 * @param oldFile
	 * @param style
	 * @param context
	 * @param validate
	 * @param siteId
	 * @param fname
	 * @param type
	 * @param dir
	 * @return Element thesaurus uploaded
	 * @throws Exception
	 */
	private Element UploadThesaurus(File oldFile, String style, ServiceContext context, boolean validate, String fname, String type, String dir) throws Exception {

		Element TS_xml = null;
		Element xml = Xml.loadFile(oldFile);
		xml.detach();
		
		if (!style.equals("_none_")) {
			TS_xml = Xml.transform(xml, stylePath +"/"+ style);
			TS_xml.detach();
		}
		else TS_xml = xml;
		
		// Load document and check namespace
		if (TS_xml.getNamespacePrefix().equals("rdf") && TS_xml.getName().equals("RDF")) {
				
			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
			ThesaurusManager thesaurusMan = gc.getThesaurusManager();

			// copy to directory according to type			
			String path = thesaurusMan.buildThesaurusFilePath( fname, type, dir);
			File newFile = new File(path);
			Xml.writeResponse(new Document(TS_xml), new FileOutputStream(newFile));
			
			Thesaurus gst = new Thesaurus(fname, type, dir, newFile);
			thesaurusMan.addThesaurus(gst);
		}
		else
		{
			oldFile.delete();
			
			throw new Exception("Unknown format (Must be in SKOS format).");
			
		}

		return new Element("Thesaurus").setText(oldFile.getName());
	}

}
