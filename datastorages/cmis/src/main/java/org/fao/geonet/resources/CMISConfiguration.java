/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.resources;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResourceExternalManagementProperties;
import org.fao.geonet.utils.Log;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuration parameters are based on the following
 * https://chemistry.apache.org/java/developing/dev-session-parameters.html
 */
public class CMISConfiguration {
    private Session client = ;

    // DFO change to 100. Due to bug with open text cmis where if max is set to 1000, it will return 100 but if it is set to 100 it will return all records.
    // https://dev.azure.com/foc-poc/EDH-CDE/_workitems/edit/95878
    public static final Integer CMIS_MAX_ITEMS_PER_PAGE = 100;
    public static final String CMIS_FOLDER_DELIMITER = "/"; // Specs indicate that "/" is the folder delimiter/separator - not sure if other delimiter can be used?.
    public static final String CMIS_SECONDARY_PROPERTY_SEPARATOR = "->";
    private static final String CMIS_DEFAULT_WEBSERVICES_ACL_SERVICE = "/services/ACLService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_DISCOVERY_SERVICE = "/services/DiscoveryService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_MULTIFILING_SERVICE = "/services/MultiFilingService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_NAVIGATION_SERVICE = "/services/NavigationService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_OBJECT_SERVICE = "/services/ObjectService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_POLICY_SERVICE = "/services/PolicyService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_RELATIONSHIP_SERVICE = "/services/RelationshipService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_REPOSITORY_SERVICE = "/services/RepositoryService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_VERSIONING_SERVICE = "/services/VersioningService?wsdl";
    private static final String CMIS_DEFAULT_WEBSERVICES_BASE_URL_SERVICE = "/cmis";
    private static final String CMIS_DEFAULT_BROWSER_URL_SERVICE = "/browser";
    private static final String CMIS_DEFAULT_ATOMPUB_URL_SERVICE = "/atom";

    private static final String CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS = "toolbar=0,width=600,height=600";
    private static final Boolean CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED = true;
    private static final Boolean CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_FOLDER_ENABLED = true;
    private static final Boolean CMIS_DEFAULT_VERSIONING_ENABLED = false;

    private String servicesBaseUrl;
    private String bindingType;
    private String baseRepositoryPath;
    private String username;
    private String password;
    private String repositoryId;
    private String repositoryName;

    /**
     * Property name for storing the metadata uuid that is expected to be a String
     * It is expected to be in one of the following formats
     * {Aspect}->{property_name}  - for secondary properties - i.e. P:cm:titled->cm:title
     * {property_name}  - for primary properties - i.e. cmis:description
     */
    private String cmisMetadataUUIDPropertyName;
    private String parsedCmisMetadataUUIDPropertyName =  null;
    private boolean cmisMetadataUUIDSecondaryProperty = false;

    /**
     * Url used for managing enhanced resource properties related to the metadata.
     */
    private String externalResourceManagementUrl;
    private String externalResourceManagementWindowParameters;
    private Boolean externalResourceManagementModalEnabled;
    private Boolean externalResourceManagementFolderEnabled;
    private String externalResourceManagementFolderRoot;

    /**
     * Property name for validation status that is expected to be an integer with values of null, 0, 1, 2
     * (See MetadataResourceExternalManagementProperties.ValidationStatus for code meaning)
     * Property name follows the same format as cmisMetadataUUIDPropertyName
     * If null then validation status will default to UNKNOWN.
     */
    private String externalResourceManagementValidationStatusPropertyName;
    private String parsedExternalResourceManagementValidationStatusPropertyName =  null;
    /**
     * Default value to be used for the validation status.
     * If null then it will use INCOMPLETE as the default.
     * Note that if property name is not supplied then it will always default to UNKNOWN
     */
    private String externalResourceManagementValidationStatusDefaultValue;
    private boolean externalResourceManagementValidationStatusSecondaryProperty = false;
    private MetadataResourceExternalManagementProperties.ValidationStatus defaultStatus = null;

    /*
     * Enable option to add versioning in the link to the resource.
     */
    private Boolean versioningEnabled;
    private VersioningState versioningState;
    private Boolean versioningMajorOnUpdate;

    private String webservicesRepositoryService;
    private String webservicesNavigationService;
    private String webservicesObjectService;
    private String webservicesVersioningService;
    private String webservicesDiscoveryService;
    private String webservicesRelationshipService;
    private String webservicesMultifilingService;
    private String webservicesPolicyService;
    private String webservicesAclService;
    private String webservicesMemoryThreshold;
    private String webservicesBaseUrl;

    private String browserUrl;

    private String atompubUrl;

    @Nonnull
    public String getServicesBaseUrl() {
        return servicesBaseUrl;
    }

    public void setServicesBaseUrl(String servicesBaseUrl) {
        this.servicesBaseUrl = servicesBaseUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nonnull
    public String getBindingType() {
        return this.bindingType;
    }

    public void setBindingType(String bindingTypeString) {
        if (StringUtils.isEmpty(bindingTypeString)) {
            this.bindingType = null;
        } else {
            this.bindingType = BindingType.fromValue(bindingTypeString).value();
        }
    }

    @Nonnull
    private BindingType getBindingTypeObject() {
        return BindingType.fromValue(bindingType);
    }

    private void setBindingTypeObject(BindingType bindingType) {
        this.bindingType = bindingType.value();
    }

    public String getBaseRepositoryPath() {
        return baseRepositoryPath;
    }

    public void setBaseRepositoryPath(String baseRepositoryPath) {
        this.baseRepositoryPath = baseRepositoryPath;
    }

    @Nonnull
    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Nonnull
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @Nonnull
    public String getExternalResourceManagementUrl() {
        return externalResourceManagementUrl;
    }

    public void setExternalResourceManagementUrl(String externalResourceManagementUrl) {
        this.externalResourceManagementUrl = externalResourceManagementUrl;
    }

    @Nonnull
    public String getExternalResourceManagementWindowParameters() {
        if (externalResourceManagementWindowParameters == null) {
            return CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS;
        } else {
            return externalResourceManagementWindowParameters;
        }
    }

    public void setExternalResourceManagementWindowParameters(String externalResourceManagementWindowParameters) {
        this.externalResourceManagementWindowParameters = externalResourceManagementWindowParameters;
    }

    @Nonnull
    public Boolean isExternalResourceManagementModalEnabled() {
        if (externalResourceManagementModalEnabled == null) {
            return CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED;
        } else {
            return externalResourceManagementModalEnabled;
        }
    }

    public void setExternalResourceManagementModalEnabled(Boolean externalResourceManagementModalEnabled) {
        this.externalResourceManagementModalEnabled = externalResourceManagementModalEnabled;
    }

    public void setExternalResourceManagementModalEnabled(String externalResourceManagementModalEnabled) {
        this.externalResourceManagementModalEnabled = BooleanUtils.toBooleanObject(externalResourceManagementModalEnabled);
    }

    public Boolean isExternalResourceManagementFolderEnabled() {
        if (externalResourceManagementFolderEnabled == null) {
            return CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_FOLDER_ENABLED;
        } else {
            return externalResourceManagementFolderEnabled;
        }
    }

    public void setExternalResourceManagementFolderEnabled(Boolean externalResourceManagementFolderEnabled) {
        this.externalResourceManagementFolderEnabled = externalResourceManagementFolderEnabled;
    }

    public String getExternalResourceManagementFolderRoot() {
        return this.externalResourceManagementFolderRoot;
    }

    public void setExternalResourceManagementFolderRoot(String externalResourceManagementFolderRoot) {
        String folderRoot = externalResourceManagementFolderRoot;
        if (folderRoot != null) {
            if (!folderRoot.startsWith(getFolderDelimiter())) {
                folderRoot = getFolderDelimiter() + folderRoot;
            }
            if (folderRoot.endsWith(getFolderDelimiter())) {
                folderRoot = folderRoot.substring(0, folderRoot.length() - 1);
            }
        }

        this.externalResourceManagementFolderRoot = folderRoot;
    }

    public String getExternalResourceManagementValidationStatusDefaultValue() {
        return externalResourceManagementValidationStatusDefaultValue;
    }

    public void setExternalResourceManagementValidationStatusDefaultValue(String externalResourceManagementValidationStatusDefaultValue) {
        this.externalResourceManagementValidationStatusDefaultValue = externalResourceManagementValidationStatusDefaultValue;
    }

    @Nonnull
    public VersioningState getVersioningState() {
        if (versioningState == null) {
            return VersioningState.MAJOR;
        } else {
            return this.versioningState;
        }
    }

    public void setVersioningState(VersioningState versioningState) {
        if (versioningState != null && versioningState.equals(VersioningState.CHECKEDOUT)) {
            throw new IllegalArgumentException("Versioning state CHECKEDOUT is not supported in this context");
        }
        this.versioningState = versioningState;
    }

    public void setVersioningState(String versioningState) {
        setVersioningState(StringUtils.isEmpty(versioningState) ? null : VersioningState.valueOf(versioningState.toUpperCase()));
    }

    @Nonnull
    public Boolean isVersioningMajorOnUpdate() {
        if (versioningMajorOnUpdate == null) {
            return false;
        } else {
            return versioningMajorOnUpdate;
        }
    }

    public void setVersioningMajorOnUpdate(Boolean versioningMajorOnUpdate) {
        this.versioningMajorOnUpdate = versioningMajorOnUpdate;
    }

    public void setVersioningMajorOnUpdate(String versioningMajorOnUpdate) {
        this.versioningMajorOnUpdate = BooleanUtils.toBooleanObject(versioningMajorOnUpdate);
    }

    @Nonnull
    public Boolean isVersioningEnabled() {
        if (versioningEnabled == null) {

            return CMIS_DEFAULT_VERSIONING_ENABLED;
        } else {
            return versioningEnabled;
        }
    }

    public void setVersioningEnabled(Boolean versioningEnabled) {
        this.versioningEnabled = versioningEnabled;
    }

    public void setVersioningEnabled(String versioningEnabled) {
        this.versioningEnabled = BooleanUtils.toBooleanObject(versioningEnabled);
    }

    @Nonnull
    public String getWebservicesRepositoryService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesRepositoryService, CMIS_DEFAULT_WEBSERVICES_REPOSITORY_SERVICE);
    }

    public void setWebservicesRepositoryService(String webservicesRepositoryService) {
        this.webservicesRepositoryService = webservicesRepositoryService;
    }

    @Nonnull
    public String getWebservicesNavigationService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesNavigationService, CMIS_DEFAULT_WEBSERVICES_NAVIGATION_SERVICE);
    }

    public void setWebservicesNavigationService(String webservicesNavigationService) {
        this.webservicesNavigationService = webservicesNavigationService;
    }

    @Nonnull
    public String getWebservicesObjectService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesObjectService, CMIS_DEFAULT_WEBSERVICES_OBJECT_SERVICE);
    }

    public void setWebservicesObjectService(String webservicesObjectService) {
        this.webservicesObjectService = webservicesObjectService;
    }

    @Nonnull
    public String getWebservicesVersioningService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesVersioningService, CMIS_DEFAULT_WEBSERVICES_VERSIONING_SERVICE);
    }

    public void setWebservicesVersioningService(String webservicesVersioningService) {
        this.webservicesVersioningService = webservicesVersioningService;
    }

    @Nonnull
    public String getWebservicesDiscoveryService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesDiscoveryService, CMIS_DEFAULT_WEBSERVICES_DISCOVERY_SERVICE);
    }

    public void setWebservicesDiscoveryService(String webservicesDiscoveryService) {
        this.webservicesDiscoveryService = webservicesDiscoveryService;
    }

    @Nonnull
    public String getWebservicesRelationshipService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesRelationshipService, CMIS_DEFAULT_WEBSERVICES_RELATIONSHIP_SERVICE);
    }

    public void setWebservicesRelationshipService(String webservicesRelationshipService) {
        this.webservicesRelationshipService = webservicesRelationshipService;
    }

    @Nonnull
    public String getWebservicesMultifilingService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesMultifilingService, CMIS_DEFAULT_WEBSERVICES_MULTIFILING_SERVICE);
    }

    public void setWebservicesMultifilingService(String webservicesMultifilingService) {
        this.webservicesMultifilingService = webservicesMultifilingService;
    }

    @Nonnull
    public String getWebservicesPolicyService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesPolicyService, CMIS_DEFAULT_WEBSERVICES_POLICY_SERVICE);
    }

    public void setWebservicesPolicyService(String webservicesPolicyService) {
        this.webservicesPolicyService = webservicesPolicyService;
    }

    @Nonnull
    public String getWebservicesAclService() {
        return getServiceUrl(getWebservicesBaseUrl(), webservicesAclService, CMIS_DEFAULT_WEBSERVICES_ACL_SERVICE);
    }

    public void setWebservicesAclService(String webservicesAclService) {
        this.webservicesAclService = webservicesAclService;
    }

    @Nonnull
    public String getWebservicesMemoryThreshold() {
        return webservicesMemoryThreshold;
    }

    public void setWebservicesMemoryThreshold(String webservicesMemoryThreshold) {
        this.webservicesMemoryThreshold = webservicesMemoryThreshold;
    }

    @Nonnull
    public String getWebservicesBaseUrl() {
        return getServiceUrl(servicesBaseUrl, webservicesBaseUrl, CMIS_DEFAULT_WEBSERVICES_BASE_URL_SERVICE);
    }

    public void setWebservicesBaseUrl(String webservicesBaseUrl) {
        this.webservicesBaseUrl = webservicesBaseUrl;
    }

    @Nonnull
    public String getBrowserUrl() {
        return getServiceUrl(servicesBaseUrl, browserUrl, CMIS_DEFAULT_BROWSER_URL_SERVICE);
    }

    public void setBrowserUrl(String browserUrl) {
        this.browserUrl = browserUrl;
    }

    @Nonnull
    public String getAtompubUrl() {
        return getServiceUrl(servicesBaseUrl, atompubUrl, CMIS_DEFAULT_ATOMPUB_URL_SERVICE);
    }

    public void setAtompubUrl(String atompubUrl) {
        this.atompubUrl = atompubUrl;
    }

    public String getCmisMetadataUUIDPropertyName() {
        // If we were able to parse the field on startup then return the parsed version.
        if (parsedCmisMetadataUUIDPropertyName != null) {
            return parsedCmisMetadataUUIDPropertyName;
        } else {
            return cmisMetadataUUIDPropertyName;
        }
    }

    public void setCmisMetadataUUIDPropertyName(String cmisMetadataUUIDPropertyName) {
        if (!StringUtils.isEmpty(cmisMetadataUUIDPropertyName) && cmisMetadataUUIDPropertyName.contains(CMIS_SECONDARY_PROPERTY_SEPARATOR)) {
            String[] splitPropertyNames = cmisMetadataUUIDPropertyName.split(Pattern.quote(CMIS_SECONDARY_PROPERTY_SEPARATOR));
            if (splitPropertyNames.length != 2) {
                Log.error(Geonet.RESOURCES,
                    String.format("Invalid format for property name %s property will not be used", cmisMetadataUUIDPropertyName));
                this.cmisMetadataUUIDPropertyName = null;
                this.cmisMetadataUUIDSecondaryProperty = false;
                return;
            } else {
                this.cmisMetadataUUIDSecondaryProperty = true;
            }
        }
        this.cmisMetadataUUIDPropertyName = cmisMetadataUUIDPropertyName;
    }

    public String getExternalResourceManagementValidationStatusPropertyName() {
        // If we were able to parse the field on startup then return the parsed version.
        if (parsedExternalResourceManagementValidationStatusPropertyName != null) {
            return parsedExternalResourceManagementValidationStatusPropertyName;
        } else {
            return externalResourceManagementValidationStatusPropertyName;
        }
    }

    public void setExternalResourceManagementValidationStatusPropertyName(String externalResourceManagementValidationStatusPropertyName) {
        this.externalResourceManagementValidationStatusPropertyName = externalResourceManagementValidationStatusPropertyName;
        if (!StringUtils.isEmpty(externalResourceManagementValidationStatusPropertyName) && externalResourceManagementValidationStatusPropertyName.contains(CMIS_SECONDARY_PROPERTY_SEPARATOR)) {
            String[] splitPropertyNames = externalResourceManagementValidationStatusPropertyName.split(Pattern.quote(CMIS_SECONDARY_PROPERTY_SEPARATOR));
            if (splitPropertyNames.length != 2) {
                Log.error(Geonet.RESOURCES,
                    String.format("Invalid format for property name %s property will not be used", externalResourceManagementValidationStatusPropertyName));
                this.externalResourceManagementValidationStatusPropertyName = null;
                this.externalResourceManagementValidationStatusSecondaryProperty = false;
            } else {
                this.externalResourceManagementValidationStatusSecondaryProperty = true;
            }
        }
    }

    public MetadataResourceExternalManagementProperties.ValidationStatus getValidationStatusDefaultValue() {
        // We only need to set the default if there is a status property supplied, and it is not already set
        if (this.defaultStatus == null &&  org.springframework.util.StringUtils.hasLength(getExternalResourceManagementValidationStatusPropertyName())) {
            if (getExternalResourceManagementValidationStatusDefaultValue() != null) {
                // If a default property name does exist then use it
                this.defaultStatus = MetadataResourceExternalManagementProperties.ValidationStatus.valueOf(getExternalResourceManagementValidationStatusDefaultValue());
            } else {
                // Otherwise let's default to incomplete.
                // Reason - as the administrator decided to use the status, it most likely means that there are extra properties that need to be set after a file is uploaded so defaulting it to
                // incomplete seems reasonable.
                this.defaultStatus = MetadataResourceExternalManagementProperties.ValidationStatus.INCOMPLETE;
            }
        }
        return this.defaultStatus;
    }

    @PostConstruct
    public void init() {
        // If we have a cmisMetadataUUIDPropertyName then call the set so that it also validates the value.
        if (cmisMetadataUUIDPropertyName != null) {
            setCmisMetadataUUIDPropertyName(cmisMetadataUUIDPropertyName);
        }

        // default factory implementation
        Map<String, String> parameters = new HashMap<>();

        if (this.baseRepositoryPath == null) {
            this.baseRepositoryPath = "";
        }

        // Base path should end with delimiter.
        if (this.baseRepositoryPath.endsWith(CMIS_FOLDER_DELIMITER)) {
            this.baseRepositoryPath = this.baseRepositoryPath.substring(1);
        }

        // Base path should start with delimiter (unless it is the root for which it would remain as an empty string).
        if (baseRepositoryPath.length() > 1 && !baseRepositoryPath.startsWith(CMIS_FOLDER_DELIMITER)) {
            this.baseRepositoryPath = CMIS_FOLDER_DELIMITER + baseRepositoryPath;
        }

        if (username != null) {
            // user credentials
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);
        }

        // connection settings
        if (getBindingTypeObject() == null) {
            throw new IllegalArgumentException("CMIS Binding type must be supplied");
        }

        String repositoryUrl;
        switch (getBindingTypeObject()) {
            case WEBSERVICES:
                parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, getWebservicesAclService());
                parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, getWebservicesDiscoveryService());
                parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, getWebservicesMultifilingService());
                parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, getWebservicesNavigationService());
                parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, getWebservicesObjectService());
                parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, getWebservicesPolicyService());
                parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, getWebservicesRelationshipService());
                parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, getWebservicesRepositoryService());
                parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, getWebservicesVersioningService());
                repositoryUrl = getWebservicesBaseUrl();
                break;
            case BROWSER:
                parameters.put(SessionParameter.BROWSER_URL, getBrowserUrl());
                repositoryUrl = getBrowserUrl();
                break;
            case ATOMPUB:
                parameters.put(SessionParameter.ATOMPUB_URL, getAtompubUrl());
                repositoryUrl = getAtompubUrl();
                break;
            default:
                throw new IllegalArgumentException("Unsupported CMIS Binding type '" + getBindingTypeObject().value() + "'.");
        }
        parameters.put(SessionParameter.BINDING_TYPE, getBindingTypeObject().value());

        SessionFactory factory = SessionFactoryImpl.newInstance();

        if (repositoryId == null) {
            if (repositoryName != null) {
                // Try to find the repository by name.
                try {
                    for (Repository repository : factory.getRepositories(parameters)) {
                        if (repository.getName().equalsIgnoreCase(repositoryName)) {
                            this.repositoryId = repository.getId();
                            break;
                        }
                    }
                } catch (CmisRuntimeException | CmisConnectionException e) {
                    Log.error(Geonet.RESOURCES, "CMIS Repository ID not found for repositoryName=\"" + repositoryName +
                        "\". " + (e.getErrorContent() == null ? "" : "  Got following results from cmis api call:" + e.getErrorContent()), e);
                }
            }
        } else {
            // Try to find the repository name for the id that we have specified.
            try {
                for (Repository repository : factory.getRepositories(parameters)) {
                    if (repository.getId().equalsIgnoreCase(this.repositoryId)) {
                        this.repositoryName = repository.getName();
                        break;
                    }
                }
            } catch (CmisRuntimeException | CmisConnectionException e) {
                Log.error(Geonet.RESOURCES, "CMIS Repository name not found repositoryName=\"" + repositoryName +
                    "\". " + (e.getErrorContent() == null ? "" : "  Got following results from cmis api call:" + e.getErrorContent()), e);
            }
        }

        if (repositoryId != null) {

            parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);

            try {
                client = factory.createSession(parameters);
                Log.info(Geonet.RESOURCES, "Connected to CMIS using binding '" + client.getBinding().getBindingType().value() + "' with base url '" +
                    repositoryUrl + "' using product '" + client.getRepositoryInfo().getProductName() + "' version '" +
                    client.getRepositoryInfo().getProductVersion() + "'.");

                // Check if we can parse the secondary parameters from human-readable to secondary ids.
                parsedCmisMetadataUUIDPropertyName = parseSecondaryProperty(client, cmisMetadataUUIDPropertyName);
                parsedExternalResourceManagementValidationStatusPropertyName = parseSecondaryProperty(client, externalResourceManagementValidationStatusPropertyName);

                // Setup default options
                // Ensure caching is on.
                if (!client.getDefaultContext().isCacheEnabled()) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context cache to enabled.");
                    client.getDefaultContext().setCacheEnabled(true);
                }
                // Don't get allowable actions by default
                if (client.getDefaultContext().isIncludeAllowableActions()) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include allowable actions.");
                    client.getDefaultContext().setIncludeAllowableActions(false);
                }
                // Don't get ACLS by default
                if (client.getDefaultContext().isIncludeAcls()) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include acls.");
                    client.getDefaultContext().setIncludeAcls(false);
                }
                // Don't include path segments by default
                if (client.getDefaultContext().isIncludePathSegments()) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include path segments.");
                    client.getDefaultContext().setIncludePathSegments(false);
                }
                // Don't include policies by default
                if (client.getDefaultContext().isIncludePolicies()) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include policies.");
                    client.getDefaultContext().setIncludePolicies(false);
                }
                // IncludeRelationships should be NONE
                if (!client.getDefaultContext().getIncludeRelationships().equals(IncludeRelationships.NONE)) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include relationships.");
                    client.getDefaultContext().setIncludeRelationships(IncludeRelationships.NONE);
                }

                if (client.getDefaultContext().getMaxItemsPerPage() != CMIS_MAX_ITEMS_PER_PAGE) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS max items per page to " + CMIS_MAX_ITEMS_PER_PAGE + ".");
                    client.getDefaultContext().setMaxItemsPerPage(CMIS_MAX_ITEMS_PER_PAGE);
                }

                // Setup default filter. Only include properties that are used by the application.
                // Having too many may cause performance issues on some systems.
                // The default is generally an empty string meaning all properties are used.
                if (StringUtils.isEmpty(client.getDefaultContext().getFilterString())) {
                    Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context filter.");
                    // excluding "cmis:secondaryObjectTypeIds" from the list as it could decrease performance on some systems.
                    client.getDefaultContext().setFilter(new HashSet<>(Arrays.asList(
                        PropertyIds.NAME,
                        PropertyIds.OBJECT_ID,
                        PropertyIds.OBJECT_TYPE_ID,
                        PropertyIds.BASE_TYPE_ID,
                        PropertyIds.CREATED_BY,
                        PropertyIds.CREATION_DATE,
                        PropertyIds.LAST_MODIFIED_BY,
                        PropertyIds.LAST_MODIFICATION_DATE,
                        PropertyIds.CHANGE_TOKEN,
                        PropertyIds.DESCRIPTION,
                        PropertyIds.IS_LATEST_VERSION,
                        PropertyIds.IS_MAJOR_VERSION,
                        PropertyIds.IS_LATEST_MAJOR_VERSION,
                        PropertyIds.VERSION_LABEL,
                        PropertyIds.VERSION_SERIES_ID,
                        PropertyIds.CONTENT_STREAM_LENGTH,
                        PropertyIds.CONTENT_STREAM_MIME_TYPE,
                        PropertyIds.CONTENT_STREAM_FILE_NAME,
                        PropertyIds.CONTENT_STREAM_ID)));
                }

            } catch (CmisRuntimeException | CmisConnectionException e) {
                client = null;
                Log.error(Geonet.RESOURCES,
                    "CMIS error creating session using base url \"" + repositoryUrl + "\" with repositoryName=\"" + repositoryName +
                        "\". " + (e.getErrorContent() == null ? "" : "  Got following results from cmis api call:" + e.getErrorContent()), e);
            }
        }
    }

    @Nonnull
    public Session getClient() {
        if (client == null) {
            init();
        }
        if (client == null) {
            throw new RuntimeException("Failed to create CMIS session.");
        }
        return client;
    }

    public String getFolderDelimiter() {
        return CMIS_FOLDER_DELIMITER;
    }

    public String getSecondaryPropertySeparator() {
        return CMIS_SECONDARY_PROPERTY_SEPARATOR;
    }

    public boolean existSecondaryProperty() {
        return cmisMetadataUUIDSecondaryProperty || externalResourceManagementValidationStatusSecondaryProperty;
    }

    public boolean existMetadataUUIDSecondaryProperty() {
        return cmisMetadataUUIDSecondaryProperty;
    }

    public boolean existExternalResourceManagementValidationStatusSecondaryProperty() {
        return externalResourceManagementValidationStatusSecondaryProperty;
    }

    /**
     * Generate a full url based on the supplied entered serviceUrl and the default.
     *
     * @param baseUrl                Base url
     * @param serviceUrl             Supplied service url (This could start with / or http. If it starts with http then ignore baseUrl)
     * @param defaultServicePathInfo default ending url for the service.
     * @return a full url to the service.
     */
    private String getServiceUrl(String baseUrl, String serviceUrl, String defaultServicePathInfo) {
        // If no service url was not supplied then lets default to the base url plus the default path info
        if (StringUtils.isEmpty(serviceUrl)) {
            return baseUrl + defaultServicePathInfo;
            // If the service url supplied starts with / then default to base url plus supplied service url
        } else if (serviceUrl.startsWith("/")) {
            return baseUrl + serviceUrl;
            // Otherwise assume that a full url was supplied and just return it.
        } else {
            return serviceUrl;
        }
    }

    /**
     * On certain cmis system the secondary property name/id is a bunch of codes that get changed each time the application is deployed.
     *  Example from Open Text CMIS -   otCat:43102->otCat:43102:43102_5
     *  This can cause issues for system that are re-deployed and the new codes need to be entered.
     *  This function help convert a human-readable entry like
     *     Catalogue->GeoNetwork Catalogue ID
     *  to
     *     otCat:43102->otCat:43102:43102_5
     *  The parsing is done by using the property display name.
     *
     *  Note that using the display name means that it will stop on the next restart if the display name for the property is changed.
     *  so care should be taken in changing the display names if using this option.
     *
     * @param client connection to use to access CMIS server.
     * @param propertyName to be parsed
     * @return null if the property name cannot be parsed otherwise it will return the parsed value.
     */
    private String parseSecondaryProperty(Session client, String propertyName) {
        if (!StringUtils.isEmpty(propertyName) && propertyName.contains(CMIS_SECONDARY_PROPERTY_SEPARATOR)) {
            String[] splitPropertyNames = propertyName.split(Pattern.quote(CMIS_SECONDARY_PROPERTY_SEPARATOR));
            if (splitPropertyNames.length == 2) {
                Folder baseFolder = (Folder) client.getObjectByPath(this.baseRepositoryPath);

                for (SecondaryType secondaryType : baseFolder.getSecondaryTypes()) {
                    if (secondaryType.getId().equals(splitPropertyNames[0]) || secondaryType.getDisplayName().equals(splitPropertyNames[0])) {
                        for (Map.Entry<String, PropertyDefinition<?>> entry : secondaryType.getPropertyDefinitions().entrySet()) {
                            if (entry.getValue().getId().equals(splitPropertyNames[1]) || entry.getValue().getDisplayName().equals(splitPropertyNames[1])) {
                                String parsedPropertyName = secondaryType.getId() + CMIS_SECONDARY_PROPERTY_SEPARATOR + entry.getKey();
                                // The parsed property equals to the original property then we can simply return null as there were no changes.
                                if (parsedPropertyName.equals(propertyName)) {
                                    return null;
                                } else {
                                    Log.info(Geonet.RESOURCES,
                                        String.format("Parsed CMIS secondary properties from '%s' to '%s'", propertyName, parsedPropertyName));
                                    return parsedPropertyName;
                                }
                            }
                        }
                    }
                }

                // If we make it here then it means that we were not able to parse the value, so it is most likely invalid.
                Log.error(Geonet.RESOURCES,
                    String.format("Unable to locate secondary property name '%s'", propertyName));
            }
        }
        return null;
    }
}
