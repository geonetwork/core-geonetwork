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

import jeeves.resources.dbms.Dbms;

//=============================================================================

class Task
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Task(Dbms dbms, Type type, Setting s)
	{
		this.dbms    = dbms;
		this.type    = type;
		this.setting = s;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Static API methods
	//---
	//---------------------------------------------------------------------------

	public static Task getNameChangedTask(Dbms dbms, Setting s, String name)
	{
		Task task = new Task(dbms, Type.NAME_CHANGED, s);
		task.name = name;

		return task;
	}

	//---------------------------------------------------------------------------

	public static Task getValueChangedTask(Dbms dbms, Setting s, String value)
	{
		Task task = new Task(dbms, Type.VALUE_CHANGED, s);
		task.value = value;

		return task;
	}

	//---------------------------------------------------------------------------

	public static Task getAddedTask(Dbms dbms, Setting parent, Setting child)
	{
		Task task = new Task(dbms, Type.ADDED, child);
		task.parent = parent;

		return task;
	}

	//---------------------------------------------------------------------------

	public static Task getRemovedTask(Dbms dbms, Setting s)
	{
		return new Task(dbms, Type.REMOVED, s);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public boolean matches(Object resource)
	{
		return (dbms == resource);
	}

	//---------------------------------------------------------------------------

	public Setting getAddedSetting(Dbms dbms, int id)
	{
		if (type == Type.ADDED && dbms == this.dbms && setting.getId() == id)
			return setting;

		return null;
	}

	//---------------------------------------------------------------------------

	public void commit()
	{
		switch(type)
		{
			case NAME_CHANGED :
					setting.setName(name);
					break;

			case VALUE_CHANGED :
					setting.setValue(value);
					break;

			case ADDED:
					parent.addChild(setting);
					break;

			case REMOVED:
					setting.removeFromParent();
					break;
					
			default:
			    throw new IllegalArgumentException("Doesn't handle:" + type);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private enum Type { NAME_CHANGED, VALUE_CHANGED, ADDED, REMOVED }

	private Dbms    dbms;
	private Type    type;
	private Setting setting;
	private Setting parent;
	private String  name;
	private String  value;
}

//=============================================================================

