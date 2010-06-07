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

package org.wfp.vam.intermap.util;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSEX_GeographicBoundingBox;



public class Util
{

	/**
	 * Retrieve bbnorth, bb... from params.
	 * If all of them are parsable coords, the corresponding BoundingBox is returned.
	 *
	 * @return   a BoundingBox or null if a bb was not parsable,
	 *
	 * @author ETj
	 */
	public static BoundingBox parseBoundingBox(Element params)
	{
		try
		{
			float n = Float.parseFloat(params.getChildText("northBL"));
			float e = Float.parseFloat(params.getChildText("eastBL"));
			float s = Float.parseFloat(params.getChildText("southBL"));
			float w = Float.parseFloat(params.getChildText("westBL"));
			return new BoundingBox(n, s, e, w);
		}
		catch (NullPointerException e) // child not found
		{
			System.err.println("No valid bbox found");
			return null;
		}
		catch (NumberFormatException e) //
		{
			System.err.println("Bad float value ("+e.getMessage()+")");
			return null;
		}
	}

	public static BoundingBox getBB(WMSEX_GeographicBoundingBox gbb)
	{
		return new BoundingBox( gbb.getNorth(),
								gbb.getSouth(),
								gbb.getEast(),
								gbb.getWest());
	}

	/**
	 * Try and parse a String as an int.
	 *
	 * @return the parsed int, or defaultValue if parsableInteger is null or not parsable (in latter case, an error will be output)
	 *
	 * @author ETj
	 */
	public static int parseInt(String parsableInteger, int defaultValue)
	{
		if(parsableInteger == null)
			return defaultValue;

		try
		{
			return Integer.parseInt(parsableInteger);
		}
		catch (NumberFormatException e)
		{
			System.err.println("Bad int value '"+parsableInteger+"'");
			return defaultValue;
		}
	}

}

