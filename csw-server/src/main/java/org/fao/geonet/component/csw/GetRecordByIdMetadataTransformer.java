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

package org.fao.geonet.component.csw;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.GeonetworkExtension;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * An extension point to allow plugins to transform the metadata returned by {@link GetRecordById}
 *
 * User: Jesse Date: 11/7/13 Time: 3:23 PM
 */
public interface GetRecordByIdMetadataTransformer extends GeonetworkExtension {

    /**
     * Transform the metadata record in some way.  Optional.absent() should be returned if the
     * record does not apply.
     *
     * @param context      a service context.
     * @param metadata     the metadata to transform
     * @param outputSchema the output schema GetRecords parameter.
     * @return Optional.absent() if transformer does not apply to the metadata or output schema.  Or
     * Optional.of() if a change is made.
     */
    @Nonnull
    public Optional<Element> apply(@Nonnull ServiceContext context, @Nonnull Element metadata,
                                   @Nonnull String outputSchema) throws CatalogException;
}
