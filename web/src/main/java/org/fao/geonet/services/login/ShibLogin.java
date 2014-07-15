//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

package org.fao.geonet.services.login;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

//=============================================================================

/**
 * This service does nothing.
 *
 * This service used to handle the logic for performing shibboleth login.
 * Since now the authentication procedures are driven by Spring, this logic has been moved to
 * a class that conforms to the Spring specifications.
 *
 * @see org.fao.geonet.kernel.security.shibboleth.ShibbolethPreAuthFilter
 *
 * <b>Previous doc follows</b>
 * <hr/>
 *
 * <code>ShibLogin</code> processes the result of a Shibboleth (or other external 
 * authentication system) login. The user will have already been challenged for 
 * userid and password and will have had their credentials placed in the HTTP 
 * headers. These are then used to find or create the user's account.
 * 
 * @author James Dempsey <James.Dempsey@csiro.au>
 * @version $Revision: 1629 $
 */
@Deprecated
public class ShibLogin extends NotInReadOnlyModeService
{

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		context.info("Shibboleth login service does nothing.");
		return new Element("error");
    }

}
