/**
 * 
 */
package org.fao.geonet.kernel.datamanager.draft;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jeeves.server.context.ServiceContext;

/**
 * Use draft to publish and cleanup draft.
 * 
 * @author delawen
 * 
 * 
 */
@Component
public class DraftUnpublish
        implements ApplicationListener<MetadataUnpublished> {

    @Autowired
    private IMetadataIndexer indexer;

    @Autowired
    private IMetadataManager manager;

    @Autowired
    private MetadataDraftRepository mdDraftRepository;

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     * @param event
     */
    @Override
    public void onApplicationEvent(MetadataUnpublished event) {
        // If it has a draft associated
        IMetadata metadata = event.getMd();

        if (!(metadata instanceof MetadataDraft)) {

            MetadataDraft draft = mdDraftRepository
                    .findOneByUuid(metadata.getUuid());

            if (draft != null) {
                ServiceContext serviceContext = ServiceContext.get();

                // Copy the draft content to the published metadata
                try {
                    manager.updateMetadata(serviceContext,
                            Integer.toString(event.getMd().getId()),
                            draft.getXmlData(false), false, false, true, "",
                            draft.getDataInfo().getChangeDate()
                                    .getDateAndTime(),
                            false);
                    // Remove the draft
                    manager.deleteMetadata(serviceContext,
                            Integer.toString(draft.getId()));

                    indexer.indexMetadata(Integer.toString(draft.getId()),
                            false, null);
                } catch (Exception e) {
                    Log.error(Geonet.DATA_MANAGER, e);
                }

            }
        }

    }
}
