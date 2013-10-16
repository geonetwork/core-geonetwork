package org.fao.geonet.repository;

/**
 * Custom methods for interacting with HarvestHistory repository.
 * <p/>
 * User: Jesse
 * Date: 9/21/13
 * Time: 11:21 AM
 */
public interface HarvestHistoryRepositoryCustom {

    int deleteAllById(Iterable<Integer> ids);
}
