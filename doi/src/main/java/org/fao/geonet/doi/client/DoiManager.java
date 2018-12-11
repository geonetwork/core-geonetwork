//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to register/unregister DOIs using the Datacite Metadata Store (MDS) API.
 *
 * See https://support.datacite.org/docs/mds-api-guide
 *
 * @author Jose García
 * @author Francois Prunayre
 */
public class DoiManager {
    private static final String DOI_ADD_XSL_PROCESS = "process/doi-add.xsl";
    private static final String DOI_REMOVE_XSL_PROCESS = "process/doi-remove.xsl";
    public static final String DATACITE_XSL_CONVERSION_FILE = "formatter/datacite/view.xsl";
    public static final String DOI_PREFIX_PARAMETER = "doiPrefix";
    public static final String HTTPS_DOI_ORG = "https://doi.org/";

    private DoiClient client;
    private String doiPrefix;
    private String landingPageTemplate;
    private boolean initialised = false;

    DataManager dm;
    SettingManager sm;
    BaseMetadataSchemaUtils schemaUtils;

    public static final String DOI_GET_SAVED_QUERY = "doi-get";

    public DoiManager() {
        sm = ApplicationContextHolder.get().getBean(SettingManager.class);
        dm = ApplicationContextHolder.get().getBean(DataManager.class);
        schemaUtils = ApplicationContextHolder.get().getBean(BaseMetadataSchemaUtils.class);

        loadConfig();
    }

    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Check parameters and build the client.
     *
     */
    public void loadConfig() {
        initialised = false;
        if (sm != null) {

            String serverUrl = sm.getValue(DoiSettings.SETTING_PUBLICATION_DOI_DOIURL);
            String username = sm.getValue(DoiSettings.SETTING_PUBLICATION_DOI_DOIUSERNAME);
            String password = sm.getValue(DoiSettings.SETTING_PUBLICATION_DOI_DOIPASSWORD);

            doiPrefix = sm.getValue(DoiSettings.SETTING_PUBLICATION_DOI_DOIKEY);
            landingPageTemplate = sm.getValue(DoiSettings.SETTING_PUBLICATION_DOI_LANDING_PAGE_TEMPLATE);

            final boolean emptyUrl = StringUtils.isEmpty(serverUrl);
            final boolean emptyUsername = StringUtils.isEmpty(username);
            final boolean emptyPassword = StringUtils.isEmpty(password);
            final boolean emptyPrefix = StringUtils.isEmpty(doiPrefix);
            if (emptyUrl ||
                emptyUsername ||
                emptyPassword ||
                emptyPrefix) {
                StringBuilder report = new StringBuilder("DOI configuration is not complete. Check in System Configuration to fill the DOI configuration.");
                if (emptyUrl) {
                    report.append("\n* URL MUST be set");
                }
                if (emptyUsername) {
                    report.append("\n* Username MUST be set");
                }
                if (emptyPassword) {
                    report.append("\n* Password MUST be set");
                }
                if (emptyPrefix) {
                    report.append("\n* Prefix MUST be set");
                }
                Log.warning(DoiSettings.LOGGER_NAME,
                    report.toString());
                this.initialised = false;
            } else {
                Log.debug(DoiSettings.LOGGER_NAME,
                    "DOI configuration looks perfect.");
                // TODO: Check connection ?
                this.client = new DoiClient(serverUrl, username, password);
                initialised = true;
            }
        }
    }


    public Map<String, Boolean> check(ServiceContext serviceContext, AbstractMetadata metadata, Element dataciteMetadata) throws Exception {
        Map<String, Boolean> conditions = new HashMap<>();
        checkInitialised();
        conditions.put(DoiConditions.API_CONFIGURED, true);

        String doi =  DoiBuilder.create(this.doiPrefix, metadata.getUuid());
        checkPreConditions(metadata, doi);
        conditions.put(DoiConditions.RECORD_IS_PUBLIC, true);
        conditions.put(DoiConditions.STANDARD_SUPPORT, true);


        // ** Convert to DataCite format
        Element dataciteFormatMetadata =
            dataciteMetadata == null ?
            convertXmlToDataCiteFormat(metadata.getDataInfo().getSchemaId(),
                metadata.getXmlData(false)) : dataciteMetadata;
        checkPreConditionsOnDataCite(metadata, doi, dataciteFormatMetadata);
        conditions.put(DoiConditions.DATACITE_FORMAT_IS_VALID, true);
        return conditions;
    }

    public Map<String, String> register(ServiceContext context, AbstractMetadata metadata) throws Exception {
        Map<String, String> doiInfo = new HashMap<>(3);
        // The new DOI for this record
        String doi =  DoiBuilder.create(this.doiPrefix, metadata.getUuid());
        doiInfo.put("doi", doi);

        // The record in datacite format
        Element dataciteFormatMetadata =
                convertXmlToDataCiteFormat(metadata.getDataInfo().getSchemaId(),
                    metadata.getXmlData(false));


        check(context, metadata, dataciteFormatMetadata);
        createDoi(context, metadata, doiInfo, dataciteFormatMetadata);
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
    private void checkPreConditions(AbstractMetadata metadata, String doi) throws DoiClientException, IOException, JDOMException {
        // Record MUST be public
        AccessManager am = ApplicationContextHolder.get().getBean(AccessManager.class);
        boolean visibleToAll = false;
        try {
            visibleToAll = am.isVisibleToAll(metadata.getId() + "");
        } catch (Exception e) {
            throw new DoiClientException(String.format(
                "Failed to check if record '%s' is visible to all for DOI creation." +
                   " Error is %s.",
                metadata.getUuid(), e.getMessage()));
        }

        if (!visibleToAll) {
            throw new DoiClientException(String.format(
                "Record '%s' is not public and we cannot request a DOI for such a record. Publish this record first.",
                metadata.getUuid()));
        }


        // Record MUST not contains a DOI
        final MetadataSchema schema = schemaUtils.getSchema(metadata.getDataInfo().getSchemaId());
        Element xml = metadata.getXmlData(false);
        try {
            String currentDoi = schema.queryString(DOI_GET_SAVED_QUERY, xml);
            if (StringUtils.isNotEmpty(currentDoi)) {
                // Current doi does not match the one going to be inserted. This is odd
                if (!currentDoi.equals(HTTPS_DOI_ORG + doi)) {
                    throw new DoiClientException(String.format(
                        "Record '%s' already contains a DOI '%s' which is not equal " +
                            "to the DOI about to be created (ie. '%s'). " +
                            "Maybe current DOI does not correspond to that record? " +
                            "This may happen when creating a copy of a record having " +
                            "an existing DOI.",
                        metadata.getUuid(), currentDoi, HTTPS_DOI_ORG + doi));
                }

                throw new DoiClientException(String.format(
                    "Record '%s' already contains a DOI. The DOI is '%s'. " +
                        "We cannot register it again. " +
                        "Remove the DOI reference if it does not apply to that record.",
                    metadata.getUuid(), currentDoi));
            }
        } catch (ResourceNotFoundException e) {
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
                DOI_GET_SAVED_QUERY, e.getMessage(),
                schema.getName()));
        }
    }


    /**
     * Check conditions on DataCite side.
     * @param metadata
     * @param doi
     * @param dataciteMetadata
     */
    private void checkPreConditionsOnDataCite(AbstractMetadata metadata, String doi, Element dataciteMetadata) throws DoiClientException {
        // * DataCite API is up an running ?

        // XSD validation
        try {
            Xml.validate(dataciteMetadata);
        } catch (Exception e) {
            throw new DoiClientException(String.format(
                "Record '%s' converted to DataCite format is invalid. Error is: %s. " +
                    "Required fields in DataCite are: identifier, creators, titles, publisher, publicationYear, resourceType. Check the DataCite format output at " +
                    "%sapi/records/%s/formatters/datacite?output=xml and " +
                    "adpat the record content to add missing information.",
                metadata.getUuid(), e.getMessage(), sm.getNodeURL(), metadata.getUuid()));
        }

        // * MDS / DOI does not exist already
        // curl -i --user username:password https://mds.test.datacite.org/doi/10.5072/GN
        // Return 404
        final String doiResponse = client.retrieveDoi(doi);
        if (doiResponse != null) {
            throw new DoiClientException(String.format(
                "Record '%s' looks to be already published on DataCite on DOI '%s'. Check DataCite DOI: %s. " +
                    "If the DOI is not correct, remove it from the record and ask for a new one.",
                metadata.getUuid(), doi, doiResponse));
        }
        // TODO: Could be relevant at some point to return states (draft/findable)

        // * MDS / Metadata does not exist either
        // curl -i --user username:password https://mds.test.datacite.org/metadata/10.5072/GN
        // Return 404
    }

    /**
     * Use the DataCite API to register the new DOI.
     * @param context
     * @param metadata
     */
    private void createDoi(ServiceContext context, AbstractMetadata metadata, Map<String, String> doiInfo, Element dataciteMetadata) throws Exception {
        // * Now, let's create the DOI
        // picking a DOI name,

        // The identifier in the DataCite XML metadata file can be left empty, as the value from the PUT URL will be used.
        // curl -H "Content-Type: application/xml;charset=UTF-8" -X POST -i --user username:password -d @/tmp/GN.xml https://mds.test.datacite.org/metadata/10.5072/GN

        // ** Validate output ? XSD
        // ** POST metadata
        // 201 Created: operation successful,
        client.createDoiMetadata(doiInfo.get("doi"), Xml.getString(dataciteMetadata));


        // Register the URL
        // 201 Created: operation successful;
        String landingPage = landingPageTemplate.replace(
                        "{{uuid}}", metadata.getUuid());
        doiInfo.put("doiLandingPage", landingPage);
        client.createDoi(doiInfo.get("doi"), landingPage);


        // Add the DOI in the record
        Element recordWithDoi = setDOIValue(doiInfo.get("doi"), metadata.getDataInfo().getSchemaId(), metadata.getXmlData(false));
        // Update the published copy
        //--- needed to detach md from the document
//        md.detach();

        dm.updateMetadata(context, metadata.getId() + "", recordWithDoi, false, true, true,
            context.getLanguage(), new ISODate().toString(), true);

        doiInfo.put("doiUrl", HTTPS_DOI_ORG + doiInfo.get("doi"));

    }


    /**
     * Check that the DOI is properly created.
     * @param metadata
     * @param doi
     */
    private void checkDoiCreation(AbstractMetadata metadata, Map<String, String> doi) {
        // Check it is available on DataCite Metadata Store
        // curl -X GET --user INIST.IFREMER https://mds.test.datacite.org/metadata/10.5072/GN2
        // Check it is in the record
    }


    public void unregisterDoi(AbstractMetadata metadata, ServiceContext context) throws DoiClientException, ResourceNotFoundException {
        checkInitialised();

        final String doi = DoiBuilder.create(this.doiPrefix, metadata.getUuid());
        final String doiResponse = client.retrieveDoi(doi);
        if (doiResponse == null) {
            throw new ResourceNotFoundException(String.format(
                "Record '%s' is not available on DataCite. DOI '%s' does not exist.",
                metadata.getUuid(), doi));
        }


        try {
            Element md = metadata.getXmlData(false);

            String doiUrl = schemaUtils.getSchema(metadata.getDataInfo().getSchemaId())
                .queryString(DOI_GET_SAVED_QUERY, md);

            client.deleteDoiMetadata(doi);
            client.deleteDoi(doi);

            Element recordWithoutDoi = removeDOIValue(doiUrl, metadata.getDataInfo().getSchemaId(), md);

            dm.updateMetadata(context, metadata.getId() + "", recordWithoutDoi, false, true, true,
                context.getLanguage(), new ISODate().toString(), true);
        } catch (Exception ex) {
            throw new DoiClientException(ex.getMessage());
        }
    }

    /**
     * Sets the DOI URL value in the metadata record using the process DOI_ADD_XSL_PROCESS.
     *
     */
    public Element setDOIValue(String doi, String schema, Element md) throws Exception {
        Path styleSheet = dm.getSchemaDir(schema).resolve(DOI_ADD_XSL_PROCESS);
        boolean exists = Files.exists(styleSheet);
        if (!exists) {
            throw new DoiClientException(String.format("To create a DOI, the schema has to defined how to insert a DOI in the record. The schema_plugins/%s/process/%s was not found. Create the XSL transformation.",
                schema, DOI_ADD_XSL_PROCESS));
        }

        Map<String, Object> params = new HashMap<>(1);
        params.put("doi", HTTPS_DOI_ORG + doi);
        return Xml.transform(md, styleSheet, params);
    }

    /**
     * Sets the DOI URL value in the metadata record using the process DOI_ADD_XSL_PROCESS.
     *
     */
    public Element removeDOIValue(String doi, String schema, Element md) throws Exception {
        Path styleSheet = dm.getSchemaDir(schema).resolve(DOI_REMOVE_XSL_PROCESS);
        boolean exists = Files.exists(styleSheet);
        if (!exists) {
            throw new DoiClientException(String.format("To remove a DOI, the schema has to defined how to remove a DOI in the record. The schema_plugins/%s/process/%s was not found. Create the XSL transformation.",
                schema, DOI_REMOVE_XSL_PROCESS));
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
    private Element convertXmlToDataCiteFormat(String schema, Element md) throws Exception {
        final Path styleSheet = dm.getSchemaDir(schema).resolve(DATACITE_XSL_CONVERSION_FILE);
        final boolean exists = Files.exists(styleSheet);
        if (!exists) {
            throw new DoiClientException(String.format("To create a DOI, the record needs to be converted to the DataCite format (https://schema.datacite.org/). You need to create a formatter for this in schema_plugins/%s/%s. If the standard is a profile of ISO19139, you can simply point to the ISO19139 formatter.",
                schema, DATACITE_XSL_CONVERSION_FILE));
        };

        Map<String,Object> params = new HashMap<String,Object>();
        params.put(DOI_PREFIX_PARAMETER, this.doiPrefix);
        Element dataciteMetadata = Xml.transform(md, styleSheet, params);
        return dataciteMetadata;
    }

    private void checkInitialised() throws DoiClientException {
        if (!initialised) {
            throw new DoiClientException("DOI configuration is not complete. Check System Configuration and set the DOI configuration.");
        }
    }

}
