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

package org.fao.geonet.services.thesaurus;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

import java.io.File;

//=============================================================================

/** Removes a thesaurus from the system.
  */

public class Delete implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		ThesaurusManager manager = gc.getThesaurusManager();
		
		// Get parameters
		String name = Util.getParam(params, Params.REF);
		

		// Load file
		Thesaurus thesaurus = manager.getThesaurusByName(name);
		File item = thesaurus.getFile();
		
		// Remove old file from thesaurus manager
		manager.remove(name);
		
		// Remove file
		if (item.exists()) {
			item.delete();

            // Delete thesaurus record in the database
            String query = "DELETE FROM Thesaurus WHERE id =?";
            Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);
            dbms.execute(query, thesaurus.getFname());
        } else {
            throw new IllegalArgumentException("Thesaurus not found --> " + name);
        }
		
		return new Element(Jeeves.Elem.RESPONSE)
							.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.REMOVED));
	}
}

//=============================================================================

