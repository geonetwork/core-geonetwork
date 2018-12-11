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

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.events.md.MetadataPublished;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import jeeves.server.context.ServiceContext;

/**
 * 
 * When a record with workflow enabled gets published, status will automatically
 * change to approved
 * 
 * @author delawen
 *
 */
@Service
public class PublishRecord implements ApplicationListener<MetadataPublished> {

	@Autowired
	private IMetadataStatus metadataStatus;

	@Override
	public void onApplicationEvent(MetadataPublished event) {

		Log.trace(Geonet.DATA_MANAGER, "Metadata with id " + event.getMd().getId() + " published.");

		try {
			// Only do something if the workflow is enabled
			if (metadataStatus.getStatus(event.getMd().getId()) != null) {
				changeToApproved(event.getMd());
			}
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER, "Error upgrading workflow", e);
		}
	}

	/**
	 * This needs to be done on a new separated transaction to make sure the
	 * previous one is committed.
	 * 
	 * @param event
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private void changeToApproved(AbstractMetadata md) throws NumberFormatException, Exception {

		ServiceContext context = ServiceContext.get();

		// This status should be associated to original record, not draft
		metadataStatus.setStatusExt(context, md.getId(), Integer.valueOf(Params.Status.APPROVED), new ISODate(),
				"Record published.");

		Log.trace(Geonet.DATA_MANAGER, "Metadata with id " + md.getId() + " automatically approved due to publishing.");

	}

}