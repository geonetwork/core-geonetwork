/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.mef;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

/**
 * TODO:
 * * If only URL provided and no map as string, download context
 * * Create map preview
 * * Save map and attached document to metadata record
 */
public class ImportWebMap extends NotInReadOnlyModeService {

    private String styleSheetWmc;

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
        this.styleSheetWmc = appPath + File.separator + Geonet.Path.IMPORT_STYLESHEETS + File.separator + "OGCWMC-OR-OWSC-to-ISO19139.xsl";
    }


    @Override
    @Deprecated
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String mapString = Util.getParam(params, "map_string");
        String mapUrl = Util.getParam(params, "map_url", "");
        String viewerUrl = Util.getParam(params, "map_viewer_url", "");
        String groupId = Util.getParam(params, "group_id", null);
        String mapAbstract = Util.getParam(params, "map_abstract", "");
        String title = Util.getParam(params, "map_title", "");
        String mapFileName = Util.getParam(params, "map_filename", "map-context.ows");
        String mapImage = Util.getParam(params, "map_image", "");
        String mapImageFilename = Util.getParam(params, "map_image_filename", "map.png");

        FilePathChecker.verify(mapFileName);

        String topic = Util.getParam(params, "topic", "");


        Map<String, Object> xslParams = new HashMap<String, Object>();
        xslParams.put("viewer_url", viewerUrl);
        xslParams.put("map_url", mapUrl);
        xslParams.put("topic", topic);
        xslParams.put("title", title);
        xslParams.put("abstract", mapAbstract);
        xslParams.put("lang", context.getLanguage());

        UserSession us = context.getUserSession();

        if (us != null) {
            xslParams.put("currentuser_name", us.getName() + " " + us.getSurname());
            // phone number is georchestra-specific
            //xslParams.put("currentuser_phone", us.getPrincipal().getPhone());
            xslParams.put("currentuser_mail", us.getEmailAddr());
            xslParams.put("currentuser_org", us.getOrganisation());
        }

        // 1. JDOMize the string
        Element wmcDoc = Xml.loadString(mapString, false);
        // 2. Apply XSL (styleSheetWmc)
        Element transformedMd = Xml.transform(wmcDoc, new File(styleSheetWmc).toPath(), xslParams);

        // 4. Inserts the metadata (does basically the same as the metadata.insert.paste service (see Insert.java)
        String uuid = UUID.randomUUID().toString();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = context.getBean(SettingManager.class);
        DataManager dm = gc.getBean(DataManager.class);
        SchemaManager schemaMan = context.getBean(SchemaManager.class);

        String uuidAction = Util.getParam(params, Params.UUID_ACTION, Params.NOTHING);

        String date = new ISODate().toString();

        final List<String> id = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();

        md.add(transformedMd);

        // Import record
        Importer.importRecord(uuid, MEFLib.UuidAction.parse(uuidAction), md, "iso19139", 0, sm.getSiteId(),
            sm.getSiteName(), null, context, id, date, date, groupId,
            MetadataType.METADATA);

        final Store store = context.getBean("resourceStore", Store.class);

        // Save the context if no context-url provided
        if (StringUtils.isEmpty(mapUrl)) {
            store.putResource(context, uuid, mapFileName, IOUtils.toInputStream(Xml.getString(wmcDoc)), null,
                    MetadataResourceVisibility.PUBLIC, true);

            // Update the MD
            Map<String, Object> onlineSrcParams = new HashMap<String, Object>();
            onlineSrcParams.put("protocol", "WWW:DOWNLOAD-OGC:OWS-C");
            onlineSrcParams.put("url", sm.getNodeURL() + String.format("api/records/%s/attachments/%s", uuid, mapFileName));
            onlineSrcParams.put("name", mapFileName);
            onlineSrcParams.put("desc", title);
            Element mdWithOLRes = Xml.transform(transformedMd, schemaMan.getSchemaDir("iso19139").resolve("process").resolve("onlinesrc-add.xsl"), onlineSrcParams);
            dm.updateMetadata(context, id.get(0), mdWithOLRes, false, true, true, context.getLanguage(), null, true);
        }

        if (StringUtils.isNotEmpty(mapImage) && StringUtils.isNotEmpty(mapImageFilename)) {
            store.putResource(context, uuid, mapImageFilename, new ByteArrayInputStream(Base64.decodeBase64(mapImage)), null,
                    MetadataResourceVisibility.PUBLIC, true);

            // Update the MD
            Map<String, Object> onlineSrcParams = new HashMap<String, Object>();
            onlineSrcParams.put("thumbnail_url", sm.getNodeURL() + String.format("api/records/%s/attachments/%s", uuid, mapFileName));
            Element mdWithOLRes = Xml.transform(transformedMd, schemaMan.getSchemaDir("iso19139").resolve("process").resolve("thumbnail-add.xsl"), onlineSrcParams);
            dm.updateMetadata(context, id.get(0), mdWithOLRes, false, true, true, context.getLanguage(), null, true);
        }

        dm.indexMetadata(id);

        Element result = new Element("uuid");
        result.setText(uuid);

        return result;
    }
}
