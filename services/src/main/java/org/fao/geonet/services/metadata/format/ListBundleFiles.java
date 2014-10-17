//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata.format;

import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Allows a user to set the xsl used for displaying metadata.
 * 
 * @author jeichar
 */
public class ListBundleFiles extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {

        String xslid = Util.getParam(params, Params.ID);
        String schema = Util.getParam(params, Params.SCHEMA, null);
        File schemaDir = null;
        if (schema != null) {
            schemaDir = new File(context.getBean(SchemaManager.class).getSchemaDir(schema));
        }

        File formatDir = getAndVerifyFormatDir(context.getBean(GeonetworkDataDirectory.class), Params.ID, xslid, schemaDir).getCanonicalFile();

        Element result = new Element("bundleFiles");
        makeTree("", formatDir, result);

        return result;
    }

    private void makeTree(String parentId, File dir, Element result) throws UnsupportedEncodingException {

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            String name = URLEncoder.encode(f.getName(), Constants.ENCODING);
            Element element;
            String id = parentId + "/" + f.getName();
            if (f.isDirectory() && legalFile(f)) {
                element = new Element("dir");
                makeTree(id, f, element);
                if (element.getChildren().size() > 0) {
                    element.setAttribute("leaf", "false");
                    element.setAttribute("text", f.getName()).setAttribute("path", id).setAttribute("name", name);
                    result.addContent(element);
                }
            } else if (isEditibleFileType(f) && legalFile(f)) {
                element = new Element("file");
                element.setAttribute("leaf", "true");
                element.setAttribute("text", f.getName()).setAttribute("path", id).setAttribute("name", name);
                result.addContent(element);
            }
        }
    }
	private final static String[] extensions = {"properties", "xml", "xsl", "css", ".js"};

    private boolean isEditibleFileType(File f) {
		String fileName = f.getName();
		for (String ext : extensions) {
			if(fileName.endsWith("."+ext)) return true;
		}
		
		return fileName.equalsIgnoreCase("README");
	}

	private boolean legalFile(File f) {
        return !f.getName().startsWith(".") && !f.isHidden() && f.canRead() && f.canWrite();
    }


}
