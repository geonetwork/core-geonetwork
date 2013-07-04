package org.fao.geonet.kernel;

import org.jdom.Element;

/**
 * An interface so that Datamanager can obtain harvest information without needing to depend
 * on harvesting module.
 *
 * @author Jesse
 */
public interface HarvestInfoProvider {

    /**
     * Determine harvesting information for metadata.
     * 
     * @param harvestUuid UUID of harvester that harvested the metadata
     * @param id id of metadata
     * @param uuid uuid of metadata
     * @return
     */
    Element getHarvestInfo(String harvestUuid, String id, String uuid);

}
