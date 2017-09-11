//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.templates;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * A simple service that add all metadata templates available from schemas being handled in the
 * SchemaManager
 */
@Deprecated
public class AddDefault implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * schemaList is a list of comma separated schemas to load
     *
     * @return A report on the template import with information about the status of the insertion
     * operation (failed|loaded).
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {

        String schemaList = Util.getParam(params, Params.SCHEMA);
        String serviceStatus = "true";

        Element result = new Element(Jeeves.Elem.RESPONSE);
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);
        SchemaManager schemaMan = gc.getBean(SchemaManager.class);

        String siteId = gc.getBean(SettingManager.class).getSiteId();
        int owner = context.getUserSession().getUserIdAsInt();

        Log.info(Geonet.DATA_MANAGER, "Loading templates for schemas "
            + schemaList);
        String schemas[] = schemaList.split(",");

        for (String schemaName : schemas) {

            Element schema = new Element(schemaName);

            Path templatesDir = schemaMan.getSchemaTemplatesDir(schemaName);
            if (templatesDir == null) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format("Skipping - No templates found for schema '%s'.", schemaName));
                continue;
            }
            final String prefix = "sub-";
            final int prefixLength = prefix.length();
            try (DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(templatesDir, "*.xml")) {
                for (Path temp : newDirectoryStream) {
                    String status = "failed";
                    String templateName = temp.getFileName().toString();

                    Element template = new Element("template");
                    template.setAttribute("name", templateName);

                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER,
                            String.format(" - Adding %s template file %s ...",
                                schemaName, templateName));
                    }

                    try {
                        Element xml = Xml.loadFile(temp);
                        String uuid = UUID.randomUUID().toString();
                        String isTemplate = "y";
                        String title = null;

                        if (templateName.startsWith(prefix)) {
                            isTemplate = "s";
                            // subtemplates loaded here can have a title or uuid attribute
                            String tryUuid = xml.getAttributeValue("uuid");
                            if (tryUuid != null && tryUuid.length() > 0) uuid = tryUuid;
                            title = xml.getAttributeValue("title");
                            if (title == null || title.length() == 0) {
                              title = templateName.substring(prefixLength,
                                templateName.length() - prefixLength);
                            }
                            // throw away the uuid and title attributes if present as they
                            // cause problems for validation
                            xml.removeAttribute("uuid");
                            xml.removeAttribute("title");
                        }
                        //
                        // insert metadata
                        //
                        Metadata metadata = new Metadata();
                        metadata.setUuid(uuid);
                        metadata.getDataInfo().
                            setSchemaId(schemaName).
                            setRoot(xml.getQualifiedName()).
                            setType(MetadataType.lookup(isTemplate)).
                            setTitle(title);
                        metadata.getSourceInfo().
                            setSourceId(siteId).
                            setOwner(owner).
                            setGroupOwner(1);

                        dataMan.insertMetadata(context, metadata, xml, true, true, true, UpdateDatestamp.NO, false, false);

                        status = "loaded";
                    } catch (Exception e) {
                        serviceStatus = "false";
                        Log.error(Geonet.DATA_MANAGER,
                            String.format("Error loading %s template file %s. Error is %s.",
                                schemaName, temp, e.getMessage()),
                            e);
                    }
                    template.setAttribute("status", status);
                    schema.addContent(template);
                }
            }
            result.addContent(schema);
        }
        result.setAttribute("status", serviceStatus);
        return result;
    }
}
