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

package org.fao.geonet.repository;

import org.fao.geonet.domain.Schematron;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for the {@link org.fao.geonet.domain.Schematron} entities.
 *
 * @author delawen
 */
public interface SchematronRepository extends
    GeonetRepository<Schematron, Integer>,
    JpaSpecificationExecutor<Schematron> {
    /**
     * Look up a schematrons by its schema
     *
     * @param schemaName the name of the schema
     */
    public List<Schematron> findAllBySchemaName(String schemaName);

    /**
     * Look up a schematrons by its file
     *
     * @param file       path from schema directory to the file
     * @param schemaName name of the schema
     */
    public Schematron findOneByFileAndSchemaName(String file, String schemaName);

}
