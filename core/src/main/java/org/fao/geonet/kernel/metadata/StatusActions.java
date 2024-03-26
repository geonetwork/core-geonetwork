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

package org.fao.geonet.kernel.metadata;

import jeeves.server.context.ServiceContext;

import java.util.List;
import java.util.Set;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;

/**
 * Facade performing actions with record status.
 */
public interface StatusActions {

    /**
     * Setup using provided externally managed service context.
     *
     * @param context Externally managed service context.
     */
    public void init(ServiceContext context) throws Exception;

    /**
     * Called when a record is edited to set/reset status.
     *
     * @param id        The metadata id that has been edited.
     * @param minorEdit If true then the edit was a minor edit.
     */
    public void onEdit(int id, boolean minorEdit) throws Exception;

    /**
     * Called when a record status is added.
     *
     * @param statusList List of status to update
     * @param updateIndex index update flag
     * @return Ids of unchanged metadata records
     */
    public Set<Integer> onStatusChange(List<MetadataStatus> statusList, boolean updateIndex) throws Exception;

}
