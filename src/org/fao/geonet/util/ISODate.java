//==============================================================================
//===
//===   ISODate
//===
//==============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

//==============================================================================

public class ISODate
{
	public int year;	//--- 4 digits
	public int month;	//--- 1..12
	public int day;	//--- 1..31
	public int hour;	//--- 0..23
	public int min;	//--- 0..59
	public int sec;	//--- 0..59

	//---------------------------------------------------------------------------

	private static Calendar calendar = Calendar.getInstance();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ISODate()
	{
		this(System.currentTimeMillis());
	}

	//---------------------------------------------------------------------------

	public ISODate(long time)
	{
		synchronized(calendar)
		{
			calendar.setTimeInMillis(time);

			year  = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) +1;
			day   = calendar.get(Calendar.DAY_OF_MONTH);

			hour  = calendar.get(Calendar.HOUR_OF_DAY);
			min   = calendar.get(Calendar.MINUTE);
			sec   = calendar.get(Calendar.SECOND);
		}
	}

	//---------------------------------------------------------------------------

	public ISODate(String isoDate)
	{
		setDate(isoDate);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setDate(String isoDate)
	{
		try
		{
			String sep = (isoDate.indexOf(" ") != -1) ? " " : "T";

			StringTokenizer st  = new StringTokenizer(isoDate, sep);
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), "-");
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ":");

			year  = Integer.parseInt(st1.nextToken());
			month = Integer.parseInt(st1.nextToken());
			day   = Integer.parseInt(st1.nextToken());

			hour  = Integer.parseInt(st2.nextToken());
			min   = Integer.parseInt(st2.nextToken());
			sec   = Integer.parseInt(st2.nextToken());
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Invalid ISO date : "+ isoDate);
		}
	}

	//---------------------------------------------------------------------------
	/** Subtract a date from this date and return the seconds between them */

	public long sub(ISODate date)
	{
		return getSeconds() - date.getSeconds();
	}

	//--------------------------------------------------------------------------

	public String getDate()
	{
		return year +"-"+ pad(month) +"-"+ pad(day);
	}

	//--------------------------------------------------------------------------

	public String getTime()
	{
		return pad(hour) +":"+ pad(min) +":"+ pad(sec);
	}

	//--------------------------------------------------------------------------

	public String toString()
	{
		return getDate() +"T"+ getTime();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private String pad(int value)
	{
		if (value > 9)
			return Integer.toString(value);

		return "0"+ value;
	}

	//---------------------------------------------------------------------------

	private long getSeconds()
	{
		synchronized(calendar)
		{
			calendar.clear();
   	   calendar.set(year, month -1, day, hour, min, sec);

			return calendar.getTimeInMillis() / 1000;
		}
	}
}

//==============================================================================




