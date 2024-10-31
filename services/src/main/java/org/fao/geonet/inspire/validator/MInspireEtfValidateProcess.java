package org.fao.geonet.inspire.validator;

import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.api.records.formatters.cache.Key;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.transaction.TransactionStatus;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

@ManagedResource()
public class MInspireEtfValidateProcess implements SelfNaming {

    private final ApplicationContext appContext;
    private final ServiceContext serviceContext;
    private final String url;
    private final String urlQuery;

    private ObjectName probeName;
    private int metadataToAnalyseCount = -1;
    private int metadataAnalysed = 0;
    private int metadataNotInspire = 0;
    private int metadataNotAllowed = 0;
    private int metadataAnalysedInError = 0;
    private long deleteAllDate = Long.MAX_VALUE;
    private long analyseMdDate = Long.MAX_VALUE;


    public MInspireEtfValidateProcess(String catalogueId,
                                      String url, String urlQuery,
                                      ServiceContext serviceContext,
                                      ApplicationContext appContext) {
        this.url = url;
        this.urlQuery = urlQuery;
        this.serviceContext = serviceContext;
        this.appContext = appContext;

        try {
            this.probeName = new ObjectName(String.format(
                "geonetwork-%s:name=batch-etf-inspire,idx=%s",
                catalogueId, this.hashCode()));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @ManagedAttribute
    public int getMetadataToAnalyseCount() {
        return metadataToAnalyseCount;
    }

    @ManagedAttribute
    public int getMetadataAnalysed() {
        return metadataAnalysed;
    }

    @ManagedAttribute
    public int getMetadataNotInspire() {
        return metadataNotInspire;
    }

    @ManagedAttribute
    public int getMetadataNotAllowed() {
        return metadataNotAllowed;
    }

    @ManagedAttribute
    public int getMetadataAnalysedInError() {
        return metadataAnalysedInError;
    }

    @ManagedAttribute
    public long getDeleteAllDate() {
        return deleteAllDate;
    }

    @ManagedAttribute
    public long getAnalyseMdDate() {
        return analyseMdDate;
    }

    @ManagedAttribute
    public ObjectName getObjectName() {
        return this.probeName;
    }

    public void deleteAll() {
        runInNewTransaction("minspireetfvalidate-deleteall", new TransactionTask<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                deleteAllDate = System.currentTimeMillis();
                //urlAnalyser.deleteAll();
                return null;
            }
        });
    }

    public void processMetadata(Set<String> uuids, String mode) throws Exception {
        IMetadataUtils metadataRepository = appContext.getBean(IMetadataUtils.class);
        MetadataValidationRepository metadataValidationRepository = appContext.getBean(MetadataValidationRepository.class);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        InspireValidatorUtils inspireValidatorUtils = appContext.getBean(InspireValidatorUtils.class);
        SchemaManager schemaManager = appContext.getBean(SchemaManager.class);
        DataManager dataManager = appContext.getBean(DataManager.class);
        IMetadataSchemaUtils metadataSchemaUtils = appContext.getBean(IMetadataSchemaUtils.class);

        metadataToAnalyseCount = uuids.size();
        analyseMdDate = System.currentTimeMillis();

        ServiceContext context = serviceContext;

        for (String uuid : uuids) {
            if (!metadataRepository.existsMetadataUuid(uuid)) {
                metadataAnalysed++;
                metadataNotAllowed++;
                continue;
            }

            for (AbstractMetadata metadataRecord : metadataRepository.findAllByUuid(uuid)) {
                try {
                    if (!accessManager.canEdit(serviceContext, String.valueOf(metadataRecord.getId()))) {
                        metadataAnalysed++;
                        metadataNotAllowed++;
                    } else {
                        runInNewTransaction("minspireetfvalidate-process-metadata", new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                // Evaluate test conditions for INSPIRE test suites to apply to the metadata
                                Map<String, String> testsuiteConditions =
                                    inspireValidatorUtils.calculateTestsuitesToApply(metadataRecord.getDataInfo().getSchemaId(), metadataSchemaUtils);

                                boolean reindexMetadata = false;

                                String mdToValidate = retrieveMetadataToValidate(context, metadataRecord);

                                try {
                                    boolean inspireMetadata = false;

                                    if (StringUtils.isNotEmpty(mdToValidate)) {
                                        for (Map.Entry<String, String> entry : testsuiteConditions.entrySet()) {
                                            boolean applyCondition = false;
                                            try {
                                                // Checks the condition in the original record
                                                applyCondition = Xml.selectBoolean(metadataRecord.getXmlData(false),
                                                    entry.getValue(),
                                                    schemaManager.getSchema(metadataRecord.getDataInfo().getSchemaId()).getNamespaces());
                                            } catch (Exception ex) {
                                                Log.error(API.LOG_MODULE_NAME, String.format("Error checking INSPIRE rule %s to apply to metadata: %s",
                                                    entry.getKey(), metadataRecord.getUuid()), ex);
                                            }

                                            if (applyCondition) {

                                                String testId = null;
                                                String getRecordByIdUrl = null;
                                                if (StringUtils.isEmpty(mode)) {
                                                    testId = inspireValidatorUtils.submitFile(serviceContext, url, urlQuery,
                                                        new ByteArrayInputStream(mdToValidate.getBytes()), entry.getKey(), metadataRecord.getUuid());
                                                } else {
                                                    String portal = null;
                                                    if (!NodeInfo.DEFAULT_NODE.equals(mode)) {
                                                        Source source = appContext.getBean(SourceRepository.class).findOneByUuid(mode);
                                                        if (source == null) {
                                                            metadataAnalysedInError++;
                                                            Log.warning(API.LOG_MODULE_NAME, String.format(
                                                                "Portal %s not found. There is no CSW endpoint at this URL " +
                                                                    "that we can send to the validator.", mode));
                                                        }
                                                        portal = mode;
                                                    } else {
                                                        portal = NodeInfo.DEFAULT_NODE;
                                                    }

                                                    if (portal  != null) {
                                                        getRecordByIdUrl = String.format(
                                                            "%s%s/eng/csw?SERVICE=CSW&REQUEST=GetRecordById&VERSION=2.0.2&" +
                                                                "OUTPUTSCHEMA=%s&ELEMENTSETNAME=full&ID=%s",
                                                            appContext.getBean(SettingManager.class).getBaseURL(),
                                                            portal,
                                                            ISO19139Namespaces.GMD.getURI(),
                                                            metadataRecord.getUuid());
                                                        testId = inspireValidatorUtils.submitUrl(serviceContext, url, urlQuery, getRecordByIdUrl, entry.getKey(), metadataRecord.getUuid());
                                                    }
                                                }
                                                if (testId != null) {

                                                    inspireValidatorUtils.waitUntilReady(serviceContext, url, testId);

                                                    String reportUrl = inspireValidatorUtils.getReportUrl(url, testId);
                                                    String reportXmlUrl = InspireValidatorUtils.getReportUrlXML(url, testId);
                                                    String reportXml = inspireValidatorUtils.retrieveReport(serviceContext, reportXmlUrl);

                                                    String validationStatus = inspireValidatorUtils.isPassed(serviceContext, url, testId);

                                                    MetadataValidationStatus metadataValidationStatus =
                                                        inspireValidatorUtils.calculateValidationStatus(validationStatus);

                                                    MetadataValidation metadataValidation = new MetadataValidation()
                                                        .setId(new MetadataValidationId(metadataRecord.getId(), "inspire"))
                                                        .setStatus(metadataValidationStatus).setRequired(false)
                                                        .setReportUrl(reportUrl).setReportContent(reportXml);

                                                    metadataValidationRepository.save(metadataValidation);

                                                    //new RecordValidationTriggeredEvent(record.getId(),
                                                    //    ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                                                    //    metadataValidation.getStatus().getCode()).publish(appContext);

                                                    reindexMetadata = true;
                                                    inspireMetadata = true;
                                                }
                                            }
                                        }
                                    }

                                    if (!inspireMetadata) {
                                        metadataNotInspire++;

                                        MetadataValidation metadataValidation = new MetadataValidation()
                                            .setId(new MetadataValidationId(metadataRecord.getId(), "inspire"))
                                            .setStatus(MetadataValidationStatus.DOES_NOT_APPLY).setRequired(false);

                                        metadataValidationRepository.save(metadataValidation);

                                        //new RecordValidationTriggeredEvent(record.getId(),
                                        //    ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                                        //    metadataValidation.getStatus().getCode()).publish(appContext);

                                        reindexMetadata = true;
                                    }

                                    if (reindexMetadata) {
                                        dataManager.indexMetadata(metadataRecord.getId() + "", DirectIndexSubmittor.INSTANCE);
                                    }

                                } catch (Exception ex) {
                                    metadataAnalysedInError++;
                                    Log.error(API.LOG_MODULE_NAME,
                                        String.format("Error validating metadata %s in INSPIRE validator: %s",
                                            metadataRecord.getUuid(), ex.getMessage()), ex);
                                }

                                metadataAnalysed++;

                                return null;
                            }
                        });
                    }

                } catch (Exception ex) {
                    metadataAnalysedInError++;
                    Log.error(API.LOG_MODULE_NAME,
                        String.format("Error validating metadata %s in INSPIRE validator: %s",
                            metadataRecord.getUuid(), ex.getMessage()), ex);
                }


            }
        }
    }

    private final void runInNewTransaction(String name, TransactionTask<Object> transactionTask) {
        TransactionManager.runInTransaction(name, appContext, CREATE_NEW, ALWAYS_COMMIT, false, transactionTask);
    }


    /**
     * Returns the metadata to validate in INSPIRE validator:
     * - For iso19139 schema returns the iso19139 xml.
     * - For other schemas uses the iso19139 formatter to convert it,
     * otherwise if not available an iso19139 formatter returns null.
     *
     * @param context
     * @param metadataRecord
     * @return Metadata to validate or null if can't be converted to iso19139 format.
     */
    private String retrieveMetadataToValidate(ServiceContext context, AbstractMetadata metadataRecord) {
        String mdToValidate = null;

        if (!metadataRecord.getDataInfo().getSchemaId().equals(ISO19139SchemaPlugin.IDENTIFIER)) {
            try {
                Key key = new Key(metadataRecord.getId(), "eng", FormatType.xml, "iso19139", true, FormatterWidth._100);

                final FormatterApi.FormatMetadata formatMetadata =
                    new FormatterApi().new FormatMetadata(context, key, null);
                final byte[] data = formatMetadata.call().data;
                mdToValidate = new String(data, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                Log.error(API.LOG_MODULE_NAME,
                    String.format("Error converting metadata %s to ISO19139 for INSPIRE validator: %s",
                        metadataRecord.getUuid(), ex.getMessage()), ex);
            }
        } else {
            mdToValidate = metadataRecord.getData();
        }

        return mdToValidate;
    }

}
