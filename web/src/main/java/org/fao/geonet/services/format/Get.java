//=============================================================================
//===	Copyright (C) 2008 Swisstopo
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

package org.fao.geonet.services.format;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.fao.geonet.constants.*;

//=============================================================================

/** 
 * Retrieves a particular format by identifier.
 * 
 * @author fxprunayre
 * @see jeeves.interfaces.Service
 * 
 */
public class Get implements Service
{
	/* (non-Javadoc)
	 * @see jeeves.interfaces.Service#init(String, ServiceConfig)
	 */
	public void init(String appPath, ServiceConfig params) throws Exception {}

	/* (non-Javadoc)
	 * @see jeeves.interfaces.Service#exec(org.jdom.Element, jeeves.server.context.ServiceContext)
	 */
	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id = params.getChildText(Params.ID);

		if (id == null)
			return new Element(Jeeves.Elem.RESPONSE);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		Element el = dbms.select ("SELECT * FROM Formats WHERE id=" + id);

		return el;
	}
}

//=============================================================================

