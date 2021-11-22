/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records;

import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.Arrays;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

/**
 * Task executed when INSPIRE validation report is available for a metadata
 * that stores the validation report in the metadata validation table.
 */
public class InspireValidationRunnable implements Runnable {
    private final String testId;
    private final String endPoint;
    private final int mdId;
    /** Provided service context, cleaned up when task is complete */
    private ServiceContext validationContext;

    /**
     * Schedule INSPIRE validation for later.
     *
     * @param context Validation context, it is the responsibility of this runnable to clean up
     * @param endPoint
     * @param testId
     * @param mdId
     */
    public InspireValidationRunnable(ServiceContext context,
                                     String endPoint, String testId, int mdId) {
        this.validationContext = context;
        this.testId = testId;
        this.mdId = mdId;
        this.endPoint = endPoint;
    }

    public void run() {
        validationContext.setAsThreadLocal();
        try {
            TransactionManager.runInTransaction("inspire-validation", ApplicationContextHolder.get(),
                CREATE_NEW, ALWAYS_COMMIT, false, new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                        InspireValidatorUtils inspireValidatorUtils =
                            ApplicationContextHolder.get().getBean(InspireValidatorUtils.class);

                        MetadataValidationRepository metadataValidationRepository =
                            ApplicationContextHolder.get().getBean(MetadataValidationRepository.class);

                        // Waits until the validation result is available
                        inspireValidatorUtils.waitUntilReady(validationContext, endPoint, testId);

                        String reportUrl = inspireValidatorUtils.getReportUrl(endPoint, testId);
                        String reportXmlUrl = InspireValidatorUtils.getReportUrlXML(endPoint, testId);
                        String reportXml = inspireValidatorUtils.retrieveReport(validationContext, reportXmlUrl);

                        String validationStatus = inspireValidatorUtils.isPassed(validationContext, endPoint, testId);

                        MetadataValidationStatus metadataValidationStatus =
                            inspireValidatorUtils.calculateValidationStatus(validationStatus);

                        MetadataValidation metadataValidation = new MetadataValidation()
                            .setId(new MetadataValidationId(mdId, "inspire"))
                            .setStatus(metadataValidationStatus).setRequired(false)
                            .setReportUrl(reportUrl).setReportContent(reportXml);

                        metadataValidationRepository.save(metadataValidation);

                        DataManager dataManager =
                            ApplicationContextHolder.get().getBean(DataManager.class);

                        dataManager.indexMetadata(new ArrayList<>(Arrays.asList(mdId + "")));

                        return null;
                    }
                });
        } finally {
            validationContext.clearAsThreadLocal();
            validationContext.clear();
        }
    }
}
