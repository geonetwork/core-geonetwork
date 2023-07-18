/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords.es;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.fao.geonet.kernel.csw.services.getrecords.IdentityFieldMapper;
import org.springframework.context.annotation.Bean;

public class CswFilter2EsTestConfiguration {

    @Bean
    public IFieldMapper fieldMapper() {
        return new IdentityFieldMapper();
    }

    @Bean
    public CatalogConfiguration catalogConfiguration() {
        return new CatalogConfiguration();
    }

    @Bean
    public GeonetworkDataDirectory dataDirectory() {
        return new GeonetworkDataDirectory();
    }

    @Bean
    public SchemaManager schemaManager() {
        return new SchemaManager();
    }
}
