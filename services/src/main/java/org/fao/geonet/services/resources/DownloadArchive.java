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

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.ResourceNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.commons.io.IOUtils;
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
import org.fao.geonet.services.Utils;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
	private String stylePath;

	//----------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//----------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
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
		DataManager   dm   = gc.getBean(DataManager.class);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		UserSession session = context.getUserSession();

		String id = Utils.getIdentifierFromParameters(params, context);
		String access = Util.getParam(params, Params.ACCESS, Params.Access.PUBLIC);

		//--- resource required is public (thumbnails)
		if (access.equals(Params.Access.PUBLIC)) { 
			File dir = new File(Lib.resource.getDir(context, access, id));
			String fname = Util.getParam(params, Params.FNAME);

			if (fname.contains("..")) {
				throw new BadParameterEx("Invalid character found in resource name.", fname);
			}
			
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
		String userId  = session.getUserId();

		//--- get feedback/reason for download info passed in & record in 'entered'
//		String name     = Util.getParam(params, Params.NAME);
//		String org      = Util.getParam(params, Params.ORG);
//		String email    = Util.getParam(params, Params.EMAIL);
//		String comments = Util.getParam(params, Params.COMMENTS);
		Element entered = new Element("entered").addContent(params.cloneContent());
	
		//--- get logged in user details & record in 'userdetails'
		Element userDetails = new Element("userdetails");
		if (!username.equals("internet")) {
			Element elUser = dbms.select ("SELECT username, surname, name, address, state, zip, country, email, organisation FROM Users WHERE id=?",Integer.valueOf(userId));
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

		@SuppressWarnings("unchecked")
        List<Element> files = params.getChildren(Params.FNAME);
		for (Element elem : files) {
			String fname = elem.getText();

			if (fname.contains("..")) {
				continue;
			}
			
			File file = new File(dir, fname);
			if (!file.exists()) throw new ResourceNotFoundEx(file.getAbsolutePath());

			Element fileInfo = new Element("file");

			Element details = BinaryFile.encode(200, file.getAbsolutePath(), false);
			String remoteURL = details.getAttributeValue("remotepath");
			if (remoteURL != null) {
                if(context.isDebug())
                    context.debug("Downloading "+remoteURL+" to archive "+zFile.getName());
				fileInfo.setAttribute("size","unknown");
				fileInfo.setAttribute("datemodified","unknown");
				fileInfo.setAttribute("name",remoteURL);
				notifyAndLog(doNotify, id, info.uuid, access, username, remoteURL+" (local config: "+file.getAbsolutePath()+")", context);
				fname = details.getAttributeValue("remotefile");
			} else {
                if(context.isDebug())
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
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element elMd = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

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
        if(context.isDebug())
            context.debug("Passed to metadata-license-annex.xsl:\n "+Xml.getString(root));

		//--- create the license annex html file using the info in root element and
		//--- add it to the zip stream
		String licenseAnnexXslt = stylePath + Geonet.File.LICENSE_ANNEX_XSL;
		File licenseAnnex = File.createTempFile(username+"_"+info.uuid,".annex");
		FileOutputStream las = new FileOutputStream(licenseAnnex);
		Xml.transform(root, licenseAnnexXslt, las);
		las.close();
        InputStream in = null;
        try {
            in = new FileInputStream(licenseAnnex);
    		addFile(out, Geonet.File.LICENSE_ANNEX, in);
        } finally {
            IOUtils.closeQuietly(in);
        }

		//--- if a license is specified include any license files mirrored locally 
		//--- for inclusion
		includeLicenseFiles(context, out, root);
		
		//--- export the metadata as a partial mef/zip file and add that to the zip
		//--- stream FIXME: some refactoring required here to avoid metadata 
		//--- being read yet again(!) from the database by the MEF exporter
		String outmef = MEFLib.doExport(context, info.uuid, MEFLib.Format.PARTIAL.toString(), false, true, true);
		FileInputStream in2 = null;
		try {
            in2 = new FileInputStream(outmef);
    		addFile(out, "metadata.zip", in2);
		} finally {
		    IOUtils.closeQuietly(in2);
		}

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
            if(context.isDebug())
                context.debug("license URL = " + licenseURL);

			String licenseFilesPath = getLicenseFilesPath(licenseURL, context);
            if(context.isDebug())
                context.debug(" licenseFilesPath = " + licenseFilesPath);

			if (licenseFilesPath != null) {
				File licenseFilesDir = new File(licenseFilesPath);
				File[] licenseFiles = licenseFilesDir.listFiles();
				if (licenseFiles == null) return;
				for (File licenseFile : licenseFiles) {
                    if(context.isDebug())
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
        if(context.isDebug())
            context.debug("licenseFilesPath= " + licenseFilesPath);

		//--- Get local mirror directory for license files
		String path    = context.getAppPath();
        if(context.isDebug())
            context.debug("path= " + path);

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		ServiceConfig configHandler = gc.getBean(ServiceConfig.class);
		String licenseDir = configHandler.getValue(Geonet.Config.LICENSE_DIR);
        if(context.isDebug())
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
	
	private void addFile(ZipOutputStream zos, String name, @Nonnull InputStream in) throws IOException {
		ZipEntry entry = null;
		try {
    		entry = new ZipEntry(name);
    		zos.putNextEntry(entry);
    		BinaryFile.copy(in, zos);
		} finally {
		    try {
    		    if(zos != null) {
    		        zos.closeEntry();
    		    }
		    } finally {
		        IOUtils.closeQuietly(in);
		    }
		}
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
		SettingManager sm = gc.getBean(SettingManager.class);
		DataManager    dm = gc.getBean(DataManager.class);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- increase metadata popularity
		if (access.equals(Params.Access.PRIVATE)) dm.increasePopularity(context, id);

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
                if(context.isDebug()) context.debug("Skipping email notification");
			} else {
                if(context.isDebug()) context.debug("Sending email notification for file : "+ theFile);

					// send emails about downloaded file to groups with notify privilege
				StringBuffer query = new StringBuffer();
				query.append("SELECT g.id, g.name, g.email ");
				query.append("FROM   OperationAllowed oa, Groups g ");
				query.append("WHERE  oa.operationId =" + AccessManager.OPER_NOTIFY + " ");
				query.append("AND    oa.metadataId = ?");
				query.append("AND    oa.groupId = g.id");

				Element groups = dbms.select(query.toString(), Integer.valueOf(id));
                @SuppressWarnings("unchecked")
                List<Element> groupsEls = groups.getChildren();
                for (Element group : groupsEls) {
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
							sender.send(host, Integer.parseInt(port), 
							        sm.getValue("system/feedback/mailServer/username"), 
							        sm.getValue("system/feedback/mailServer/password"), 
							        sm.getValueAsBool("system/feedback/mailServer/ssl"), 
							        from, fromDescr, email, null, subject, message);
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

