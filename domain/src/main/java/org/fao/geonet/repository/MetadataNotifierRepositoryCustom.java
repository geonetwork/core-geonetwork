package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotifier;

import java.util.List;

/**
 * The custom metadata notifier repository methods that can't be implemented by the spring-data api.
 * <p/>
 * User: Jesse
 * Date: 8/28/13
 * Time: 7:30 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MetadataNotifierRepositoryCustom {
    /**
     * Find all the enabled/disabled notifiers
     *
     * @param enabled if true then  find the enabled
     */
    List<MetadataNotifier> findAllByEnabled(boolean enabled);
}
