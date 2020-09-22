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

import org.fao.geonet.domain.AbstractMetadata;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

/**
 * This class is responsible of reading and writing xml on the database. It works on tables like
 * (id, data, lastChangeDate).
 */
public class XmlSerializerDb extends XmlSerializer {

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table'
     * and the string read is converted into xml, XLinks are resolved when config'd on.
     */
    public Element select(ServiceContext context, String id) throws Exception {
        Element rec = internalSelect(id, false, true);
        if (resolveXLinks()) Processor.detachXLink(rec, context);
        return rec;
    }

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table'
     * and the string read is converted into xml, XLinks are NOT resolved even if they are config'd
     * on - this is used when you want to do XLink processing yourself.
     */
    public Element selectNoXLinkResolver(String id, boolean isIndexingTask, boolean applyOperationsFilters) throws Exception {
        return internalSelect(id, isIndexingTask, applyOperationsFilters);
    }

    /**
     * TODO javadoc.
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
     * Updates an xml element into the database. The new data replaces the old one.
     */
    public void update(String id, Element xml, String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) throws SQLException {
        updateDb(id, xml, changeDate, xml.getQualifiedName(), updateDateStamp, uuid);
    }

    /**
     * Deletes an xml element given its id.
     */
    public void delete(String id, ServiceContext context) throws Exception {
        deleteDb(id);
    }

}
