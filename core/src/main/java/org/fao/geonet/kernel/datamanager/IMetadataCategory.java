//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager;

import java.util.Collection;

import org.fao.geonet.domain.MetadataCategory;

import jeeves.server.context.ServiceContext;

/**
 * Interface to handle categories of records.
 * 
 * @author delawen
 *
 */
public interface IMetadataCategory {
    /**
     * Given a record id and a category id, returns if the record has that category assigned.
     * 
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    boolean isCategorySet(String mdId, int categId) throws Exception;

    /**
     * Given a record id and a category id, assign that category to the previous record
     * 
     * @param context
     * @param mdId
     * @param categId
     * @return if the category was assigned
     * @throws Exception
     */
    boolean setCategory(ServiceContext context, String mdId, String categId) throws Exception;

    /**
     * Given a record id and a category id, unassign that category from the previous record
     * 
     * @param context
     * @param mdId
     * @param categId
     * @return if the category was deassigned
     * @throws Exception
     */
    boolean unsetCategory(ServiceContext context, String mdId, int categId) throws Exception;

    /**
     * Given a record id, return the list of categories associated to that record
     * 
     * @param mdId
     * @return
     * @throws Exception
     */
    Collection<MetadataCategory> getCategories(String mdId) throws Exception;
}
