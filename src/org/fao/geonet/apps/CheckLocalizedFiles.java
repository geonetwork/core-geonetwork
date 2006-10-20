package org.fao.geonet.apps;

import java.io.*;
import java.util.*;

import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;

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
		for (int i = 0; i < languages.length; i++)
			println("- " + languages[i]);
		println();
		
		// scan main language directory
		println("main language files");
		Hashtable mlFiles = new Hashtable();
		scan(mainLanguageDir, mlFiles);
		println();
		
		// check additional languages
		for (int i = 0; i < languages.length; i++)
		{
			String language = languages[i];
			File languageDir = new File(languagesDir, language);
			Hashtable lFiles = new Hashtable();
			
			println("scanning files for language '" + language + "'");
			scan(languageDir, lFiles);
			
			// for each file in main language directory
			for (Enumeration keys = mlFiles.keys(); keys.hasMoreElements(); )
			{
				String mlPath = (String)keys.nextElement();
				
				// check if file does not exists in localized directory
				File lFile = (File)lFiles.get(mlPath);
				if (lFile == null)
				{
					println("**** file " + mlPath + " is missing for language '" + language + "'");
					continue;
				}
			}
			// for each file in localized directory
			for (Enumeration keys = lFiles.keys(); keys.hasMoreElements(); )
			{
				String lPath = (String)keys.nextElement();
				
				println("- " + lPath);
				
				File lFile = (File)lFiles.get(lPath);
				
				// check if file does not exists in main language directory
				File mlFile = (File)mlFiles.get(lPath);
				if (mlFile == null)
				{
					println("**** extra file " + lPath);
					continue;
				}
				// if file is an XML file compare with main language one
				if (lPath.endsWith(".xml"))
					compareXML(lFile, mlFile);
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
			for (Iterator i = mlElem.getChildren().iterator(); i.hasNext(); )
			{
				Element mlChild = (Element)i.next();
				String  name    = mlChild.getName();
				Namespace ns    = mlChild.getNamespace();
				
				// check if child not exists in localized document
				Element lChild = lElem.getChild(name, ns);
				if (lChild == null)
				{
					println("**** element <" + name + "> is missing");
					println("\t" + Xml.getString(mlChild));
					continue;
				}
			}
			// for each root child in lElem
			for (Iterator i = lElem.getChildren().iterator(); i.hasNext(); )
			{
				Element lChild = (Element)i.next();
				String  name    = lChild.getName();
				Namespace ns    = lChild.getNamespace();
				
				// check if child not exists in main language document
				Element mlChild = mlElem.getChild(name, ns);
				if (mlChild == null)
				{
					println("**** extra element <" + name + ">");
					continue;
				}
			}
		}
		catch (Exception e)
		{
			println("**** exception: " + e.getMessage());
		}
	}
	
	private static void scan(File file, Hashtable paths)
	{
		scan(file, file.getPath().length() + 1, paths);
	}
	
	private static void scan(File file, int basePathOffset, Hashtable paths)
	{
		if (file.isDirectory())
		{
			File entries[] = file.listFiles(new RealFilenameFilter());
			for (int i = 0; i < entries.length; i++)
				scan(entries[i], basePathOffset, paths);
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

