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

package org.fao.geonet.api.records.formatters.groovy.util;

/**
 * Represents the direction of a relationship.
 *
 * @author Jesse on 4/23/2015.
 */
public enum Direction {
    /**
     * Indicates that the metadata is the parent of a relationship.  For most relationships the
     * child metadata contains the link to the parent.  For example parentIdentifier,
     * aggregationInfo and lineage links are in the child.  Other types like coupledResource (in
     * services) then the parent contains the relationship.
     */
    PARENT,
    /**
     * Indicates that the metadata is the child of a relationship.  For most relationships the child
     * metadata contains the link to the parent.  For example parentIdentifier, aggregationInfo and
     * lineage links are in the child.  Other types like coupledResource (in services) then the
     * parent contains the relationship.
     */
    CHILD,
    /**
     * Indicates that the metadata is the sibling of the related metadata.  In otherwords, both
     * metadata are part of the same group of related metadata, both are children metadata and part
     * of the same group/relationship.
     */
    SIBLING;

}
