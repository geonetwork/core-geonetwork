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

package org.fao.geonet.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jeeves.utils.Util;

//=============================================================================

public class TextLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public List<String> load(String file) throws FileNotFoundException, IOException
	{
		FileInputStream is = new FileInputStream(file);
		BufferedReader  ir = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));

		ArrayList<String> al = new ArrayList<String>();

		String line;

		try
		{
			while ((line = ir.readLine()) != null)
				al.add(line);

			return al;
		}
		finally
		{
			ir.close();
		}
	}

	public List<String> load(String file, String encoding) throws FileNotFoundException, IOException
	{
		FileInputStream is = new FileInputStream(file);
		BufferedReader  ir = new BufferedReader(new InputStreamReader(is, encoding));

		ArrayList<String> al = new ArrayList<String>();
		
		String line;
		try
		{
			while ((line = ir.readLine()) != null)
				al.add(line);
			return al;
		}
		finally
		{
			ir.close();
		}
	}
	
	//---------------------------------------------------------------------------

	public void save(String file, List<String> lines) throws FileNotFoundException, IOException
	{
		FileOutputStream os = new FileOutputStream(file);
		BufferedWriter   ow = new BufferedWriter(new OutputStreamWriter(os, "ISO-8859-1"));

		try
		{
			for (String line : lines)
			{
				ow.write(line);
				ow.newLine();
			}
		}
		finally
		{
			ow.close();
		}
	}

	//---------------------------------------------------------------------------

	public String getProperty(List<String> lines, String name)
	{
		for (String line : lines)
			if (!line.startsWith("#"))
			{
				int pos = line.indexOf("=");

				if (pos != -1)
				{
					String curName  = line.substring(0, pos).trim();
					String curValue = line.substring(pos +1).trim();

					if (name.equals(curName))
						return curValue;
				}
			}

		return null;
	}

	//---------------------------------------------------------------------------

	public void setProperty(List<String> lines, String name, String value)
	{
		for (int i=0; i<lines.size(); i++)
		{
			String line = lines.get(i).trim();
			int    pos  = line.indexOf('=');

			if (!line.startsWith("#") && pos != -1)
			{
				String curName = line.substring(0, pos).trim();

				if (name.equals(curName))
				{
					lines.set(i, name +"="+ value);
					return;
				}
			}
		}

		lines.add(name +"="+ value);
	}

	//---------------------------------------------------------------------------

	public String getRandomString(int length)
	{
		StringBuffer sb = new StringBuffer();

		for (int i=0; i<length; i++)
			sb.append(getRandomChar());

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public char getRandomChar()
	{
		int pos = (int) (Math.random() * 62);

		if (pos <26)
			return (char) ('a' + pos);

		pos -= 26;

		if (pos <26)
			return (char) ('A' + pos);

		pos -= 26;

		return (char) ('0' + pos);
	}

	//---------------------------------------------------------------------------

	public void replace(List<String> lines, Map<String, ? extends Object> vars)
	{
		for (int i=0; i<lines.size(); i++)
		{
			String line = lines.get(i);

			for (Map.Entry<String, ? extends Object> entry : vars.entrySet())
				line = Util.replaceString(line, entry.getKey(), entry.getValue().toString());

			lines.set(i, line);
		}
	}
}

//=============================================================================

