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

package org.fao.geonet.kernel;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * This class is responsible of reading and writing xml on the database. It works on tables like (id, data,
 * lastChangeDate).
 */
public class XmlSerializer {

	private static SettingManager sm;

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' and the string read is converted into xml.
     *
     * @param dbms
     * @param table
     * @param id
     * @return
     * @throws Exception
     */
	private static Element internalSelect(Dbms dbms, String table, String id) throws Exception {
		String query = "SELECT * FROM " + table + " WHERE id = ?";
		Element rec = dbms.select(query, new Integer(id)).getChild(Jeeves.Elem.RECORD);

		if (rec == null)
			return null;

		String xmlData = rec.getChildText("data");
		rec = Xml.loadString(xmlData, false);
		return (Element) rec.detach();
	}

    /**
     *
     * @param sMan
     */
	public static void setSettingManager(SettingManager sMan) {
		sm = sMan;
	}

    /**
     *
     * @return
     */
	public static boolean resolveXLinks() {
		if (sm == null) { // no initialization, no XLinks
			Log.error(Geonet.DATA_MANAGER,"No settingManager in XmlSerializer, XLink Resolver disabled.");
			return false; 
		}

		String xlR = sm.getValue("system/xlinkResolver/enable");
		if (xlR != null) {
			boolean isEnabled = xlR.equals("true");
			if (isEnabled) Log.info(Geonet.DATA_MANAGER,"XLink Resolver enabled.");
			else Log.info(Geonet.DATA_MANAGER,"XLink Resolver disabled.");
			return isEnabled; 
		} else {
			Log.error(Geonet.DATA_MANAGER,"XLink resolver setting does not exist! XLink Resolver disabled.");
			return false;
		}
	}

    /**
     *  Retrieves the xml element which id matches the given one. The element is read from 'table' and the string read
     *  is converted into xml, XLinks are resolved when config'd on.
     *
     * @param dbms
     * @param table
     * @param id
     * @return
     * @throws Exception
     */
	public static Element select(Dbms dbms, String table, String id) throws Exception {
		Element rec = internalSelect(dbms, table, id);
		if (resolveXLinks()) Processor.detachXLink(rec);
		return rec;
	}

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' and the string read is
     * converted into xml, XLinks are NOT resolved even if they are config'd on - this is used when you want to do XLink
     * processing yourself
     * @param dbms
     * @param table
     * @param id
     * @return
     * @throws Exception
     */
	public static Element selectNoXLinkResolver(Dbms dbms, String table, String id) throws Exception {
		return internalSelect(dbms, table, id);
	}

    /**
     *
     * @param dbms
     * @param schema
     * @param xml
     * @param serial
     * @param source
     * @param uuid
     * @param owner
     * @param groupOwner
     * @return
     * @throws SQLException
     */
	public static String insert(Dbms dbms, String schema, Element xml, int serial,
										 String source, String uuid, int owner, String groupOwner) throws SQLException {
		return insert(dbms, schema, xml, serial, source, uuid, null, null, "n", null, owner, groupOwner, "");
	}

    /**
     *
     * @param dbms
     * @param schema
     * @param xml
     * @param serial
     * @param source
     * @param uuid
     * @param isTemplate
     * @param title
     * @param owner
     * @param groupOwner
     * @return
     * @throws SQLException
     */
	public static String insert(Dbms dbms, String schema, Element xml, int serial,
										 String source, String uuid, String isTemplate,
										 String title, int owner, String groupOwner) throws SQLException {
		return insert(dbms, schema, xml, serial, source, uuid, null, null, isTemplate, title, owner, groupOwner, "");
	}

    /**
     *
     * @param dbms
     * @param schema
     * @param xml
     * @param serial
     * @param source
     * @param uuid
     * @param createDate
     * @param changeDate
     * @param isTemplate
     * @param title
     * @param owner
     * @param groupOwner
     * @param docType
     * @return
     * @throws SQLException
     */
	public static String insert(Dbms dbms, String schema, Element xml, int serial,
										 String source, String uuid, String createDate,
										 String changeDate, String isTemplate, String title,
										 int owner, String groupOwner, String docType) throws SQLException {
	
		if (resolveXLinks()) Processor.removeXLink(xml);

		String date = new ISODate().toString();

		if (createDate == null)
			createDate = date;

		if (changeDate == null)
			changeDate = date;

		fixCR(xml);

		StringBuffer fields = new StringBuffer("id, schemaId, data, createDate, changeDate, source, "+
															"uuid, isTemplate, isHarvested, root, owner, doctype");
		StringBuffer values = new StringBuffer("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

		Vector<Serializable> args = new Vector<Serializable>();
		args.add(serial);
		args.add(schema);
		args.add(Xml.getString(xml));
		args.add(createDate);
		args.add(changeDate);
		args.add(source);
		args.add(uuid);
		args.add(isTemplate);
		args.add("n");
		args.add(xml.getQualifiedName());
		args.add(owner);
		args.add(docType);

		if (groupOwner != null) {
			fields.append(", groupOwner");
			values.append(", ?");
			args.add(new Integer(groupOwner));
		}

		if (title != null)
		{
			fields.append(", title");
			values.append(", ?");
			args.add(title);
		}

		String query = "INSERT INTO Metadata (" + fields + ") VALUES(" + values + ")";
		dbms.execute(query, args.toArray());

		return Integer.toString(serial);
	}

    /**
     *  Updates an xml element into the database. The new data replaces the old one.
     *
     * @param dbms
     * @param id
     * @param xml
     * @param changeDate
     * @throws SQLException
     */
	public static void update(Dbms dbms, String id, Element xml, String changeDate) throws SQLException {
		if (resolveXLinks()) Processor.removeXLink(xml);

		String query = "UPDATE Metadata SET data=?, changeDate=?, root=? WHERE id=?";

		Vector<Serializable> args = new Vector<Serializable>();

		fixCR(xml);
		args.add(Xml.getString(xml));

		if (changeDate == null)	args.add(new ISODate().toString());
			else                 args.add(changeDate);

		args.add(xml.getQualifiedName());
		args.add(new Integer(id));

		dbms.execute(query, args.toArray());
	}

    /**
     * Deletes an xml element given its id.
     *
     * @param dbms
     * @param table
     * @param id
     * @throws SQLException
     */
	public static void delete(Dbms dbms, String table, String id) throws SQLException {
		// TODO: Ultimately we want to remove any xlinks in this document
		// that aren't already in use from the xlink cache. For now we
		// rely on the admin clearing cache and reindexing regularly
		String query = "DELETE FROM " + table + " WHERE id="+id;
		dbms.execute(query);
	}

    /**
     *
     * @param xml
     */
	private static void fixCR(Element xml) {
		List list = xml.getChildren();
		if (list.size() == 0) {
			String text = xml.getText();
			xml.setText(Util.replaceString(text, "\r\n", "\n"));
		}
		else {
            for (Object o : list) {
                fixCR((Element) o);
            }
        }
	}
}