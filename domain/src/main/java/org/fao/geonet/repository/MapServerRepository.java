package org.fao.geonet.repository;

import org.fao.geonet.domain.MapServer;
import org.fao.geonet.domain.StatusValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MapServer} entities.
 *
 * @author Francois
 */
public interface MapServerRepository
        extends GeonetRepository<MapServer, String>,
            MapServerRepositoryCustom,
            JpaSpecificationExecutor<MapServer> {
    MapServer findOneById(int id);
}
