package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;

/**
 * TODO javadoc.
 */
public class IncreasePopularityTask implements Runnable {

    private int metadataId;
    @Qualifier("DataManager")
    @Autowired
    private DataManager _dataManager;
    @Autowired
    private MetadataRepository _metadataRepository;


    public void setMetadataId(final int metadataId) {
        this.metadataId = metadataId;
    }

    public void run() {

        _metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                final MetadataDataInfo dataInfo = entity.getDataInfo();
                int popularity = dataInfo.getPopularity();
                dataInfo.setPopularity(popularity + 1);
            }
        });
        try {
            _dataManager.indexMetadata(String.valueOf(metadataId), false);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "There may have been an error updating the popularity of the metadata "
                                           + metadataId + ". Error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
