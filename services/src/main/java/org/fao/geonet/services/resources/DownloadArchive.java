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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.resources.handlers.IResourceDownloadHandler;
import org.fao.geonet.util.MailSender;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Sends the resource to the client in a zip archive with metadata and license
 */

public class DownloadArchive implements Service {
    private static String FS = File.separator;
    private Path stylePath;

    //----------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //----------------------------------------------------------------------------

    private static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    //----------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //----------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.STYLESHEETS);
    }

    //----------------------------------------------------------------------------
    //---
    //--- Private Methods
    //---
    //----------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        UserSession session = context.getUserSession();

        String id = Utils.getIdentifierFromParameters(params, context);
        String access = Util.getParam(params, Params.ACCESS, Params.Access.PUBLIC);

        //--- resource required is public (thumbnails)
        if (access.equals(Params.Access.PUBLIC)) {
            Path dir = Lib.resource.getDir(context, access, id);
            String fname = Util.getParam(params, Params.FNAME);

			FilePathChecker.verify(fname);

			Path file = dir.resolve(fname);
			return BinaryFile.encode(200, file, false).getElement();
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
        Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
        doNotify = true;

        //--- set username for emails and logs
        String username = session.getUsername();
        if (username == null) username = "internet";
        String userId = session.getUserId();

        //--- get feedback/reason for download info passed in & record in 'entered'
//		String name     = Util.getParam(params, Params.NAME);
//		String org      = Util.getParam(params, Params.ORG);
//		String email    = Util.getParam(params, Params.EMAIL);
//		String comments = Util.getParam(params, Params.COMMENTS);
        Element entered = new Element("entered").addContent(params.cloneContent());

        //--- get logged in user details & record in 'userdetails'
        Element userDetails = new Element("userdetails");
        if (!username.equals("internet")) {
            final User user = context.getBean(UserRepository.class).findOne(userId);
            if (user != null) {
                userDetails.addContent(user.asXml());
            }
        }

        //--- get metadata info
        AbstractMetadata info = context.getBean(IMetadataUtils.class).findOne(id);

        // set up zip output stream
        Path zFile = Files.createTempFile(username + "_" + info.getUuid(), ".zip");
        try (FileSystem zipFs = ZipUtil.openZipFs(zFile)) {
            //--- now add the files chosen from the interface and record in 'downloaded'
            Element downloaded = new Element("downloaded");
            Path dir = Lib.resource.getDir(context, access, id);

            @SuppressWarnings("unchecked")
            List<Element> files = params.getChildren(Params.FNAME);
            for (Element elem : files) {
                String fname = elem.getText();

                try {
                    FilePathChecker.verify(fname);
                } catch (Exception ex) {
                    continue;
                }

                Path file = dir.resolve(fname);
                if (!Files.exists(file))
                    throw new ResourceNotFoundEx(file.toAbsolutePath().normalize().toString());

                Element fileInfo = new Element("file");

                BinaryFile details = BinaryFile.encode(200, file.toAbsolutePath().normalize(), false);
                String remoteURL = details.getElement().getAttributeValue("remotepath");
                if (remoteURL != null) {
                    if (context.isDebugEnabled())
                        context.debug("Downloading " + remoteURL + " to archive " + zFile.getFileName());
                    fileInfo.setAttribute("size", "unknown");
                    fileInfo.setAttribute("datemodified", "unknown");
                    fileInfo.setAttribute("name", remoteURL);
                    notifyAndLog(doNotify, id, info.getUuid(), access, username, remoteURL + " (local config: " + file.toAbsolutePath()
                        .normalize() + ")", context);
                    fname = details.getElement().getAttributeValue("remotefile");
                } else {
                    if (context.isDebugEnabled())
                        context.debug("Writing " + fname + " to archive " + zFile.getFileName());
                    fileInfo.setAttribute("size", Files.size(file) + "");
                    fileInfo.setAttribute("name", fname);
                    Date date = new Date(Files.getLastModifiedTime(file).toMillis());
                    fileInfo.setAttribute("datemodified", sdf.format(date));
                    notifyAndLog(doNotify, id, info.getUuid(), access, username, file.toAbsolutePath().normalize().toString(), context);
                }
                final Path dest = zipFs.getPath(fname);
                try (OutputStream zos = Files.newOutputStream(dest)) {
                    details.write(zos);
                }
                downloaded.addContent(fileInfo);
            }

            //--- get metadata
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element elMd = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

            if (elMd == null)
                throw new MetadataNotFoundEx("Metadata not found - deleted?");

            //--- manage the download hook
            IResourceDownloadHandler downloadHook = (IResourceDownloadHandler) context.getApplicationContext().getBean
                ("resourceDownloadHandler");
            Element response = downloadHook.onDownloadMultiple(context, params, Integer.parseInt(id), files);

            // Return null to do the default processing. TODO: Check to move the code to the default hook.
            if (response != null) {
                return response;

            } else {
                //--- transform record into brief version
                Path briefXslt = stylePath.resolve(Geonet.File.METADATA_BRIEF);
                Element elBrief = Xml.transform(elMd, briefXslt);

                //--- create root element for passing all the info we've gathered
                //--- to license annex xslt generator
                Element root = new Element("root");
                elBrief.setAttribute("changedate", info.getDataInfo().getChangeDate().getDateAndTime());
                elBrief.setAttribute("currdate", now());
                root.addContent(elBrief);
                root.addContent(downloaded);
                root.addContent(entered);
                root.addContent(userDetails);
                if (context.isDebugEnabled())
                    context.debug("Passed to metadata-license-annex.xsl:\n " + Xml.getString(root));

                //--- create the license annex html file using the info in root element and
                //--- add it to the zip stream
                Path licenseAnnexXslt = stylePath.resolve(Geonet.File.LICENSE_ANNEX_XSL);
                try (OutputStream las = Files.newOutputStream(zipFs.getPath(Geonet.File.LICENSE_ANNEX))) {
                    Xml.transform(root, licenseAnnexXslt, las);
                }

                //--- if a license is specified include any license files mirrored locally
                //--- for inclusion
                includeLicenseFiles(context, zipFs, root);

                //--- export the metadata as a partial mef/zip file and add that to the zip
                //--- stream FIXME: some refactoring required here to avoid metadata
                //--- being read yet again(!) from the database by the MEF exporter
                Path outmef = MEFLib.doExport(context, info.getUuid(), MEFLib.Format.PARTIAL.toString(), false, true, true, true);
                final Path toPath = zipFs.getPath("metadata.zip");
                Files.copy(outmef, toPath);
            }
            return BinaryFile.encode(200, zFile.toAbsolutePath().normalize(), true).getElement();
        }
    }

    //----------------------------------------------------------------------------

    private void includeLicenseFiles(ServiceContext context,
                                     FileSystem zipFs, Element root) throws Exception,
        MalformedURLException, FileNotFoundException, IOException {
        Element license = Xml.selectElement(root, "metadata/*/licenseLink");
        if (license != null) {
            String licenseURL = license.getText();
            if (context.isDebugEnabled())
                context.debug("license URL = " + licenseURL);

            Path licenseFilesDir = getLicenseFilesPath(licenseURL, context);
            if (context.isDebugEnabled())
                context.debug(" licenseFilesPath = " + licenseFilesDir);

            if (licenseFilesDir != null && Files.exists(licenseFilesDir)) {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(licenseFilesDir)) {
                    for (Path licenseFile : paths) {
                        if (context.isDebugEnabled()) {
                            context.debug("adding " + licenseFile + " to zip file");
                        }
                        Files.copy(licenseFile, zipFs.getPath(licenseFile.getFileName().toString()));
                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------

    private Path getLicenseFilesPath(String licenseURL, ServiceContext context) throws MalformedURLException {
        // TODO: Ideally this method should probably read an xml file to map
        // license url's to the sub-directory containing mirrored files
        // but for the moment we'll just use the url path to identify this

        //--- Get license files subdirectory for license
        URL url = new URL(licenseURL);
        String licenseFilesPath = url.getHost() + url.getPath();
        if (context.isDebugEnabled())
            context.debug("licenseFilesPath= " + licenseFilesPath);

        //--- Get local mirror directory for license files
        Path path = context.getAppPath();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ServiceConfig configHandler = gc.getBean(ServiceConfig.class);
        String licenseDirAsString = configHandler.getValue(Geonet.Config.LICENSE_DIR);
        if (licenseDirAsString == null) return null;

        Path licenseDir = IO.toPath(licenseDirAsString);
        if (!licenseDir.isAbsolute()) licenseDir = path.resolve(licenseDir);

        if (context.isDebugEnabled()) {
            context.debug("licenseDir = " + licenseDir);
        }

        //--- return license files directory
        return licenseDir.resolve(licenseFilesPath);
    }

    //----------------------------------------------------------------------------

    private void notifyAndLog(boolean doNotify, String id, String uuid, String access, String username, String theFile, ServiceContext context) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);
        DataManager dm = gc.getBean(DataManager.class);

        //--- increase metadata popularity
        if (access.equals(Params.Access.PRIVATE)) {
            dm.increasePopularity(context, id);
        }

        //--- send email notification
        if (doNotify) {

            String site = sm.getSiteId();
            String host = sm.getValue("system/feedback/mailServer/host");
            String port = sm.getValue("system/feedback/mailServer/port");
            String from = sm.getValue("system/feedback/email");

            String fromDescr = "GeoNetwork administrator";

            String dateTime = now();
            context.info("DOWNLOADED:" + theFile + "," + id + "," + uuid + "," + context.getIpAddress() + "," + username);

            if (host.trim().length() == 0 || from.trim().length() == 0) {
                if (context.isDebugEnabled()) {
                    context.debug("Skipping email notification");
                }
            } else {
                if (context.isDebugEnabled()) {
                    context.debug("Sending email notification for file : " + theFile);
                }

                OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
                List<OperationAllowed> opsAllowed = opAllowedRepo.findByMetadataId(id);

                final GroupRepository groupRepository = context.getBean(GroupRepository.class);

                for (OperationAllowed opAllowed : opsAllowed) {
                    Group group = groupRepository.findOne(opAllowed.getId().getGroupId());
                    String name = group.getName();
                    String email = group.getEmail();

                    if (email.trim().length() != 0) {
                        String subject = "File " + theFile + " has been downloaded at " + dateTime;
                        String message = "GeoNetwork (site: " + site + ") notifies you, as supervisor of group " + name
                            + " that data file " + theFile
                            + " attached to metadata record with id number " + id
                            + " (uuid: " + uuid + ")"
                            + " has been downloaded from address " + context.getIpAddress()
                            + " by user " + username
                            + ".";

                        try {
                            MailSender sender = new MailSender(context);
                            sender.send(host, Integer.parseInt(port),
                                sm.getValue("system/feedback/mailServer/username"),
                                sm.getValue("system/feedback/mailServer/password"),
                                sm.getValueAsBool("system/feedback/mailServer/ssl"),
                                sm.getValueAsBool("system/feedback/mailServer/tls"),
                                sm.getValueAsBool("system/feedback/mailServer/ingoreSslCertificateErrors", false),
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

