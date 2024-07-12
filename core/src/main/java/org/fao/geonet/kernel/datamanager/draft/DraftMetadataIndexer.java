//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager.draft;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataIndexer;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DraftMetadataIndexer extends BaseMetadataIndexer implements IMetadataIndexer {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    @Autowired
    EsSearchManager searchManager;

    @Override
    public void init(ServiceContext context, Boolean force) throws Exception {
        super.init(context, force);
        metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
        statusRepository = context.getBean(MetadataStatusRepository.class);
    }

    /**
     * Adds the specific draft related fields.
     *
     * @param fullMd
     */
    protected Multimap<String, Object> addExtraFields(AbstractMetadata fullMd) {
        Multimap<String, Object> extraFields = ArrayListMultimap.create();

        extraFields.putAll(super.addExtraFields(fullMd));

        if (fullMd instanceof MetadataDraft) {
            Log.trace(Geonet.DATA_MANAGER, "We are indexing a draft with uuid " + fullMd.getUuid());
            extraFields.put(Geonet.IndexFieldNames.DRAFT, "y");
        } else {
            MetadataDraft metadataDraft = metadataDraftRepository.findOneByUuid(fullMd.getUuid());
            if (metadataDraft != null) {
                Log.trace(Geonet.DATA_MANAGER,
                    "We are indexing a record with a draft associated with uuid " + fullMd.getUuid());
                extraFields.put(Geonet.IndexFieldNames.DRAFT, "e");
                extraFields.put(Geonet.IndexFieldNames.DRAFT_ID, metadataDraft.getId());

                String status = "";
                String statusDraft = "";

                // get status
                Sort statusSort = Sort.by(Sort.Direction.DESC,
                    MetadataStatus_.changeDate.getName());
                List<MetadataStatus> statuses = statusRepository.findAllByMetadataIdAndByType(fullMd.getId(), StatusValueType.workflow, statusSort);
                if (!statuses.isEmpty()) {
                    MetadataStatus stat = statuses.get(0);
                    status = String.valueOf(stat.getStatusValue().getName());
                }

                // get status of draft
                statuses = statusRepository.findAllByMetadataIdAndByType(metadataDraft.getId(), StatusValueType.workflow, statusSort);
                if (!statuses.isEmpty()) {
                    MetadataStatus stat = statuses.get(0);
                    statusDraft = String.valueOf(stat.getStatusValue().getName());
                }

                extraFields.put(Geonet.IndexFieldNames.STATUS_WORKFLOW, status + "-" + statusDraft);

            } else {
                Log.trace(Geonet.DATA_MANAGER,
                    "We are indexing a record with no draft associated with uuid " + fullMd.getUuid());
                extraFields.put(Geonet.IndexFieldNames.DRAFT, "n");

                // get status
                Sort statusSort = Sort.by(Sort.Direction.DESC,
                    MetadataStatus_.changeDate.getName());
                List<MetadataStatus> statuses = statusRepository.findAllByMetadataIdAndByType(fullMd.getId(), StatusValueType.workflow, statusSort);
                if (!statuses.isEmpty()) {
                    MetadataStatus stat = statuses.get(0);
                    String status = String.valueOf(stat.getStatusValue().getName());
                    extraFields.put(Geonet.IndexFieldNames.STATUS_WORKFLOW, status);
                }
            }
        }
        return extraFields;
    }
}
