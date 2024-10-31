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

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.events.md.MetadataDraftRemove;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
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
public class DraftRemoved {

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    public void doAfterCommit(MetadataDraftRemove event) {
        Log.trace(Geonet.DATA_MANAGER, "Reindexing non drafted versions of uuid " + event.getMd().getUuid());

        try {
            for (AbstractMetadata md : getRecords(event)) {
                if (!(md instanceof MetadataDraft)) {
                    Log.trace(Geonet.DATA_MANAGER, "Reindexing " + md.getId());
                    try {
                        metadataIndexer.indexMetadata(String.valueOf(md.getId()), DirectIndexSubmittor.INSTANCE, IndexingMode.full);
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, e, e);
                    }
                }
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Couldn't reindex the non drafted versions of " + event.getMd(), e);
        }
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    private List<? extends AbstractMetadata> getRecords(MetadataDraftRemove event) {
        return metadataUtils.findAllByUuid(event.getMd().getUuid());
    }

}
