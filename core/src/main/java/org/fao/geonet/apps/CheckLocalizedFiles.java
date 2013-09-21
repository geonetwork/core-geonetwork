//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.apps;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Hashtable;

public class CheckLocalizedFiles
{
	public static void main(String args[])
	{
		if (args.length != 2)
			error("usage: CheckLocalizedFiles languageDir mainLanguageCode");
		
		final String languageDirPath  = args[0];
		final String mainLanguageCode = args[1];
		
		File languagesDir = new File(languageDirPath);
		if (!languagesDir.isDirectory())
			error("directory " + languageDirPath + " does not exist");

		File mainLanguageDir = new File(languagesDir, mainLanguageCode);
		if (!mainLanguageDir.isDirectory())
			error("main language directory " + mainLanguageDir.getPath() + " does not exist");
		
		println("LOCALIZED FILES REPORT");
		println();
		println("main language: " + mainLanguageCode);
		println();
		
		// find additional languages
		String languages[] = languagesDir.list(new RealFilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						return super.accept(dir, name) && !name.equals(mainLanguageCode);
					}
				});
		println("additional languages");
        for (String language1 : languages) {
            println("- " + language1);
        }
		println();
		
		// scan main language directory
		println("main language files");
		Hashtable<String, File> mlFiles = new Hashtable<String, File>();
		scan(mainLanguageDir, mlFiles);
		println();
		
		// check additional languages
        for (String language : languages) {
            File languageDir = new File(languagesDir, language);
            Hashtable<String, File> lFiles = new Hashtable<String, File>();

            println("scanning files for language '" + language + "'");
            scan(languageDir, lFiles);

            // for each file in main language directory
            for (Enumeration<String> keys = mlFiles.keys(); keys.hasMoreElements();) {
                String mlPath = keys.nextElement();

                // check if file does not exists in localized directory
                File lFile = lFiles.get(mlPath);
                if (lFile == null) {
                    println("**** file " + mlPath + " is missing for language '" + language + "'");
                }
            }
            // for each file in localized directory
            for (Enumeration<String> keys = lFiles.keys(); keys.hasMoreElements();) {
                String lPath = keys.nextElement();

                println("- " + lPath);

                File lFile = lFiles.get(lPath);

                // check if file does not exists in main language directory
                File mlFile = mlFiles.get(lPath);
                if (mlFile == null) {
                    println("**** extra file " + lPath);
                    continue;
                }
                // if file is an XML file compare with main language one
                if (lPath.endsWith(".xml")) {
                    compareXML(lFile, mlFile);
                }
            }
            println();
        }
	}
	
	private static void compareXML(File lFile, File mlFile)
	{
		try
		{
			Element mlElem = Xml.loadFile(mlFile);
			Element lElem  = Xml.loadFile(lFile);
			
			// for each root child in mlElem
            for (Object o : mlElem.getChildren()) {
                Element mlChild = (Element) o;
                String name = mlChild.getName();
                Namespace ns = mlChild.getNamespace();

                // check if child not exists in localized document
                Element lChild = lElem.getChild(name, ns);
                if (lChild == null) {
                    println("**** element <" + name + "> is missing");
                    println("\t" + Xml.getString(mlChild));
                }
            }
			// for each root child in lElem
            for (Object o : lElem.getChildren()) {
                Element lChild = (Element) o;
                String name = lChild.getName();
                Namespace ns = lChild.getNamespace();

                // check if child not exists in main language document
                Element mlChild = mlElem.getChild(name, ns);
                if (mlChild == null) {
                    println("**** extra element <" + name + ">");
                }
            }
		}
		catch (Exception e)
		{
			println("**** exception: " + e.getMessage());
		}
	}
	
	private static void scan(File file, Hashtable<String, File> paths)
	{
		scan(file, file.getPath().length() + 1, paths);
	}
	
	private static void scan(File file, int basePathOffset, Hashtable<String, File> paths)
	{
		if (file.isDirectory())
		{
			File entries[] = file.listFiles(new RealFilenameFilter());
            for (File entry : entries) {
                scan(entry, basePathOffset, paths);
            }
		}
		else
		{
			String relPath = file.getPath().substring(basePathOffset);
			paths.put(relPath, file);
			
			// println("- " + relPath); // DEBUG
		}
	}
	
	private static void error(String message)
	{
		System.err.println(message);
		System.exit(1);
	}
	
	private static void println(String message)
	{
		System.out.println(message);
	}
	
	private static void println()
	{
		System.out.println();
	}
	
	private static class RealFilenameFilter implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			return !(name.startsWith(".") || name.equals("CVS"));
		}
	}
}

