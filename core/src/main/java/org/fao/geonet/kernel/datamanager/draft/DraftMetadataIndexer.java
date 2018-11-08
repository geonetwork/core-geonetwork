package org.fao.geonet.kernel.datamanager.draft;

import java.util.Vector;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataIndexer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

public class DraftMetadataIndexer extends BaseMetadataIndexer implements IMetadataIndexer {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Override
    public void init(ServiceContext context, Boolean force) throws Exception {
        super.init(context, force);
        metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
    }

    @Override
    /**
     * Adds the specific draft related fields. 
     * @param fullMd
     * @param moreFields
     */
    protected void addExtraFields(AbstractMetadata fullMd, Vector<Element> moreFields) {
        super.addExtraFields(fullMd, moreFields);

        if (fullMd instanceof MetadataDraft) {
            moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "Y", true, false));
        } else {
            if (metadataDraftRepository.exists(fullMd.getId())) {
                moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "E", true, false));
            } else {
                moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "N", true, false));
            }
        }
    }
}
