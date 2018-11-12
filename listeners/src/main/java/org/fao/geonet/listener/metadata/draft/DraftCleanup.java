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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataFileUpload_;
import org.fao.geonet.events.md.MetadataRemove;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

/**
 * 
 * If an approved metadata gets removed, remove all draft associated to it.
 * 
 * This doesn't need to be disabled if no draft is used, as it only removes
 * drafts.
 * 
 * @author delawen
 *
 */
@Service
public class DraftCleanup implements ApplicationListener<MetadataRemove> {
    
	@Autowired
	private MetadataDraftRepository metadataDraftRepository;

	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private XmlSerializer xmlSerializer;

	@Autowired
	private IMetadataOperations metadataOperations;
	
	@Autowired
	private MetadataFileUploadRepository metadataFileUploadRepository;
	
	@Autowired
	private MetadataRatingByIpRepository metadataRatingByIpRepository;
	
	@Autowired
	private MetadataValidationRepository metadataValidationRepository;
	
	@Autowired
	private MetadataStatusRepository metadataStatusRepository;
	
	@Autowired
	private UserSavedSelectionRepository userSavedSelectionRepository;

	@Override
	@Transactional(value=TxType.REQUIRES_NEW)
	public void onApplicationEvent(MetadataRemove event) {
		List<MetadataDraft> toRemove = metadataDraftRepository
				.findAll((Specification<MetadataDraft>) MetadataSpecs.hasMetadataUuid(event.getMd().getUuid()));
		
		for(MetadataDraft md : toRemove) {
			remove(md);
		}
	}

	private void remove(MetadataDraft metadata) {
		try {
			ServiceContext context = ServiceContext.get();
			
     		//Remove related data
			metadataOperations.deleteMetadataOper(context, String.valueOf(metadata.getId()), false);
			metadataRatingByIpRepository.deleteAllById_MetadataId(metadata.getId());
			metadataValidationRepository.deleteAllById_MetadataId(metadata.getId());
			metadataStatusRepository.deleteAllById_MetadataId(metadata.getId());
			userSavedSelectionRepository.deleteAllByUuid(metadata.getUuid());

			// Logical delete for metadata file uploads
			PathSpec<MetadataFileUpload, String> deletedDatePathSpec = new PathSpec<MetadataFileUpload, String>() {
				@Override
				public javax.persistence.criteria.Path<String> getPath(Root<MetadataFileUpload> root) {
					return root.get(MetadataFileUpload_.deletedDate);
				}
			};
			metadataFileUploadRepository.createBatchUpdateQuery(deletedDatePathSpec, new ISODate().toString(),
					MetadataFileUploadSpecs.isNotDeletedForMetadata(metadata.getId()));

			// --- remove metadata
			xmlSerializer.delete(String.valueOf(metadata.getId()), context);

			searchManager.delete(metadata.getId() + "");
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER, e);
		}
	}
}