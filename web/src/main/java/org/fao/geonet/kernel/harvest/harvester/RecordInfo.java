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

package org.fao.geonet.kernel.harvest.harvester;

import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.JODAISODate;
import org.jdom.Element;

//=============================================================================

public class RecordInfo
{
    private final static long SECONDS_PER_DAY = 60* 60 * 24;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public RecordInfo(String uuid, String changeDate)
	{
		this(uuid, changeDate, null, null);
	}

	//---------------------------------------------------------------------------

	public RecordInfo(String uuid, String changeDate, String schema, String source)
	{
		if (changeDate == null)
		{
			dateWasNull = true;
			changeDate  = new ISODate().toString();
		}

		this.uuid       = uuid;
		this.changeDate = changeDate;
		this.schema     = schema;
		this.source     = source;
	}

	//---------------------------------------------------------------------------

	public RecordInfo(Element record)
	{
		id         = record.getChildText("id");
		uuid       = record.getChildText("uuid");
		isTemplate = record.getChildText("istemplate");
		changeDate = record.getChildText("changedate");
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public int hashCode() { return uuid.hashCode(); }

	//---------------------------------------------------------------------------

	public boolean isMoreRecentThan(String localChangeDate)
	{
		if (dateWasNull)
			return true;

        try {
            // JODAISODate manages better gYear and gYearMonth, allowed in xs:date.
            // Dates a parsed with JODAISODate first to get an ISODate and used in ISODate
            String remoteDateParsed = JODAISODate.parseISODateTime(changeDate);
            String localDateParsed = JODAISODate.parseISODateTime(localChangeDate);

            ISODate remoteDate = new ISODate(remoteDateParsed);
            ISODate localDate  = new ISODate(localDateParsed);

            //--- modified:  we accept metadata modified from 24 hours before
            //--- to harvest several changes during a day (if short date format used) or date local differences
            return (remoteDate.sub(localDate) > (-1) * SECONDS_PER_DAY);

        } catch (Exception ex) {
            // If can't parse any of the dates then assume that is more recent the remote copy, to update it locally
            // and avoid failing the harvester due to a parse exception.
            return true;
        }
	}

	//-----------------------------------------------------------------------------
	
	public boolean isOlderThan(String remoteChangeDate) {
		if (dateWasNull)
			return true;

        try {
            // JODAISODate manages gYear and gYearMonth, allowed in xs:date
            // Dates a parsed with JODAISODate first to get an ISODate and used in ISODate
            String remoteDateParsed = JODAISODate.parseISODateTime(remoteChangeDate);
            String localDateParsed = JODAISODate.parseISODateTime(changeDate);

            ISODate remoteDate = new ISODate(remoteDateParsed);
            ISODate localDate  = new ISODate(localDateParsed);

            //--- modified:  we accept metadata modified from 24 hours before
            //--- to harvest several changes during a day (if short date format used) or date local differences
            return (remoteDate.sub(localDate) > (-1) * SECONDS_PER_DAY);
        } catch (Exception ex) {
            // If can't parse any of the dates then assume that is older the remote copy, to update it locally
            // and avoid failing the harvester due to a parse exception.
            return true;
        }
    }
	//---------------------------------------------------------------------------

	public boolean equals(Object o)
	{
		if (o instanceof RecordInfo)
		{
			RecordInfo ri = (RecordInfo) o;

			return uuid.equals(ri.uuid);
		}

		return false;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String id;
	public String uuid;
	public String changeDate;
	public String schema;
	public String source;
	public String isTemplate;

	private boolean dateWasNull;

}

//=============================================================================

