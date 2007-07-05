/**
 * TimeExtent.java
 *
 * @author Stefano Giaccio
 */

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

	public TimeExtent(Element extent) {
		String value = extent.getText();

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

