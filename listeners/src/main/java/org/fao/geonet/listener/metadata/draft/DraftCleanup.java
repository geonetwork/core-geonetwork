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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.events.md.MetadataRemove;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * If an approved metadata gets removed, remove all draft associated to it.
 * <p>
 * This doesn't need to be disabled if no draft is used, as it only removes
 * drafts.
 *
 * @author delawen
 */
@Component
public class DraftCleanup {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Autowired
    private DraftUtilities draftUtilities;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doAfterCommit(MetadataRemove event) {
        Log.trace(Geonet.DATA_MANAGER,
            "A metadata has been removed. Cleanup associated drafts of " + event.getSource());
        try {
            List<MetadataDraft> toRemove = metadataDraftRepository
                .findAll((Specification<MetadataDraft>) MetadataSpecs.hasMetadataUuid(event.getMd().getUuid()));

            for (MetadataDraft md : toRemove) {
                draftUtilities.removeDraft(md);
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't clean up associated drafts of " + event.getSource(), e);
        }

        Log.trace(Geonet.DATA_MANAGER, "Finished cleaning up of " + event.getSource());
    }
}
