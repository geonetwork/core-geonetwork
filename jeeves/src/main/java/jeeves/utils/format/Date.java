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

package jeeves.utils.format;

import java.util.StringTokenizer;

//=============================================================================

/** Represents a database field formatter
  */

public class Date implements Formatter
{
	public String toDB(String in, String format)
		throws Exception
	{
		if      (format.equalsIgnoreCase("dd-mm-yyyy")) return swap(in, "-", "-");
		else if (format.equalsIgnoreCase("dd/mm/yyyy")) return swap(in, "/", "-");
		else if (format.equalsIgnoreCase("yyyy/mm/dd")) return in.replace('/', '-');
		else return in; // other formats are tolerated
	}

	public String fromDB(String out, String format)
	{
		if      (format.equalsIgnoreCase("dd-mm-yyyy")) return swap(out, "-", "-");
		else if (format.equalsIgnoreCase("dd/mm/yyyy")) return swap(out, "-", "/");
		else if (format.equalsIgnoreCase("yyyy/mm/dd")) return out.replace('-', '/');
		else return out; // other formats are tolerated
	}

	private String swap(String in, String inDelimiter, String outDelimiter)
	{
		StringTokenizer st = new StringTokenizer(in, inDelimiter);
		String first = st.nextToken();
		String second = st.nextToken();
		String third = st.nextToken();
		return third + outDelimiter + second + outDelimiter + first;
	}
}

//=============================================================================

