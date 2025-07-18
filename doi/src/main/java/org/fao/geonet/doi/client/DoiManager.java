//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.doi.client;

import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.ApplicableSchematron;
import org.fao.geonet.kernel.SchematronValidator;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SavedQuery;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.doi.client.DoiMedraClient.MEDRA_SEARCH_KEY;

/**
 * Class to register/unregister DOIs using the Datacite Metadata Store (MDS) API.
 * <p>
 * See <a href="https://support.datacite.org/docs/mds-api-guide">...</a>
 *
 */
public class DoiManager {
    private static final String DOI_ADD_XSL_PROCESS = "process/doi-add.xsl";
    private static final String DOI_REMOVE_XSL_PROCESS = "process/doi-remove.xsl";
    public static final String DATACITE_XSL_CONVERSION_FILE = "formatter/datacite/view.xsl";
    public static final String DATACITE_MEDRA_XSL_CONVERSION_FILE = "formatter/eu-po-doi/view.xsl";
    public static final String DOI_ID_PARAMETER = "doiId";
    public static final String DOI_DEFAULT_URL = "https://doi.org/";
    public static final String DOI_DEFAULT_PATTERN = "{{uuid}}";

    private final SettingManager sm;
    private final BaseMetadataSchemaUtils schemaUtils;
    private final BaseMetadataManager metadataManager;
    private final BaseMetadataUtils metadataUtils;
    private final SchematronValidator validator;
    private final DoiBuilder doiBuilder;
    private final SchematronRepository schematronRepository;


    public DoiManager(final SettingManager sm, final BaseMetadataSchemaUtils schemaUtils,
                      final BaseMetadataManager metadataManager, final BaseMetadataUtils metadataUtils,
                      final SchematronValidator validator, final DoiBuilder doiBuilder,
                      final SchematronRepository schematronRepository) {
        this.sm = sm;
        this.schemaUtils = schemaUtils;
        this.metadataManager = metadataManager;
        this.metadataUtils = metadataUtils;
        this.validator = validator;
        this.doiBuilder = doiBuilder;
        this.schematronRepository = schematronRepository;

    }

    private IDoiClient createDoiClient(DoiServer doiServer) {
        boolean isMedra = isMedraServer(doiServer);
        return isMedra ?
            new DoiMedraClient(doiServer.getUrl(), doiServer.getUsername(), doiServer.getPassword(), doiServer.getPublicUrl()) :
            new DoiDataciteClient(doiServer.getUrl(), doiServer.getUsername(), doiServer.getPassword(), doiServer.getPublicUrl());
    }

    public String checkDoiUrl(DoiServer doiServer, AbstractMetadata metadata) throws DoiClientException {
        checkInitialised(doiServer);
        checkCanHandleMetadata(doiServer, metadata);

        return doiBuilder.create(doiServer.getPattern(), doiServer.getPrefix(), metadata);
    }

    public Map<String, Boolean> check(ServiceContext serviceContext, DoiServer doiServer, AbstractMetadata metadata, Element dataciteMetadata) throws Exception {
        Map<String, Boolean> conditions = new HashMap<>();
        checkInitialised(doiServer);
        checkCanHandleMetadata(doiServer, metadata);
        conditions.put(DoiConditions.API_CONFIGURED, true);

        IDoiClient doiClient = createDoiClient(doiServer);
        String doi = doiBuilder.create(doiServer.getPattern(), doiServer.getPrefix(), metadata);
        checkPreConditions(doiClient, metadata, doi);
        conditions.put(DoiConditions.RECORD_IS_PUBLIC, true);
        conditions.put(DoiConditions.STANDARD_SUPPORT, true);


        // ** Convert to DataCite format
        Element dataciteFormatMetadata =
            dataciteMetadata == null ?
                convertXmlToDataCiteFormat(doiServer, metadata.getDataInfo().getSchemaId(),
                    metadata.getXmlData(false), doi) : dataciteMetadata;
        checkPreConditionsOnDataCite(doiClient, metadata, doi, dataciteFormatMetadata, serviceContext.getLanguage());
        conditions.put(DoiConditions.DATACITE_FORMAT_IS_VALID, true);
        return conditions;
    }

    public Map<String, String> register(ServiceContext context, DoiServer doiServer, AbstractMetadata metadata) throws Exception {
        Map<String, String> doiInfo = new HashMap<>(3);
        // The new DOI for this record
        String doi = doiBuilder.create(doiServer.getPattern(), doiServer.getPrefix(), metadata);
        doiInfo.put("doi", doi);

        // The record in datacite format
        Element dataciteFormatMetadata =
            convertXmlToDataCiteFormat(doiServer, metadata.getDataInfo().getSchemaId(),
                metadata.getXmlData(false), doi);

        try {
            check(context, doiServer, metadata, dataciteFormatMetadata);
        } catch (ResourceAlreadyExistException ignore) {
            // Update DOI
            doiInfo.put("update", "true");
        } catch (Exception e) {
            throw e;
        }

        IDoiClient doiClient = createDoiClient(doiServer);
        createDoi(context, doiClient, doiServer, metadata, doiInfo, dataciteFormatMetadata);
        checkDoiCreation(metadata, doiInfo);

        return doiInfo;
    }

    /**
     * Check DOI conditions on current records.
     *
     * @param metadata
     * @param doi
     * @throws DoiClientException
     * @throws IOException
     * @throws JDOMException
     */
    private void checkPreConditions(IDoiClient doiClient, AbstractMetadata metadata, String doi) throws DoiClientException, IOException, JDOMException, ResourceAlreadyExistException {
        // Record MUST be public
        AccessManager am = ApplicationContextHolder.get().getBean(AccessManager.class);
        boolean visibleToAll = false;
        try {
            visibleToAll = am.isVisibleToAll(metadata.getId() + "");
        } catch (Exception e) {
            throw new DoiClientException(String.format(
                "Failed to check if record '%s' is visible to all for DOI creation." +
                    " Error is %s.",
                metadata.getUuid(), e.getMessage()))
                .withMessageKey("exception.doi.failedVisibilityCheck")
                .withDescriptionKey("exception.doi.failedVisibilityCheck.description",
                    new String[]{metadata.getUuid(), e.getMessage()});
        }

        if (!visibleToAll) {
            throw new DoiClientException(String.format(
                "Record '%s' is not public and we cannot request a DOI for such a record. Publish this record first.",
                metadata.getUuid()))
                .withMessageKey("exception.doi.recordNotPublic")
                .withDescriptionKey("exception.doi.recordNotPublic.description", new String[]{metadata.getUuid()});
        }

        // Record MUST not contains a DOI
        try {
            String currentDoi = metadataUtils.getDoi(metadata.getUuid());
            if (StringUtils.isNotEmpty(currentDoi)) {
                // Current doi does not match the one going to be inserted. This is odd
                String newDoi = doiClient.createPublicUrl(doi);
                if (!currentDoi.equals(newDoi)) {
                    throw new DoiClientException(String.format(
                        "Record '%s' already contains a DOI <a href='%s'>%s</a> which is not equal " +
                            "to the DOI about to be created (ie. '%s'). " +
                            "Maybe current DOI does not correspond to that record? " +
                            "This may happen when creating a copy of a record having " +
                            "an existing DOI.",
                        metadata.getUuid(), currentDoi, currentDoi, newDoi))
                        .withMessageKey("exception.doi.resourcesContainsDoiNotEqual")
                        .withDescriptionKey("exception.doi.resourcesContainsDoiNotEqual.description", new String[]{metadata.getUuid(), currentDoi, currentDoi, newDoi});
                }

                throw new ResourceAlreadyExistException(String.format(
                    "Record '%s' already contains a DOI. The DOI is <a href='%s'>%s</a>. " +
                        "You've to update existing DOI. " +
                        "Remove the DOI reference if it does not apply to that record.",
                    metadata.getUuid(), currentDoi, currentDoi))
                    .withMessageKey("exception.doi.resourceContainsDoi")
                    .withDescriptionKey("exception.doi.resourceContainsDoi.description",
                        new String[]{metadata.getUuid(), currentDoi, currentDoi});
            }
        } catch (ResourceNotFoundException e) {
            final MetadataSchema schema = schemaUtils.getSchema(metadata.getDataInfo().getSchemaId());
            // Schema not supporting DOI extraction and needs to be configured
            // Check bean configuration which should contains something like
            //            <bean class="org.fao.geonet.kernel.schema.SavedQuery">
            //              <property name="id" value="doi-get"/>
            //              <property name="xpath"
            //                value="*//gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString = 'WWW:DOI']/gmd:URL/text()"/>
            //            </bean>
            throw new DoiClientException(String.format(
                "Record '%s' is in schema '%s' and we cannot find a saved query " +
                    "with id '%s' to retrieve the DOI. Error is %s. " +
                    "Check the schema %sSchemaPlugin and add the DOI get query.",
                metadata.getUuid(), schema.getName(),
                SavedQuery.DOI_GET, e.getMessage(),
                schema.getName()))
                .withMessageKey("exception.doi.missingSavedquery")
                .withDescriptionKey("exception.doi.missingSavedquery.description",
                    new String[]{metadata.getUuid(), schema.getName(),
                        SavedQuery.DOI_GET, e.getMessage(),
                        schema.getName()});
        }
    }


    /**
     * Check conditions on DataCite side.
     *
     * @param metadata
     * @param doi
     * @param dataciteMetadata
     * @param language
     */
    private void checkPreConditionsOnDataCite(IDoiClient doiClient, AbstractMetadata metadata, String doi, Element dataciteMetadata, String language) throws DoiClientException, ResourceAlreadyExistException {
        // * DataCite API is up an running ?
        try {
            List<MetadataValidation> validations = new ArrayList<>();
            List<ApplicableSchematron> applicableSchematron = Lists.newArrayList();
            ApplicableSchematron schematron =
                new ApplicableSchematron(
                    SchematronRequirement.REQUIRED,
                    schematronRepository.findOneByFileAndSchemaName("schematron-rules-datacite.xsl",
                        metadata.getDataInfo().getSchemaId()));
            applicableSchematron.add(schematron);
            Element rules = validator.applyCustomSchematronRules(metadata.getDataInfo().getSchemaId(),
                metadata.getId(),
                metadata.getXmlData(false),
                language,
                validations,
                applicableSchematron);


            List<Namespace> namespaces = new ArrayList<>();
            namespaces.add(Geonet.Namespaces.GEONET);
            namespaces.add(Geonet.Namespaces.SVRL);
            List<?> failures = Xml.selectNodes(rules, ".//svrl:failed-assert/svrl:text/*", namespaces);
            StringBuilder message = new StringBuilder();
            if (!failures.isEmpty()) {
                message.append("<ul>");
                failures.forEach(f -> message.append("<li>").append(((Element) f).getTextNormalize()).append("</li>"));
                message.append("</ul>");

                throw new DoiClientException(String.format(
                    "Record '%s' is not conform with DataCite format. %d mandatory field(s) missing. %s",
                    metadata.getUuid(), failures.size(), message))
                    .withMessageKey("exception.doi.recordNotConformantMissingInfo")
                    .withDescriptionKey("exception.doi.recordNotConformantMissingInfo.description",
                        new String[]{metadata.getUuid(), String.valueOf(failures.size()), message.toString()});
            }
        } catch (IOException | JDOMException e) {
            throw new DoiClientException(String.format(
                "Record '%s' is not conform with DataCite validation rules for mandatory fields. Error is: %s. " +
                    "Required fields in DataCite are: identifier, creators, titles, publisher, publicationYear, resourceType. " +
                    "<a href='%sapi/records/%s/formatters/datacite?output=xml'>Check the DataCite format output</a> and " +
                    "adapt the record content to add missing information.",
                metadata.getUuid(), e.getMessage(), sm.getNodeURL(), metadata.getUuid()))
                .withMessageKey("exception.doi.recordNotConformantMissingMandatory")
                .withDescriptionKey("exception.doi.recordNotConformantMissingMandatory.description",
                    new String[]{metadata.getUuid(), e.getMessage(), sm.getNodeURL(), metadata.getUuid()});
        }

        // XSD validation
        try {
            Xml.validate(dataciteMetadata);
        } catch (Exception e) {
            throw new DoiClientException(String.format(
                "Record '%s' converted to DataCite format is invalid. Error is: %s. " +
                    "Required fields in DataCite are: identifier, creators, titles, publisher, publicationYear, resourceType. " +
                    "<a href='%sapi/records/%s/formatters/datacite?output=xml'>Check the DataCite format output</a> and " +
                    "adapt the record content to add missing information.",
                metadata.getUuid(), e.getMessage(), sm.getNodeURL(), metadata.getUuid()))
                .withMessageKey("exception.doi.recordInvalid")
                .withDescriptionKey("exception.doi.recordInvalid.description",
                    new String[]{metadata.getUuid(), e.getMessage(), sm.getNodeURL(), metadata.getUuid()});
        }

        // * MDS / DOI does not exist already
        // curl -i --user username:password https://mds.test.datacite.org/doi/10.5072/GN
        // Return 404
        final String doiResponse = doiClient.retrieveDoi(doi);
        if (doiResponse != null) {
            throw new ResourceAlreadyExistException(String.format(
                "Record '%s' looks to be already published on DataCite with DOI <a href='%s'>'%s'</a>. DOI on Datacite point to: <a href='%s'>%s</a>. " +
                    "If the DOI is not correct, remove it from the record and ask for a new one.",
                metadata.getUuid(),
                doiClient.createUrl("doi") + "/" + doi,
                doi, doi, doiResponse))
                .withMessageKey("exception.doi.resourceAlreadyPublished")
                .withDescriptionKey("exception.doi.resourceAlreadyPublished.description", new String[]{metadata.getUuid(),
                    doiClient.createUrl("doi") + "/" + doi,
                    doi, doi, doiResponse});

        }
        // TODO: Could be relevant at some point to return states (draft/findable)

        // * MDS / Metadata does not exist either
        // curl -i --user username:password https://mds.test.datacite.org/metadata/10.5072/GN
        // Return 404
    }

    /**
     * Use the DataCite API to register the new DOI.
     *
     * @param context
     * @param metadata
     */
    private void createDoi(ServiceContext context, IDoiClient doiClient, DoiServer doiServer,
                           AbstractMetadata metadata, Map<String, String> doiInfo, Element dataciteMetadata) throws Exception {
        // * Now, let's create the DOI
        // picking a DOI name,

        // The identifier in the DataCite XML metadata file can be left empty, as the value from the PUT URL will be used.
        // curl -H "Content-Type: application/xml;charset=UTF-8" -X POST -i --user username:password -d @/tmp/GN.xml https://mds.test.datacite.org/metadata/10.5072/GN

        // ** Validate output ? XSD
        // ** POST metadata
        // 201 Created: operation successful for Datacite,
        // 200 Ok: operation successful for Medra,
        doiClient.createDoiMetadata(doiInfo.get("doi"), Xml.getString(dataciteMetadata));

        // Register the URL for Datacite
        String landingPage = doiServer.getLandingPageTemplate().replace(
            DOI_DEFAULT_PATTERN, metadata.getUuid());
        doiInfo.put("doiLandingPage", landingPage);
        doiInfo.put("doiUrl",
            doiClient.createPublicUrl(doiInfo.get("doi")));
        doiClient.createDoi(doiInfo.get("doi"), landingPage);


        // Add the DOI in the record
        Element recordWithDoi = setDOIValue(doiClient, doiInfo.get("doi"), metadata.getDataInfo().getSchemaId(), metadata.getXmlData(false));
        // Update the published copy
        //--- needed to detach md from the document

        metadataManager.updateMetadata(context, metadata.getId() + "", recordWithDoi, false, true,
            context.getLanguage(), new ISODate().toString(), true, IndexingMode.full);
    }


    /**
     * TODO: Check that the DOI is properly created.
     *
     * @param metadata
     * @param doi
     */
    private void checkDoiCreation(AbstractMetadata metadata, Map<String, String> doi) {
        // Check it is available on DataCite Metadata Store
        // curl -X GET --user USER https://mds.test.datacite.org/metadata/10.5072/GN2
        // Check it is in the record
    }


    public void unregisterDoi(DoiServer doiServer, AbstractMetadata metadata, ServiceContext context) throws DoiClientException, ResourceNotFoundException {
        checkInitialised(doiServer);
        checkCanHandleMetadata(doiServer, metadata);

        IDoiClient doiClient = createDoiClient(doiServer);
        final String doi = doiBuilder.create(doiServer.getPattern(), doiServer.getPrefix(), metadata);
        final String doiResponse = doiClient.retrieveDoi(doi);
        if (doiResponse == null) {
            throw new ResourceNotFoundException(String.format(
                "Record '%s' is not available on DataCite. DOI '%s' does not exist.",
                metadata.getUuid(), doi));
        }


        try {
            Element md = metadata.getXmlData(false);
            String doiUrl = metadataUtils.getDoi(metadata.getUuid());

            doiClient.deleteDoiMetadata(doi);
            doiClient.deleteDoi(doi);

            Element recordWithoutDoi = removeDOIValue(doiUrl, metadata.getDataInfo().getSchemaId(), md);

            metadataManager.updateMetadata(context, metadata.getId() + "", recordWithoutDoi, false, true,
                context.getLanguage(), new ISODate().toString(), true, IndexingMode.full);
        } catch (Exception ex) {
            throw new DoiClientException(String.format(
                "Error unregistering DOI: %s",
                ex.getMessage()))
                .withMessageKey("exception.doi.serverErrorUnregister")
                .withDescriptionKey("exception.doi.serverErrorUnregister.description", new String[]{ex.getMessage()});
        }
    }

    /**
     * Sets the DOI URL value in the metadata record using the process DOI_ADD_XSL_PROCESS.
     */
    public Element setDOIValue(IDoiClient doiClient, String doi, String schema, Element md) throws Exception {
        Path styleSheet = schemaUtils.getSchemaDir(schema).resolve(DOI_ADD_XSL_PROCESS);
        boolean exists = Files.exists(styleSheet);
        if (!exists) {
            String message = String.format("To create a DOI, the schema has to defined how to insert a DOI in the record. The schema_plugins/%s/process/%s was not found. Create the XSL transformation.",
                schema, DOI_ADD_XSL_PROCESS);

            throw new DoiClientException(String.format(
                "Error creating  DOI: %s",
                message))
                .withMessageKey("exception.doi.serverErrorCreate")
                .withDescriptionKey("exception.doi.serverErrorCreate.description", new String[]{message});
        }

        String doiPublicUrl = doiClient.createPublicUrl("");

        Map<String, Object> params = new HashMap<>(1);
        params.put("doi", doi);
        params.put("doiProxy", doiPublicUrl);
        return Xml.transform(md, styleSheet, params);
    }

    /**
     * Sets the DOI URL value in the metadata record using the process DOI_ADD_XSL_PROCESS.
     */
    public Element removeDOIValue(String doi, String schema, Element md) throws Exception {
        Path styleSheet = schemaUtils.getSchemaDir(schema).resolve(DOI_REMOVE_XSL_PROCESS);
        boolean exists = Files.exists(styleSheet);
        if (!exists) {
            String message = String.format("To remove a DOI, the schema has to defined how to remove a DOI in the record. The schema_plugins/%s/process/%s was not found. Create the XSL transformation.",
                schema, DOI_REMOVE_XSL_PROCESS);

            throw new DoiClientException(String.format(
                "Error deleting  DOI: %s",
                message))
                .withMessageKey("exception.doi.serverErrorDelete")
                .withDescriptionKey("exception.doi.serverErrorDelete.description", new String[]{message});

        }

        Map<String, Object> params = new HashMap<>(1);
        params.put("doi", doi);
        return Xml.transform(md, styleSheet, params);
    }

    /**
     * Convert a metadata record to the DataCite Metadata Schema.
     * See http://schema.datacite.org/.
     *
     * @return The record converted into the DataCite format.
     * @throws Exception if there is no conversion available.
     */
    private Element convertXmlToDataCiteFormat(DoiServer doiServer, String schema, Element md, String doi) throws Exception {
        final Path styleSheet = schemaUtils.getSchemaDir(schema).resolve(
            isMedraServer(doiServer) ? DATACITE_MEDRA_XSL_CONVERSION_FILE : DATACITE_XSL_CONVERSION_FILE);
        final boolean exists = Files.exists(styleSheet);
        if (!exists) {
            String message = String.format("To create a DOI, the record needs to be converted to the DataCite format (https://schema.datacite.org/). You need to create a formatter for this in schema_plugins/%s/%s. If the standard is a profile of ISO19139, you can simply point to the ISO19139 formatter.",
                schema, DATACITE_XSL_CONVERSION_FILE);

            throw new DoiClientException(String.format(
                "Error creating  DOI: %s",
                message))
                .withMessageKey("exception.doi.serverErrorCreate")
                .withDescriptionKey("exception.doi.serverErrorCreate.description", new String[]{message});
        }

        Map<String, Object> params = new HashMap<>();
        params.put(DOI_ID_PARAMETER, doi);
        params.put("doiServerId", doiServer.getId() + "");
        return Xml.transform(md, styleSheet, params);
    }

    private void checkInitialised(DoiServer doiServer) throws DoiClientException {
        final boolean emptyUrl = StringUtils.isEmpty(doiServer.getUrl());
        final boolean emptyUsername = StringUtils.isEmpty(doiServer.getUsername());
        final boolean emptyPassword = StringUtils.isEmpty(doiServer.getPassword());
        final boolean emptyPrefix = StringUtils.isEmpty(doiServer.getPrefix());

        if (emptyUrl ||
            emptyUsername ||
            emptyPassword ||
            emptyPrefix) {
            throw new DoiClientException("DOI server configuration is not complete. Check the DOI server configuration to complete it.")
                .withMessageKey("exception.doi.configurationMissing")
                .withDescriptionKey("exception.doi.configurationMissing.description", new String[]{});

        }
    }

    /**
     * Checks if the DOI server can handle the metadata:
     *  - The DOI server is not publishing metadata for certain metadata group(s) or
     *  - it publishes metadata from the metadata group owner.
     *
     * @param doiServer     The DOI server.
     * @param metadata      The metadata to process.
     * @throws DoiClientException
     */
    private void checkCanHandleMetadata(DoiServer doiServer, AbstractMetadata metadata) throws DoiClientException {
        if (!doiServer.getPublicationGroups().isEmpty()) {
            Integer groupOwner = metadata.getSourceInfo().getGroupOwner();

            if (doiServer.getPublicationGroups().stream().noneMatch(g -> g.getId() == groupOwner)) {
                throw new DoiClientException(
                    String.format("DOI server '%s' can not handle the metadata with UUID '%s'.",
                        doiServer.getName(), metadata.getUuid()))
                    .withMessageKey("exception.doi.serverCanNotHandleRecord")
                    .withDescriptionKey("exception.doi.serverCanNotHandleRecord.description", new String[]{doiServer.getName(), metadata.getUuid()});
            }
        }

    }

    private boolean isMedraServer(DoiServer doiServer) {
        return doiServer.getUrl().contains(MEDRA_SEARCH_KEY);
    }
}
