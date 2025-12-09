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

import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.fao.geonet.domain.MetadataDraft;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data Access object for the {@link MetadataDraft} entities.
 *
 * The use of this class is discouraged, you should use IMetadataUtils or IMetadataManager instead.
 *
 * @author Jesse
 */
public interface MetadataDraftRepository
    extends GeonetRepository<MetadataDraft, Integer>,
    MetadataDraftRepositoryCustom<MetadataDraft>,
    JpaSpecificationExecutor<MetadataDraft> {
    /**
     * Find one metadata by the metadata's uuid.
     *
     * @param uuid the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable
    MetadataDraft findOneByUuid(@Nonnull String uuid);

    /**
     * Find all metadata by the metadata's uuid.
     *
     * @param uuid the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable
    List<MetadataDraft> findAllByUuid(@Nonnull String uuid);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull
    List<MetadataDraft> findAllByHarvestInfo_Uuid(@Nonnull String uuid);

    /**
     * Get the metadata after preforming a search and replace on it.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @return The metadata with the search and replace applied.
     */
    @Query(value = "SELECT replace(data, :search, :replace) FROM MetadataDraft m " +
        "WHERE uuid = :uuid",
        nativeQuery = true)
    String selectOneWithSearchAndReplace(
        @Param("uuid") String uuid,
        @Param("search") String search,
        @Param("replace") String replace);

    /**
     * Get the metadata after preforming a regex search and replace on it.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @return The metadata with the search and replace applied.
     */
    @Query(value = "SELECT regexp_replace(data, :pattern, :replace) FROM MetadataDraft m " +
        "WHERE uuid = :uuid",
        nativeQuery = true)
    String selectOneWithRegexSearchAndReplace(
        @Param("uuid") String uuid,
        @Param("pattern") String search,
        @Param("replace") String replace);

    /**
     * Get the metadata after preforming a regex search and replace on it with regex flags.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @param flags   The regex flags to use.
     * @return The metadata with the search and replace applied.
     */
    @Query(value = "SELECT regexp_replace(data, :pattern, :replace, :flags) FROM MetadataDraft m " +
        "WHERE uuid = :uuid",
        nativeQuery = true)
    String selectOneWithRegexSearchAndReplaceWithFlags(
        @Param("uuid") String uuid,
        @Param("pattern") String search,
        @Param("replace") String replace,
        @Param("flags") String flags);
}
