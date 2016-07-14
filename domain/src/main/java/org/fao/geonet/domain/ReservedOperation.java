/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import javax.annotation.Nullable;

/**
 * The system reserved operations. Ids and names are hardcoded and have special meaning in
 * Geonetwork.
 *
 * @author Jesse Eichar
 */
public enum ReservedOperation {
    /**
     * The operation required to view the metadata.
     */
    view(0),
    /**
     * The operation required to download the metadata.
     */
    download(1),
    /**
     * The operation required to edit the metadata.
     */
    editing(2),
    /**
     * The operation required for listeners to be notified of changes about the metadata.
     */
    notify(3),
    /**
     * Identifies a metadata as having a "dynamic" component.
     */
    dynamic(5),
    /**
     * Operation that allows the metadata to be one of the featured metadata.
     */
    featured(6);

    // Not final so Tests can change id
    private int _id;

    private ReservedOperation(int id) {
        this._id = id;
    }

    /**
     * Look up a reserved operation by id.  Returns null if not a reserved operation.
     *
     * @param opId the id of the operation to look up.
     * @return null or the reserved operation.
     */
    public static
    @Nullable
    ReservedOperation lookup(int opId) {
        for (ReservedOperation op : ReservedOperation.values()) {
            if (op._id == opId) {
                return op;
            }
        }
        return null;
    }

    /**
     * Get the id of the operation.
     *
     * @return the id of the operation.
     */
    public int getId() {
        return _id;
    }

    /**
     * Create a transient operation entity with the data of the ReservedOperation
     */
    public Operation getOperationEntity() {
        return new Operation().setId(_id).setName(name());
    }

    public String getLuceneIndexCode() {
        return "_op" + _id;
    }
}
