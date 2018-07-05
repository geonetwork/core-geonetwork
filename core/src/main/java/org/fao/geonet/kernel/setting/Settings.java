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
    public static final String SYSTEM_SERVER_SECURE_PORT = "system/server/securePort";
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
    public static final GNSetting SYSTEM_LUCENE_IGNORECHARS = new GNSetting("system/requestedLanguage/ignorechars", true);
    public static final String SYSTEM_REQUESTED_LANGUAGE_SORTED = "system/requestedLanguage/sorted";
    public static final String SYSTEM_REQUESTED_LANGUAGE_ONLY = "system/requestedLanguage/only";
    public static final String SYSTEM_AUTODETECT_ENABLE = "system/autodetect/enable";
    public static final String SYSTEM_XLINKRESOLVER_ENABLE = "system/xlinkResolver/enable";
    public static final String SYSTEM_XLINK_ALLOW_REFERENCED_DELETION = "system/xlinkResolver/referencedDeletionAllowed";
    public static final String SYSTEM_SERVER_LOG = "system/server/log";
    public static final String SYSTEM_INSPIRE_ENABLE = "system/inspire/enable";
    public static final String SYSTEM_INSPIRE_ATOM = "system/inspire/atom";
    public static final String SYSTEM_INSPIRE_ATOM_SCHEDULE = "system/inspire/atomSchedule";
    public static final String SYSTEM_PREFER_GROUP_LOGO = "system/metadata/prefergrouplogo";
    public static final String SYSTEM_REQUESTEDLANGUAGE_SORTED = "system/requestedLanguage/sorted";
    public static final String SYSTEM_INDEXOPTIMIZER_ENABLE = "system/indexoptimizer/enable";
    public static final String SYSTEM_INDEXOPTIMIZER_HOUR = "system/indexoptimizer/at/hour";
    public static final String SYSTEM_INDEXOPTIMIZER_MIN = "system/indexoptimizer/at/min";
    public static final String SYSTEM_INDEXOPTIMIZER_SEC = "system/indexoptimizer/at/sec";
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
    public static final String SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION = "system/metadata/validation/removeSchemaLocation";
    public static final GNSetting SYSTEM_SITE_SVNUUID = new GNSetting("system/site/svnUuid", true);
    public static final String SYSTEM_INTRANET_NETWORK = "system/intranet/network";
    public static final String SYSTEM_INTRANET_NETMASK = "system/intranet/netmask";
    public static final String SYSTEM_Z3950_ENABLE = "system/z3950/enable";
    public static final String SYSTEM_Z3950_PORT = "system/z3950/port";
    public static final String SYSTEM_SELECTIONMANAGER_MAXRECORDS = "system/selectionmanager/maxrecords";
    public static final String SYSTEM_CSW_ENABLE = "system/csw/enable";
    public static final String SYSTEM_CSW_ENABLEWHENINDEXING = "system/csw/enabledWhenIndexing";
    public static final String SYSTEM_CSW_CONTACT_ID = "system/csw/contactId";
    public static final String SYSTEM_CSW_METADATA_PUBLIC = "system/csw/metadataPublic";
    public static final String SYSTEM_USERSELFREGISTRATION_ENABLE = "system/userSelfRegistration/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE = "system/userSelfRegistration/recaptcha/enable";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_PUBLICKEY = "system/userSelfRegistration/recaptcha/publickey";
    public static final String SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY = "system/userSelfRegistration/recaptcha/secretkey";
    public static final String SYSTEM_USERFEEDBACK_ENABLE = "system/userFeedback/enable";
    public static final String SYSTEM_USER_LASTNOTIFICATIONDATE = "system/userFeedback/lastNotificationDate";
    public static final String SYSTEM_LOCALRATING_ENABLE = "system/localrating/enable";
    public static final String SYSTEM_XLINK_RESOLVER_IGNORE = "system/xlinkResolver/ignore";
    public static final String SYSTEM_HIDEWITHHELDELEMENTS_ENABLE_LOGGING = "system/hidewithheldelements/enableLogging";
    public static final String SYSTEM_AUTOFIXING_ENABLE = "system/autofixing/enable";
    public static final String SYSTEM_OAI_MDMODE = "system/oai/mdmode";
    public static final String SYSTEM_OAI_MAXRECORDS = "system/oai/maxrecords";
    public static final String SYSTEM_OAI_TOKENTIMEOUT = "system/oai/tokentimeout";
    public static final String SYSTEM_OAI_CACHESIZE = "system/oai/cachesize";
    public static final String SYSTEM_INSPIRE_ENABLE_SEARCH_PANEL = "system/inspire/enableSearchPanel";
    public static final String SYSTEM_HARVESTER_ENABLE_EDITING = "system/harvester/enableEditing";
    public static final String SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES = "system/harvester/disabledHarvesterTypes";
    public static final String SYSTEM_METADATAPRIVS_USERGROUPONLY = "system/metadataprivs/usergrouponly";
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
    public static final String REGION_GETMAP_BACKGROUND = "region/getmap/background";
    public static final String REGION_GETMAP_MAPPROJ = "region/getmap/mapproj";
    public static final String REGION_GETMAP_WIDTH = "region/getmap/width";
    public static final String REGION_GETMAP_SUMMARY_WIDTH = "region/getmap/summaryWidth";
    public static final String METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP = "metadata/workflow/draftWhenInGroup";
    public static final String METADATA_IMPORT_RESTRICT = "metadata/import/restrict";
    public static final String METADATA_BACKUPARCHIVE_ENABLE = "metadata/backuparchive/enable";
    public static final String VIRTUAL_SETTINGS_SUFFIX_ISDEFINED = "IsDefined";

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
