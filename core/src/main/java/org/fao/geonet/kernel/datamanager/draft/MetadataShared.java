/**
 * 
 */
package org.fao.geonet.kernel.datamanager.draft;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.events.md.sharing.MetadataShare;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * if privileges on draft are modified
 * 
 * @author delawen
 * 
 * 
 */
@Component
public class MetadataShared implements ApplicationListener<MetadataShare> {

    @Autowired
    private IMetadataUtils manager;

    @Autowired
    private IMetadataIndexer indexer;

    @Autowired
    private MetadataRepository mdRepository;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     * @param event
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(MetadataShare event) {

        Integer idmd = event.getOp().getId().getMetadataId();
        try {
            IMetadata md = manager.findOne(idmd);
            if (md instanceof MetadataDraft) {
                // Update on original metadata too
                Metadata original = mdRepository.findOneByUuid(md.getUuid());
                OperationAllowed operationAllowed = null;

                switch (event.getType()) {
                case ADD:
                    // Can't use operations directly because there is no
                    // context. Mixing Jeeves and Spring dead-end
                    // Optional<OperationAllowed> opAllowed =
                    // operations.getOperationAllowedToAdd(context,
                    // mdId, grpId, opId);

                    operationAllowed = operationAllowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(
                            event.getOp().getId().getGroupId(), original.getId(), event.getOp().getId().getOperationId());

                    if (operationAllowed == null) {
                        operationAllowed = new OperationAllowed(new OperationAllowedId().setGroupId(event.getOp().getId().getGroupId())
                                .setMetadataId(original.getId()).setOperationId(event.getOp().getId().getOperationId()));

                        operationAllowedRepository.save(operationAllowed);
                    }

                    break;
                case REMOVE:
                    // Can't use this because there is no context. Mixing Jeeves
                    // and Spring dead-end
                    // manager.deleteMetadataOper
                    operationAllowed = operationAllowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(
                            event.getOp().getId().getGroupId(), original.getId(), event.getOp().getId().getOperationId());

                    if (operationAllowed != null) {
                        operationAllowedRepository.delete(operationAllowed);
                    }
                    break;
                default:
                    // There is a case for this?
                    break;

                }

                indexer.indexMetadata(String.valueOf(original.getId()), true, null);
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, e, e);
        }

    }

}
