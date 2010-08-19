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

package org.fao.geonet.services.resources;

import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.ResourceNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//=============================================================================

/** Sends the resource to the client in a zip archive with metadata and license
  */

public class DownloadArchive implements Service
{
	private static String FS = File.separator;
	private String appPath;
	private String stylePath;
	private final static String FILE_NAME_SUBSTR = "filename=";

	//----------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//----------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
		this.appPath = appPath;
		this.stylePath = appPath + FS + Geonet.Path.STYLESHEETS + FS;
	}

	//----------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//----------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm   = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		UserSession session = context.getUserSession();


		String id;
		// does the request contain a UUID ?
		try {
			String uuid = Util.getParam(params, Params.UUID);
			// lookup ID by UUID
			id = dm.getMetadataId(context, uuid.toLowerCase());
			if (id == null) throw new IllegalArgumentException("Metadata not found --> " + uuid);
		} catch (MissingParameterEx x) {
			try {
				id = Util.getParam(params, Params.ID);
			} catch (MissingParameterEx xx) {
				throw new Exception("Request must contain a UUID or an ID");
			}
		}
		String access = Util.getParam(params, Params.ACCESS, Params.Access.PUBLIC);

		//--- resource required is public (thumbnails)
		if (access.equals(Params.Access.PUBLIC)) { 
			File dir = new File(Lib.resource.getDir(context, access, id));
			String fname = Util.getParam(params, Params.FNAME);
			File file = new File(dir, fname);
			return BinaryFile.encode(200, file.getAbsolutePath(),false);
		}

		//--- from here on resource required is private datafile(s)

		//--- check if disclaimer for this metadata has been displayed
		Element elData = (Element) session.getProperty(Geonet.Session.FILE_DISCLAIMER);
		if (elData == null) {
			return new Element("response");
		} else {
			String idAllowed = elData.getChildText(Geonet.Elem.ID);
			if (idAllowed == null || !idAllowed.equals(id)) {
				return new Element("response");
			}
		}

		//--- check whether notify is required
		boolean doNotify = false;
		Lib.resource.checkPrivilege(context, id, AccessManager.OPER_DOWNLOAD);
		doNotify = true;

		//--- set username for emails and logs
		String username = session.getUsername();
		if (username == null) username = "internet";
		String profile = session.getProfile();
		String userId  = session.getUserId();

		//--- get feedback/reason for download info passed in & record in 'entered'
		String name     = Util.getParam(params, Params.NAME);
		String org      = Util.getParam(params, Params.ORG);
		String email    = Util.getParam(params, Params.EMAIL);
		String comments = Util.getParam(params, Params.COMMENTS);
		Element entered = new Element("entered").addContent(params.cloneContent());
	
		//--- get logged in user details & record in 'userdetails'
		Element userDetails = new Element("userdetails");
		if (!username.equals("internet")) {
			Element elUser = dbms.select ("SELECT username, surname, name, address, state, zip, country, email, organisation FROM Users WHERE id=" + userId);
			if (elUser.getChild("record") != null) {
				userDetails.addContent(elUser.getChild("record").cloneContent());
			}
		}
	
		//--- get metadata info
		MdInfo info = dm.getMetadataInfo(dbms, id);

		// set up zip output stream
		File zFile = File.createTempFile(username+"_"+info.uuid,".zip");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zFile));

		//--- because often content has already been compressed
   	out.setLevel(Deflater.NO_COMPRESSION);

		//--- now add the files chosen from the interface and record in 'downloaded'
		Element downloaded = new Element("downloaded");
		File dir = new File(Lib.resource.getDir(context, access, id));

		List files = params.getChildren(Params.FNAME);
		for (Object o : files) {
			Element elem = (Element)o;
			String fname = elem.getText();

			File file = new File(dir, fname);
			if (!file.exists()) throw new ResourceNotFoundEx(file.getAbsolutePath());

			Element fileInfo = new Element("file");

			Element details = BinaryFile.encode(200, file.getAbsolutePath(), false);
			String remoteURL = details.getAttributeValue("remotepath");
			if (remoteURL != null) {
				context.debug("Downloading "+remoteURL+" to archive "+zFile.getName());
				fileInfo.setAttribute("size","unknown");
				fileInfo.setAttribute("datemodified","unknown");
				fileInfo.setAttribute("name",remoteURL);
				notifyAndLog(doNotify, id, info.uuid, access, username, remoteURL+" (local config: "+file.getAbsolutePath()+")", context);
				fname = details.getAttributeValue("remotefile");
			} else {
				context.debug("Writing "+fname+" to archive "+zFile.getName());
				fileInfo.setAttribute("size",file.length()+"");
				fileInfo.setAttribute("name",fname);
				Date date = new Date(file.lastModified());
				fileInfo.setAttribute("datemodified",sdf.format(date));
				notifyAndLog(doNotify, id, info.uuid, access, username, file.getAbsolutePath(), context);
			}
			addFile(out, file.getAbsolutePath(), details, fname);
			downloaded.addContent(fileInfo);
    }

		//--- get metadata
		Element elMd = dm.getMetadata(context, id, false);
		if (elMd == null)
			throw new MetadataNotFoundEx("Metadata not found - deleted?");

		//--- transform record into brief version
		String briefXslt = stylePath + Geonet.File.METADATA_BRIEF;
		Element elBrief = Xml.transform(elMd, briefXslt);

		//--- create root element for passing all the info we've gathered 
		//--- to license annex xslt generator
		Element root = new Element("root");
		elBrief.setAttribute("changedate",info.changeDate);
		elBrief.setAttribute("currdate",now());
		root.addContent(elBrief);
		root.addContent(downloaded);
		root.addContent(entered);
		root.addContent(userDetails);
		context.debug("Passed to metadata-license-annex.xsl:\n "+Xml.getString(root));

		//--- create the license annex html file using the info in root element and
		//--- add it to the zip stream
		String licenseAnnexXslt = stylePath + Geonet.File.LICENSE_ANNEX_XSL;
		File licenseAnnex = File.createTempFile(username+"_"+info.uuid,".annex");
		FileOutputStream las = new FileOutputStream(licenseAnnex);
		Xml.transform(root, licenseAnnexXslt, las);
		las.close();
    InputStream in = new FileInputStream(licenseAnnex);
		addFile(out, Geonet.File.LICENSE_ANNEX, in);
    in.close();

		//--- if a license is specified include any license files mirrored locally 
		//--- for inclusion
		includeLicenseFiles(context, out, root);
		
		//--- export the metadata as a partial mef/zip file and add that to the zip
		//--- stream FIXME: some refactoring required here to avoid metadata 
		//--- being read yet again(!) from the database by the MEF exporter
		String outmef = MEFLib.doExport(context, info.uuid, MEFLib.Format.PARTIAL.toString(), false);
    in = new FileInputStream(outmef);
		addFile(out, "metadata.zip", in);
    in.close();

		//--- now close the zip file and send it out
		if (out != null) out.close();
		return BinaryFile.encode(200, zFile.getAbsolutePath(),true);

	}

	//----------------------------------------------------------------------------
	//---
	//--- Private Methods
	//---
	//----------------------------------------------------------------------------
	
	private void includeLicenseFiles(ServiceContext context,
								ZipOutputStream out, Element root) throws Exception,
								MalformedURLException, FileNotFoundException, IOException {
		Element license = Xml.selectElement(root, "metadata/*/licenseLink");
		if (license != null) {
			String licenseURL = license.getText();
			context.debug("license URL = " + licenseURL);

			String licenseFilesPath = getLicenseFilesPath(licenseURL, context);
			context.debug(" licenseFilesPath = " + licenseFilesPath);

			if (licenseFilesPath != null) {
				File licenseFilesDir = new File(licenseFilesPath);
				File[] licenseFiles = licenseFilesDir.listFiles();
				if (licenseFiles == null) return;
				for (File licenseFile : licenseFiles) {
					context.debug("adding " + licenseFile.getAbsolutePath() + " to zip file");
					InputStream in = new FileInputStream(licenseFile);
					addFile(out, licenseFile.getName(), in);
				}
			}
		}
	}

	//----------------------------------------------------------------------------
	
	private String getLicenseFilesPath(String licenseURL, ServiceContext context) throws MalformedURLException {
		// TODO: Ideally this method should probably read an xml file to map
		// license url's to the sub-directory containing mirrored files
		// but for the moment we'll just use the url path to identify this

		//--- Get license files subdirectory for license
		URL url = new URL(licenseURL);
		String licenseFilesPath = url.getHost() + url.getPath();
		context.debug("licenseFilesPath= " + licenseFilesPath);

		//--- Get local mirror directory for license files
		String path    = context.getAppPath();
		context.debug("path= " + path);

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		ServiceConfig configHandler = gc.getHandlerConfig();
		String licenseDir = configHandler.getValue(Geonet.Config.LICENSE_DIR);
		context.debug("licenseDir= " + licenseDir);
		if (licenseDir == null) return null;

		File directory = new File(licenseDir);
		if (!directory.isAbsolute()) licenseDir = path + licenseDir;

		//--- return license files directory
		return licenseDir + '/' + licenseFilesPath;
	}

	//----------------------------------------------------------------------------
	
	private void addFile(ZipOutputStream zos, String path, Element details, String name) throws Exception {
		ZipEntry entry = new ZipEntry(name);
		zos.putNextEntry(entry);
		BinaryFile.write(details, zos);
		zos.closeEntry();
	}

	//----------------------------------------------------------------------------
	
	private void addFile(ZipOutputStream zos, String name, InputStream in) throws IOException {
		ZipEntry entry = new ZipEntry(name);
		zos.putNextEntry(entry);
		BinaryFile.copy(in, zos, true, false);
		zos.closeEntry();
	}

	//----------------------------------------------------------------------------
	
	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());
	}

	//----------------------------------------------------------------------------
	
	private void notifyAndLog(boolean doNotify, String id, String uuid, String access, String username, String theFile, ServiceContext context) throws Exception { 

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();
		DataManager    dm = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- increase metadata popularity
		if (access.equals(Params.Access.PRIVATE)) dm.increasePopularity(dbms, id);

		//--- send email notification
		if (doNotify) {
			
			String site = sm.getValue("system/site/siteId");
			String host = sm.getValue("system/feedback/mailServer/host");
			String port = sm.getValue("system/feedback/mailServer/port");
			String from = sm.getValue("system/feedback/email");

			String fromDescr = "GeoNetwork administrator";

			String dateTime = now();
			context.info("DOWNLOADED:"+theFile+","+id+","+uuid+","+context.getIpAddress()+","+username);

			if (host.trim().length() == 0 || from.trim().length() == 0) {
				context.debug("Skipping email notification");
			} else {
				context.debug("Sending email notification for file : "+ theFile);

					// send emails about downloaded file to groups with notify privilege
				StringBuffer query = new StringBuffer();
				query.append("SELECT g.id, g.name, g.email ");
				query.append("FROM   OperationAllowed oa, Groups g ");
				query.append("WHERE  oa.operationId =" + AccessManager.OPER_NOTIFY + " ");
				query.append("AND    oa.metadataId = " + id + " ");
				query.append("AND    oa.groupId = g.id");

				Element groups = dbms.select(query.toString());

				for (Iterator i = groups.getChildren().iterator(); i.hasNext(); ) {
					Element group = (Element)i.next();
					String  name  = group.getChildText("name");
					String  email = group.getChildText("email");


					if (email.trim().length() != 0) {
						String subject = "File " + theFile + " has been downloaded at "+dateTime;
						String message = "GeoNetwork (site: "+site+") notifies you, as supervisor of group "+ name
							+ " that data file "+ theFile 
							+ " attached to metadata record with id number "+ id
							+ " (uuid: "+uuid+")"
							+ " has been downloaded from address " + context.getIpAddress() 
							+ " by user " + username
							+ ".";

						try {
							MailSender sender = new MailSender(context);
							sender.send(host, Integer.parseInt(port), from, fromDescr, email, null, subject, message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}

//=============================================================================

