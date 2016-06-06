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

/**
 * All *Repository objects are Data access objects for search and loading entities in the domain
 * packages. <p> The objects are created using spring-data-jpa and thus have the same design.
 * </p><p> The objects *RepositoryCustom are the interfaces for custom query methods for a
 * repository. In other words, queries that cannot be defined via the normal spring-data query
 * mechanism. </p> The objects *RepositoryImpl are the implementations of *RepositoryCustom.
 * spring-data wires together *Repository, *RepositoryCustom and *RepositoryImpl together so as an
 * end user you only need to concern yourself with the *Repository interface. </p><p> For more
 * information on implementation details see the spring-data-jpa documentation. </p>
 *
 * @author Jesse
 */
package org.fao.geonet.repository;
