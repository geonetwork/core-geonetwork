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
import jeeves.utils.Log;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

/**
 * This class is responsible for reading and writing metadata extras from the 
 * database and xml from subversion. 
 * 
 */
public class XmlSerializerSvn extends XmlSerializer {

	SvnManager svnMan = null;

	/** Sets the repository URL - at present we really only expect that this
	  * will be a local filesystem repository
		*/
	public XmlSerializerSvn(SettingManager sMan, SvnManager svnMan) throws Exception {
		super(sMan);
	
		this.svnMan = svnMan;
	}

    /**
     * Retrieves the xml element whose id matches the given one. The element is read from the database as subversion may be busy with commit changes.
     * @param id
     *
     * @return
     * @throws Exception
     */
	protected Element internalSelect(Dbms dbms, String table, String id, boolean isIndexingTask) throws Exception {
		Element rec = super.internalSelect(dbms, table, id, isIndexingTask);
		if (rec != null) return (Element) rec.detach();
		else return null;
	}

    /**
     *  Retrieves the xml element which id matches the given one. The element 
		 *  is read from 'table' or the subversion repo and the string read
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
     * Retrieves the xml element which id matches the given one. The element
		 * is read from 'table' or subversion and the string read is
     * converted into xml, XLinks are NOT resolved even if they are config'd 
		 * on - this is used when you want to do XLink processing yourself.
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
     * Inserts a metadata into the database. Does not insert the metadata 
		 * into the subversion repository. Instead this is done when an update
		 * is generated on the metadata (eg. from editor).
     *
     * @param dbms
     * @param schema
     * @param xml
     * @param id
     * @param source
     * @param uuid
     * @param createDate
     * @param changeDate
     * @param isTemplate
     * @param title
     * @param owner
     * @param groupOwner
     * @param docType
     * @param context 
     * @return
     * @throws SQLException
     */
	public String insert(Dbms dbms, String schema, Element xml, int id,
					 String source, String uuid, String createDate,
					 String changeDate, String isTemplate, String title,
			 int owner, String groupOwner, String docType, ServiceContext context) 
			 throws Exception {

		return insertDb(dbms, schema, xml, id, source, uuid, createDate, changeDate, isTemplate, xml.getQualifiedName(), title, owner, groupOwner, docType);
	}

    /**
     *  Updates an xml element in the database and the subversion repo. 
		 *  The new metadata replaces the old metadata in the database. The old
		 *  metadata in the database is added to the subversion repo first time
		 *  an update is generated. In general the old metadata is diff'ed with 
		 *  the new metadata to generate a delta in the subversion repository.
     *
     * @param dbms
     * @param id
     * @param xml
     * @param changeDate
     * @param updateDateStamp
     * @param context 
     * @throws SQLException, SVNException
     */
	public void update(Dbms dbms, String id, Element xml, String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) throws Exception {

		// old XML comes from the database
		updateDb(dbms, id, xml, changeDate, xml.getQualifiedName(), updateDateStamp, uuid);

		if (svnMan == null) { // do nothing
			Log.error(Geonet.DATA_MANAGER, "SVN repository for metadata enabled but no repository available");
		} else {
			// set subversion manager to record history on this metadata when commit
			// takes place
			svnMan.setHistory(dbms, id, context);
		}

	}

    /**
     * Deletes a metadata record given its id. The metadata record is deleted
		 * from 'table' and from the subversion repo (if present).
     *
     * @param dbms
     * @param table
     * @param id
     * @param context
     * @throws SQLException, SVNException
     */
	public void delete(Dbms dbms, String table, String id, ServiceContext context) throws Exception {

		deleteDb(dbms, table, id);
		if (svnMan == null) { // do nothing
			Log.error(Geonet.DATA_MANAGER, "SVN repository for metadata enabled but no repository available");
		} else {
			svnMan.deleteDir(id, context);
		}

	}

}
