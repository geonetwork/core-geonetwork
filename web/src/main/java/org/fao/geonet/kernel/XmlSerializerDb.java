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

import java.sql.SQLException;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

/**
 * This class is responsible of reading and writing xml on the database. It works on tables like (id, data,
 * lastChangeDate).
 */
public class XmlSerializerDb extends XmlSerializer {

		public XmlSerializerDb(SettingManager sMan) {
			super(sMan);
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
	public Element select(Dbms dbms, String table, String id, ServiceContext context) throws Exception {
		Element rec = internalSelect(dbms, table, id, false);
		if (resolveXLinks()) Processor.detachXLink(rec, context);
		return rec;
	}

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' and the string read is
     * converted into xml, XLinks are NOT resolved even if they are config'd on - this is used when you want to do XLink
     * processing yourself.
     *
     * @param dbms
     * @param table
     * @param id
     * @return
     * @throws Exception
     */
	public Element selectNoXLinkResolver(Dbms dbms, String table, String id, boolean isIndexingTask) throws Exception {
		return internalSelect(dbms, table, id, isIndexingTask);
	}

    /**
     * TODO javadoc.
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
		 * @param session
     * @return
     * @throws SQLException
     */
	public String insert(Dbms dbms, String schema, Element xml, int serial,
					 String source, String uuid, String createDate,
					 String changeDate, String isTemplate, String title,
					 int owner, String groupOwner, String docType, ServiceContext context) 
					 throws SQLException {

		return insertDb(dbms, schema, xml, serial, source, uuid, createDate, changeDate, isTemplate, xml.getQualifiedName(), title, owner, groupOwner, docType);

	}

    /**
     *  Updates an xml element into the database. The new data replaces the old one.
     *
     * @param dbms
     * @param id
     * @param xml
     * @param changeDate
     * @param updateDateStamp
     * @param context
     * @param userId
     * @throws SQLException
     */
	public void update(Dbms dbms, String id, Element xml, String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) throws SQLException {
		updateDb(dbms, id, xml, changeDate, xml.getQualifiedName(), updateDateStamp, uuid);
	}

    /**
     * Deletes an xml element given its id.
     *
     * @param dbms
     * @param table
     * @param id
		 * @param context
     * @throws Exception
     */
	public void delete(Dbms dbms, String table, String id, ServiceContext context) throws Exception {
		deleteDb(dbms, table, id);
	}

}
