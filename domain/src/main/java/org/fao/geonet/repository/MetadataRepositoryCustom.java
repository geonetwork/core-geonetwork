package org.fao.geonet.repository;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.fao.geonet.repository.statistic.MetadataStatisticsQueries;
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
     * Return an object that contains functions for calculating several different statistical calculations (related to the metadata)
     * based on the data in the database.
     *
     * @return an object for performing statistic calculation queries.
     */
    MetadataStatisticsQueries getMetadataStatistics();

    /**
     * Return an object that contains functions for calculating several different statistical calculations (related to the metadata)
     * based on the data in the database.
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
     * @return
     */
    @Nullable
    Metadata findOne(@Nonnull String id);

    /**
     * Find the list of Metadata Ids and changes dates for the metadata.
     * <p>
     * When constructing sort objects use the MetaModel objects:
     * <ul>
     * <li><code>new Sort(Metadata_.id.getName())</code></li>
     * <li><code>new Sort(Sort.Direction.ASC, Metadata_.id.getName())</code></li>
     * </ul>
     * </p>
     *
     * @param pageable if non-null then control which subset of the results to return (and how to sort the results).
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
     * 
     * @param spec
     * @return
     */
    List<SimpleMetadata> findAllSimple(String harvestUuid);

}
