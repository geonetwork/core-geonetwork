//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.records.editing.InspireValidatorUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.harvest.event.HarvesterTaskCompletedEvent;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PostHarvestingValidationTask {
    private boolean applyInternalValidation = true;
    private boolean applyRemoteInspireValidation = true;

    /**
     * An XSLT process define on a per schema basis.
     * If the file is not found, no process applied.
     */
    private String xsltPostProcess;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("geonetwork.tasks");

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    MetadataDraftRepository metadataDraftRepository;

    @Autowired
    IMetadataValidator metadataValidator;

    @Autowired
    MetadataValidationRepository validationRepository;

    @Autowired
    InspireValidatorUtils inspireValidatorUtils;

    @Autowired
    SettingManager settingManager;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    public PostHarvestingValidationTask() {
        //
    }


    @EventListener
    @Async
    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void apply(HarvesterTaskCompletedEvent harvesterTaskCompletedEvent) {
        List<Metadata> harvesterRecords = metadataRepository.findAllByHarvestInfo_Uuid(
            harvesterTaskCompletedEvent.getHarvesterId()
        );

        List<Integer> recordIds = harvesterRecords.stream().map(Metadata::getId).collect(Collectors.toList());
        logger.debug("ValidationTask started on {} record(s)",
            recordIds.size());

        harvesterRecords.forEach(metadata -> {
            Instant start = Instant.now();

            logger.debug("ValidationTask / {} / Validation start.",
                metadata.getUuid());

            String schema = metadata.getDataInfo().getSchemaId();
            if (applyInternalValidation) {
                metadataValidator.doValidate(metadata, Geonet.DEFAULT_LANGUAGE);
            }

            if (applyRemoteInspireValidation) {
                if (schema.equals("iso19139")) {    // TODO: Support ISO19115-3
                    runInspireValidation(metadata);
                } else {
                    logger.debug("ValidationTask / {} ({}) / Validation done in {}.",
                        metadata.getUuid(),
                        schema,
                        Duration.between(start, Instant.now()).toMillis());
                }
            }

            if (StringUtils.isNotEmpty(xsltPostProcess)) {
                applyXsltProcess(metadata);
            }

            try {
                metadataIndexer.indexMetadata(new ArrayList<>(
                    List.of(String.valueOf(metadata.getId()))));
            } catch (Exception e) {
                logger.debug("ValidationTask / {} / Error while indexing record: {}",
                    metadata.getUuid(), e.getMessage());
            }
        });
    }

    private void applyXsltProcess(Metadata metadata) {
        Path xslt = schemaManager.getSchemaDir(metadata.getDataInfo().getSchemaId())
            .resolve("process")
            .resolve(xsltPostProcess);

        if (Files.exists(xslt)) {
            try {
                logger.debug("ValidationTask / {} / Applying process {}",
                    metadata.getUuid(), xsltPostProcess);
                Element initial = metadata.getXmlData(false);
                Element transformed = Xml.transform(metadata.getXmlData(false), xslt);
                boolean changed = !transformed.equals(initial);
                if (changed) {
                    metadataRepository.update(metadata.getId(), entity -> entity.setDataAndFixCR(transformed));
                }
            } catch (Exception e) {
                logger.debug("ValidationTask / {} / Error while process {}: {}",
                    metadata.getUuid(), xsltPostProcess, e.getMessage());
            }
        }
    }


    private void runInspireValidation(Metadata metadata) {
        Instant start = Instant.now();

        logger.debug("ValidationTask / {} / INSPIRE validation started.",
            metadata.getUuid());

        String inspireValidatorUrl = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL);
        String inspireValidatorCheckUrl = settingManager.getValue(Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_URL_QUERY);
        if (StringUtils.isEmpty(inspireValidatorUrl)) {
            logger.error("ValidationTask / INSPIRE validator URL is missing. Configure it in the admin console or disable INSPIRE valiation.");
            return;
        }

        if (StringUtils.isEmpty(inspireValidatorCheckUrl)) {
            inspireValidatorCheckUrl = inspireValidatorUrl;
        }

        try {
            Element xml = metadata.getXmlData(false);
            boolean isService = Xml.selectBoolean(xml,
                "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue = 'service'",
                ISO19139SchemaPlugin.allNamespaces.asList());
            // TODO: Should this be configurable?
            String testsuite = isService
                ? "TG version 2.0 - Network services"
                : "TG version 2.0 - Data sets and series";

            InputStream metadataToTest = convertElement2InputStream(xml);

            ServiceContext context = ServiceContext.get();
            String testId = inspireValidatorUtils.submitFile(context, inspireValidatorUrl, inspireValidatorCheckUrl, metadataToTest, testsuite, metadata.getUuid());
            InspireValidationRunnable inspireValidationRunnable =
                new InspireValidationRunnable(context, inspireValidatorUrl, testId, metadata.getId());

            ListeningExecutorService executor = MoreExecutors.newDirectExecutorService();
            CompletableFuture<Void> completed = CompletableFuture.runAsync(inspireValidationRunnable, executor);
            completed.thenRun(() -> logger.debug("ValidationTask / {} / INSPIRE validation {} done in {}s.",
                metadata.getUuid(), testsuite,
                Duration.between(start, Instant.now()).toSeconds()));
        } catch (Exception e) {
            logger.debug("ValidationTask / {} / INSPIRE validation error: {}",
                metadata.getUuid(), e.getMessage());
        }
    }

    private InputStream convertElement2InputStream(Element md)
        throws TransformerFactoryConfigurationError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLOutputter xmlOutput = new XMLOutputter();
        try {
            xmlOutput.output(new Document(md), outputStream);
        } catch (IOException e) {
            logger.error("ValidationTask / Error in conversion of XML document to stream.", e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public boolean isApplyInternalValidation() {
        return applyInternalValidation;
    }

    public void setApplyInternalValidation(boolean applyInternalValidation) {
        this.applyInternalValidation = applyInternalValidation;
    }

    public boolean isApplyRemoteInspireValidation() {
        return applyRemoteInspireValidation;
    }

    public void setApplyRemoteInspireValidation(boolean applyRemoteInspireValidation) {
        this.applyRemoteInspireValidation = applyRemoteInspireValidation;
    }

    public String getXsltPostProcess() {
        return xsltPostProcess;
    }

    public void setXsltPostProcess(String xsltPostProcess) {
        this.xsltPostProcess = xsltPostProcess;
    }
}
