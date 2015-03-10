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

package org.fao.geonet.services.util.z3950;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//=============================================================================

/** Z3950 Repositories - setup needed for both client and server ops 
  */

public class Repositories
{

	private static Path configPath;

	//--------------------------------------------------------------------------

	/** builds the repositories config file from template
	  */
	public static boolean build(URL cfgUrl, ServiceContext context) 
	{
        String configPathString;
		try
		{
		    if (cfgUrl == null) {
		        context.warning("Cannot initialize Z39.50 repositories because the file "+Geonet.File.JZKITCONFIG_TEMPLATE+" could not be found in the classpath");
		        return false;
		    } else {
                configPathString = URLDecoder.decode(cfgUrl.getFile(), Constants.ENCODING);
		    }
			//--- build repositories file from template repositories file

			Path realRepo = IO.toPath(StringUtils.substringBefore(configPathString, ".tem"));
            configPath = IO.toPath(configPathString);
			Path tempRepo = configPath;

			buildRepositoriesFile(tempRepo, realRepo);

		}
		catch (Exception e)
		{
			context.warning("Cannot initialize Z39.50 repositories : "+ e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//--------------------------------------------------------------------------

	/** clear the repositories template file - read the file and remove all
	  * <Repository> entries except the GNSearchable one
	  */
	public static boolean clearTemplate(ServiceContext context) 
	{
		Path tempRepo = configPath;
        Path backRepo = getBackupPath(tempRepo);

		boolean copied = false;

		try {
            java.nio.file.Files.copy(tempRepo, backRepo);
            copied = true;

			Element root  = Xml.loadFile(tempRepo);
			Element copy  = new Element(root.getName());
			@SuppressWarnings("unchecked")
            List<Element> children = root.getChildren();
			for (Element child : children) {
				if (child.getName().equals("Repository") && child.getAttributeValue("className").equals("org.jzkit.search.provider.z3950.Z3950Origin")) continue;
				copy.addContent((Content)child.clone());
			}
             try (OutputStream os = java.nio.file.Files.newOutputStream(tempRepo)) {
                 Xml.writeResponse(new Document(copy), os);
             }

		} catch (Exception e) {
			context.warning("Cannot clear Z39.50 repositories template : "+ e.getMessage());
			e.printStackTrace();
			// restore the backup copy
			if (copied) {
				try {
                    java.nio.file.Files.copy(backRepo, tempRepo);
				} catch (IOException ioe) {
					context.error("Cannot restore Z39.50 repositories template : this is serious and should not happen"+ ioe.getMessage());
					ioe.printStackTrace();
				}
			}
			return false;
		}
		return true;
	}

    protected static Path getBackupPath(Path tempRepo) {
        String backupName = tempRepo.getFileName().toString() + ".backup";
        return tempRepo.getParent().resolve(backupName);
    }

    //--------------------------------------------------------------------------

	/** Add a <Repository> element to the template file or replace one that
	  * is already present
	  */
	public static boolean addRepo(ServiceContext context, String code, Element repo) 
	{
		Path tempRepo = configPath;
        Path backRepo = getBackupPath(tempRepo);

		boolean copied = false;
		boolean replaced = false;

		try {
            java.nio.file.Files.copy(tempRepo, backRepo);
            copied = true;

			Element root  = Xml.loadFile(tempRepo);
			Element copy  = new Element(root.getName());
			@SuppressWarnings("unchecked")
            List<Element> children = root.getChildren();
			for (Element child : children) {
				if (child.getName().equals("Repository") && child.getAttributeValue("code").equals(code)) {
					copy.addContent(repo);
					replaced = true;
				} else {
					copy.addContent((Content)child.clone());
				}
			}
			if (!replaced) copy.addContent(repo); // just add it

            try (OutputStream os = Files.newOutputStream(tempRepo)) {
                Xml.writeResponse(new Document(copy), os);
            }
		} catch (Exception e) {
			context.warning("Cannot add Z39.50 repository " + Xml.getString(repo) + " : "+ e.getMessage());
			e.printStackTrace();
			// restore the backup copy
			if (copied) {
				try {
				    Files.copy(backRepo, tempRepo);
				} catch (IOException ioe) {
					context.error("Cannot restore Z39.50 repositories template : this is serious and should not happen"+ ioe.getMessage());
					ioe.printStackTrace();
				}
			}
			return false;
		}
		return true;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private static void buildRepositoriesFile(Path src, Path des) throws Exception
	{
		Element root  = Xml.loadFile(src);

		// --- insert warning into file as comment - first child element
		root.addContent(0,new Comment("\nWARNING - Do NOT MODIFY this file!\n"+
		"It is AUTOMATICALLY GENERATED by GeoNetwork each time it starts up \n"+
		"from the contents of "+src+".\n"));

        //--- now output the repositories file
        try (OutputStream os = Files.newOutputStream(des)) {
            Xml.writeResponse(new Document(root), os);
        }
	}

}

//=============================================================================

