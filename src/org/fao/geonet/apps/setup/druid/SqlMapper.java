//==============================================================================
//===
//===   SqlMapper (adapted class from druid project http://druid.sf.net)
//===
//===   Copyright (C) by Andrea Carboni.
//===   This file may be distributed under the terms of the GPL license.
//==============================================================================

package org.fao.geonet.apps.setup.druid;

import java.sql.Types;

//==============================================================================

public class SqlMapper
{
	private static SqlType[] types =
	{
		new SqlType(Types.ARRAY,         "ARRAY",        SqlType.UNKNOWN),
		new SqlType(Types.BIGINT,        "BIGINT",       SqlType.CONST),
		new SqlType(Types.BINARY,        "BINARY",       SqlType.UNKNOWN),
		new SqlType(Types.BIT,           "BIT",          SqlType.BOTH),
		new SqlType(Types.BLOB,          "BLOB",         SqlType.CONST),
		new SqlType(Types.CHAR,          "CHAR",         SqlType.BOTH),
		new SqlType(Types.CLOB,          "CLOB",         SqlType.CONST),
		new SqlType(Types.DATE,          "DATE",         SqlType.CONST),
		new SqlType(Types.DECIMAL,       "DECIMAL",      SqlType.BOTH),
		new SqlType(Types.DISTINCT,      "DISTINCT",     SqlType.UNKNOWN),
		new SqlType(Types.DOUBLE,        "DOUBLE",       SqlType.CONST),
		new SqlType(Types.FLOAT,         "FLOAT",        SqlType.BOTH),
		new SqlType(Types.INTEGER,       "INTEGER",      SqlType.CONST),
		new SqlType(Types.JAVA_OBJECT,   "JAVA_OBJECT",  SqlType.UNKNOWN),
		new SqlType(Types.LONGVARBINARY, "LONGVARBINARY",SqlType.CONST),
		new SqlType(Types.LONGVARCHAR,   "LONGVARCHAR",  SqlType.CONST),
		new SqlType(Types.NULL,          "NULL",         SqlType.UNKNOWN),
		new SqlType(Types.NUMERIC,       "NUMERIC",      SqlType.BOTH),
		new SqlType(Types.OTHER,         "OTHER",        SqlType.UNKNOWN),
		new SqlType(Types.REAL,          "REAL",         SqlType.CONST),
		new SqlType(Types.REF,           "REF",          SqlType.UNKNOWN),
		new SqlType(Types.SMALLINT,      "SMALLINT",     SqlType.CONST),
		new SqlType(Types.STRUCT,        "STRUCT",       SqlType.UNKNOWN),
		new SqlType(Types.TIME,          "TIME",         SqlType.BOTH),
		new SqlType(Types.TIMESTAMP,     "TIMESTAMP",    SqlType.BOTH),
		new SqlType(Types.TINYINT,       "TINYINT",      SqlType.CONST),
		new SqlType(Types.VARBINARY,     "VARBINARY",    SqlType.VAR),
		new SqlType(Types.VARCHAR,       "VARCHAR",      SqlType.VAR)
	};

	//---------------------------------------------------------------------------

	public static SqlType mapId(Object id)
	{
		if (id != null)
			for(int i=0; i<types.length; i++)
				if (id.toString().equals("" + types[i].iId))
					return types[i];

		return new SqlType(-999, "(unknown)", SqlType.UNKNOWN);
	}

	//---------------------------------------------------------------------------

	public static SqlType mapName(String name)
	{
		if (name != null)
			for(int i=0; i<types.length; i++)
				if (name.equals(types[i].sName))
					return types[i];

		return new SqlType(-999, "(unknown)", SqlType.UNKNOWN);
	}

	//---------------------------------------------------------------------------

	/** This method is called to obtain the datatype of a table column
	  * id is the numeric java id as defined in java.sql.Types
	  * dbType is the string name of the type as reported by the dbms
	  * decimals is an integer specifying how many decimal digits the type has
	  */

	public static SqlType map(Object id, String dbType, Object decimals)
	{
		boolean hasDecimals = false;

		if (decimals != null && !decimals.toString().equals("0"))
			hasDecimals = true;

		SqlType sqlType = mapId(id);

		//--- some DBMSs (like Oracle) treat a decimal(nn) like an integer
		//--- in this case we must remap the type

		if (sqlType.iId == Types.DECIMAL || sqlType.iId == Types.NUMERIC)
			if (!hasDecimals)
				return mapId("" + Types.INTEGER);

		//--- some DBMSs (like Oracle) treat the blob type as 'other'
		//--- in this case another remap is needed

		if (sqlType.iId == Types.OTHER && dbType.toLowerCase().equals("blob"))
			return mapId("" + Types.BLOB);

		if (sqlType.iId == Types.OTHER && dbType.toLowerCase().equals("clob"))
			return mapId("" + Types.CLOB);

		return sqlType;
	}
}

//==============================================================================

