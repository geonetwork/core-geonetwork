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
public class WMCExtension
{
	private List<Entry> children = new ArrayList(); 

	private WMCExtension()
	{}

	public static WMCExtension newInstance()
	{
		return new WMCExtension();
	}

	public void add(String name, String xmlElement)
	{
		children.add(new Entry(name, xmlElement));
	}

	public String get(String name)
	{
		for(WMCExtension.Entry entry : children) {
			if(name.equalsIgnoreCase(entry.name))
				return entry.xml;
		}
		return null;
	}
	
	public Iterable<String> getExtensionsIterator()
	{
		return new Iterable<String>()
		{
			public Iterator<String> iterator() {
				return new Iterator<String>()
				{
					Iterator<Entry> entit = children.iterator();

					public boolean hasNext() {
						return entit.hasNext();
					}

					public String next() {
						return entit.next().xml;
					}

					public void remove() {
						entit.remove();
					}
					
				};
			}
		};
	}
	
	class Entry
	{
		String name;
		String xml; // strings are well-formed XML elements

		public Entry(String name, String xml) {
			this.name = name;
			this.xml = xml;
		}
	}
		
}
