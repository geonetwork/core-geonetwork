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

/**
 * The list of reserved groups. Ids and names are hardcoded and have special meaning in Geonetwork.
 *
 * @author Jesse
 */
public enum ReservedGroup {
    /**
     * The "All" group.  IE the group that represents all.
     */
    all(1),
    /**
     * The Intranet group.  IE the group that represents all users within the same intranet as the
     * geonetwork server.
     */
    intranet(0),
    /**
     * The "Guest" group.  IE the group representing all users not signed in.
     */
    guest(-1);

    // Not final so Tests can change id
    private int _id;

    private ReservedGroup(int id) {
        _id = id;
    }

    public static boolean isReserved(int grpId) {
        for (ReservedGroup reservedGroup : values()) {
            if (reservedGroup.getId() == grpId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the id of the reserved group.
     *
     * @return the id of the reserved group.
     */
    public int getId() {
        return _id;
    }

    /**
     * Create a detached Group that represents the reserved group.
     *
     * @return a detached Group that represents the reserved group.
     */
    public Group getGroupEntityTemplate() {
        return new Group().setId(_id).setName(name()).setDescription(name());
    }
}
