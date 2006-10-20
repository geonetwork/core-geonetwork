//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.user;

import java.util.*;
import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.*;

import org.fao.geonet.constants.*;
import org.fao.geonet.exceptions.GeoNetException;

//=============================================================================

/** Update the password of logged user
  */

public class PwUpdate implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String password    = Util.getParam(params, Params.PASSWORD);
		String newPassword = Util.getParam(params, Params.NEW_PASSWORD);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		// check old password
		UserSession session = context.getUserSession();
		String userId = session.getUserId();
		if (userId == null)
			throw new GeoNetException("unknown user", GeoNetException.ERROR);
		
		Element elUser = dbms.select(	"SELECT * FROM Users " +
												"WHERE id=" + userId + " AND password='" + password + "'");
		if (elUser.getChildren().size() == 0)
			throw new GeoNetException("unknown user", GeoNetException.ERROR);
		
		// change password
		Vector vArgs = new Vector ();
		vArgs.add(newPassword);
		vArgs.add(new Integer(userId));

		dbms.execute ( "UPDATE Users SET password=? WHERE id=?", vArgs);

		return new Element(Jeeves.Elem.RESPONSE);
	}
}

//=============================================================================

