/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.listener.metadata.draft;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

/**
 * 
 * When a record gets approved, check if there is a draft associated to it. If
 * there is, modify its contents with the ones on the draft and remove the
 * draft.
 * 
 * @author delawen
 *
 */
@Service
public class ApproveRecord implements ApplicationListener<MetadataStatusChanged> {

	@Autowired
	private MetadataDraftRepository metadataDraftRepository;

	@Autowired
	private MetadataRepository metadataRepository;

	@Autowired
	private SearchManager searchManager;

	@Autowired
	private XmlSerializer xmlSerializer;

	@Autowired
	private IMetadataManager metadataManager;

	@Autowired
	private IMetadataStatus metadataStatus;

	@Autowired
	private IMetadataIndexer metadataIndexer;

	@Autowired
	private MetadataValidationRepository metadataValidationRepository;

	@Autowired
	private IMetadataOperations metadataOperations;

	@Autowired
	private MetadataFileUploadRepository metadataFileUploadRepository;

	@Autowired
	private MetadataStatusRepository metadataStatusRepository;

	@Override
	public void onApplicationEvent(MetadataStatusChanged event) {

		Log.trace(Geonet.DATA_MANAGER, "Status changed for metadata with id " + event.getMd().getId());

		// Only do something if we are moving to approved
		if (event.getStatus().equalsIgnoreCase(Params.Status.APPROVED)) {
			try {
				AbstractMetadata md = approveWithDraft(event);
				validate(md);
			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

			}
		}
	}

	/**
	 * This needs to be done on a new separated transaction to make sure the
	 * previous one is committed.
	 * 
	 * @param event
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private void validate(AbstractMetadata event) {
		List<String> metadataIds = new LinkedList<String>();
		metadataIds.add(String.valueOf(event.getId()));
		try {
			metadataIndexer.indexMetadata(metadataIds);
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER, "Error validating record with id " + event.getId(), e);
		}
	}

	/**
	 * This needs to be done on a separated transaction to make sure the validation
	 * is done on the right data.
	 * 
	 * @param event
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private AbstractMetadata approveWithDraft(MetadataStatusChanged event) throws NumberFormatException, Exception {
		Log.debug(Geonet.DATA_MANAGER, "Record '" + event.getMd().getUuid() + "' approved.");

		ServiceContext context = ServiceContext.get();
		AbstractMetadata md = event.getMd();
		AbstractMetadata draft = null;

		if (md instanceof MetadataDraft) {
			draft = md;
			md = metadataRepository.findOneByUuid(draft.getUuid());

			// This status should be associated to original record, not draft
			metadataStatus.setStatusExt(context, md.getId(), Integer.valueOf(event.getStatus()), new ISODate(),
					event.getMessage());

		} else if (md instanceof Metadata) {
			draft = metadataDraftRepository.findOneByUuid(md.getUuid());
		}

		if (draft != null) {

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
				Element xmlData = draft.getXmlData(false);
				String changeDate = draft.getDataInfo().getChangeDate().getDateAndTime();

				// Remove draft
				metadataOperations.deleteMetadataOper(context, String.valueOf(draft.getId()), false);
				metadataStatusRepository.deleteAllById_MetadataId(draft.getId());
				xmlSerializer.delete(String.valueOf(draft.getId()), context);

				Log.trace(Geonet.DATA_MANAGER, "Remove draft with id " + draft.getId());
				searchManager.delete(draft.getId() + "");

				if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
					Log.trace(Geonet.DATA_MANAGER, "Updating record " + md.getId() + " with ");
					Log.trace(Geonet.DATA_MANAGER, (new org.jdom.output.XMLOutputter()).outputString(xmlData));
				}

				// Copy contents
				Log.trace(Geonet.DATA_MANAGER, "Update approved record with id " + md.getId());
				md = metadataManager.updateMetadata(context, String.valueOf(md.getId()), xmlData, true, true, true,
						context.getLanguage(), changeDate, true);

				Log.info(Geonet.DATA_MANAGER, "Record approved with draft contents: " + md.getId());

			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER, "Error upgrading from draft record with id " + md.getId(), e);
			}
		}

		return md;
	}
}