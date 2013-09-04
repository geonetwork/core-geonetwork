package org.fao.geonet.repository;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Custom (Non spring-data) Query methods for {@link Metadata} entities.
 *
 * @author Jesse
 */
public interface MetadataRepositoryCustom {
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
     *     When constructing sort objects use the MetaModel objects:
     *     <ul>
     *         <li><code>new Sort(Metadata_.id.getName())</code></li>
     *         <li><code>new Sort(Sort.Direction.ASC, Metadata_.id.getName())</code></li>
     *     </ul>
     * </p>
     *
     * @param pageable if non-null then control which subset of the results to return (and how to sort the results).
     * @return List of &lt;MetadataId, changeDate&gt;
     */
    @Nonnull
    List<Pair<Integer, ISODate>> findAllIdsAndChangeDates(@Nonnull Pageable pageable);
}
