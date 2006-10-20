//==============================================================================
//===
//===   Util
//===
//==============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.apps.common;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import javax.swing.JOptionPane;

//==============================================================================

public class Util
{
	//---------------------------------------------------------------------------
	//---
	//--- Main method
	//---
	//---------------------------------------------------------------------------

	public static void boot(String gnPath, String className)
	{
		URL urls[] = getJarUrls(gnPath+"/web/WEB-INF/lib");

		if (urls == null)
			return;

		URLClassLoader mcl = new URLClassLoader(urls);

		try
		{
			Starter starter = (Starter) Class.forName(className, true, mcl).newInstance();

			starter.start(gnPath);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			showError(e.getMessage());

			while (e.getCause() != null)
			{
				e = e.getCause();
				showError(e.getMessage());
			}

			//--- this line is needed to exit in case of Errors
			//--- (not Exceptions) when the GUI is up

			System.exit(-1);
		}
	}

	//---------------------------------------------------------------------------

	public static void showError(String message)
	{
		showError(null, message);
	}

	//---------------------------------------------------------------------------

	public static void showError(Component c, String message)
	{
		JOptionPane.showMessageDialog(c, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	//---------------------------------------------------------------------------

	public static void showInfo(String message)
	{
		showInfo(null, message);
	}

	//---------------------------------------------------------------------------

	public static void showInfo(Component c, String message)
	{
		JOptionPane.showMessageDialog(c, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private static URL[] getJarUrls(String dir)
	{
		try
		{
			String jars[] = new File(dir).list();

			Vector v = new Vector();

			for(int i=0; i<jars.length; i++)
				if (jars[i].endsWith(".jar"))
					 v.addElement(jars[i]);

			URL urls[] = new URL[v.size()];

			for(int i=0; i<v.size(); i++)
				urls[i] = new URL("file:" + dir + "/" + v.get(i));

			return urls;
		}

		catch(MalformedURLException e)
		{
			showError("Malformed URL --> " + e.getMessage());
			return null;
		}

		catch(NullPointerException e)
		{
			showError("Null pointer ex while scanning : " +dir);
			return null;
		}
	}
}

//==============================================================================


