package org.fao.geonet.repository;

import org.fao.geonet.domain.MapServer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MapServer} entities.
 *
 * @author Francois
 */

public class MapServerRepositoryImpl implements MapServerRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public MapServer findOneById(final String id) {
        return _entityManager.find(MapServer.class, Integer.valueOf(id));
    }
}
