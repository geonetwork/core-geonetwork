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

package org.fao.geonet.services.region;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Params;
import org.jdom.Element;

import com.vividsolutions.jts.geom.Geometry;

//=============================================================================

/** Returns a specific region and coordinates given its id
  */

public class GetGeom implements Service
{
	private static final String SIMPLIFIED_PARAM = null;
	
	private GeomFormat format;

    public void init(String appPath, ServiceConfig params) throws Exception {
        format = GeomFormat.valueOf(params.getMandatoryValue("format"));
    }

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id = params.getChildText(Params.ID);
		boolean simplified = Util.getParam(params, SIMPLIFIED_PARAM, false);

		if (id == null)
			return new Element(Jeeves.Elem.RESPONSE);

		RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
		Geometry geom = dao.getGeom(context, id, simplified);
		if (geom == null) {
		    throw  new RegionNotFoundEx(id);
		}
		
		return format.toElement(geom);
	}
}

//=============================================================================

