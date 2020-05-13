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

package org.fao.geonet.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//=============================================================================

/**
 * Retrieves download links from metadata and adds certain info to prepare a list of online
 * resources for the user. Access is restricted
 */

public class PrepareFileDownload implements Service {
    private Path appPath;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.appPath = appPath;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element response = new Element("response");

        //--- check access
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataManager = gc.getBean(DataManager.class);

        boolean addEdit = false;

        //--- the request should contain an ID or UUID
        String id = Utils.getIdentifierFromParameters(params, context);

        if (id == null) {
            throw new MetadataNotFoundEx("No record has this UUID");
        }

        //--- check download access
        Lib.resource.checkPrivilege(context, id, ReservedOperation.download);

        //--- get metadata
        boolean withValidationErrors = false, keepXlinkAttributes = false;
        Element elMd = dataManager.getMetadata(context, id, addEdit, withValidationErrors, keepXlinkAttributes);

        if (elMd == null) {
            throw new MetadataNotFoundEx("Metadata not found - deleted?");
        }

        response.addContent(new Element("id").setText(id));

        //--- transform record into brief version
        Path briefXslt = appPath.resolve("xsl/metadata-brief.xsl");
        Element elBrief = Xml.transform(elMd, briefXslt);

        XPath xp;
        //--- process links to a file (have name field not blank)
        //--- if they are a reference to a downloadable local file then get size
        //--- and date modified, if not then set local to false
        xp = XPath.newInstance("link[starts-with(@protocol,'WWW:DOWNLOAD') and @name!='']");
        @SuppressWarnings("unchecked")
        List<Element> downloadLinks = xp.selectNodes(elBrief);
        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
        response = processDownloadLinks(context, id, siteURL, downloadLinks, response);

        //--- now process web links so that they can be displayed as well
        xp = XPath.newInstance("link[starts-with(@protocol,'WWW:LINK')]");
        @SuppressWarnings("unchecked")
        List<Element> webLinks = xp.selectNodes(elBrief);
        response = processWebLinks(webLinks, response);

        return response;
    }

    //--------------------------------------------------------------------------

    /**
     * Process the links to downloadable files
     */
    private Element processDownloadLinks(ServiceContext context, String id, String siteURL, List<Element> elems, Element response) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String uuid = metadataUtils.getMetadataUuid(id);


        for (Element elem : elems) {
            elem = (Element) elem.clone();

            // --- get file name and href
            String fname = elem.getAttributeValue("name");
            String href = elem.getAttributeValue("href");

            if (href != null) {
                boolean local = href.startsWith(siteURL);
                boolean found = false;
                long size = 0;
                String dateModified = "";

                String linkPieces[] = href.split("\\&");

                // local file (possibly)
                if (linkPieces.length > 0) {

                    if (local) {
                        String access = "private";
                        for (int i = 0; i < linkPieces.length; i++) {
                            String lp = linkPieces[i];
                            if (lp.startsWith("access=")) {
                                access = lp.substring(lp.indexOf('=') + 1);
                            }
                        }

                        final MetadataResource description =
                                store.getResourceDescription(context, uuid, MetadataResourceVisibility.parse(access), id, true);
                        if (description != null) {
                            size = description.getSize();
                            Date date = description.getLastModification();
                            dateModified = sdf.format(date);
                            found = true;
                        }
                    }
                }
                elem.setAttribute("size", size + "");
                elem.setAttribute("datemodified", dateModified);
                elem.setAttribute("found", found + "");
                elem.setAttribute("local", local + "");
                elem.setAttribute("download", "true");
                response.addContent(elem);
            } else {
                context.info("Unknown download link: " + Xml.getString(elem));
            }
        }

        return response;
    }

    //--------------------------------------------------------------------------

    /**
     * Process web links (actually just add them in)
     */
    private Element processWebLinks(List<Element> elems, Element response) {

        for (Element elem : elems) {
            elem = (Element) elem.clone();

            elem.setAttribute("weblink", "true");
            response.addContent(elem);
        }
        return response;
    }

}


//=============================================================================

