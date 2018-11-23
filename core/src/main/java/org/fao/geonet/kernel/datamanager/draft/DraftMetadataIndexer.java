package org.fao.geonet.kernel.datamanager.draft;

import java.util.Vector;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataIndexer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.utils.Log;
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
	 * 
	 * @param fullMd
	 * @param moreFields
	 */
	protected void addExtraFields(AbstractMetadata fullMd, Vector<Element> moreFields) {
		super.addExtraFields(fullMd, moreFields);

		if (fullMd instanceof MetadataDraft) {
			Log.trace(Geonet.DATA_MANAGER, "We are indexing a draft with uuid " + fullMd.getUuid());
			moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "y", true, true));
		} else {
			if (metadataDraftRepository.findOneByUuid(fullMd.getUuid()) != null) {
				Log.trace(Geonet.DATA_MANAGER,
						"We are indexing a record with a draft associated with uuid " + fullMd.getUuid());
				moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "e", true, true));
			} else {
				Log.trace(Geonet.DATA_MANAGER,
						"We are indexing a record with no draft associated with uuid " + fullMd.getUuid());
				moreFields.addElement(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, "n", true, true));
			}
		}
	}
}
