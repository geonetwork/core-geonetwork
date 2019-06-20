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
//=============================================================================

package org.fao.geonet.services.feedback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Adds Limitations/Constraints etc from Metadata record and prepares them for display
 */

public class AddLimitations implements Service {
    // This shouldn't be static because DateFormat is not thread safe
    private final SimpleDateFormat _dateFormat = createDateFormatter();
    private Path stylePath;
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    protected static void addElement(Element root, String name, String value) {
        root.addContent(new Element(name).setText(value));
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    private static String now() {
        Calendar cal = Calendar.getInstance();
        return createDateFormatter().format(cal.getTime());
    }

    //---------------------------------------------------------------------------

    private static SimpleDateFormat createDateFormatter() {
        // This shouldn't be static because DateFormat is not thread safe
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    //---------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.STYLESHEETS);
    }

    public Element exec(Element params, final ServiceContext context) throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);

        String access = Util.getParam(params, Params.ACCESS);

        Lib.resource.checkPrivilege(context, id, ReservedOperation.download);

        //--- get metadata info
        AbstractMetadata info = context.getBean(IMetadataUtils.class).findOne(id);

        if (info == null)
            throw new IllegalArgumentException("Metadata not found --> " + id);

        //--- start building response
        Element response = new Element("response");
        addElement(response, Params.ID, id);
        addElement(response, Params.UUID, info.getUuid());

        //--- now add the files chosen from the interface and record in 'downloaded'
        Element downloaded = new Element("downloaded");
        Path dir = Lib.resource.getDir(context, access, id);

        @SuppressWarnings("unchecked")
        List<Element> files = params.getChildren(Params.FNAME);
        for (Element elem : files) {
            response.addContent((Element) elem.clone());

            String fname = elem.getText();

            try {
                FilePathChecker.verify(fname);
            } catch (Exception ex) {
                continue;    // Avoid unsecured file name
            }

            Path file = dir.resolve(fname);

            Element fileInfo = new Element("file");

            BinaryFile bFile = BinaryFile.encode(200, file, false);
            Element details = bFile.getElement();
            String remoteURL = details.getAttributeValue("remotepath");
            if (remoteURL != null) {
                fileInfo.setAttribute("size", "unknown");
                fileInfo.setAttribute("datemodified", "unknown");
                fileInfo.setAttribute("name", remoteURL);
            } else {
                fileInfo.setAttribute("size", Files.size(file) + "");
                fileInfo.setAttribute("name", fname);
                Date date = new Date(Files.getLastModifiedTime(file).toMillis());
                fileInfo.setAttribute("datemodified", _dateFormat.format(date));
            }
            downloaded.addContent(fileInfo);
        }
        addElement(response, Params.ACCESS, access);

        //--- get metadata
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        final DataManager dataManager = context.getBean(DataManager.class);
        Element elMd = dataManager.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

        if (elMd == null)
            throw new IllegalArgumentException("Metadata not found --> " + id);

        //--- place xml in metadata element
        Element md = new Element(Geonet.Elem.METADATA);
        md.addContent((Element) elMd.clone());
        response.addContent(md);

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
        if (context.isDebugEnabled())
            context.debug("Passed to metadata-license-annex.xsl:\n " + Xml.getString(root));

        //--- create the license annex html using the info in root element and
        //--- add it to response under license element
        Path licenseAnnexXslt = stylePath.resolve(Geonet.File.LICENSE_ANNEX_XSL);
        Element license = Xml.transform(root, licenseAnnexXslt);
        response.addContent(new Element("license").addContent(license));

        //--- Now set the id into the users session so that future services can do
        //--- the download
        UserSession session = context.getUserSession();
        Element resourceData = (Element) session.getProperty(Geonet.Session.FILE_DISCLAIMER);
        if (resourceData == null) {
            resourceData = new Element(Geonet.Session.FILE_DISCLAIMER);
            addElement(resourceData, Params.ID, id);
        } else {
            Element idEl = resourceData.getChild(Params.ID);
            if (idEl == null) addElement(resourceData, Params.ID, id);
            else idEl.setText(id);
        }
        session.setProperty(Geonet.Session.FILE_DISCLAIMER, resourceData);

        //--- now get the users name, organisation and email address to
        //--- prepopulate the feedback form (if they are logged in)
        if (session.getUserId() != null) {
            User user = context.getBean(UserRepository.class).findOne(session.getUserIdAsInt());
            if (user != null) {
                Element elRec = user.asXml();
                elBrief.setName("record");
                response.addContent(elRec.cloneContent());
            }
        }

        return response;
    }
}

//=============================================================================
