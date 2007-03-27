//==============================================================================
//===
//===   Import (adapted class from druid project http://druid.sf.net)
//===
//===   Copyright (C) by Andrea Carboni.
//===   This file may be distributed under the terms of the GPL license.
//==============================================================================

package org.fao.gast.lib.druid;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import org.dlib.tools.FullTokenizer;
import org.dlib.tools.TVector;

//==============================================================================

public class Import
{
	public static void load(Connection conn, String table, String fileName)
							throws SQLException, FileNotFoundException, IOException, Exception
	{
		DdfLoader loader = new DdfLoader();

		loader.setHandler(new ImportHandler(conn, table));
		loader.load(fileName);
	}
}

//==============================================================================

class ImportHandler implements DdfLoader.Handler
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ImportHandler(Connection conn, String table)
	{
		this.conn  = conn;
		this.table = table;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Handler interface
	//---
	//---------------------------------------------------------------------------

	public void handleFields(List<ImportField> fields) throws Exception
	{
		this.fields = fields;

		TVector tvFields = new TVector();
		TVector tvQMarks = new TVector();

		for (ImportField field : fields)
		{
			tvFields.add(field.name);
			tvQMarks.add("?");
		}

		String query = "INSERT INTO " + table + "(" + tvFields + ") VALUES (" + tvQMarks + ")";

		stmt = conn.prepareStatement(query);
	}

	//---------------------------------------------------------------------------

	public void handleRow(List<String> values) throws Exception
	{
		for(int i=0; i<fields.size(); i++)
		{
			ImportField impf    = fields.get(i);
			SqlType     sqlType = impf.sqlType;

			String token = values.get(i);

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
					stmt.setCharacterStream(i+1, new StringReader(Codec.decodeString(token)), token.length());

				else
				{
					//--- we arrive here is the sql type is unknown
					stmt.setNull(i+1, sqlType.iId);
				}
			}
		}

		stmt.executeUpdate();
	}

	//---------------------------------------------------------------------------

	public void cleanUp()
	{
		try
		{
			if (stmt != null)
				stmt.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Connection conn;
	private String     table;

	private List<ImportField> fields;
	private PreparedStatement stmt;
}

//==============================================================================

