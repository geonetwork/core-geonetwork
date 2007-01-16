//==============================================================================
//===
//===   Import (adapted class from druid project http://druid.sf.net)
//===
//===   Copyright (C) by Andrea Carboni.
//===   This file may be distributed under the terms of the GPL license.
//==============================================================================

package org.fao.gast.lib.druid;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import org.dlib.tools.FullTokenizer;
import org.dlib.tools.TVector;

//==============================================================================

public class Import
{
	private static final int START  = 0;
	private static final int FIELDS = 1;
	private static final int DATA   = 2;

	//---------------------------------------------------------------------------

	public static void load(Connection conn, String table, String fileName)
							throws SQLException, IOException
	{
		BufferedReader	rdr = new BufferedReader(new FileReader(fileName));

		String line;

		int status = START;

		TVector vsFields = new TVector();
		TVector vsQMarks = new TVector();

		PreparedStatement stmt = null;

		while ((line = rdr.readLine()) != null)
		{
			//--- skip comments or blank lines
			if (line.equals("") || line.startsWith("#")) continue;

			//--- start [FIELDS] section
			if (line.equals("[FIELDS]"))
				status = FIELDS;

			//--- start [DATA] section and build prepared statement
			else if (line.equals("[DATA]"))
			{
				status = DATA;
				String query = "INSERT INTO " + table + "(" + vsFields + ") " +
									"VALUES (" + vsQMarks + ")";

				stmt = conn.prepareStatement(query);
			}
			else
			{
				if (status == FIELDS)
				{
					vsFields.addElement(new ImportField(line));
					vsQMarks.addElement("?");
				}

				else if (status == DATA)
				{
					insertRow(stmt, vsFields, line);
				}

				else
					throw new IllegalArgumentException("Data not allowed before [FIELDS] section");
			}
		}

		if (stmt != null) stmt.close();

		rdr.close();

		if (status != DATA)
			throw new IllegalArgumentException("Unexpected EOF encountered");
	}

	//---------------------------------------------------------------------------

	private static void insertRow(PreparedStatement stmt, TVector vsFields, String line)
											throws SQLException
	{
		FullTokenizer ft = new FullTokenizer(line, "\t");

		if (ft.countTokens() != vsFields.size())
			throw new IllegalArgumentException("Field count differs from token count");

		String token;

		for(int i=0; i<ft.countTokens(); i++)
		{
			ImportField impf    = (ImportField) vsFields.elementAt(i);
			SqlType     sqlType = impf.sqlType;

			token = ft.nextToken();

			if (token.equals(""))
				stmt.setNull(i+1, sqlType.iId);
			else
			{
				if (sqlType.isDate())
					stmt.setDate(i+1, java.sql.Date.valueOf(token));

				else if (sqlType.isTime())
					stmt.setTime(i+1, Time.valueOf(token));

				else if (sqlType.isTimeStamp())
					stmt.setTimestamp(i+1, Timestamp.valueOf(token));

				else if (sqlType.isInteger())
					stmt.setLong(i+1, Long.parseLong(token));

				else if (sqlType.isReal())
					stmt.setDouble(i+1, Double.parseDouble(token));

				else if (sqlType.isBinaryType())
				{
					byte data[] = Codec.decodeBytes(token);
					stmt.setBinaryStream(i+1, new ByteArrayInputStream(data), data.length);
				}

				//--- we must divide the case of strings and longvarchar because
				//--- some drivers don't implement the setCharacterStream method

				else if (sqlType.isString())
					stmt.setString(i+1, Codec.decodeString(token));

				else if (sqlType.isLongVarChar())
				{
					String s = Codec.decodeString(token);
					stmt.setCharacterStream(i+1, new StringReader(s), s.length());
				}

				else
				{
					//--- we arrive here is the sql type is unknown
					stmt.setNull(i+1, sqlType.iId);
				}
			}
		}

		stmt.executeUpdate();
	}
}

//==============================================================================

class ImportField
{
	public String  sName;
	public String  sType;
	public SqlType sqlType;

	//---------------------------------------------------------------------------

	public ImportField(String line)
	{
		int idx = line.indexOf(",");

		if (idx == -1)
			throw new IllegalArgumentException("Bad field format");

		sName   = line.substring(0, idx).trim();
		sType   = line.substring(idx+1).trim();
		sqlType = SqlMapper.mapName(sType);

		if (!sType.equals(sqlType.sName))
			throw new IllegalArgumentException("Field types differ");
	}

	//---------------------------------------------------------------------------

	public String toString() { return sName; }
}

//==============================================================================

