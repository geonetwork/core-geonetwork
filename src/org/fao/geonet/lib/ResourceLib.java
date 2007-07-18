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

package org.fao.geonet.lib;

import java.io.File;
import java.util.HashSet;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;

//=============================================================================

public class ResourceLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public String getDataDir(ServiceContext context)
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		String dataDir = gc.getHandlerConfig().getMandatoryValue(Geonet.Config.DATA_DIR);

		if (!new File(dataDir).isAbsolute())
			dataDir = context.getAppPath() + dataDir;

		return dataDir;
	}

	//-----------------------------------------------------------------------------

	public String getDir(ServiceContext context, String access, String id)
	{
		return getDir(getDataDir(context), access, id);
	}

	//-----------------------------------------------------------------------------

	public String getDir(String dataDir, String access, String id)
	{
		String group    = pad(Integer.parseInt(id) / 100, 3);
		String groupDir = group +"00-"+ group +"99";
		String subDir   = (access != null && access.equals(Params.Access.PUBLIC))
									? Params.Access.PUBLIC
									: Params.Access.PRIVATE;

		return dataDir +"/"+ groupDir +"/"+ id +"/"+ subDir +"/";
	}

	//-----------------------------------------------------------------------------

	public void checkPrivilege(ServiceContext context, String id, String operation) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		AccessManager accessMan = gc.getAccessManager();

		HashSet hsOper = accessMan.getOperations(context, id, context.getIpAddress());

		if (!hsOper.contains(operation))
			throw new OperationNotAllowedEx();
	}

	//-----------------------------------------------------------------------------

	public void checkEditPrivilege(ServiceContext context, String id) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();

		if (!am.canEdit(context, id))
			throw new OperationNotAllowedEx();
	}

	//-----------------------------------------------------------------------------

	public String getRemovedDir(ServiceContext context, String id)
	{
		return getRemovedDir(getRemovedDir(context), id);
	}

	//-----------------------------------------------------------------------------
	/** @return the absolute path of the folder choosen to store all deleted metadata */

	public String getRemovedDir(ServiceContext context)
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		String remDir = gc.getSettingManager().getValue("system/removedMetadata/dir");

		if (!new File(remDir).isAbsolute())
			remDir = context.getAppPath() + remDir;

		return remDir;
	}

	//-----------------------------------------------------------------------------
	/** @return the absolute path of the folder where the given metadata should be
	  * stored when it is removed */

	public String getRemovedDir(String removedDir, String id)
	{
		String group    = pad(Integer.parseInt(id) / 100, 3);
		String groupDir = group +"00-"+ group +"99";

		return removedDir +"/"+ groupDir +"/";
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//-----------------------------------------------------------------------------

	private String pad(int group, int lenght)
	{
		String text = Integer.toString(group);

		while(text.length() < lenght)
			text = "0" + text;

		return text;
	}
}

//=============================================================================

