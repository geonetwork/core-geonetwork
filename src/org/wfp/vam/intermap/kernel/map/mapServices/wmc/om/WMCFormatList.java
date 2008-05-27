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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ETj
 */
public class WMCFormatList 
{
	private List<WMCFormat> _list = new ArrayList<WMCFormat>();

	private WMCFormatList()
	{}

	/**
	 * Method newInstance
	 */
	public static WMCFormatList newInstance()
	{
		return new WMCFormatList();
	}

	public void addFormat(WMCFormat format)
	{
		_list.add(format);
	}

	public Iterable<WMCFormat> getFormatIterator()
	{
		return new Iterable<WMCFormat>()
		{
			public Iterator<WMCFormat> iterator()
			{
				return _list.iterator();
			}
		};
	}
	
	public String getCurrentFormat()
	{
		for (WMCFormat wmcFormat : _list) {
			if(wmcFormat.isCurrent())
				return wmcFormat.getFormat();
		}
		
		return _list.isEmpty() ? null : _list.get(0).getFormat();
	}

	public boolean isEmpty() {
		return _list.isEmpty();
	}
}
