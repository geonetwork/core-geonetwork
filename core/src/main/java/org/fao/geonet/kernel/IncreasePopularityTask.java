package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

/**
 * TODO javadoc.
 */
public class IncreasePopularityTask implements Runnable {
    private DataManager dataManager;
    private ServiceContext context;
    private int metadataId;

    public void configure(final DataManager dataManager, final ServiceContext context,
                                  final int metadataId) {
        this.dataManager = dataManager;
        this.context = context;
        this.metadataId = metadataId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void run() {
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

        metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                final MetadataDataInfo dataInfo = entity.getDataInfo();
                int popularity = dataInfo.getPopularity();
                dataInfo.setPopularity(popularity + 1);
            }
        });
        try {
            dataManager.indexMetadata(String.valueOf(metadataId), false);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "There may have been an error updating the popularity of the metadata "
                                           + metadataId + ". Error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
