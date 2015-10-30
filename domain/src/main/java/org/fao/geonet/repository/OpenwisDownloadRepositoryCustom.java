package org.fao.geonet.repository;

import org.fao.geonet.domain.OpenwisDownload;
import org.fao.geonet.domain.User;

/**
 * Custom methods for loading {@link OpenwisDownload} entities.
 *
 * @author María Arias de Reyna
 */
public interface OpenwisDownloadRepositoryCustom {

    /**
     * Given a user and a uuid, check if there exists an openwisDownload and
     * return it
     * 
     * @param user
     * @param uuid
     * @return null or associated OpenwisDownload
     */
    OpenwisDownload findByUserAndUuid(User user, String uuid);

    
    boolean existsRequestId(Long id);
}
