//==============================================================================
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

package org.fao.gast.boot;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import org.fao.gast.localization.Messages;

//==============================================================================

public class Util
{
	//---------------------------------------------------------------------------
	//---
	//--- Boot methods
	//---
	//---------------------------------------------------------------------------

	/** Returns the full path of the jar file that contains the running class
	  */

	public static String getJarFile(String classPath)
	{
		String dir = ClassLoader.getSystemResource(classPath).toString();

		try
		{
			dir = URLDecoder.decode(dir, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			//--- this should not happen but ...

			e.printStackTrace();
		}

		dir = dir.replace('\\', '/');

		if (dir.startsWith("jar:"))	dir = dir.substring(4);
		if (dir.startsWith("file:"))	dir = dir.substring(5);

		//--- skip the ending string "/"+clazz

		dir = dir.substring(0, dir.length() - classPath.length() -1);

		//--- we must skip the "xxx.jar!" string (if the case)

		if (dir.endsWith("!"))
			dir = dir.substring(0, dir.length() - 1);

		//--- hack for windows : dirs like '/C:/...' must be changed to remove the
		//--- starting slash

		if (dir.startsWith("/") && dir.indexOf(":") != -1)
			dir = dir.substring(1);

		return dir;
	}

	//---------------------------------------------------------------------------

	public static URL[] getJarUrls(String[] dirs) throws Exception
	{
		ArrayList<String> al = new ArrayList<String>();

		for (String dir : dirs) {
			try {
				String jars[] = new File(dir).list();
				for(String jar : jars) {
					if (jar.endsWith(".jar")) {
					 	al.add("file:" + dir + "/" + jar);
					}
				}
			} catch(NullPointerException e) {
				showError("Null pointer ex while scanning : " +dir);
				throw e;
			}
		}

		URL urls[] = new URL[al.size()];
	
		int pos = 0;
		try {
			for(String jar : al) {
				urls[pos++] = new URL(jar);
			}
		} catch(MalformedURLException e) {
			showError("Malformed URL --> " + e.getMessage());
			throw e;
		}
	
		return urls;
	}

	//---------------------------------------------------------------------------

	public static void boot(String appPath, URL[] jarFiles, String className,
									String args[]) throws Exception
	{
		URLClassLoader mcl = new URLClassLoader(jarFiles);

		try
		{
			Starter starter = (Starter) Class.forName(className, true, mcl).newInstance();

			starter.start(appPath, args);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			showError(e);

			while (e.getCause() != null)
			{
				e = e.getCause();
				showError(e);
			}

			//--- this line is needed to exit in case of Errors
			//--- (not Exceptions) when the GUI is up

			System.exit(-1);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- GUI methods
	//---
	//---------------------------------------------------------------------------

	public static void showError(Throwable t)
	{
		String message = t.getClass().getSimpleName() +"\n"+ t.getMessage();

		JOptionPane.showMessageDialog(null, message, Messages.getString("error"), JOptionPane.ERROR_MESSAGE);
	}

	//---------------------------------------------------------------------------

	public static void showError(String message)
	{
		JOptionPane.showMessageDialog(null, message, Messages.getString("error"), JOptionPane.ERROR_MESSAGE);
	}

	//---------------------------------------------------------------------------

	public static void showInfo(String message)
	{
		JOptionPane.showMessageDialog(null, message, Messages.getString("info"), JOptionPane.INFORMATION_MESSAGE);
	}
}

//==============================================================================


