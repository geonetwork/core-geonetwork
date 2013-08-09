//==============================================================================
//===
//===   JODAISODate
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

//==============================================================================

public class JODAISODate
{
	private static String dt = "3000-01-01T00:00:00.000Z"; // JUNK Value

	/*
	 * Converts a given ISO date time into the form used to index in Lucene
	 * Returns null if it gets back a ridiculous value
	 */
	public static String parseISODateTime(String input1)
	{
		String newDateTime = parseISODateTimes(input1, null);
		if (newDateTime.equals(dt)) return null;
		else return newDateTime;
	}

	/* 
	 * Converts two ISO date times into standard form used to index in Lucene
	 * Always returns something because it is used during the indexing of the
	 * metadata record in Lucene - if exception during parsing then it is 
	 * something ridiculous like JUNK value above
	 */
	public static String parseISODateTimes(String input1,String input2)
	{
		DateTimeFormatter dto = ISODateTimeFormat.dateTime();
		PeriodFormatter p = ISOPeriodFormat.standard();
		DateTime odt1;
		String odt = "";

		// input1 should be some sort of ISO time 
		// eg. basic: 20080909, full: 2008-09-09T12:21:00 etc
		// convert everything to UTC so that we remove any timezone
		// problems
		try {
			DateTime idt = parseBasicOrFullDateTime(input1);
			odt1 = dto.parseDateTime(idt.toString()).withZone(DateTimeZone.forID("UTC"));
			odt = odt1.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return dt;
		}

		if (input2 == null || input2.equals("")) return odt; 


		// input2 can be an ISO time as for input1 but also an ISO time period
		// eg. -P3D or P3D - if an ISO time period then it must be added to the
		// DateTime generated for input1 (odt1)
		// convert everything to UTC so that we remove any timezone
		// problems
		try {
			boolean minus = false;
			if (input2.startsWith("-P")) {
				input2 = input2.substring(1);
				minus = true;
			}

			if (input2.startsWith("P")) {
				Period ip = p.parsePeriod(input2);
				DateTime odt2;
				if (!minus) odt2 = odt1.plus(ip.toStandardDuration().getMillis());
				else odt2 = odt1.minus(ip.toStandardDuration().getMillis());
				odt = odt+"|"+odt2.toString();
			} else {
				DateTime idt = parseBasicOrFullDateTime(input2);
				DateTime odt2 = dto.parseDateTime(idt.toString()).withZone(DateTimeZone.forID("UTC"));
				odt = odt+"|"+odt2.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return odt+"|"+dt;
		}

		return odt;
	}

	public static DateTime parseBasicOrFullDateTime(String input1) throws Exception {
		DateTimeFormatter bd = ISODateTimeFormat.basicDate();
		DateTimeFormatter bt = ISODateTimeFormat.basicTime();
		DateTimeFormatter bdt = ISODateTimeFormat.basicDateTime();
		DateTimeFormatter dtp = ISODateTimeFormat.dateTimeParser();
		DateTime idt;
		if (input1.length() == 8 && !input1.startsWith("T")) {
			idt = bd.parseDateTime(input1);
		} else if (input1.startsWith("T") && !input1.contains(":")) {
			idt = bt.parseDateTime(input1);
		} else if (input1.contains("T") && !input1.contains(":") && !input1.contains("-")) {
			idt = bdt.parseDateTime(input1);
		} else {
			idt = dtp.parseDateTime(input1);
		}
		return idt;
	}
}
//==============================================================================




