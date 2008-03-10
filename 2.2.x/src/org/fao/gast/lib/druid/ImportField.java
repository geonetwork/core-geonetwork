//==============================================================================
//===
//===   Import (adapted class from druid project http://druid.sf.net)
//===
//===   Copyright (C) by Andrea Carboni.
//===   This file may be distributed under the terms of the GPL license.
//==============================================================================

package org.fao.gast.lib.druid;

//==============================================================================

public class ImportField
{
	public String  name;
	public String  type;
	public SqlType sqlType;

	//---------------------------------------------------------------------------

	ImportField(String line)
	{
		int idx = line.indexOf(",");

		if (idx == -1)
			throw new IllegalArgumentException("Bad field format");

		name   = line.substring(0, idx).trim();
		type   = line.substring(idx+1).trim();
		sqlType = SqlMapper.mapName(type);

		if (!type.equals(sqlType.sName))
			throw new IllegalArgumentException("Field types differ");
	}

	//---------------------------------------------------------------------------

	public String toString() { return name; }
}

//==============================================================================

