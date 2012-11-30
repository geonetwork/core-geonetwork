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
	public String getSiteUrl() {
        String protocol = sm.getValue(Geonet.Settings.SERVER_PROTOCOL);
        return getSiteUrl(protocol.equalsIgnoreCase("https"));
	}
	/** Return a string like 'http://HOST[:PORT]' */
	public String getSiteUrl(boolean secureUrl) {
		String protocol;
		Integer port;
        String host = sm.getValue(Geonet.Settings.SERVER_HOST);
		Integer secureport = toIntOrNull(Geonet.Settings.SERVER_SECURE_PORT);
		Integer insecureport = toIntOrNull(Geonet.Settings.SERVER_PORT);
		if (secureUrl) {
            protocol = "https";
		    if (secureport == null && insecureport == null) {
		        port = 443;
		    } else if (secureport != null) {
		        port = secureport;
		    } else {
	            protocol = "http";
		        port = insecureport;
		    }
		} else {
            protocol = "http";
            if (secureport == null && insecureport == null) {
                port = 80;
            } else if (insecureport != null) {
                port = insecureport;
            } else {
                protocol = "https";
                port = secureport;
            }		    
		}

		StringBuffer sb = new StringBuffer(protocol + "://");

		sb.append(host);

		if (port != null) {
			sb.append(":");
			sb.append(port);
		}

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	private Integer toIntOrNull(String key) {
        try {
             return Integer.parseInt(sm.getValue(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSelectionMaxRecords()
	{
		String value = sm.getValue("system/selectionmanager/maxrecords");
		if (value == null) value = "10000";
		return value;
	}

    /**
     * Whether to use auto detection of the language used in search terms.
     * @return
     */
    public boolean getAutoDetect() {
        String value = sm.getValue("system/autodetect/enable");
        if(value == null) {
            return false;
        }
        else {
            return value.equals("true");
        }
    }

    /**
     * Whether search results should be only in the requested language.
     * @return
     */
    public boolean getRequestedLanguageOnly() {
        String value = sm.getValue("system/requestedLanguage/only");
        if(value == null) {
            return false;
        }
        else {
            return value.equals("true");
        }
    }

    /**
     * Whether search results should be sorted with the requested language on top.
     * @return
     */
    public boolean getRequestedLanguageOnTop() {
        String value = sm.getValue("system/requestedLanguage/sorted");
        if(value == null) {
            return false;
        }
        else {
            return value.equals("true");
        }
    }

    /**
     * Whether search should ignore the requested language.
     * @return
     */
    public boolean getIgnoreRequestedLanguage() {
        String value = sm.getValue("system/requestedLanguage/ignored");
        if(value == null) {
            return false;
        }
        else {
            return value.equals("true");
        }
    }

    public boolean getLuceneIndexOptimizerSchedulerEnabled()
	{
		String value = sm.getValue("system/indexoptimizer/enable");
		if (value == null) return false;
		else return value.equals("true");
	}

	//---------------------------------------------------------------------------

	public boolean isXLinkResolverEnabled()
	{
		String value = sm.getValue("system/xlinkResolver/enable");
		if (value == null) return false;
		else return value.equals("true");
	}

	//---------------------------------------------------------------------------

	public boolean isSearchStatsEnabled()
	{
		String value = sm.getValue("system/searchStats/enable");
		if (value == null) return false;
		else return value.equals("true");
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

    public boolean getInspireEnabled()
    {
        return sm.getValueAsBool("system/inspire/enable");
    }

	//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private SettingManager sm;

}

//=============================================================================

