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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

/**
 * This class is responsible for reading and writing metadata extras from the database and xml from
 * subversion.
 */
public class XmlSerializerSvn extends XmlSerializer {

    /**
     * Retrieves the xml element whose id matches the given one. The element is read from the
     * database as subversion may be busy with commit changes.
     */
    protected Element internalSelect(String id, boolean isIndexingTask, boolean applyOperationsFilters) throws Exception {
        Element rec = super.internalSelect(id, isIndexingTask, applyOperationsFilters);
        if (rec != null) return (Element) rec.detach();
        else return null;
    }

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' or
     * the subversion repo and the string read is converted into xml, XLinks are resolved when
     * config'd on.
     */
    public Element select(ServiceContext context, String id) throws Exception {
        Element rec = internalSelect(id, false, false);
        if (resolveXLinks()) Processor.detachXLink(rec, context);
        return rec;
    }

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' or
     * subversion and the string read is converted into xml, XLinks are NOT resolved even if they
     * are config'd on - this is used when you want to do XLink processing yourself.
     */
    public Element selectNoXLinkResolver(String id, boolean isIndexingTask, boolean applyOperationsFilters) throws Exception {
        return internalSelect(id, isIndexingTask, applyOperationsFilters);
    }

    /**
     * Inserts a metadata into the database. Does not insert the metadata into the subversion
     * repository. Instead this is done when an update is generated on the metadata (eg. from
     * editor).
     *
     * @param newMetadata the metadata to insert
     * @param dataXml     the data to set on the metadata before saving
     * @param context     a service context
     * @return the saved metadata
     */
    public AbstractMetadata insert(final AbstractMetadata newMetadata, final Element dataXml, ServiceContext context) throws SQLException {
        return insertDb(newMetadata, dataXml, context);
    }

    /**
     * Updates an xml element in the database and the subversion repo. The new metadata replaces the
     * old metadata in the database. The old metadata in the database is added to the subversion
     * repo first time an update is generated. In general the old metadata is diff'ed with the new
     * metadata to generate a delta in the subversion repository.
     *
     * @throws SQLException, SVNException
     */
    public void update(String id, Element xml, String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) throws Exception {
        SvnManager svnMan = context.getBean(SvnManager.class);
        // old XML comes from the database
        updateDb(id, xml, changeDate, xml.getQualifiedName(), updateDateStamp, uuid);

        svnMan.setHistory(id, context);

    }

    /**
     * Deletes a metadata record given its id. The metadata record is deleted from 'table' and from
     * the subversion repo (if present).
     *
     * @throws SQLException, SVNException
     */
    public void delete(String id, ServiceContext context) throws Exception {
        deleteDb(id);
        try {
            SvnManager svnMan = context.getBean(SvnManager.class);
            svnMan.deleteDir(id, context);
        } catch (NoSuchBeanDefinitionException e) {
            Log.error(Geonet.DATA_MANAGER, "SVN manager not found. No SVN repository available.");
        }
    }

}
