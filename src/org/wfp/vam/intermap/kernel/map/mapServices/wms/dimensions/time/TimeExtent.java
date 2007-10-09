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

import java.util.*;

import org.jdom.*;

import org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.time.WmsTime;

public class TimeExtent
{
	private Vector dates = new Vector();

	public static void main(String args[]) {
		TimeExtent te = new TimeExtent(new Element("asdfasdf"));

		te.getIntervalDates("2003-01-01/2003-12-01/P1M"); // DEBUG
		System.out.println("\n\n\n" + jeeves.utils.Xml.getString(te.getJdom())); // DEBUG
	}

	public TimeExtent(Element extent)
	{
		this(extent.getText());
	}

	public TimeExtent(String value)
	{
//		Element values = new Element("values");

		StringTokenizer s = new StringTokenizer(value, ",");

//		Time t = new Time("2003-12-23"); // DEBUG
//		t.setPeriod("P1D"); // DEBUG
//		t.increment();

//		getIntervalDates("2003-12-23T11:12:13/2003-12-23T11:12:20/PT1S"); // DEBUG
//		getIntervalDates("2003-07/2003-12/P1M"); // DEBUG
//		System.out.println("\n\n\n" + jeeves.utils.Xml.getString(getJdom())); // DEBUG

		while (s.hasMoreTokens()) {
			String token = s.nextToken();

			if (token.indexOf("/") == -1) { // doesn't contain "/" so it is a date and not an interval
//				values.addContent(new Element(token));
				dates.add(token);
			}
			else
				getIntervalDates(token); // contains "/" so it is an interval (ex. 1995-01-01/2004-01-06/P1D)
		}

	}

	/**
	 * Returns a Jdom Element containig all the dates
	 *
	 * @return   an Element
	 *
	 */
	public Element getJdom() {
		Element elDates = new Element("extent").setAttribute("name", "time");
		for (Iterator i = dates.iterator(); i.hasNext(); ) {
			String date = (String)i.next();
			elDates.addContent(new Element("value").setText(date));
		}

		return elDates;
	}

	/**
	 * Returns all the dates contained in the given interval
	 *
	 * @param    interval            a  String
	 *
	 */
	private void getIntervalDates(String interval) {
		System.out.println("interval: " + interval);

		StringTokenizer st = new StringTokenizer(interval, "/");
		WmsTime start = new WmsTime(st.nextToken());
		WmsTime stop = new WmsTime(st.nextToken());
		start.setPeriod(st.nextToken());

		getDates(start, stop);
	}

	private void getDates(WmsTime beginDate, WmsTime endDate) {
		for ( ; !beginDate.after(endDate); beginDate.increment()) {
			dates.add(beginDate.toString());
		}
	}

}

