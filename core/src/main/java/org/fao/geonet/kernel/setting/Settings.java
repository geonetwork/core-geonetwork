/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.setting;

/**
 * Setting constant
 */
public class Settings {
    public static final String SYSTEM_SITE_ORGANIZATION = "system/site/organization";
    public static final String SYSTEM_SITE_SITE_ID_PATH = "system/site/siteId";
    public static final String SYSTEM_SITE_NAME_PATH = "system/site/name";
    public static final String SYSTEM_SITE_LABEL_PREFIX = "system/site/labels/";
    public static final String SYSTEM_SERVER_HOST = "system/server/host";
    public static final String SYSTEM_SERVER_PORT = "system/server/port";
    public static final String SYSTEM_SERVER_PROTOCOL = "system/server/protocol";
    public static final String SYSTEM_PLATFORM_VERSION = "system/platform/version";
    public static final String SYSTEM_PLATFORM_SUBVERSION = "system/platform/subVersion";
    public static final String SYSTEM_CORS_ALLOWEDHOSTS = "system/cors/allowedHosts";
    public static final String SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS = "system/csw/transactionUpdateCreateXPath";
    public static final String SYSTEM_PROXY_USE = "system/proxy/use";
    public static final String SYSTEM_PROXY_HOST = "system/proxy/host";
    public static final String SYSTEM_PROXY_PORT = "system/proxy/port";
    public static final String SYSTEM_PROXY_USERNAME = "system/proxy/username";
    public static final String SYSTEM_PROXY_PASSWORD = "system/proxy/password";
    public static final String SYSTEM_PROXY_IGNOREHOSTLIST = "system/proxy/ignorehostlist";
    public static final String SYSTEM_XLINKRESOLVER_ENABLE = "system/xlinkResolver/enable";
    public static final String SYSTEM_XLINK_ALLOW_REFERENCED_DELETION = "system/xlinkResolver/referencedDeletionAllowed";
    public static final String SYSTEM_SERVER_LOG = "system/server/log";
    public static final String SYSTEM_SERVER_TIMEZONE = "system/server/timeZone";

    public static final String METADATA_URL_SITEMAPLINKURL = "metadata/url/sitemapLinkUrl";
    public static final String METADATA_URL_SITEMAPDOIFIRST = "metadata/url/sitemapDoiFirst";
    public static final String METADATA_URL_DYNAMICAPPLINKURL = "metadata/url/dynamicAppLinkUrl";

    public static final String SYSTEM_INSPIRE_ENABLE = "system/inspire/enable";
    public static final String SYSTEM_INSPIRE_ATOM = "system/inspire/atom";
    public static final String SYSTEM_INSPIRE_ATOM_SCHEDULE = "system/inspire/atomSchedule";
    public static final String SYSTEM_PREFER_GROUP_LOGO = "system/metadata/prefergrouplogo";
    public static final String SYSTEM_USERS_IDENTICON = "system/users/identicon";
    public static final String SYSTEM_SEARCHSTATS = "system/searchStats/enable";
    public static final String SYSTEM_FEEDBACK_EMAIL = "system/feedback/email";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_HOST = "system/feedback/mailServer/host";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_PORT = "system/feedback/mailServer/port";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_USERNAME = "system/feedback/mailServer/username";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_PASSWORD = "system/feedback/mailServer/password";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_SSL = "system/feedback/mailServer/ssl";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_TLS = "system/feedback/mailServer/tls";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS =
        "system/feedback/mailServer/ignoreSslCertificateErrors";
    public static final String SYSTEM_ENABLE_ALL_THESAURUS = "system/metadata/allThesaurus";
    public static final String SYSTEM_METADATA_THESAURUS_NAMESPACE = "system/metadata/thesaurusNamespace";
    public static final String SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION = "system/metadata/validation/removeSchemaLocation";
    public static final String SYSTEM_METADATA_HISTORY_ENABLED = "system/metadata/history/enabled";
    public static final GNSetting SYSTEM_SITE_SVNUUID = new GNSetting("system/site/svnUuid", true);
    public static final String SYSTEM_INTRANET_NETWORK = "system/intranet/network";
    public static final String SYSTEM_INTRANET_NETMASK = "system/intranet/netmask";
    public static final String SYSTEM_INTRANET_IP_SEPARATOR = ",";
    public static final String SYSTEM_Z3950_ENABLE = "system/z3950/enable";
    public static final String SYSTEM_Z3950_PORT = "system/z3950/port";
    public static final String SYSTEM_SELECTIONMANAGER_MAXRECORDS = "system/selectionmanager/maxrecords";
    public static final String SYSTEM_CSW_ENABLE = "system/csw/enable";
    public static final String SYSTEM_CSW_ENABLEWHENINDEXING = "system/csw/enabledWhenIndexing";
    public static final String SYSTEM_CSW_CAPABILITY_RECORD_UUID = "system/csw/capabilityRecordUuid";
    public static final String SYSTEM_CSW_METADATA_PUBLIC = "system/csw/metadataPublic";
    public static final String SYSTEM_USERSELFREGISTRATION_ENABLE = "system/userSelfRegistration/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE = "system/userSelfRegistration/recaptcha/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_PUBLICKEY = "system/userSelfRegistration/recaptcha/publickey";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY = "system/userSelfRegistration/recaptcha/secretkey";
    public static final String SYSTEM_USERFEEDBACK_ENABLE = "system/userFeedback/enable";
    public static final String SYSTEM_USER_LASTNOTIFICATIONDATE = "system/userFeedback/lastNotificationDate";
    public static final String SYSTEM_LOCALRATING_ENABLE = "system/localrating/enable";
    public static final String SYSTEM_LOCALRATING_NOTIFICATIONLEVEL = "system/localrating/notificationLevel";
    public static final String SYSTEM_LOCALRATING_NOTIFICATIONGROUPS = "system/localrating/notificationGroups";
    public static final String SYSTEM_XLINK_RESOLVER_IGNORE = "system/xlinkResolver/ignore";
    public static final String SYSTEM_HIDEWITHHELDELEMENTS_ENABLE_LOGGING = "system/hidewithheldelements/enableLogging";
    public static final String SYSTEM_AUTOFIXING_ENABLE = "system/autofixing/enable";
    public static final String SYSTEM_OAI_MDMODE = "system/oai/mdmode";
    public static final String SYSTEM_OAI_MAXRECORDS = "system/oai/maxrecords";
    public static final String SYSTEM_OAI_TOKENTIMEOUT = "system/oai/tokentimeout";
    public static final String SYSTEM_OAI_CACHESIZE = "system/oai/cachesize";
    public static final String SYSTEM_HARVESTER_ENABLE_EDITING = "system/harvester/enableEditing";
    public static final String SYSTEM_HARVESTER_ENABLE_PRIVILEGES_MANAGEMENT = "system/harvester/enablePrivilegesManagement";
    public static final String SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES = "system/harvester/disabledHarvesterTypes";
    public static final String SYSTEM_METADATAPRIVS_USERGROUPONLY = "system/metadataprivs/usergrouponly";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATIONBYGROUPOWNERONLY = "system/metadataprivs/publicationbyrevieweringroupowneronly";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATIONNOTIFICATION_EMAILS = "system/metadataprivs/publication/notificationEmails";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL = "system/metadataprivs/publication/notificationLevel";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONGROUPS = "system/metadataprivs/publication/notificationGroups";
    public static final String SYSTEM_INSPIRE_ATOM_PROTOCOL = "system/inspire/atomProtocol";
    public static final String SYSTEM_HARVESTING_MAIL_RECIPIENT = "system/harvesting/mail/recipient";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL3 = "system/harvesting/mail/level3";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL2 = "system/harvesting/mail/level2";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL1 = "system/harvesting/mail/level1";
    public static final String SYSTEM_HARVESTING_MAIL_ENABLED = "system/harvesting/mail/enabled";
    public static final String SYSTEM_HARVESTING_MAIL_SUBJECT = "system/harvesting/mail/subject";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE_WARNING = "system/harvesting/mail/templateWarning";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE_ERROR = "system/harvesting/mail/templateError";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE = "system/harvesting/mail/template";
    public static final String SYSTEM_METADATACREATE_GENERATE_UUID = "system/metadatacreate/generateUuid";
    public static final String SYSTEM_THREADEDINDEXING_MAXTHREADS = "system/threadedindexing/maxthreads";
    public static final String SYSTEM_RESOURCE_PREFIX = "metadata/resourceIdentifierPrefix";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_URL = "system/inspire/remotevalidation/url";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_URL_QUERY = "system/inspire/remotevalidation/urlquery";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_APIKEY = "system/inspire/remotevalidation/apikey";
    public static final String REGION_GETMAP_BACKGROUND = "region/getmap/background";
    public static final String REGION_GETMAP_MAPPROJ = "region/getmap/mapproj";
    public static final String REGION_GETMAP_WIDTH = "region/getmap/width";
    public static final String REGION_GETMAP_SUMMARY_WIDTH = "region/getmap/summaryWidth";
    public static final String REGION_GETMAP_GEODESIC_EXTENTS = "region/getmap/useGeodesicExtents";
    public static final String METADATA_WORKFLOW_ENABLE = "metadata/workflow/enable";
    public static final String METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP = "metadata/workflow/draftWhenInGroup";
    public static final String METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD = "metadata/workflow/allowSubmitApproveInvalidMd";
    public static final String METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD = "metadata/workflow/allowPublishInvalidMd";
    public static final String METADATA_WORKFLOW_ALLOW_PUBLISH_NON_APPROVED_MD = "metadata/workflow/allowPublishNonApprovedMd";
    public static final String METADATA_WORKFLOW_AUTOMATIC_UNPUBLISH_INVALID_MD = "metadata/workflow/automaticUnpublishInvalidMd";
    public static final String METADATA_WORKFLOW_FORCE_VALIDATION_ON_MD_SAVE = "metadata/workflow/forceValidationOnMdSave";
    public static final String METADATA_LINK_EXCLUDEPATTERN = "metadata/link/excludedUrlPattern";
    public static final String METADATA_IMPORT_RESTRICT = "metadata/import/restrict";
    public static final String METADATA_IMPORT_USERPROFILE = "metadata/import/userprofile";
    public static final String METADATA_BATCH_EDITING_ACCESS_LEVEL = "metadata/batchediting/accesslevel";
    public static final String METADATA_PUBLISHED_DELETE_USERPROFILE = "metadata/delete/profilePublishedMetadata";
    public static final String METADATA_PUBLISH_USERPROFILE = "metadata/publication/profilePublishMetadata";
    public static final String METADATA_UNPUBLISH_USERPROFILE = "metadata/publication/profileUnpublishMetadata";
    public static final String METADATA_BACKUPARCHIVE_ENABLE = "metadata/backuparchive/enable";
    public static final String METADATA_VCS = "metadata/vcs/enable";
    public static final String VIRTUAL_SETTINGS_SUFFIX_ISDEFINED = "IsDefined";
    public static final String NODE = "node/id";
    public static final String NODE_DEFAULT = "node/default";
    public static final String NODE_NAME = "node/name";

    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_MINLENGH = "system/security/passwordEnforcement/minLength";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_MAXLENGH = "system/security/passwordEnforcement/maxLength";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_USEPATTERN = "system/security/passwordEnforcement/usePattern";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_PATTERN = "system/security/passwordEnforcement/pattern";
    public static final String SYSTEM_SECURITY_PASSWORD_ALLOWADMINRESET = "system/security/password/allowAdminReset";

    public static final String MICROSERVICES_ENABLED = "microservices/enabled";

    public static class GNSetting {
        private String name;
        private boolean nullable;

        GNSetting(String name, boolean nullable)
        {
            this.name = name;
            this.nullable = nullable;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public boolean isNullable()
        {
            return nullable;
        }

        public void setNullable(boolean nullable)
        {
            this.nullable = nullable;
        }

    }
}
