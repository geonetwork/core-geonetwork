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
 *
 */
public class InspireValidationRunnable implements Runnable {
    private String testId;
    private String endPoint;
    private int mdId;
    private ServiceContext context;

    public InspireValidationRunnable(ServiceContext context,
                                     String endPoint, String testId, int mdId) {
        this.context = context;
        this.testId = testId;
        this.mdId = mdId;
        this.endPoint = endPoint;
    }

    public void run() {
        TransactionManager.runInTransaction("inspire-validation", ApplicationContextHolder.get(),
            CREATE_NEW,  ALWAYS_COMMIT, false, new TransactionTask<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                    InspireValidatorUtils inspireValidatorUtils =
                        ApplicationContextHolder.get().getBean(InspireValidatorUtils.class);

                    MetadataValidationRepository metadataValidationRepository  =
                        ApplicationContextHolder.get().getBean(MetadataValidationRepository.class);

                    // Waits until the validation result is available
                    inspireValidatorUtils.waitUntilReady(context, endPoint, testId);

                    String reportUrl = inspireValidatorUtils.getReportUrl(endPoint, testId);
                    String reportXmlUrl = inspireValidatorUtils.getReportUrlXML(endPoint, testId);
                    String reportXml = inspireValidatorUtils.retrieveReport(context, reportXmlUrl);

                    String validationStatus = inspireValidatorUtils.isPassed(context, endPoint, testId);

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
    }
}
