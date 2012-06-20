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

import static org.fao.geonet.services.extent.ExtentHelper.ID;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;

import org.fao.geonet.constants.*;
import org.fao.geonet.services.reusable.Reject;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.kernel.reusable.ReusableTypes;

//=============================================================================

/**
 * Manager distribution formats (PUT/DELETE)
 *
 * @author fxprunayre
 * @see jeeves.interfaces.Service
 *
 */
public class Manager implements Service {
	/*
	 * (non-Javadoc)
	 *
	 * @see jeeves.interfaces.Service#init(String, ServiceConfig)
	 */
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jeeves.interfaces.Service#exec(org.jdom.Element,
	 * jeeves.server.context.ServiceContext)
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String action = params.getChildText(Geocat.Params.ACTION);
		String id = params.getChildText(Params.ID);
		String name = params.getChildText(Params.NAME);
		String version = params.getChildText(Params.VERSION);
		boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
		
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		
		Element elRes = new Element(Jeeves.Elem.RESPONSE);

		if (action.equals("DELETE")) {
	        if(!Boolean.parseBoolean(Util.getParam(params, "forceDelete", "false"))) {
	            String msg = LangUtils.loadString("reusable.rejectDefaultMsg", context.getAppPath(), context.getLanguage());
	            return new Reject().reject(context, ReusableTypes.formats, new String[]{id}, msg, null, testing);
	        } else {
    			dbms.execute("DELETE FROM Formats WHERE id=" + id);
    			elRes.addContent(new Element(Jeeves.Elem.OPERATION)
    					.setText(Jeeves.Text.REMOVED));
	        }
		} else {
			if (id == null) {
				int newId = context.getSerialFactory().getSerial(dbms,
						"Formats");
				String query = "INSERT INTO Formats(id, name, version) VALUES (?, ?, ?)";
				dbms.execute(query, newId, name, version);
				elRes.addContent(new Element(Jeeves.Elem.OPERATION)
						.setText(Jeeves.Text.ADDED));
			} else {
				String query = "UPDATE Formats SET name=?, version=? WHERE id=?";
				dbms.execute(query, name, version, new Integer(id));
				elRes.addContent(new Element(Jeeves.Elem.OPERATION)
						.setText(Jeeves.Text.UPDATED));

				Processor.uncacheXLinkUri(XLink.LOCAL_PROTOCOL+"xml.format.get?id=" + id);

			}
		}

		return elRes;
	}
}

// =============================================================================

