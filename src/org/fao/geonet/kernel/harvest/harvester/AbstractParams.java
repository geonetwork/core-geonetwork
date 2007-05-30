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

package org.fao.geonet.kernel.harvest.harvester;

import jeeves.exceptions.BadParameterEx;
import org.jdom.Element;

//=============================================================================

public abstract class AbstractParams
{
	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected String getValue(Element el, String name, String defValue)
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		return (value != null) ? value : defValue;
	}

	//---------------------------------------------------------------------------

	protected boolean getValue(Element el, String name, boolean defValue) throws BadParameterEx
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		if (value == null)
			return defValue;

		if (!value.equals("true") && !value.equals("false"))
			throw new BadParameterEx(name, value);

		return Boolean.parseBoolean(value);
	}

	//---------------------------------------------------------------------------

	protected int getValue(Element el, String name, int defValue) throws BadParameterEx
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		if (value == null || value.length() == 0)
			return defValue;

		try
		{
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			throw new BadParameterEx(name, value);
		}
	}

	//---------------------------------------------------------------------------

	protected void checkEvery(int every) throws BadParameterEx
	{
		if (every <1 || every > MAX_EVERY)
			throw new BadParameterEx("every", every);
	}

	//---------------------------------------------------------------------------

	protected void checkPort(int port) throws BadParameterEx
	{
		if (port <1 || port > 65535)
			throw new BadParameterEx("port", port);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static final int MAX_EVERY = 1000000;
}

//=============================================================================

