//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.resources.dbms;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.utils.Log;
import org.jdom.Element;

//=============================================================================

/** Represents a database connection
  */

public class Dbms
{
	public static final String DEFAULT_DATE_FORMAT      = "dd-MM-yyyy";
	public static final String DEFAULT_TIME_FORMAT      = "HH:mm:ss";
	public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private String     url;
	private Connection conn;
	private long       lastConnTime;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Constructs a DBMS object that contains a jdbc connection */

	public Dbms(String driver, String url) throws ClassNotFoundException
	{
		// loads the driver
		Class.forName(driver);

		this.url = url;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Connection methods
	//---
	//--------------------------------------------------------------------------

	/** connects to a DBMS */

	public void connect(String username, String password) throws SQLException
	{
		String actualUrl = url;
		if (actualUrl.contains("postgis")) actualUrl = actualUrl.replaceFirst("postgis","postgresql");
		conn = DriverManager.getConnection(actualUrl, username, password);

		conn.setAutoCommit(false);
		if (actualUrl.toUpperCase().contains("ORACLE")) {
			Log.debug(Log.RESOURCES,"ORACLE is using TRANSACTION_READ_COMMITTED");
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} else {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}

		lastConnTime = System.currentTimeMillis();
	}

	//--------------------------------------------------------------------------
	/** disconnects from the DBMS */

	public void disconnect()
	{
		try
		{
			conn.close();
		}
		catch(SQLException e) { e.printStackTrace(); }
	}

	//--------------------------------------------------------------------------
	/** Returns the jdbc connection to the DBMS */

	public Connection getConnection() { return conn; }

	//--------------------------------------------------------------------------
	/** Returns the time since last connection in milliseconds */

	public long getLastConnTime() { return lastConnTime; }

	//--------------------------------------------------------------------------

	public String getURL() { return url; }

	//--------------------------------------------------------------------------

	public boolean isClosed()
	{
		try
		{
			return conn.isClosed();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return true;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Transaction methods
	//---
	//--------------------------------------------------------------------------

	/** commits the current transaction */

	public void commit() throws SQLException
	{
		conn.commit();
	}

	//--------------------------------------------------------------------------
	/** aborts the current transaction */

	public boolean abort()
	{
		try
		{
			conn.rollback();
			return true;
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Select methods
	//---
	//--------------------------------------------------------------------------

	public Element select(String query) throws SQLException
	{
		return selectFull(query, new Hashtable(), (Object[]) null);
	}

	//--------------------------------------------------------------------------

	public Element select(String query, Object... args) throws SQLException
	{
		return selectFull(query, new Hashtable(), args);
	}

	//--------------------------------------------------------------------------

	public Element select(String query, Hashtable formats) throws SQLException
	{
		return selectFull(query, formats, (Object[]) null);
	}

	//--------------------------------------------------------------------------

	public Element selectFull(String query, Hashtable formats, Object... args) throws SQLException
	{
		Log.debug(Log.Dbms.SELECT, "Query : "+ query);

		if (args != null)
			Log.debug(Log.Dbms.SELECT, "Args  : "+ getArgs(args));

		PreparedStatement stmt = conn.prepareStatement(query);

		if (args != null)
			for(int i=0; i<args.length; i++)
				setObject(stmt, i, args[i]);

		try
		{
			long start = System.currentTimeMillis();
			Element result = buildResponse(stmt.executeQuery(), formats);
			long end = System.currentTimeMillis();

			float time = end - start;

			Log.debug(Log.Dbms.SELECT, "Found "+ result.getContentSize() +" records in "+time/1000+" secs");

			return result;
		}
		finally
		{
			stmt.close();
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Execute methods
	//---
	//--------------------------------------------------------------------------

	/** A simple wrapper to the other execute method */

	public int execute(String query) throws SQLException
	{
		return execute(query, (Object[]) null);
	}

	//--------------------------------------------------------------------------
	/** Executes a database statement. It can be an INSERT, an UPDATE, a DELETE
	  * or a generic data manipulation language query
	  */

	public int execute(String query, Object... args) throws SQLException
	{
		Log.debug(Log.Dbms.EXECUTE, "Query    : "+ query);

		if (args != null)
			Log.debug(Log.Dbms.EXECUTE, "Args     : "+ getArgs(args));

		PreparedStatement stmt = conn.prepareStatement(query);

		if (args != null)
			for(int i=0; i<args.length; i++)
				setObject(stmt, i, args[i]);

		try
		{
			long start = System.currentTimeMillis();
			int result = stmt.executeUpdate();
			long end = System.currentTimeMillis();

			float time = end - start;

			Log.debug(Log.Dbms.EXECUTE, "Affected "+ result +" records in "+ time/1000 +" secs");

			return result;
		}
		finally
		{
			stmt.close();
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Element buildResponse(ResultSet rs, Hashtable formats) throws SQLException
	{
		ResultSetMetaData md = rs.getMetaData();

		int colNum = md.getColumnCount();

		//--- retrieve name and type of fields

		Vector vHeaders = new Vector();
		Vector vTypes   = new Vector();

		for (int i = 0; i < colNum; i++)
		{
			vHeaders.add(md.getColumnName(i + 1).toLowerCase());
			vTypes.add(new Integer(md.getColumnType(i + 1)));
		}

		//--- build the jdom tree

		Element root = new Element(Jeeves.Elem.RESPONSE);

		while (rs.next())
		{
			Element record = new Element(Jeeves.Elem.RECORD);

			for(int i = 0; i < colNum; i++)
			{
				String name = vHeaders.get(i).toString();
				int    type = ((Integer)vTypes.get(i)).intValue();
				record.addContent(buildElement(rs, i, name, type, formats));
			}
			root.addContent(record);
		}
		return root;
	}

	//--------------------------------------------------------------------------

	private Element buildElement(ResultSet rs, int col, String name, int type, Hashtable formats) throws SQLException
	{
		String value = null;

		switch (type)
		{
		case Types.DATE:
			Date date = rs.getDate(col +1);
			if (date == null) value = null;
			else
			{
				String format = (String)formats.get(name);
				SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_DATE_FORMAT) : new SimpleDateFormat(format);
				value = df.format(date);
			}
			break;

		case Types.TIME:
			Time time = rs.getTime(col +1);
			if (time == null) value = null;
			else
			{
				String format = (String)formats.get(name);
				SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_TIME_FORMAT) : new SimpleDateFormat(format);
				value = df.format(time);
			}
			break;

		case Types.TIMESTAMP:
			Timestamp timestamp = rs.getTimestamp(col +1);
			if (timestamp == null) value = null;
			else
			{
				String format = (String)formats.get(name);
				SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT) : new SimpleDateFormat(format);
				value = df.format(timestamp);
			}
			break;

		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
			long l = rs.getLong(col +1);
			if (rs.wasNull()) value = null;
			else
			{
				String format = (String)formats.get(name);
				if (format == null) value = l+"";
				else
				{
					DecimalFormat df = new DecimalFormat(format);
					value = df.format(l);
				}
			}
			break;

		case Types.DECIMAL:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.REAL:
		case Types.NUMERIC:
			double n = rs.getDouble(col +1);

			if (rs.wasNull())
				value = null;
			else
			{
				String format = (String)formats.get(name);

				if (format == null)
				{
					value = n+"";

					//--- this fix is mandatory for oracle
					//--- that shit returns integers like xxx.0

					if (value.endsWith(".0"))
						value = value.substring(0, value.length() -2);
				}
				else
				{
					DecimalFormat df = new DecimalFormat(format);
					value = df.format(n);
				}
			}
			break;

		default:
			value = rs.getString(col +1);
			if (value != null) { value = stripIllegalChars(value); }

			break;
		}
		return new Element(name.toString()).setText(value);
	}

    //--------------------------------------------------------------------------
	private String stripIllegalChars(String input) {
		String output = input;
		for (int i=127; i<160; i++) {
			String c = String.valueOf((char)i);
				output = output.replaceAll(c, "");
		}
		
		return output;
	}


	//--------------------------------------------------------------------------

	private void setObject(PreparedStatement stmt, int i, Object obj) throws SQLException
	{
		if (obj instanceof String)
		{
			String s = (String) obj;

			if (s.length() < 4000)	stmt.setString(i+1, s);
				else						stmt.setCharacterStream(i+1, new StringReader(s), s.length());
		}
		else
			stmt.setObject(i+1, obj);
	}

	//--------------------------------------------------------------------------

	private String getArgs(Object[] args)
	{
		StringBuffer sb = new StringBuffer();

		for (int i=0; i<args.length; i++)
		{
			sb.append(args[i]);

			if (i < args.length -1)
				sb.append(", ");
		}

		return sb.toString();
	}
}

//=============================================================================

