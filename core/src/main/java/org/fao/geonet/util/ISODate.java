//==============================================================================
//===
//===   ISODate
//===
//==============================================================================
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

package org.fao.geonet.util;

import java.util.Calendar;

//==============================================================================

public class ISODate implements Cloneable
{
	public int year;	//--- 4 digits
	public int month;	//--- 1..12
	public int day;	//--- 1..31
	public int hour;	//--- 0..23
	public int min;	//--- 0..59
	public int sec;	//--- 0..59
	public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public boolean isShort; //--- 'true' if the format is yyyy-mm-dd

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

	private ISODate(int y, int m, int d, int h, int mi, int s)
	{
		year  = y;
		month = m;
		day   = d;
		hour  = h;
		min   = mi;
		sec   = s;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public ISODate clone()
	{
	    ISODate clone;
        try {
            clone = (ISODate) super.clone();
            clone.year = year;
            clone.month = month;
            clone.day = day;
            clone.hour = hour;
            clone.min = min;
            clone.sec = sec;
            return clone;
        } catch (CloneNotSupportedException e) {
            return new ISODate(year, month, day, hour, min, sec);
        }
	}

	//---------------------------------------------------------------------------

	public void setDate(String isoDate)
	{
		if (isoDate == null)
			throw new IllegalArgumentException("ISO date is null");

		if (isoDate.length() < 10)
			throw new IllegalArgumentException("Invalid ISO date : "+ isoDate);

		try
		{
            if (isoDate.contains("T")) {
                // Check if iso date contains time info and if using non UTC time zone to parse the date with
                // JODAISODate. This class converts to UTC format to avoid timezones issues.
                boolean timeZoneInfo = ((isoDate.split("T")[1].contains("+")) || (isoDate.split("T")[1].contains("-")));

                if (timeZoneInfo) {
                    isoDate = JODAISODate.parseISODateTime(isoDate);
                }
            }

            year  = Integer.parseInt(isoDate.substring(0,  4));
            month = Integer.parseInt(isoDate.substring(5,  7));
            day   = Integer.parseInt(isoDate.substring(8, 10));

            isShort = true;

            hour = 0;
            min  = 0;
            sec  = 0;

            //--- is the date in 'yyyy-mm-dd' or 'yyyy-mm-ddZ' format?

            if (isoDate.length() < 12)
                return;

            isoDate = isoDate.substring(11);

            hour  = Integer.parseInt(isoDate.substring(0,2));
            min   = Integer.parseInt(isoDate.substring(3,5));
            sec   = Integer.parseInt(isoDate.substring(6,8));



            isShort = false;

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

	public long getSeconds()
	{
		synchronized(calendar)
		{
			calendar.clear();
   	   calendar.set(year, month -1, day, hour, min, sec);

			return calendar.getTimeInMillis() / 1000;
		}
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + day;
        result = prime * result + hour;
        result = prime * result + (isShort ? 1231 : 1237);
        result = prime * result + min;
        result = prime * result + month;
        result = prime * result + sec;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ISODate other = (ISODate) obj;
        if (day != other.day)
            return false;
        if (hour != other.hour)
            return false;
        if (isShort != other.isShort)
            return false;
        if (min != other.min)
            return false;
        if (month != other.month)
            return false;
        if (sec != other.sec)
            return false;
        if (year != other.year)
            return false;
        return true;
    }
}

//==============================================================================




