//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//=============================================================================

/**
 * A container of I/O methods. <P>
 * 
 */
public final class IO
{
	/**
    * Default constructor.
    * Builds a IO.
    */
   private IO() {}
   
   /**
	 * Loads a text file, handling the exceptions
	 * @param name
	 * @return
	 */
	public static String loadFile(String name)
	{
		StringBuffer sb = new StringBuffer();

		try
		{
			BufferedReader	rdr = new BufferedReader(new FileReader(name));

			String inputLine;

			while ((inputLine = rdr.readLine()) != null) {
				sb.append(inputLine);
				sb.append('\n');
			}

			rdr.close();

			return sb.toString();
		}
		catch (IOException e)
		{
			return null;
		}
	}
}

//=============================================================================

