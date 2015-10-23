package org.fao.geonet.repository;

import org.fao.geonet.domain.OpenwisDownload;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link OpenwisDownload} entities.
 *
 * @author María Arias de Reyna
 */
public interface OpenwisDownloadRepository
        extends GeonetRepository<OpenwisDownload, Integer>,
        JpaSpecificationExecutor<OpenwisDownload>,
        OpenwisDownloadRepositoryCustom {

}
