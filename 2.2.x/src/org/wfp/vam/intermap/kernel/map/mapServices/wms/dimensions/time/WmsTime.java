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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.time;

import java.text.*;
import java.util.*;

public class WmsTime
{
	//private String date;

	private Calendar c;

	private Integer century;
	private Integer year;
	private String weekDay; // TODO: to be implemented
	private String zone; // TODO: to be implemented

	private Hashtable htTime = new Hashtable(); // Hashtable containing the time fields
	private Hashtable htPeriod = new Hashtable(); // Hashtable containing the time period fields

	/**
	 * Constructor
	 *
	 * @param    t                   a  String
	 *
	 */
	public WmsTime(String t) {
		// separate the date from the time
		String date = getDate(t);
		String time = getTime(t);

		parseDate(date);
		parseTime(time);

		// set the internal Calendar variable
		setCalendar();
	}

	/**
	 * Sets the period for incrementing the time
	 *
	 * @param p  the WMS representation of the interval (P1D, PT1H...)
	 *
	 */
	public void setPeriod(String p) {
		// remove the "P" from the string
		p = p.substring(1);

		// separate the date from the time
		String datePeriod = getDate(p);
		String timePeriod = getTime(p);

		// set the period
		setDatePeriod(datePeriod);
		setTimePeriod(timePeriod);
	}

	/**
	 * Sets the date part of the period
	 *
	 * @param    period              a  String
	 *
	 */
	private void setDatePeriod(String period) {
		StringTokenizer st = new StringTokenizer(period, "YMD", true);
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			String unit = st.nextToken();
			Integer intValue = new Integer(Integer.parseInt(value));
			Integer calUnit = null;

			if (unit.equals("D")) calUnit = new Integer(Calendar.DAY_OF_YEAR);
			if (unit.equals("M")) calUnit = new Integer(Calendar.MONTH);
			if (unit.equals("Y")) calUnit = new Integer(Calendar.YEAR);

			if (calUnit != null) htPeriod.put(calUnit, intValue);
		}
	}

	/**
	 * Sets the time part of the period
	 *
	 * @param    period              a  String
	 *
	 */
	private void setTimePeriod(String period) {
		StringTokenizer st = new StringTokenizer(period, "HMS", true);
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			String unit = st.nextToken();

			if (unit.equals("S")) { setSecondsPeriod(value); }

			else {
				Integer intValue = new Integer(Integer.parseInt(value));
				Integer calUnit = null;

				if (unit.equals("H")) calUnit = new Integer(Calendar.HOUR_OF_DAY);
				if (unit.equals("M")) calUnit = new Integer(Calendar.MINUTE);

				if (calUnit != null) htPeriod.put(calUnit, intValue);
			}
		}
	}

	/**
	 * Set the seconds part of the period
	 *
	 * @param    value               a  String
	 *
	 * @exception   NumberFormatException
	 *
	 */
	private void setSecondsPeriod(String value) throws NumberFormatException
	{
		int p = value.indexOf(".");
		if (p == -1) p = value.length();

		String stSec = value.substring(0, p);
		String stMsec = value.substring(p + 1, value.length());

		if (!stSec.equals(""))
		{
			Integer sec = new Integer(Integer.parseInt(stSec));
			htPeriod.put(new Integer(Calendar.SECOND), sec);
		}
		if (!stMsec.equals(""))
		{
			float fMsec = Float.parseFloat("." + stMsec) * 1000;
			Integer msec = new Integer((int)Math.abs(fMsec));
			htPeriod.put(new Integer(Calendar.MILLISECOND), msec);
		}
	}


	/**
	 * Increments the time of a period
	 *
	 */
	public void increment() {
		for (Enumeration e = htPeriod.keys(); e.hasMoreElements(); ) {
			Integer key = (Integer)e.nextElement();
			Integer period = (Integer)htPeriod.get(key);

			c.add(key.intValue(), period.intValue());
		}
	}

	public boolean before(WmsTime t) { return c.before(t.c); }

	public boolean after(WmsTime t) {return c.after(t.c); }

	/**
	 * Returns the wms representation of the time
	 *
	 * @return   a String
	 *
	 */
	public String toString() {
		String date = "";

		String stYear = c.get(Calendar.YEAR) + "";

		// century
		if (century == null) date += "-";
		else date += stYear.substring(0, 2);

		// year
		if (year == null) date += "-";
		else date += stYear.substring(2, 4);

		// month
		if (!htTime.containsKey(new Integer(Calendar.MONTH))) return date;
		DecimalFormat f = new DecimalFormat("00");
		date += "-";
		String month = (String)htTime.get(new Integer(Calendar.MONTH));
		if (month.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.MONTH) + 1);

		// day
		if (!htTime.containsKey(new Integer(Calendar.DAY_OF_YEAR))) return date;
		date += "-";
		String day = (String)htTime.get(new Integer(Calendar.DAY_OF_YEAR));
		if (day.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.DAY_OF_MONTH));

		// hour
		if (!htTime.containsKey(new Integer(Calendar.HOUR))) return date;
		date += "T";
		String hour = (String)htTime.get(new Integer(Calendar.HOUR));
		if (hour.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.HOUR));

		// minute
		if (!htTime.containsKey(new Integer(Calendar.MINUTE))) return date;
		date += ":";
		String minute = (String)htTime.get(new Integer(Calendar.MINUTE));
		if (minute.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.MINUTE));

		if (!htTime.containsKey(new Integer(Calendar.SECOND))) return date;
		date += ":";
		String second = (String)htTime.get(new Integer(Calendar.SECOND));
		if (second.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.SECOND));

		if (!htTime.containsKey(new Integer(Calendar.MILLISECOND))) return date;
		f = new DecimalFormat("000");
		date += ".";
		String millisecond = (String)htTime.get(new Integer(Calendar.MILLISECOND));
		if (millisecond.equals("-")) date += "-";
		else date += f.format(c.get(Calendar.MILLISECOND));

		return date;
	}

	/**
	 * Method getDate
	 *
	 * @param    s                   a  String
	 *
	 * @return   the date portion of the given string
	 *
	 */
	private String getDate(String s) {
		int t = s.indexOf("T");
		if (t == -1) return s;

		return s.substring(0, t);
	}

	/**
	 * Method getTime
	 *
	 * @param    s                   a  String
	 *
	 * @return   the time portion of the given string
	 *
	 */
	private String getTime(String s) {
		int t = s.indexOf("T");
		if (t == -1) return "";

		return s.substring(t + 1);
	}

	/**
	 * Parses the date portion
	 *
	 * @param    date                a  String
	 *
	 */
	private void parseDate(String date) {
//		String[] weekDays = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };

		if (date.equals("")) return;

		// century
		int t = date.indexOf("-");
		if (t == 0) {
			century = null;
			date = date.substring(1);
		} else {
			String stCentury = date.substring(0, 2);
			century = new Integer(Integer.parseInt(stCentury));
			date = date.substring(2);
		}

		if (date.equals("")) return;

		// year
		t = date.indexOf("-");
		if (t == 0) {
			year = null;
			date = date.substring(1);
		} else {
			String stYear = date.substring(0, 2);
			year = new Integer(Integer.parseInt(stYear));
			date = date.substring(2);
		}

		// month
		if (date.equals("")) return;
		t = date.indexOf("-", 1);
		if (t == -1) t = date.length();
		String value;
		if (t == 1) htTime.put(new Integer(Calendar.MONTH), "-");
		else {
			value = date.substring(1, t);
			htTime.put(new Integer(Calendar.MONTH), value);
		}
		date = date.substring(t);

		// day
		if (date.equals("")) return;
		t = date.indexOf("-", 1);
		if (t == -1) t = date.length();
		if (t == 1) htTime.put(new Integer(Calendar.DAY_OF_YEAR), "-");
		else {
			value = date.substring(1, t);
			htTime.put(new Integer(Calendar.DAY_OF_YEAR), value);
		}

		date = date.substring(t);
	}

	private void setCalendar() {
		c = Calendar.getInstance();

		// set the year
		int iYear;
		if (century != null) iYear = century.intValue() * 100;
		else iYear = 1900; // set an arbitrary century
		if (year != null) iYear += year.intValue();

		c.set(Calendar.YEAR, iYear);

		// set the month
		String month = (String)htTime.get(new Integer(Calendar.MONTH));
		if (month != null && !month.equals("-")) c.set(Calendar.MONTH, Integer.parseInt(month) - 1);

		// set the day
		String day = (String)htTime.get(new Integer(Calendar.DAY_OF_YEAR));
		if (day != null && !day.equals("-")) c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));

		// parsing of time is not yet implemented
		// set the hour
		String hour = (String)htTime.get(new Integer(Calendar.HOUR));
		if (hour != null && !hour.equals("-")) c.set(Calendar.HOUR, Integer.parseInt(hour));

		// set the minute
		String minute = (String)htTime.get(new Integer(Calendar.MINUTE));
		if (minute != null && !minute.equals("-")) c.set(Calendar.MINUTE, Integer.parseInt(minute));

		// set the second
		String second = (String)htTime.get(new Integer(Calendar.SECOND));
		if (second != null && !second.equals("-")) c.set(Calendar.SECOND, Integer.parseInt(second));

		// set the millisecond
		String millisecond = (String)htTime.get(new Integer(Calendar.MILLISECOND));
		if (millisecond != null && !millisecond.equals("") &&  !millisecond.equals("-")) {


			float fMsec = Float.parseFloat("." + millisecond) * 1000;
			int msec = (int)Math.abs(fMsec);
			c.set(Calendar.MILLISECOND,  msec);


		}
	}

	/**
	 * Parses the time portion
	 *
	 * @param    time                a  String
	 *
	 */
	private void parseTime(String time) {
		StringTokenizer tok = new StringTokenizer(time, ":");
		if (!tok.hasMoreTokens()) return;

		// hour
		String t = tok.nextToken();
		addTime(t, Calendar.HOUR);
		if (!tok.hasMoreTokens()) return;

		// minute
		t = tok.nextToken();
		addTime(t, Calendar.MINUTE);
		if (!tok.hasMoreTokens()) return;

		// second
		t = tok.nextToken();
		int p = t.indexOf(".");
		if (p == -1) p = t.length();

		String stSec = t.substring(0, p);
		String stMsec = t.substring(p + 1, t.length());

		if (stSec != null) addTime(stSec, Calendar.SECOND);
		if (stMsec != null && !stMsec.equals("")) addTime(stMsec, Calendar.MILLISECOND);
	}

	private void addTime(String s, int i) throws NumberFormatException
	{
		htTime.put(new Integer(i), s);
	}

}

