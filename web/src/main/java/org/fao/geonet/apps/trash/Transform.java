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
package org.fao.geonet.apps.trash;

import jeeves.resources.dbms.Dbms;
import jeeves.resources.dbms.DbmsPool;
import jeeves.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * TODO javadoc.
 *
 */
public class Transform {
    /**
     * TODO javadoc.
     *
     * @param args
     */
	public static void main(String args[]) {
		// check args
		if (args.length != 4) {
			System.err.println("usage: transform table field template config");
			System.exit(1);
		}
		// get args
		String table        = args[0];
		String field        = args[1];
		String templateFile = args[2];
		String configFile   = args[3];

		DbmsPool pool = null;
		Dbms     dbms = null;
		try {
			// get dbms pool and open dbms connection
			pool = Util.getDbmsPool(configFile);
			dbms = (Dbms)pool.open();

			// get id list from table
			Element result = dbms.select("SELECT id, "+ field + " FROM " + table);

			// loop on ids
			System.out.println("transforming records"); // DEBUG
			List records = result.getChildren("record");
			for (int i = 0; i < records.size(); i++) {
				Element record = (Element)records.get(i);

				// get id
				String sId = record.getChildText("id");
				int id = Integer.parseInt(sId);

				// get and transform field
				String sField = record.getChildText(field);
				String newField = applyTemplate(sField, templateFile);
				// System.out.println("- new field is " + newField); // DEBUG

				// save transformed field

				dbms.execute("UPDATE " + table + " SET " + field + " = ? WHERE id = ?", newField, new Integer(id));
				System.out.println("- record " + id + " saved"); // DEBUG
			}
			// commit changes
			System.out.println("saving changes"); // DEBUG
			pool.close(dbms);
			System.out.println("database closed, exiting"); // DEBUG
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();

			// possibly abort the transaction
			if (pool != null && dbms != null)
				try {
					System.out.println("aborting changes"); // DEBUG
					pool.abort(dbms);
					System.out.println("database closed"); // DEBUG
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			System.out.println("exiting"); // DEBUG
			System.exit(1);
		}
	}

    /**
     * TODO javadoc.
     *
     * @param source
     * @param templateFile
     * @return
     * @throws Exception
     */
	private static String applyTemplate(String source, String templateFile) throws Exception {
		Element xml = Xml.loadString(source, false);
		Element result = Xml.transform(xml, templateFile);
		return Xml.getString(result);
	}
}