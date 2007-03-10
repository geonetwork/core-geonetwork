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

package org.fao.geonet.services.util.z3950;

import jeeves.server.context.ServiceContext;
import jeeves.server.UserSession;
import jeeves.interfaces.Logger;

import java.util.*;
import javax.naming.*;

import com.k_int.IR.*;
import com.k_int.util.LoggingFacade.*;

//=============================================================================

public class GNSearchable implements Searchable, Scanable // RGFIX: should I implement Scanable?
{
	private LoggingContext cat = LogContextFactory.getContext("GeoNetwork"); // RGFIX: maybe should use servlet name

	private Properties properties     = null;
	private Context    naming_context = null;

	ServiceContext _srvContext;

	//--------------------------------------------------------------------------

	public GNSearchable()
	{
		cat.debug("New GNSearchable");
	}

	//--------------------------------------------------------------------------

	public void init(Properties p)
	{
		this.properties = p;

		_srvContext = (ServiceContext)p.get("srvContext");
	}

	public void init(Properties p, Context naming_context)
	{
		init(p);
		this.naming_context = naming_context;
	}

	//--------------------------------------------------------------------------

	public void destroy()
	{
		// TODO: check if empty method is correct
	}

	//--------------------------------------------------------------------------

	public int getManagerType()
	{
		return com.k_int.IR.Searchable.SPECIFIC_SOURCE;
	}

	//--------------------------------------------------------------------------
	// Evaluate the enquiry, waiting at most will_wait_for seconds for a response
	public SearchTask createTask(IRQuery q, Object user_data)
	{
		return this.createTask(q, user_data, null);
	}

	//--------------------------------------------------------------------------

	public SearchTask createTask(IRQuery q,
										  Object user_data,
										  Observer[] observers)
	{
		GNSearchTask retval = new GNSearchTask(q, this, observers, properties, _srvContext);
		return retval;
	}

	//--------------------------------------------------------------------------

	public boolean isScanSupported()
	{
		return false;
	}

	//--------------------------------------------------------------------------

	public ScanInformation doScan(ScanRequestInfo req)
	{
		return null;
	}
}
//=============================================================================

