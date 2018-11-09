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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import com.google.common.collect.Maps;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Inserts a new metadata to the system (data is validated).
 */
@Deprecated
public class Insert extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    private Path stylePath;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.IMPORT_STYLESHEETS);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, final ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        String data = Util.getParam(params, Params.DATA);
        String group = Util.getParam(params, Params.GROUP);
        MetadataType metadataType = MetadataType.lookup(Util.getParam(params, Params.TEMPLATE, "n"));
        String style = Util.getParam(params, Params.STYLESHEET, "_none_");

        boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");

//      Sub template does not need a title.
//      if (isTemplate.equals("s") && title.length() == 0)
//          throw new MissingParameterEx("title");

        //-----------------------------------------------------------------------
        //--- add the DTD to the input xml to perform validation

        Element xml = Xml.loadString(data, false);

        // Apply a stylesheet transformation if requested
        if (!style.equals("_none_")) {
            FilePathChecker.verify(style);

            xml = Xml.transform(xml, stylePath.resolve(style));
        }

        String schema = Util.getParam(params, Params.SCHEMA, null);
        if (schema == null) {
            schema = dataMan.autodetectSchema(xml);
            if (schema == null) {
                throw new BadParameterEx("Can't detect schema for metadata automatically.", "schema is unknown");
            }
        }
        if (validate) DataManager.validateMetadata(schema, xml, context);

        //-----------------------------------------------------------------------
        //--- if the uuid does not exist we generate it for metadata and templates
        String uuid;
        if (metadataType == MetadataType.SUB_TEMPLATE) {
            uuid = UUID.randomUUID().toString();
        } else {
            uuid = dataMan.extractUUID(schema, xml);
            if (uuid.length() == 0) {
                uuid = UUID.randomUUID().toString();
                xml = dataMan.setUUID(schema, uuid, xml);
            }
        }
        String uuidAction = Util.getParam(params, Params.UUID_ACTION,
            Params.NOTHING);

        String date = new ISODate().toString();

        final List<String> id = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();
        md.add(xml);


        DataManager dm = gc.getBean(DataManager.class);

        // Import record
        Map<String, String> sourceTranslations = Maps.newHashMap();
        Importer.importRecord(uuid, MEFLib.UuidAction.parse(uuidAction), md, schema, 0,
            gc.getBean(SettingManager.class).getSiteId(), gc.getBean(SettingManager.class).getSiteName(),
            sourceTranslations, context, id, date, date, group, metadataType);

        int iId = Integer.parseInt(id.get(0));


        // Set template
        dm.setTemplate(iId, metadataType, null);

        dm.activateWorkflowIfConfigured(context, id.get(0), group);


        // Import category
        final String category = Util.getParam(params, Params.CATEGORY, "");

        final String extra = Util.getParam(params, "extra", null);
        final boolean hasCategory = !category.equals("_none_") && !category.trim().isEmpty();

        if (hasCategory || extra != null) {
            context.getBean(IMetadataManager.class).update(iId, new Updater<Metadata>() {
                @Override
                public void apply(@Nonnull Metadata metadata) {
                    if (hasCategory) {
                        Element categs = new Element("categories");
                        categs.addContent((new Element("category")).setAttribute(
                            "name", category));

                        Importer.addCategoriesToMetadata(metadata, categs, context);
                    }

                    if (extra != null) {
                        metadata.getDataInfo().setExtra(extra);
                    }
                }
            });
        }

        // Index
        dm.indexMetadata(id.get(0), true, null);

        // Return response
        Element response = new Element(Jeeves.Elem.RESPONSE);
        response.addContent(new Element(Params.ID).setText(String.valueOf(iId)));
        response.addContent(new Element(Params.UUID).setText(String.valueOf(dm.getMetadataUuid(id.get(0)))));

        return response;
    }

    ;

}
