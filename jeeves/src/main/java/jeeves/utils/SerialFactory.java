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

import java.io.*;
import java.sql.*;
import java.util.*;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.resources.dbms.*;

//=============================================================================

/** Simple class with only one method to obtain the next serial for a table
  */

public class SerialFactory
{
	private Hashtable htSerials = new Hashtable(20, .75f);

	//--------------------------------------------------------------------------
	//---
	//--- API
	//---
	//--------------------------------------------------------------------------

	public int getSerial(Dbms dbms, String table) throws SQLException
	{
		return getSerial(dbms, table, "id", 0);
	}

	//--------------------------------------------------------------------------

	public int getSerial(Dbms dbms, String table, String field) throws SQLException
	{
		return getSerial(dbms, table, field, 0);
	}

	//--------------------------------------------------------------------------

	public synchronized int getSerial(Dbms dbms, String table, String field, int minSerial) throws SQLException
	{
		Integer intSerial = (Integer) htSerials.get(table);

		//--- is the serial already in memory ?

		if (intSerial == null)
		{
			//--- no, we must get the last serial from the related table

			Element res = dbms.select("SELECT MAX("+field+") as maxid FROM " + table);

			String strSerial = res.getChild(Jeeves.Elem.RECORD).getChildText("maxid");

			int i;

			if (strSerial.equals("")) i = 0;
				else                   i = Integer.parseInt(strSerial);

			i = (i < minSerial) ? minSerial : i;
			intSerial = new Integer(i);
		}

		int newSerial = intSerial.intValue() +1;
		htSerials.put(table, new Integer(newSerial));

		return newSerial;
	}
}

//=============================================================================

