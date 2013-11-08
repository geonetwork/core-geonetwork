package org.fao.geonet.repository;

import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Custom methods for interacting with HarvestHistory repository.
 * <p/>
 * User: Jesse
 * Date: 9/21/13
 * Time: 11:21 AM
 */
public interface HarvestHistoryRepositoryCustom {

    /**
     * Delete all Harvest history instances whose id is in the collection of ids.
     *
     * @param ids the ids of the history elements to delete
     * @return number or entities deleted
     */
    int deleteAllById(Collection<Integer> ids);

    /**
     * Set the deleted flag to true in all history entities for the given uuid.
     *
     * @param harvesterUuid the harvester uuid.
     * @return number or entities modified
     */
    int markAllAsDeleted(@Param("uuid") @Nonnull String harvesterUuid);
}
