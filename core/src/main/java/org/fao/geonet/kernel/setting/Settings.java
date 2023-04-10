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

package org.fao.geonet.kernel.setting;

/**
 * Setting constant
 */
public class Settings {
    public static final String SYSTEM_SITE_ORGANIZATION = "catalog/site/organization";
    public static final String SYSTEM_SITE_SITE_ID_PATH = "catalog/site/siteId";
    public static final String SYSTEM_SITE_NAME_PATH = "catalog/site/name";
    public static final String SYSTEM_SITE_LABEL_PREFIX = "catalog/site/labels/";
    public static final String SYSTEM_SERVER_HOST = "catalog/server/host";
    public static final String SYSTEM_SERVER_PORT = "catalog/server/port";
    public static final String SYSTEM_SERVER_PROTOCOL = "catalog/server/protocol";
    public static final String SYSTEM_PLATFORM_VERSION = "catalog/platform/version";
    public static final String SYSTEM_PLATFORM_SUBVERSION = "catalog/platform/subVersion";
    public static final String SYSTEM_CORS_ALLOWEDHOSTS = "network/cors/allowedHosts";
    public static final String SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS = "services/csw/transactionUpdateCreateXPath";
    public static final String SYSTEM_PROXY_USE = "network/proxy/use";
    public static final String SYSTEM_PROXY_HOST = "network/proxy/host";
    public static final String SYSTEM_PROXY_PORT = "network/proxy/port";
    public static final String SYSTEM_PROXY_USERNAME = "network/proxy/username";
    public static final String SYSTEM_PROXY_PASSWORD = "network/proxy/password";
    public static final String SYSTEM_PROXY_IGNOREHOSTLIST = "network/proxy/ignorehostlist";
    public static final String SYSTEM_XLINKRESOLVER_ENABLE = "metadata/xlinkResolver/enable";
    public static final String SYSTEM_XLINK_ALLOW_REFERENCED_DELETION = "metadata/xlinkResolver/referencedDeletionAllowed";
    public static final String SYSTEM_SERVER_LOG = "catalog/server/log";
    public static final String SYSTEM_SERVER_TIMEZONE = "catalog/server/timeZone";

    public static final String METADATA_URL_SITEMAPLINKURL = "metadata/url/sitemapLinkUrl";
    public static final String METADATA_URL_SITEMAPDOIFIRST = "metadata/url/sitemapDoiFirst";
    public static final String METADATA_URL_DYNAMICAPPLINKURL = "metadata/url/dynamicAppLinkUrl";

    public static final String SYSTEM_INSPIRE_ENABLE = "inspire/enable";
    public static final String SYSTEM_INSPIRE_ATOM = "inspire/atom";
    public static final String SYSTEM_INSPIRE_ATOM_SCHEDULE = "inspire/atomSchedule";
    public static final String SYSTEM_PREFER_GROUP_LOGO = "metadata/prefergrouplogo";
    public static final String SYSTEM_USERS_IDENTICON = "usersgroups/users/identicon";
    public static final String SYSTEM_SEARCHSTATS = "system/searchStats/enable";
    public static final String SYSTEM_FEEDBACK_EMAIL = "catalog/feedback/email";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_HOST = "catalog/feedback/mailServer/host";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_PORT = "catalog/feedback/mailServer/port";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_USERNAME = "catalog/feedback/mailServer/username";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_PASSWORD = "catalog/feedback/mailServer/password";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_SSL = "catalog/feedback/mailServer/ssl";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_TLS = "catalog/feedback/mailServer/tls";
    public static final String SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS =
        "catalog/feedback/mailServer/ignoreSslCertificateErrors";
    public static final String SYSTEM_ENABLE_ALL_THESAURUS = "metadata/allThesaurus";
    public static final String SYSTEM_METADATA_THESAURUS_NAMESPACE = "metadata/thesaurusNamespace";
    public static final String SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION = "metadata/validation/removeSchemaLocation";
    public static final String SYSTEM_METADATA_HISTORY_ENABLED = "metadata/history/enabled";
    public static final GNSetting SYSTEM_SITE_SVNUUID = new GNSetting("catalog/site/svnUuid", true);
    public static final String SYSTEM_INTRANET_NETWORK = "network/intranet/network";
    public static final String SYSTEM_INTRANET_NETMASK = "network/intranet/netmask";
    public static final String SYSTEM_INTRANET_IP_SEPARATOR = ",";
    public static final String SYSTEM_Z3950_ENABLE = "system/z3950/enable";
    public static final String SYSTEM_Z3950_PORT = "system/z3950/port";
    public static final String SYSTEM_SELECTIONMANAGER_MAXRECORDS = "system/selectionmanager/maxrecords";
    public static final String SYSTEM_CSW_ENABLE = "services/csw/enable";
    public static final String SYSTEM_CSW_ENABLEWHENINDEXING = "services/csw/enabledWhenIndexing";
    public static final String SYSTEM_CSW_CAPABILITY_RECORD_UUID = "services/csw/capabilityRecordUuid";
    public static final String SYSTEM_CSW_METADATA_PUBLIC = "services/csw/metadataPublic";
    public static final String SYSTEM_USERSELFREGISTRATION_ENABLE = "usersgroups/userSelfRegistration/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE = "usersgroups/userSelfRegistration/recaptcha/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_PUBLICKEY = "usersgroups/userSelfRegistration/recaptcha/publickey";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY = "usersgroups/userSelfRegistration/recaptcha/secretkey";
    public static final String SYSTEM_USERFEEDBACK_ENABLE = "system/userFeedback/enable";
    public static final String SYSTEM_USER_LASTNOTIFICATIONDATE = "system/userFeedback/lastNotificationDate";
    public static final String SYSTEM_LOCALRATING_ENABLE = "metadata/localrating/enable";
    public static final String SYSTEM_LOCALRATING_NOTIFICATIONLEVEL = "metadata/localrating/notificationLevel";
    public static final String SYSTEM_LOCALRATING_NOTIFICATIONGROUPS = "metadata/localrating/notificationGroups";
    public static final String SYSTEM_XLINK_RESOLVER_IGNORE = "metadata/xlinkResolver/ignore";
    public static final String SYSTEM_HIDEWITHHELDELEMENTS_ENABLE_LOGGING = "system/hidewithheldelements/enableLogging";
    public static final String SYSTEM_AUTOFIXING_ENABLE = "metadata/autofixing/enable";
    public static final String SYSTEM_OAI_MDMODE = "services/oai/mdmode";
    public static final String SYSTEM_OAI_MAXRECORDS = "services/oai/maxrecords";
    public static final String SYSTEM_OAI_TOKENTIMEOUT = "services/oai/tokentimeout";
    public static final String SYSTEM_OAI_CACHESIZE = "services/oai/cachesize";
    public static final String SYSTEM_HARVESTER_ENABLE_EDITING = "harvester/enableEditing";
    public static final String SYSTEM_HARVESTER_ENABLE_PRIVILEGES_MANAGEMENT = "harvester/enablePrivilegesManagement";
    public static final String SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES = "harvester/disabledHarvesterTypes";
    public static final String SYSTEM_METADATAPRIVS_USERGROUPONLY = "metadata/metadataprivs/usergrouponly";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATIONBYGROUPOWNERONLY = "metadata/metadataprivs/publicationbyrevieweringroupowneronly";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATIONNOTIFICATION_EMAILS = "metadata/metadataprivs/publication/notificationEmails";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL = "metadata/metadataprivs/publication/notificationLevel";
    public static final String SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONGROUPS = "metadata/metadataprivs/publication/notificationGroups";
    public static final String SYSTEM_INSPIRE_ATOM_PROTOCOL = "inspire/atomProtocol";
    public static final String SYSTEM_HARVESTING_MAIL_RECIPIENT = "harvester/mail/recipient";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL3 = "harvester/mail/level3";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL2 = "harvester/mail/level2";
    public static final String SYSTEM_HARVESTING_MAIL_LEVEL1 = "harvester/mail/level1";
    public static final String SYSTEM_HARVESTING_MAIL_ENABLED = "harvester/mail/enabled";
    public static final String SYSTEM_HARVESTING_MAIL_SUBJECT = "harvester/mail/subject";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE_WARNING = "harvester/mail/templateWarning";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE_ERROR = "harvester/mail/templateError";
    public static final String SYSTEM_HARVESTING_MAIL_TEMPLATE = "harvester/mail/template";
    public static final String SYSTEM_METADATACREATE_GENERATE_UUID = "metadata/metadatacreate/generateUuid";
    public static final String SYSTEM_THREADEDINDEXING_MAXTHREADS = "system/threadedindexing/maxthreads";
    public static final String SYSTEM_INDEX_INDEXINGTIMERECORDLINK = "system/index/indexingTimeRecordLink";
    public static final String SYSTEM_RESOURCE_PREFIX = "metadata/resourceIdentifierPrefix";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_URL = "inspire/remotevalidation/url";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_URL_QUERY = "inspire/remotevalidation/urlquery";
    public static final String SYSTEM_INSPIRE_REMOTE_VALIDATION_APIKEY = "inspire/remotevalidation/apikey";
    public static final String REGION_GETMAP_BACKGROUND = "region/getmap/background";
    public static final String REGION_GETMAP_MAPPROJ = "region/getmap/mapproj";
    public static final String REGION_GETMAP_WIDTH = "region/getmap/width";
    public static final String REGION_GETMAP_SUMMARY_WIDTH = "region/getmap/summaryWidth";
    public static final String METADATA_WORKFLOW_ENABLE = "metadata/workflow/enable";
    public static final String METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP = "metadata/workflow/draftWhenInGroup";
    public static final String METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD = "metadata/workflow/allowSumitApproveInvalidMd";
    public static final String METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD = "metadata/workflow/allowPublishInvalidMd";
    public static final String METADATA_WORKFLOW_ALLOW_PUBLISH_NON_APPROVED_MD = "metadata/workflow/allowPublishNonApprovedMd";
    public static final String METADATA_LINK_EXCLUDEPATTERN = "metadata/link/excludedUrlPattern";
    public static final String METADATA_IMPORT_RESTRICT = "metadata/import/restrict";
    public static final String METADATA_IMPORT_USERPROFILE = "metadata/import/userprofile";
    public static final String METADATA_PUBLISHED_DELETE_USERPROFILE = "metadata/delete/profilePublishedMetadata";
    public static final String METADATA_PUBLISH_USERPROFILE = "metadata/publication/profilePublishMetadata";
    public static final String METADATA_UNPUBLISH_USERPROFILE = "metadata/publication/profileUnpublishMetadata";
    public static final String METADATA_BACKUPARCHIVE_ENABLE = "metadata/backuparchive/enable";
    public static final String METADATA_VCS = "metadata/vcs/enable";
    public static final String VIRTUAL_SETTINGS_SUFFIX_ISDEFINED = "IsDefined";
    public static final String NODE = "node/id";
    public static final String NODE_DEFAULT = "node/default";
    public static final String NODE_NAME = "node/name";

    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_MINLENGH = "usersgroups/security/passwordEnforcement/minLength";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_MAXLENGH = "usersgroups/security/passwordEnforcement/maxLength";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_USEPATTERN = "usersgroups/security/passwordEnforcement/usePattern";
    public static final String SYSTEM_SECURITY_PASSWORDENFORCEMENT_PATTERN = "usersgroups/security/passwordEnforcement/pattern";
    public static final String SYSTEM_SECURITY_PASSWORD_ALLOWADMINRESET = "usersgroups/security/password/allowAdminReset";


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
