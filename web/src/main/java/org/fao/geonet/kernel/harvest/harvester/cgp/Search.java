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

package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.utils.Util;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

class Search
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Search()
	{
	}

	//---------------------------------------------------------------------------

	public Search(Element search) throws BadInputEx
	{
		freeText = Util.getParam(search, "freeText", "").trim();
		from = Util.getParam(search, "from", "");
		until = Util.getParam(search, "until", "");

		latNorth = Util.getParam(search, "latNorth", "");
		latSouth = Util.getParam(search, "latSouth", "");
		lonEast = Util.getParam(search, "lonEast", "");
		lonWest = Util.getParam(search, "lonWest", "");


		// Verify search values

		// Either from and/or until dates may be specified
		ISODate fromDate = verifyDate(from, "from");
		if (fromDate != null)
		{
			from = fromDate.getDate();
		}

		ISODate untilDate = verifyDate(until, "until");
		if (untilDate != null)
		{
			until = untilDate.getDate();
		}

		// Check from <= until if both specified
		if (fromDate != null && untilDate != null && fromDate.sub(untilDate) > 0)
		{
			throw new BadParameterEx("from greater than until", from + ">" + until);
		}

		// Verify bbox if specified
		if (lonWest.length() > 0 || latSouth.length() > 0 || lonEast.length() > 0 || latNorth.length() > 0)
		{
			// Is ok or exception
			verifyBBox();

			// BBox verified and found ok
			hasBBox = true;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Search copy()
	{
		Search s = new Search();

		s.freeText = freeText;
		s.from = from;
		s.until = until;

		return s;
	}

	//---------------------------------------------------------------------------

	public static Search createEmptySearch() throws BadInputEx
	{
		return new Search(new Element("search"));
	}

	//---------------------------------------------------------------------------

	public String getBBoxStr() throws BadInputEx
	{
		return lonWest + "," + latSouth + " " + lonEast + "," + latNorth;
	}

	//---------------------------------------------------------------------------

	public boolean hasBBox() throws BadInputEx
	{
		return hasBBox;
	}

	//---------------------------------------------------------------------------

	private void verifyBBox() throws BadInputEx
	{
		// Coordinates (format, values)  must be ok
		double[] lowerLeft = verifyCoord(lonWest, latSouth, "lonWest", "latSouth");
		double[] upperRight = verifyCoord(lonEast, latNorth, "lonEast", "latNorth");

		// Coords of Lower left to Upper right BBox must be ok.

		if (lowerLeft[0] > upperRight[0])
		{
			throw new BadParameterEx("lonWest", lonWest);
		}

		if (lowerLeft[1] > upperRight[1])
		{
			throw new BadParameterEx("latNorth", latNorth);
		}

		// ASSERT: valid BBox
	}

	//---------------------------------------------------------------------------

	private static ISODate verifyDate(String dateStr, String name) throws BadInputEx
	{
		try
		{
			return dateStr.length() > 0 ? new ISODate(dateStr) : null;
		}
		catch (Exception e)
		{
			throw new BadParameterEx(name, dateStr);
		}
	}

	//---------------------------------------------------------------------------

	private static double[] verifyCoord(String lonStr, String latStr, String lonName, String latName) throws BadInputEx
	{
		double[] result = new double[2];
		result[0] = verifyDouble(lonName, lonStr);
		result[1] = verifyDouble(latName, latStr);

		if (result[0] < -180d || result[0] > 180d)
		{
			throw new BadParameterEx(latName, lonStr);
		}

		if (result[1] < -90d || result[1] > 90d)
		{
			throw new BadParameterEx(latName, lonStr);
		}

		// Array with lon,lat
		return result;
	}

	//---------------------------------------------------------------------------

	private static double verifyDouble(String name, String d) throws BadInputEx
	{
		try
		{
			return Double.parseDouble(d);
		} catch (Throwable t)
		{
			throw new BadParameterEx(name, d);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String freeText;
	public String from;
	public String until;
	public String latNorth;
	public String latSouth;
	public String lonEast;
	public String lonWest;
	private boolean hasBBox;
}

//=============================================================================

