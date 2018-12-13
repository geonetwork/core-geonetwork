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
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

/**
 * 
 * When a record gets a status change, check if there is a draft associated to
 * it. If there is, act accordingly (replacing record with draft and/or removing
 * draft).
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
	private IMetadataStatus metadataStatus;

	@Autowired
	private IMetadataIndexer metadataIndexer;
	
	@Autowired
	private DraftUtilities draftUtilities;

	@Override
	public void onApplicationEvent(MetadataStatusChanged event) {

		Log.trace(Geonet.DATA_MANAGER, "Status changed for metadata with id " + event.getMd().getId());

		// Handle draft accordingly to the status change
		// If there is no draft involved, these operations do nothing
		String status = event.getStatus();
		switch (status) {
		case Params.Status.DRAFT:
		case Params.Status.SUBMITTED:
			if (event.getMd() instanceof Metadata) {
				Log.trace(Geonet.DATA_MANAGER,
						"Replacing contents of record (ID=" + event.getMd().getId() + ") with draft, if exists.");
				draftUtilities.replaceMetadataWithDraft(event.getMd());
			}
			break;
		case Params.Status.RETIRED:
		case Params.Status.REJECTED:
			try {
				Log.trace(Geonet.DATA_MANAGER,
						"Removing draft from record (ID=" + event.getMd().getId() + "), if exists.");

				removeDraft(event.getMd());
			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

			}
			break;
		case Params.Status.APPROVED:
			try {
				Log.trace(Geonet.DATA_MANAGER, "Replacing contents of approved record (ID=" + event.getMd().getId()
						+ ") with draft, if exists.");
				AbstractMetadata md = approveWithDraft(event);
				validate(md);
			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER, "Error upgrading status", e);

			}
			break;
		}
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private void removeDraft(AbstractMetadata md) throws Exception {
		draftUtilities.removeDraft(md);
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
			md = draftUtilities.replaceMetadataWithDraft(md, draft);
		}

		return md;
	}
}