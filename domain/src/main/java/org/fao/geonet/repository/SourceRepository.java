/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Data Access object for accessing {@link Source} entities.
 *
 * @author Jesse
 */
public interface SourceRepository extends GeonetRepository<Source, String>, JpaSpecificationExecutor<Source> {
    /**
     * Find the source with the provided Name.
     *
     * @param name the name of the source to lookup
     * @return the source with the provided name or <code>null</code>.
     */
    public
    @Nullable
    Source findOneByName(@Nonnull String name);

    /**
     * Find the source with the provided UUID.
     *
     * @param uuid the UUID of the source to lookup
     * @return the source with the provided name or <code>null</code>.
     */
    public
    @Nullable
    Source findOneByUuid(@Nonnull String uuid);


    public
    @Nullable
    List<Source> findByLogo(@Nonnull String logo);

    public
    @Nullable
    List<Source> findByType(@Nonnull SourceType sourceType, Sort sort);

    public
    @Nullable
    List<Source> findByGroupOwner(@Nonnull int groupOwner, Sort sort);

    public
    @Nullable
    List<Source> findByGroupOwnerAndType(@Nonnull int groupOwner, @Nonnull SourceType sourceType, Sort sort);
    public
    @Nullable
    List<Source> findByGroupOwnerIn(Set<Integer> groupOwner);

    boolean existsByUuidAndType(String name, SourceType type);
}
