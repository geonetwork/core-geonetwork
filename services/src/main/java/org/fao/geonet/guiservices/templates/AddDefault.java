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
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple service that add all metadata templates available from schemas being
 * handled in the SchemaManager
 * 
 */
public class AddDefault implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/**
	 * 
	 * schemaList is a list of comma separated schemas to load
	 * 
	 * @return A report on the template import with information about the status
	 *         of the insertion operation (failed|loaded).
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

			String schemaDir = schemaMan.getSchemaTemplatesDir(schemaName);
			if (schemaDir == null) {
				Log.error(Geonet.DATA_MANAGER, "Skipping - No templates?");
				continue;
			}

			File templateFiles[] = new File(schemaDir).listFiles();
			List<File> templateFilesList = new ArrayList<File>();

			if (templateFiles != null) {
				for (File file : templateFiles)
					if (file.getName().endsWith(".xml"))
						templateFilesList.add(file);
			}

            final String prefix = "sub-";
            final int prefixLength = prefix.length();
            for (File temp : templateFilesList) {
                String status = "failed";
                String templateName = temp.getName();

                Element template = new Element("template");
                template.setAttribute("name", templateName);

                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER,
                            " - Adding template file (for schema " + schemaName + "): " + templateName);
                }

                try {
                    Element xml = Xml.loadFile(temp);
                    String uuid = UUID.randomUUID().toString();
                    String isTemplate = "y";
                    String title = null;

                    if (templateName.startsWith(prefix)) {
						isTemplate = "s";
						title = templateName.substring(prefixLength,
								templateName.length() - prefixLength);
					}
                    //
                    // insert metadata
                    //
                    String groupOwner = "1";
                    String docType = null, category = null, createDate = null, changeDate = null;
                    boolean ufo = true, indexImmediate = true;
					dataMan.insertMetadata(context, schemaName, xml, uuid, owner, groupOwner, siteId,
                                           isTemplate, docType, category, createDate, changeDate, ufo, indexImmediate);

					status = "loaded";
				} catch (Exception e) {
					serviceStatus = "false";
					Log.error(Geonet.DATA_MANAGER, "Error loading template: "
							+ e.getMessage());
				}
				template.setAttribute("status", status);
				schema.addContent(template);
			}
			result.addContent(schema);
		}
		result.setAttribute("status", serviceStatus);
		return result;
	}
}
