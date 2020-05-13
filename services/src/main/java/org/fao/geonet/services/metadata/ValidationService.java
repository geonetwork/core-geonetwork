//=============================================================================
//===	Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.responses.StatusResponse;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Validate metadata records.
 *
 * See 
 */
@Controller("metadata/validate")
@Deprecated
public class ValidationService implements ApplicationContextAware {

    private ApplicationContext context;

    private Map<String, Set<Integer>> report;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    /**
     * Validate a set of records from the selection or from the list of UUIDs provided.
     *
     * Current user MUST own the metadata record.
     *
     * @param uuid One or more UUIDs to validate
     */
    @RequestMapping(value = "/{portal}/{lang}/md.validation",
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    StatusResponse validateRecords(@RequestParam(required = false) String[] uuid) throws Exception {
        ServiceContext serviceContext = ServiceContext.get();
        UserSession session = serviceContext.getUserSession();

        this.report = new HashMap<>();
        this.report.put("records", new HashSet<Integer>());
        this.report.put("validRecords", new HashSet<Integer>());
        this.report.put("notOwnerRecords", new HashSet<Integer>());

        final Set<String> setOfUuidsToValidate;

        if (uuid == null) {
            SelectionManager sm = SelectionManager.getManager(session);
            synchronized (sm.getSelection(SelectionManager.SELECTION_METADATA)) {
                setOfUuidsToValidate = Sets.newHashSet(sm.getSelection(SelectionManager.SELECTION_METADATA));
            }
        } else {
            setOfUuidsToValidate = Sets.newHashSet(Arrays.asList(uuid));
        }


        validateRecords(serviceContext, setOfUuidsToValidate);

        // index records
        DataManager dataMan = context.getBean(DataManager.class);
        BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, this.report.get("records"));
        r.process();

        return new StatusResponse(String.format(
            "%d record(s) validated. %d record(s) valid.",
            this.report.get("records").size(),
            this.report.get("validRecords").size()));
    }


    private void validateRecords(ServiceContext serviceContext,
                                 Set<String> setOfUuidsToValidate) throws Exception {


        IMetadataValidator validator = context.getBean(IMetadataValidator.class);
        AccessManager accessMan = context.getBean(AccessManager.class);

        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        for (String uuid : setOfUuidsToValidate) {
            for(AbstractMetadata record : metadataRepository.findAllByUuid(uuid)) {
	            if (!accessMan.isOwner(serviceContext, String.valueOf(record.getId()))) {
	                this.report.get("notOwnerRecords").add(record.getId());
	            } else {
	                boolean isValid = validator.doValidate(record, serviceContext.getLanguage());
	                if (isValid) {
	                    this.report.get("validRecords").add(record.getId());
	                }
	                this.report.get("records").add(record.getId());

	            }
            }
        }
    }
}
