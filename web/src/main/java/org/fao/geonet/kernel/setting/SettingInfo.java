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

package org.fao.geonet.kernel.setting;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

import java.util.Calendar;

//=============================================================================

public class SettingInfo
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SettingInfo(ServiceContext context)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		sm = gc.getSettingManager();
	}

	//---------------------------------------------------------------------------

	public SettingInfo(SettingManager sm)
	{
		this.sm = sm;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getSiteName()
	{
		return sm.getValue("system/site/name");
	}

	//---------------------------------------------------------------------------
	/** Return a string like 'http://HOST[:PORT]' */

	public String getSiteUrl()
	{
		String host = sm.getValue("system/server/host");
		String port = sm.getValue("system/server/port");

		StringBuffer sb = new StringBuffer("http://");

		sb.append(host);

		if (port.length() != 0) {
			sb.append(":");
			sb.append(port);
		}

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public String getSelectionMaxRecords()
	{
		return sm.getValue("system/selectionmanager/maxrecords");
	}

	//---------------------------------------------------------------------------

	public boolean getLuceneIndexOptimizerSchedulerEnabled()
	{
		return sm.getValue("system/indexoptimizer/enable").equals("true");
	}

	//---------------------------------------------------------------------------

	public boolean isXLinkResolverEnabled()
	{
		return sm.getValue("system/xlinkResolver/enable").equals("true");
	}

	//---------------------------------------------------------------------------

	public Calendar getLuceneIndexOptimizerSchedulerAt() throws IllegalArgumentException {
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.set(0,0,0,
					Integer.parseInt(sm.getValue("system/indexoptimizer/at/hour")),
					Integer.parseInt(sm.getValue("system/indexoptimizer/at/min")) ,
					Integer.parseInt(sm.getValue("system/indexoptimizer/at/sec")));
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed parsing schedule at info from settings: "+e.getMessage());
		}
		return calendar;
	}

	//---------------------------------------------------------------------------

	public int getLuceneIndexOptimizerSchedulerInterval() throws IllegalArgumentException {
		int result = -1;
		try {
			int day  = Integer.parseInt(sm.getValue("system/indexoptimizer/interval/day"));
			int hour = Integer.parseInt(sm.getValue("system/indexoptimizer/interval/hour"));
			int min  = Integer.parseInt(sm.getValue("system/indexoptimizer/interval/min"));
			result = (day * 24 * 60) + (hour * 60) + min;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed parsing scheduler interval from settings: "+e.getMessage());
		}
		return result;
	}

	//---------------------------------------------------------------------------

	public String getFeedbackEmail()
	{
		return sm.getValue("system/feedback/email");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private SettingManager sm;
}

//=============================================================================

