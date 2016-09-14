/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.events.md.MetadataPublished;
import org.fao.geonet.kernel.metadata.IMetadataIndexer;
import org.fao.geonet.kernel.metadata.IMetadataManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import jeeves.server.context.ServiceContext;

/**
 * Use draft to publish and cleanup draft.
 * 
 * @author delawen
 * 
 * 
 */
public class DraftPublish implements ApplicationListener<MetadataPublished> {

    @Autowired
    private IMetadataIndexer indexer;

    @Autowired
    private IMetadataManager manager;

    @Autowired
    private MetadataRepository mdRepository;

    @Autowired
    private MetadataDraftRepository mdDraftRepository;

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     * @param event
     */
    @Override
    public void onApplicationEvent(MetadataPublished event) {

        if (event.getMd() instanceof MetadataDraft) {
            IMetadata draft = event.getMd();
            Metadata md = mdRepository.findOneByUuid(draft.getUuid());

            processDraft(draft, md);

        } else {
            MetadataDraft draft = mdDraftRepository
                    .findOneByUuid(event.getMd().getUuid());

            if (draft != null) {
                processDraft(draft, event.getMd());
            }
        }

    }

    private void processDraft(IMetadata draft, IMetadata md) {
        ServiceContext serviceContext = ServiceContext.get();

        // Copy the draft content to the published metadata
        try {
            manager.updateMetadata(serviceContext, Integer.toString(md.getId()),
                    draft.getXmlData(false), false, false, true, "",
                    draft.getDataInfo().getChangeDate().getDateAndTime(), false);
            // Remove the draft
            manager.deleteMetadata(serviceContext, Integer.toString(draft.getId()));

            indexer.indexMetadata(Integer.toString(md.getId()), false);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, e);
        }

    }

}
