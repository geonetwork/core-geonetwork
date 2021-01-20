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

package org.fao.geonet.resources;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuration parameters are based on the following
 * https://chemistry.apache.org/java/developing/dev-session-parameters.html
 */
public class CMISConfiguration {
    private Session client = null;

    private final String CMIS_FOLDER_DELIMITER = "/"; // Specs indicate that "/" is the folder delimiter/separator - not sure if other delimiter can be used?.
    private final String CMIS_SECONDARY_PROPERTY_SEPARATOR ="->";
    private final String CMIS_DEFAULT_WEBSERVICES_ACL_SERVICE = "/services/ACLService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_DISCOVERY_SERVICE = "/services/DiscoveryService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_MULTIFILING_SERVICE = "/services/MultiFilingService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_NAVIGATION_SERVICE = "/services/NavigationService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_OBJECT_SERVICE = "/services/ObjectService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_POLICY_SERVICE = "/services/PolicyService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_RELATIONSHIP_SERVICE = "/services/RelationshipService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_REPOSITORY_SERVICE = "/services/RepositoryService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_VERSIONING_SERVICE = "/services/VersioningService?wsdl";
    private final String CMIS_DEFAULT_WEBSERVICES_BASE_URL_SERVICE = "/cmis";
    private final String CMIS_DEFAULT_BROWSER_URL_SERVICE = "/browser";
    private final String CMIS_DEFAULT_ATOMPUB_URL_SERVICE = "/atom";

    private final String CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS = "toolbar=0,width=600,height=600";
    private final Boolean CMIS_DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED = true;
    private final Boolean CMIS_DEFAULT_VERSIONING_ENABLED = false;

    private String servicesBaseUrl;
    private String bindingType;
    private String baseRepositoryPath;
    private String username;
    private String password;
    private String repositoryId;
    private String repositoryName;
    private String cmisMetadataUUIDPropertyName;
    /**
     * Url used for managing enhanced resource properties related to the metadata.
     */
    private String externalResourceManagementUrl = System.getenv("CMIS_EXTERNAL_RESOURCE_MANAGEMENT_URL");
    private String externalResourceManagementWindowParameters = System.getenv("CMIS_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS");
    private Boolean externalResourceManagementModalEnabled = BooleanUtils.toBooleanObject(System.getenv("CMIS_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED"));
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

    private boolean secondaryPropertyExists=false;

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
        this.externalResourceManagementModalEnabled = BooleanUtils.toBooleanObject(externalResourceManagementModalEnabled);;
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
        setVersioningState(StringUtils.isEmpty(versioningState)?null:VersioningState.valueOf(versioningState.toUpperCase()));
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
        this.versioningMajorOnUpdate = BooleanUtils.toBooleanObject(versioningMajorOnUpdate);;
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
        this.versioningEnabled = BooleanUtils.toBooleanObject(versioningEnabled);;
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
            return cmisMetadataUUIDPropertyName;
    }

    public void setCmisMetadataUUIDPropertyName(String cmisMetadataUUIDPropertyName) {
        if (!StringUtils.isEmpty(cmisMetadataUUIDPropertyName) && cmisMetadataUUIDPropertyName.contains(CMIS_SECONDARY_PROPERTY_SEPARATOR)) {
            String[] splitPropertyNames = cmisMetadataUUIDPropertyName.split(Pattern.quote(CMIS_SECONDARY_PROPERTY_SEPARATOR));
            if (splitPropertyNames.length != 2) {
                Log.error(Geonet.RESOURCES,
                    String.format("Invalid format for property name %s property will not be used", cmisMetadataUUIDPropertyName));
                this.cmisMetadataUUIDPropertyName = null;
                this.secondaryPropertyExists=false;
                return;
            } else {
                this.secondaryPropertyExists=true;
            }
        }
        this.cmisMetadataUUIDPropertyName = cmisMetadataUUIDPropertyName;
    }

    @PostConstruct
    public void init() {
        // If we have a cmisMetadataUUIDPropertyName then call the set so that it also validates the value.
        if (cmisMetadataUUIDPropertyName != null) {
            setCmisMetadataUUIDPropertyName(cmisMetadataUUIDPropertyName);
        }

        // default factory implementation
        Map<String, String> parameters = new HashMap<String, String>();

        this.baseRepositoryPath = baseRepositoryPath;
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

            username = null;
            password = null;
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
                repositoryUrl=getWebservicesBaseUrl();
                break;
            case BROWSER:
                parameters.put(SessionParameter.BROWSER_URL, getBrowserUrl());
                repositoryUrl=getBrowserUrl();
                break;
            case ATOMPUB:
                parameters.put(SessionParameter.ATOMPUB_URL, getAtompubUrl());
                repositoryUrl=getAtompubUrl();
                break;
            default:
                throw new IllegalArgumentException("Unsupported CMIS Binding type '" + getBindingTypeObject().value() + "'.");
        }
        parameters.put(SessionParameter.BINDING_TYPE, getBindingTypeObject().value());

        SessionFactory factory = SessionFactoryImpl.newInstance();

        if (repositoryId == null) {
            if (repositoryName != null) {
                // Try to find the repository by name.
                for (Repository repository:factory.getRepositories(parameters)) {
                    if (repository.getName().equalsIgnoreCase(repositoryName)) {
                        this.repositoryId = repository.getId();
                        break;
                    }
                }
            }
        } else {
            // Try to find the repository name for the id that we have specified..
            for (Repository repository : factory.getRepositories(parameters)) {
                if (repository.getId().equalsIgnoreCase(this.repositoryId)) {
                    this.repositoryName = repository.getName();
                    break;
                }
            }
        }

        if (repositoryId == null) {
            throw new IllegalArgumentException("CMIS Repository ID not found for repositoryId=\"" + repositoryId + "\" and repositoryName=\"" + repositoryName + "\"");
        }

        parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);

        client = factory.createSession(parameters);
        Log.info(Geonet.RESOURCES, "Connected to CMIS using binding '" + client.getBinding().getBindingType().value() + "' with base url '" +
                repositoryUrl + "' using product '" + client.getRepositoryInfo().getProductName() + "' version '" +
                client.getRepositoryInfo().getProductVersion() + "'.");

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
        if (client.getDefaultContext().getIncludeRelationships().equals(IncludeRelationships.NONE)) {
            Log.debug(Geonet.RESOURCES, "Changing default CMIS operational context to not include relationships.");
            client.getDefaultContext().setIncludeRelationships(IncludeRelationships.NONE);
        }
    }

    @Nonnull
    public Session getClient() {
        return client;
    }

    public String getFolderDelimiter() {
        return CMIS_FOLDER_DELIMITER;
    }

    public String getSecondaryPropertySeparator() {
        return CMIS_SECONDARY_PROPERTY_SEPARATOR;
    }

    public boolean existSecondaryProperty() {
        return secondaryPropertyExists;
    }

    /**
     * Generte a full url based on the supplied entered serviceurl and the default.
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
}
