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

package org.fao.geonet.guiservices.util;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.SchemaManager;

//=============================================================================

  /**
  * This service returns all stylesheets that can be used during batch import
  * selected tab.
  *
  * Stylesheets are searched in the default <code>xsl/conversion/import</code> dir and in
  * every <code>convert/import</code> directory in schema plugins.
  */

public class GetImportXSLs implements Service
{
	private String appPath;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		this.appPath = appPath;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String dir = appPath + Geonet.Path.IMPORT_STYLESHEETS;

		String sheets[] = new File(dir).list();

		if (sheets == null)
			throw new Exception("Cannot scan directory : "+ dir);

		Element elRoot = new Element("a");

        for (String sheet : sheets) {
            if (sheet.endsWith(".xsl")) {
                int pos = sheet.lastIndexOf(".xsl");
                String name = sheet.substring(0, pos);
                String id = sheet;

                Element el = new Element(Jeeves.Elem.RECORD);

                el.addContent(new Element(Geonet.Elem.ID).setText(id));
                el.addContent(new Element(Geonet.Elem.NAME).setText(name));

                elRoot.addContent(el);
            }
        }

        for (SortedMap.Entry<String, SortedSet<String>> schemaFiles : getImportXslForSchemas(context).entrySet()) {
            String schemaName = schemaFiles.getKey();
            Set<String> files = schemaFiles.getValue();

            for (String filename : files) {

                int pos = filename.lastIndexOf(".xsl");

                String name = schemaName + " / " + filename.substring(0, pos);
                String id = schemaName + "/" + filename;

                Element el = new Element(Jeeves.Elem.RECORD);

                el.addContent(new Element(Geonet.Elem.ID).setText(id));
                el.addContent(new Element(Geonet.Elem.NAME).setText(name));

                elRoot.addContent(el);
            }
        }

		return elRoot;
	}

    /**
     * @return a Map with the schema name as key and the import XSL filenames as values.
     */
    private SortedMap<String, SortedSet<String>> getImportXslForSchemas(ServiceContext context) {

        SortedMap<String, SortedSet<String>> ret = new TreeMap<String, SortedSet<String>>();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager schemaMan = gc.getSchemamanager();

        for (String schemaName : schemaMan.getSchemas()) {
            String schemaDir = schemaMan.getSchemaDir(schemaName);
            File convertDir = new File(schemaDir, Geonet.Path.CONVERT_STYLESHEETS);
            File importDir = new File(convertDir, "import");

            if(importDir.isDirectory()) {
                Collection<File> files = FileUtils.listFiles(importDir, new String[]{"xsl"}, false);
                SortedSet<String> fileNames = new TreeSet<String>();
                for (File file : files) {
                    fileNames.add(file.getName());
                }

                if( ! fileNames.isEmpty()) {
                    ret.put(schemaName, fileNames);
                }
            }
        }

        return ret;
    }
}

//=============================================================================

