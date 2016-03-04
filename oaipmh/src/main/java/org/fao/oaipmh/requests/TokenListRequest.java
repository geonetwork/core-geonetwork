/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.oaipmh.requests;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.GeonetHttpRequestFactory;


public abstract class TokenListRequest extends ListRequest {
	

	protected ISODate from;
	protected ISODate until;
	protected String  mdPrefix;
	protected String  set;

    public TokenListRequest(GeonetHttpRequestFactory transport) {
        super(transport);
    }

    //---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
	
	public ISODate getFrom()  { return from;  }
	public ISODate getUntil() { return until; }

	public String getMetadataPrefix() { return mdPrefix; }
	public String getSet()            { return set;      }
	
	//---------------------------------------------------------------------------

	public void setFrom(ISODate date)
	{
		from = date;
	}

	//---------------------------------------------------------------------------

	public void setUntil(ISODate date)
	{
		until = date;
	}

	//---------------------------------------------------------------------------

	public void setMetadataPrefix(String mdPrefix)
	{
		this.mdPrefix = mdPrefix;
	}

	//---------------------------------------------------------------------------

	public void setSet(String set)
	{
		this.set = set;
	}
	
}
