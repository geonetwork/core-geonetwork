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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom (Non spring-data) Query methods for {@link Metadata} entities.
 *
 * @author Jesse
 */
public interface MetadataRepositoryCustom {

    /**
     * Return an object that contains functions for calculating several different statistical
     * calculations (related to the metadata) based on the data in the database.
     *
     * @return an object for performing statistic calculation queries.
     */
    MetadataReportsQueries getMetadataReports();

    /**
     * Permit finding a metadata by its ids as a string.
     * <p/>
     * The id needs to be convertable to an integer
     * <p/>
     * This is just short for repository.findOne(Integer.parseInt(id))
     *
     * @param id the id in string form instead of integer.
     */
    @Nullable
    Metadata findOne(@Nonnull String id);

    /**
     * Find the list of Metadata Ids and changes dates for the metadata. <p> When constructing sort
     * objects use the MetaModel objects: <ul> <li><code>new Sort(Metadata_.id.getName())</code></li>
     * <li><code>new Sort(Sort.Direction.ASC, Metadata_.id.getName())</code></li> </ul> </p>
     *
     * @param pageable if non-null then control which subset of the results to return (and how to
     *                 sort the results).
     * @return List of &lt;MetadataId, changeDate&gt;
     */
    @Nonnull
    Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(@Nonnull Pageable pageable);

    /**
     * Find all ids of metadata that match the specification.
     *
     * @param spec the specification for identifying the metadata.
     * @return all ids
     */
    @Nonnull
    List<Integer> findAllIdsBy(@Nonnull Specification<Metadata> spec);

    /**
     * Find the metadata that has the oldest change date.
     *
     * @return the metadata with the oldest change date
     */
    @Nullable
    Metadata findOneOldestByChangeDate();

    /**
     * Load the source info objects for all the metadata selected by the spec.
     *
     * @param spec the specification identifying the metadata of interest
     * @return a map of metadataId -> SourceInfo
     */
    Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<Metadata> spec);

    /**
     * Load only the basic info for a metadata. Used in harvesters, mostly.
     */
    List<SimpleMetadata> findAllSimple(String harvestUuid);

}
