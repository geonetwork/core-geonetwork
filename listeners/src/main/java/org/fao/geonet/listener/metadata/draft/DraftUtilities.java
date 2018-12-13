package org.fao.geonet.listener.metadata.draft;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

@Service
public class DraftUtilities {

	@Autowired
	private IMetadataManager metadataManager;

	@Autowired
	private MetadataValidationRepository metadataValidationRepository;

	@Autowired
	private MetadataFileUploadRepository metadataFileUploadRepository;

	@Autowired
	private SearchManager searchManager;

	@Autowired
	private XmlSerializer xmlSerializer;

	@Autowired
	private IMetadataOperations metadataOperations;

	@Autowired
	private MetadataStatusRepository metadataStatusRepository;

	@Autowired
	private MetadataDraftRepository metadataDraftRepository;

	/**
	 * Replace the contents of the record with the ones on the draft, if exists, and
	 * remove the draft
	 * 
	 * @param md
	 * @param draft
	 * @return
	 */
	@Transactional(value = TxType.REQUIRED)
	public AbstractMetadata replaceMetadataWithDraft(AbstractMetadata md) {
		AbstractMetadata draft = metadataDraftRepository.findOneByUuid(md.getUuid());

		if (draft != null) {
			return replaceMetadataWithDraft(md, draft);
		}
		
		return md;
	}

	/**
	 * Replace the contents of the record with the ones on the draft and remove the
	 * draft
	 * 
	 * @param md
	 * @param draft
	 * @return
	 */
	@Transactional(value = TxType.REQUIRED)
	public AbstractMetadata replaceMetadataWithDraft(AbstractMetadata md, AbstractMetadata draft) {
		Log.trace(Geonet.DATA_MANAGER, "Found approved record with id " + md.getId());
		Log.trace(Geonet.DATA_MANAGER, "Found draft with id " + draft.getId());
		// Reassign metadata validations
		List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(draft.getId());
		for (MetadataValidation mv : validations) {
			mv.getId().setMetadataId(md.getId());
			metadataValidationRepository.save(mv);
		}

		// Reassign file uploads
		List<MetadataFileUpload> fileUploads = metadataFileUploadRepository
				.findAll(MetadataFileUploadSpecs.hasId(draft.getId()));
		for (MetadataFileUpload fu : fileUploads) {
			fu.setMetadataId(md.getId());
			metadataFileUploadRepository.save(fu);
		}

		try {
			ServiceContext context = ServiceContext.get();
			Element xmlData = draft.getXmlData(false);
			String changeDate = draft.getDataInfo().getChangeDate().getDateAndTime();

			removeDraft(draft);

			if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
				Log.trace(Geonet.DATA_MANAGER, "Updating record " + md.getId() + " with ");
				Log.trace(Geonet.DATA_MANAGER, (new org.jdom.output.XMLOutputter()).outputString(xmlData));
			}

			// Copy contents
			Log.trace(Geonet.DATA_MANAGER, "Update record with id " + md.getId());
			md = metadataManager.updateMetadata(context, String.valueOf(md.getId()), xmlData, true, true, true,
					context.getLanguage(), changeDate, true);

			Log.info(Geonet.DATA_MANAGER, "Record updated with draft contents: " + md.getId());

		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER, "Error upgrading from draft record with id " + md.getId(), e);
		}
		return md;
	}

	/**
	 * Completely remove a draft associated to the UUID of the metadata.
	 * 
	 * @param md
	 * @throws Exception
	 */
	@Transactional(value = TxType.REQUIRED)
	public void removeDraft(AbstractMetadata md) throws Exception {
		AbstractMetadata draft = metadataDraftRepository.findOneByUuid(md.getUuid());

		if (draft != null) {
			ServiceContext context = ServiceContext.get();

			Log.debug(Geonet.DATA_MANAGER, "Remove draft with id " + draft.getId());
			// Remove draft
			metadataOperations.deleteMetadataOper(context, String.valueOf(draft.getId()), false);
			metadataStatusRepository.deleteAllById_MetadataId(draft.getId());
			xmlSerializer.delete(String.valueOf(draft.getId()), context);

			searchManager.delete(draft.getId() + "");
		}
	}
}
