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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows a user to set the xsl used for displaying metadata.
 * 
 * @author jeichar
 */
public class ListBundleFiles extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {

        String xslid = Util.getParam(params, Params.ID);
        String schema = Util.getParam(params, Params.SCHEMA, null);
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = context.getBean(SchemaManager.class).getSchemaDir(schema);
        }

        Path formatDir = getAndVerifyFormatDir(context.getBean(GeonetworkDataDirectory.class), Params.ID, xslid, schemaDir).toRealPath();

        Element result = new Element("bundleFiles");
        makeTree("", formatDir, result);

        return result;
    }

    private void makeTree(String parentId, Path dir, Element result) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
            for (Path file : files) {
                String name = URLEncoder.encode(file.getFileName().toString(), Constants.ENCODING);
                Element element;
                String id = parentId + "/" + file.getFileName();
                if (Files.isDirectory(file) && legalFile(file)) {
                    element = new Element("dir");
                    makeTree(id, file, element);
                    if (element.getChildren().size() > 0) {
                        element.setAttribute("leaf", "false");
                        element.setAttribute("text", file.getFileName().toString()).setAttribute("path", id).setAttribute("name", name);
                        result.addContent(element);
                    }
                } else if (isEditibleFileType(file) && legalFile(file)) {
                    element = new Element("file");
                    element.setAttribute("leaf", "true");
                    element.setAttribute("text", file.getFileName().toString()).setAttribute("path", id).setAttribute("name", name);
                    result.addContent(element);
                }
            }
        }
    }
	private final static String[] extensions = {"properties", "xml", "xsl", "css", ".js"};

    private boolean isEditibleFileType(Path f) {
		Path fileName = f.getFileName();
		for (String ext : extensions) {
			if(fileName.endsWith("."+ext)) return true;
		}
		
		return fileName.toString().equalsIgnoreCase("README");
	}

	private boolean legalFile(Path f) throws IOException {
        return !f.getFileName().startsWith(".") && !Files.isHidden(f) && Files.isReadable(f) && Files.isWritable(f);
    }


}
