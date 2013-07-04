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

package org.fao.geonet.kernel.setting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//=============================================================================

class Setting
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Setting(int id, String name, String value)
	{
		this.id    = id;
		this.name  = name;
		this.value = value;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Setting getParent() { return parent; }
	public int     getId()     { return id;     }
	public String  getName()   { return name;   }
	public String  getValue()  { return value;  }

	public void setName (String name)  { this.name  = name;  }
	public void setValue(String value) { this.value = value; }

	//---------------------------------------------------------------------------

	public Iterable<Setting> getChildren() { return children; }

	//---------------------------------------------------------------------------

	public Setting getChild(String name)
	{
		Iterator<Setting> i = getChildren(name).iterator();

		if (!i.hasNext()) return null;
			else				return i.next();
	}

	//---------------------------------------------------------------------------

	public Iterable<Setting> getChildren(String name)
	{
		ArrayList<Setting> list = new ArrayList<Setting>();

		for(Setting s : children)
			if (name == null || name.equals(s.getName()))
				list.add(s);

		return list;
	}

	//---------------------------------------------------------------------------

	public void addChild(Setting s)
	{
		s.parent   = this;

		children.add(s);
	}

	//---------------------------------------------------------------------------

	public void removeFromParent()
	{
		parent.children.remove(this);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private int     id;
	private String  name;
	private String  value;
	private Setting parent;

	private List<Setting> children = new ArrayList<Setting>();
}

//=============================================================================


