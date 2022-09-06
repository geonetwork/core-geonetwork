//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.constants;

import jeeves.constants.Jeeves;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jdom.Namespace;

import javax.xml.XMLConstants;

/**
 * GeoNetwork configuration constants including module names and file names.
 */
public final class Geonet {

    //FIXME When we migrate everything and get rid of Jeeves, just use this one
    public static final String USER_SESSION_ATTRIBUTE_KEY = Jeeves.Elem.SESSION;

    public static final String CONTEXT_NAME = "contextName";
    // TODO make this configurable
    public static final String DEFAULT_LANGUAGE = "eng";

    public static final String CC_API_REST_URL = "http://api.creativecommons.org/rest/1.5/simple/chooser";

    /**
     * GeoNetwork base logging module name.
     *
     * @deprecated Use {@link #GEONETWORK_MARKER}
     */
    public static final String GEONETWORK = "geonetwork";
    /**
     * GeoNetwork marker for log messages.
     */
    public static final Marker GEONETWORK_MARKER =  MarkerManager.getMarker("geonetwork");

    /**
     * Marker for {@code manager} log messages.
     *
     * Manager classes provide a facade orchestrating geonetwork module lifecycle and interaction.
     */
    public static final Marker MANAGER_MARKER =  MarkerManager.getMarker("manager");

    /**
     * Logging {@code accessmanager} module name.
     *
     * @deprecated Use {@link #ACCESS_MANAGER_MARKER}
     */
    public static final String ACCESS_MANAGER = GEONETWORK + ".accessmanager";

    /**
     * Marker for {@code access_manager} log messages.
     */
    public static final Marker ACCESS_MANAGER_MARKER =  MarkerManager.getMarker("access_manager").addParents(GEONETWORK_MARKER,MANAGER_MARKER);
    /**
     * Logging {@code atom} module name.
     *
     * @deprecated Use {@link #ATOM_MARKER}
     */
    public static final String ATOM = GEONETWORK + ".atom";
    /**
     * Marker {@code atom} for log messages.
     */
    public static final Marker ATOM_MARKER = MarkerManager.getMarker("atom").addParents(GEONETWORK_MARKER);

    /**
     * Logging {@code classifier} module name.
     *
     * @deprecated Use {@link #CLASSIFIER_MARKER}
     */
    public static final String CLASSIFIER = GEONETWORK + ".classifier";
    /**
     * Marker for {@code acceclassifierssmanager} log messages.
     */
    public static final Marker CLASSIFIER_MARKER =  MarkerManager.getMarker("classifier").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code cors} module name.
     *
     * @deprecated Use {@link #CORS_MARKER}
     */
    public static final String CORS = GEONETWORK + ".cors";
    /**
     * Marker for {@code cors} log messages.
     */
    public static final Marker CORS_MARKER =  MarkerManager.getMarker("cors").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code csw} module name.
     *
     * @deprecated Use {@link #CSW_MARKER}
     */
    public static final String CSW = GEONETWORK + ".csw";
    /**
     * Marker for {@code csw} log messages.
     */
    public static final Marker CSW_MARKER =  MarkerManager.getMarker("csw").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code csw.harvest} module name.
     *
     * @deprecated Use {@link #CSW_HARVEST_MARKER}
     */
    public static final String CSW_HARVEST = CSW + ".harvest";
    /**
     * Marker for {@code csw_harvest} log messages.
     */
    public static final Marker CSW_HARVEST_MARKER =  MarkerManager.getMarker("csw_harvest").addParents(CSW_MARKER);
    /**
     * Logging {@code csw.search} module name.
     *
     * @deprecated Use {@link #CSW_SEARCH_MARKER}
     */
    public static final String CSW_SEARCH = CSW + ".search";
    /**
     * Marker for {@code csw_search} log messages.
     */
    public static final Marker CSW_SEARCH_MARKER =  MarkerManager.getMarker("csw_search").addParents(CSW_MARKER);
    /**
     * Logging {@code customelementset}module name.
     *
     * @deprecated Use {@link #CUSTOM_ELEMENTSET_MARKER}
     */
    public static final String CUSTOM_ELEMENTSET = GEONETWORK + ".customelementset";
    /**
     * Marker for {@code customelementset} log messages.
     */
    public static final Marker CUSTOM_ELEMENTSET_MARKER =  MarkerManager.getMarker("customelementset").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code data.directory} module name.
     *
     * @deprecated Use {@link #DATA_DIRECTORY_MARKER}
     */
    public static final String DATA_DIRECTORY = GEONETWORK + ".data.directory";
    /**
     * Marker for {@code data_directory} log messages.
     */
    public static final Marker DATA_DIRECTORY_MARKER =  MarkerManager.getMarker("data_directory").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code datamanager} module name.
     *
     * @deprecated Use {@link #DATA_MANAGER_MARKER}
     */
    public static final String DATA_MANAGER = GEONETWORK + ".datamanager";
    /**
     * Marker for {@code data_manager} log messages.
     */
    public static final Marker DATA_MANAGER_MARKER =  MarkerManager.getMarker("data_manager").addParents(GEONETWORK_MARKER,MANAGER_MARKER);

    /**
     * Logging {@code database} module name.
     *
     * @deprecated Use {@link #DB_MARKER}
     */
    public static final String DB = GEONETWORK + ".database";
    /**
     * Marker for {@code database} log messages.
     */
    public static final Marker DB_MARKER =  MarkerManager.getMarker("database").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code editor} module name.
     *
     * @deprecated Use {@link #EDITOR_MARKER}
     */
    public static final String EDITOR = GEONETWORK + ".editor";
    /**
     * Marker for {@code editor} log messages.
     */
    public static final Marker EDITOR_MARKER =  MarkerManager.getMarker("editor").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code editoraddelement} module name.
     *
     * @deprecated Use {@link #EDITORADDELEMENT_MARKER}
     */
    public static final String EDITORADDELEMENT = GEONETWORK + ".editoraddelement";
    /**
     * Marker for {@code editor_addelement} log messages.
     */
    public static final Marker EDITORADDELEMENT_MARKER = MarkerManager.getMarker("editor_addelement").addParents(EDITOR_MARKER);

    /**
     * Logging {@code editorexpandelement} module name.
     *
     * @deprecated Use {@link #EDITOREXPANDELEMENT_MARKER}
     */
    public static final String EDITOREXPANDELEMENT = GEONETWORK + ".editorexpandelement";
    /**
     * Marker for {@code editor_expandelement} log messages.
     */
    public static final Marker EDITOREXPANDELEMENT_MARKER = MarkerManager.getMarker("editor_expandelement").addParents(EDITOR_MARKER);
    /**
     * Logging {@code editorfileelement} module name.
     *
     * @deprecated Use {@link #EDITORFILLELEMENT_MARKER}
     */
    public static final String EDITORFILLELEMENT = GEONETWORK + ".editorfillelement";
    /**
     * Marker for {@code editor_fillelement} log messages.
     */
    public static final Marker EDITORFILLELEMENT_MARKER = MarkerManager.getMarker("editor_fillelement").addParents(EDITOR_MARKER);
    /**
     * Logging {@code editor.session} module name.
     *
     * @deprecated Use {@link #EDITOR_SESSION_MARKER}
     */
    public static final String EDITOR_SESSION = GEONETWORK + ".editor.session";
    /**
     * Marker for {@code editor_session} log messages.
     */
    public static final Marker EDITOR_SESSION_MARKER = MarkerManager.getMarker("editor_session").addParents(EDITOR_MARKER);

    /**
     * Logging {@code feedback} module name.
     *
     * @deprecated Use {@link #FEEDBACK_MARKER}
     */
    public static final String FEEDBACK = GEONETWORK + ".feedback";
    /**
     * Marker for {@code feedback} log messages.
     */
    public static final Marker FEEDBACK_MARKER = MarkerManager.getMarker("feedback").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code formatter} module name.
     *
     * @deprecated Use {@link #FORMATTER_MARKER}
     */
    public static final String FORMATTER = GEONETWORK + ".formatter";
    /**
     * Marker for {@code formatter} log messages.
     */
    public static final Marker FORMATTER_MARKER = MarkerManager.getMarker("formatter").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code geopublisher} module name.
     *
     * @deprecated Use {@link #GEOPUBLISH_MARKER}
     */
    public static final String GEOPUBLISH = GEONETWORK + ".geopublisher";
    /**
     * Marker for {@code geopublisher} log messages.
     */
    public static final Marker GEOPUBLISH_MARKER = MarkerManager.getMarker("geopublisher").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code harvester} module name.
     *
     * @deprecated Use {@link #HARVEST_MARKER}
     */
    public static final String HARVESTER = GEONETWORK + ".harvester";
    /**
     * Marker for {@code harvester} log messages.
     */
    public static final Marker HARVEST_MARKER =  MarkerManager.getMarker("harvester").addParents(GEONETWORK_MARKER);

    /**
     * Logging {@code harvest-man} module name.
     *
     * @deprecated Use {@link #HARVEST_MAN_MARKER}
     */
    @Deprecated
    public static final String HARVEST_MAN = GEONETWORK + ".harvest-man";
    /**
     * Marker for {@code harvest_manager} log messages.
     */
    public static final Marker HARVEST_MAN_MARKER =  MarkerManager.getMarker("harvest_manager").addParents(HARVEST_MARKER,MANAGER_MARKER);


    /**
     * Logging {@code index} module name.
     *
     * @deprecated Use {@link #INDEX_MARKER}
     */
    public static final String INDEX_ENGINE = GEONETWORK + ".index";
    /**
     * Marker for {@code index} log messages.
     */
    public static final Marker INDEX_MARKER =  MarkerManager.getMarker("index").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code ldap} module name.
     *
     * @deprecated Use {@link #LDAP_MARKER}
     */
    public static final String LDAP = GEONETWORK + ".ldap";
    /**
     * Marker for {@code ldap} log messages.
     */
    public static final Marker LDAP_MARKER =  MarkerManager.getMarker("ldap").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code mef} module name.
     *
     * @deprecated Use {@link #MEF_MARKER}
     */
    public static final String MEF = GEONETWORK + ".mef";
    /**
     * Marker for {@code mef} log messages.
     */
    public static final Marker MEF_MARKER =  MarkerManager.getMarker("mef").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code oai} module name.
     *
     * @deprecated Use {@link #OAI_MARKER}
     */
    public static final String OAI = GEONETWORK + ".oai";
    /**
     * Marker for {@code oai} log messages.
     */
    public static final Marker OAI_MARKER =  MarkerManager.getMarker("oai").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code oai.provider} module name.
     *
     * @deprecated Use {@link #OAI_HARVESTER_MARKER}
     */
    public static final String OAI_HARVESTER = OAI + ".provider";
    /**
     * Marker for {@code oai.provider} log messages.
     */
    public static final Marker OAI_HARVESTER_MARKER =  MarkerManager.getMarker("oai.provider").addParents(OAI_MARKER);
    /**
     * Logging {@code region} module name.
     *
     * @deprecated Use {@link #REGION_MARKER}
     */
    public static final String REGION = GEONETWORK + ".region";
    /**
     * Marker for {@code region} log messages.
     */
    public static final Marker REGION_MARKER =  MarkerManager.getMarker("region").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code resources} module name.
     *
     * @deprecated Use {@link #RESOURCES_MARKER}
     */
    public static final String RESOURCES = GEONETWORK + ".resources";
    /**
     * Marker for {@code resources} log messages.
     */
    public static final Marker RESOURCES_MARKER =  MarkerManager.getMarker("resources").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code schemamanager} module name.
     *
     * @deprecated Use {@link #SCHEMA_MANAGER_MARKER}
     */
    public static final String SCHEMA_MANAGER = GEONETWORK + ".schemamanager";
    /**
     * Marker for {@code schema_manager} log messages.
     */
    public static final Marker SCHEMA_MANAGER_MARKER =  MarkerManager.getMarker("schema_manager").addParents(GEONETWORK_MARKER,MANAGER_MARKER);
    /**
     * Logging {@code search} module name.
     *
     * @deprecated Use {@link #SEARCH_ENGINE_MARKER}
     */
    public static final String SEARCH_ENGINE = GEONETWORK + ".search";
    /**
     * Marker for {@code search} log messages.
     */
    public static final Marker SEARCH_ENGINE_MARKER =  MarkerManager.getMarker("search").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code search-logger} module name.
     *
     * @deprecated Use {@link #SEARCH_LOGGER_MARKER}
     */
    public static final String SEARCH_LOGGER = GEONETWORK + ".search-logger";
    /**
     * Marker for {@code search-logger} log messages.
     */
    public static final Marker SEARCH_LOGGER_MARKER =  MarkerManager.getMarker("search-logger").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code security} module name.
     *
     * @deprecated Use {@link #SECURITY_MARKER}
     */
    public static final String SECURITY = GEONETWORK + ".security";
    /**
     * Marker for {@code security} log messages.
     */
    public static final Marker SECURITY_MARKER =  MarkerManager.getMarker("security").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code settings} module name.
     *
     * @deprecated Use {@link #SETTINGS_MARKER}
     */
    public static final String SETTINGS = GEONETWORK + ".settings";
    /**
     * Marker for {@code settings} log messages.
     */
    public static final Marker SETTINGS_MARKER =  MarkerManager.getMarker("settings").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code spatial} module name.
     *
     * @deprecated Use {@link #SPATIAL_MARKER}
     */
    public static final String SPATIAL = GEONETWORK + ".spatial";
    /**
     * Marker for {@code spatial} log messages.
     */
    public static final Marker SPATIAL_MARKER =  MarkerManager.getMarker("spatial").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code sru} module name.
     *
     * @deprecated Use {@link #SRU_MARKER}
     */
    public static final String SRU = GEONETWORK + ".sru";
    /**
     * Marker for {@code sru} log messages.
     */
    public static final Marker SRU_MARKER =  MarkerManager.getMarker("sru").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code sru.search} module name.
     *
     * @deprecated Use {@link #SRU_SEARCH_MARKER}
     */
    public static final String SRU_SEARCH = SRU + ".search";
    /**
     * Marker for {@code sru_search} log messages.
     */
    public static final Marker SRU_SEARCH_MARKER =  MarkerManager.getMarker("sru_search").addParents(SRU_MARKER);
    /**
     * Logging {@code svnmanager} module name.
     *
     * @deprecated Use {@link #SVN_MANAGER_MARKER}
     */
    public static final String SVN_MANAGER = GEONETWORK + ".svnmanager";
    /**
     * Marker for {@code svn_manager} log messages.
     */
    public static final Marker SVN_MANAGER_MARKER =  MarkerManager.getMarker("svn_manager").addParents(GEONETWORK_MARKER,MANAGER_MARKER);
    /**
     * Logging {@code thesaurus} module name.
     *
     * @deprecated Use {@link #THESAURUS_MARKER}
     */
    public static final String THESAURUS = GEONETWORK + ".thesaurus";
    /**
     * Marker for {@code thesaurus} log messages.
     */
    public static final Marker THESAURUS_MARKER =  MarkerManager.getMarker("thesaurus").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code thesaurus-man} module name.
     *
     * @deprecated Use {@link #THESAURUS_MAN_MARKER}
     */
    public static final String THESAURUS_MAN = GEONETWORK + ".thesaurus-man";
    /**
     * Marker for {@code thesaurus_manager} log messages.
     */
    public static final Marker THESAURUS_MAN_MARKER =  MarkerManager.getMarker("thesaurus_manager").addParents(THESAURUS_MARKER,MANAGER_MARKER);
    /**
     * Logging {@code threadpool} module name.
     *
     * @deprecated Use {@link #THREADPOOL_MARKER}
     */
    public static final String THREADPOOL = GEONETWORK + ".threadpool";
    /**
     * Marker for {@code threadpool} log messages.
     */
    public static final Marker THREADPOOL_MARKER =  MarkerManager.getMarker("threadpool").addParents(GEONETWORK_MARKER);
    /**
     * Logging {@code userwatchlist} module name.
     *
     * @deprecated Use {@link #USER_WATCHLIST_MARKER}
     */
    public static final String USER_WATCHLIST = GEONETWORK + ".userwatchlist";

    /**
     * Marker for {@code userwatchlist} log messages.
     */
    public static final Marker USER_WATCHLIST_MARKER =  MarkerManager.getMarker("userwatchlist").addParents(GEONETWORK_MARKER);

    /**
     * Container for file names.
     */
    public static final class File {
        public static final String SCHEMA = "schema.xsd";
        public static final String UPDATE_FIXED_INFO = "update-fixed-info.xsl";
        public static final String UPDATE_FIXED_INFO_SUBTEMPLATE = "update-fixed-info-subtemplate.xsl";
        public static final String UPDATE_CHILD_FROM_PARENT_INFO = "update-child-from-parent-info.xsl";
        public static final String EXTRACT_UUID = "extract-uuid.xsl";
        public static final String EXTRACT_TITLES = "extract-titles.xsl";
        public static final String EXTRACT_DEFAULT_LANGUAGE = "extract-default-language.xsl";
        public static final String EXTRACT_SKOS_FROM_ISO19135 = "xml_iso19135ToSKOS.xsl";
        public static final String EXTRACT_DATE_MODIFIED = "extract-date-modified.xsl";
        public static final String SET_UUID = "set-uuid.xsl";
        public static final String SET_CREATIVECOMMONS = "set-creativecommons.xsl";
        public static final String SET_DATACOMMONS = "set-datacommons.xsl";
        public static final String DUPLICATE_METADATA = "duplicate-metadata.xsl";
        public static final String SCHEMA_SUGGESTIONS = "schema-suggestions.xml";
        public static final String SCHEMA_SUBSTITUTES = "schema-substitutes.xml";
        public static final String SCHEMA_CONVERSIONS = "schema-conversions.xml";
        public static final String SCHEMA_ID = "schema-ident.xml";
        public static final String SCHEMA_OASIS = "oasis-catalog.xml";
        public static final String SCHEMA_PLUGINS_CATALOG = "schemaplugin-uri-catalog.xml";
        public static final String SORT_HARVESTERS = "sort-harvesters.xsl";
        public static final String JZKITAPPLICATIONCONTEXT = "JZkitApplicationContext.xml";
        public static final String INFLATE_METADATA = "inflate-metadata.xsl";
        public static final String LICENSE_ANNEX = "license-annex.html";
        public static final String LICENSE_ANNEX_XSL = "metadata-license-annex.xsl";
        public static final String METADATA_BRIEF = "metadata-brief.xsl";
        public static final String METADATA_BASEBLANK = "blanks/metadata-schema00";
        public static final String METADATA_BLANK = "blanks/metadata-schema";
        public static final String ENCRYPTOR_CONFIGURATION = "encryptor.properties";
        public static final int METADATA_MAX_BLANKS = 20;

    }

    public static final class SchemaPath {
        public static final String OAI_PMH = "xml/validation/oai/OAI-PMH.xsd";
    }

    /**
     * Container for elements.
     */
    public static final class Elem {
        public static final String DOMAINS = "domains";
        public static final String GROUPS = "groups";
        public static final String GROUP = "group";
        public static final String CATEGORIES = "categories";
        public static final String CATEGORY = "category";
        public static final String REGIONS = "regions";
        public static final String RECORD = "record";
        public static final String ID = "id";
        public static final String ON = "on";
        public static final String METADATA = "metadata";
        public static final String NAME = "name";
        public static final String ORGANISATION = "organisation";
        public static final String NOTIFICATIONS = "notifications";
        public static final String SURNAME = "surname";
        public static final String PROFILE = "profile";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String OPERATIONS = "operations";
        public static final String OPER = "oper";
        public static final String SHOWVALIDATIONERRORS = "showvalidationerrors";
        public static final String TOC_INDEX = "tocIndex";
        public static final String SUMMARY = "summary";
        public static final String SITE_URL = "siteURL";
        public static final String APP_PATH = "path";
        public static final String SCHEMA = "schema";
        public static final String STATUS = "status";
        public static final String JUSTCREATED = "JUSTCREATED";
        public static final String FILTER = "filter";
        public static final String ENABLED = "enabled";
        public static final String VALUE = "value";
        public static final String VIRTUAL_CSW = "virtualcsw";
        public static final String HASH = "hash";
    }

    /**
     * Container for element attribs.
     */
    public static final class Attr {
    }

    /**
     * Resource directory and search configuration file.
     */
    public static final class Path {
        public static final String SCHEMAS = Jeeves.Path.XML + "schemas/";
        public static final String CSW = Jeeves.Path.XML + "csw/";
        public static final String VALIDATION = Jeeves.Path.XML + "validation/";
        public static final String STYLESHEETS = "xsl";
        public static final String XSLT_FOLDER = "xslt";
        public static final String CONV_STYLESHEETS = STYLESHEETS + "/conversion";
        public static final String IMPORT_STYLESHEETS = CONV_STYLESHEETS + "/import";
        public static final String WFS_STYLESHEETS = "convert/WFSToFragments";
        public static final String TDS_STYLESHEETS = "convert/ThreddsToFragments";
        public static final String TDS_19119_19139_STYLESHEETS = "convert/ThreddsCatalogto19119";
        public static final String OGC_STYLESHEETS = "convert/OGCWxSGetCapabilitiesto19119";
        public static final String CONVERT_STYLESHEETS = "convert/";
        public static final String XML = Jeeves.Path.XML;
    }

    /**
     * Session constants.
     */
    public static final class Session {
        public static final String MAIN_SEARCH = "main.search";
        public static final String SEARCH_RESULT = "search.result";
        public static final String SEARCH_REQUEST = "search.request";
        public static final String METADATA_SHOW = "metadata.show";
        public static final String METADATA_EDITING = "metadata.editing";
        // Used to track the creation of a draft copy when the metadata is edited,
        // to be able to remove it if the user cancels the editing without saving any change
        public static final String METADATA_EDITING_CREATED_DRAFT = "metadata.editing.created.draft";
        public static final String METADATA_BEFORE_ANY_CHANGES = "metadata.before.any.changes";
        public static final String METADATA_EDITING_TAB = "metadata.editing.tab";
        public static final String METADATA_POSITION = "metadata.position";
        public static final String SEARCH_KEYWORDS_RESULT = "search.keywords.result";
        public static final String SELECTED_RESULT = "selected.result";
        public static final String VALIDATION_REPORT = "validation.report";
        public static final String FILE_DISCLAIMER = "file.disclaimer";
        public static final String BATCH_PROCESSING_REPORT = "BATCH_PROCESSING_REPORT";

        /**
         * Contains the uuids of metadatas that have to be shown
         */
        public static final String METADATA_UUIDS = "metadata.uuids";

    }

    /**
     * Resource names.
     */
    public static final class Res {
        public static final String MAIN_DB = "main-db";
    }

    /**
     * Parameters that can be used in searches. See the parameters for a more complete description.
     *
     * @see ../services.util.MainUtil.getDefaultSearch for default values.
     */
    public static final class SearchResult {
        /**
         * Parameter name: {@value #TITLE} - Free text field that searches in the title
         */
        public static final String TITLE = "title";

        /**
         * Parameter name: {@value #ABSTRACT} - Free text field that searches in the abstract
         */
        public static final String ABSTRACT = "abstract";

        /**
         * Parameter name: {@value #ANY} - Free text field that searches in all the text fields of a
         * metadata record
         */
        public static final String ANY = "any";
        public static final String PHRASE = "phrase";
        public static final String OR = "or";
        public static final String WITHOUT = "without";
        public static final String ALL = "all";

        /**
         * Parameter name: {@value #REGION} - Index value of a region. Used to retrieve the name and
         * bounding box of the selected region
         */
        public static final String REGION = "region";

        /**
         * Parameter name: {@value #SOUTH_BL} - Lowest Latitude value in floating point format
         * (geographic coordinate) Default value is {@code -90}
         */
        public static final String SOUTH_BL = "southBL";

        /**
         * Parameter name: {@value #NORTH_BL} - Highest Latitude value in floating point format
         * (geographic coordinate) Default value is {@code 90}
         */
        public static final String NORTH_BL = "northBL";

        /**
         * Parameter name: {@value #EAST_BL} - Highest Longitude value in floating point format
         * (geographic coordinate) Default value is {@code 180}
         */
        public static final String EAST_BL = "eastBL";

        /**
         * Parameter name: {@value #WEST_BL} - Lowest Longitude value in floating point format
         * (geographic coordinate) Default value is {@code -180}
         */
        public static final String WEST_BL = "westBL";

        /**
         * Parameter name: {@value #BBOX} - Boundary box in comma separated xmin,ymin,xmax,ymax
         * format, as in OpenSearch-geo or WMS specification. A compact alternative to the four
         * westBL, southBL, eastBL and northBL parameters.
         */
        public static final String BBOX = "bbox";

        /**
         * Parameter name: {@value #RELATION} - Defines the type of spatial query matching used See
         * {@link Relation} for possible values Default value is {@code {@value Relation#OVERLAPS}}
         */
        public static final String RELATION = "relation";

        /**
         * Parameter name: {@value #DATE_FROM} - Start date from when the referenced resource was
         * updated. Formatted as <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>. For
         * example 1970-08-19T06:01:00 or 1970-08-19
         */
        public static final String DATE_FROM = "dateFrom";

        /**
         * Parameter name: {@value #DATE_TO} - End date until when the referenced resource was
         * updated. Formatted as <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>. For
         * example 2008-01-23T10:05:00 or 2008-01-23
         */
        public static final String DATE_TO = "dateTo";

        public static final String THEME_KEY = "themekey";
        public static final String CATEGORY = "category";

        /**
         * Parameter name: {@value #TOPIC_CAT} - Restrict search to resources that have the
         * requested Topic Category set (ISO19115)
         */
        public static final String TOPIC_CAT = "topicCat";

        /**
         * Parameter name: {@value #PROTOCOL} - Searches the protocol field that's part of the
         * online resources in ISO19115. Suggested values are those listed in the localized
         * strings.xml files at /strings/protocolChoice/@value
         */
        public static final String PROTOCOL = "protocol";

        /**
         * Parameter name: {@value #DOWNLOAD} - Boolean that restricts results to those resources
         * that have a files for download based on protocol values as defined in the Lucene indexing
         * stylesheets. Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or {@value
         * org.fao.geonet.constants.Geonet.Text#OFF} (default)
         */
        public static final String DOWNLOAD = "download";

        /**
         * Parameter name: {@value #DYNAMIC} - Boolean that restricts results to those resources
         * that have an interactive resource associated based on protocol values as defined in the
         * Lucene indexing stylesheets. Values are {@value org.fao.geonet.constants.Geonet.Text#ON}
         * or {@value org.fao.geonet.constants.Geonet.Text#OFF} (default)
         */
        public static final String DYNAMIC = "dynamic";

        /**
         * Parameter name: {@value #DIGITAL} - Boolean that restricts results to those resources
         * that describe digital data based on ISO19115 CI_PresentationFormCode codes Exact values
         * indexed are defined in the Lucene indexing stylesheets. Values are {@value
         * org.fao.geonet.constants.Geonet.Text#ON} or {@value org.fao.geonet.constants.Geonet.Text#OFF}
         * (default)
         */
        public static final String DIGITAL = "digital";

        /**
         * Parameter name: {@value #PAPER} - Boolean that restricts results to those resources that
         * describe Hardcopy data based on ISO19115 CI_PresentationFormCode codes Exact values
         * indexed are defined in the Lucene indexing stylesheets. Values are {@value
         * org.fao.geonet.constants.Geonet.Text#ON} or {@value org.fao.geonet.constants.Geonet.Text#OFF}
         * (default)
         */
        public static final String PAPER = "paper";

        /**
         * Parameter name: {@value #SITE_ID} - Limit search results to resources that originate from
         * the selected catalog. The Site's short name should be used as value
         */
        public static final String SITE_ID = "siteId";

        /**
         * Parameter name: {@value #GROUP} - Limit search results to resources that are administered
         * by the selected group. The group ID should be used as value
         */
        public static final String GROUP = "group";

        public static final String PROFILE = "profile";
        public static final String SERVER = "server";
        public static final String SERVERS = "servers";

        /**
         * Parameter name: {@value #TEMPLATE} - Boolean that defines if normal resources are
         * searched or templates are searched Values are {@code y} or {@code n}
         */
        public static final String TEMPLATE = "template";

        /**
         * Parameter name: {@value #EXTENDED} - Boolean that indicates if search is done in simple
         * or Advanced mode. Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or {@value
         * org.fao.geonet.constants.Geonet.Text#OFF} (default)
         */
        public static final String EXTENDED = "extended";

        /**
         * Parameter name: {@value #HITS_PER_PAGE} - Number of results returned by the search
         * engine. Default is 10 results
         */
        public static final String HITS_PER_PAGE = "hitsPerPage";

        /**
         * Parameter name: {@value #MAX_RECORDS} - Number of maximum results returned by the search
         * engine, given the from / to user provided parameters. Default is 100 results.
         */
        public static final String MAX_RECORDS = "maxRecords";

        /**
         * Parameter name: {@value #ALLOW_UNBOUNDED_QUERIES} - Allow XmlSearch to return as many
         * records as the search returns (this was the default behaviour before 3.8.x).
         */
        public static final String ALLOW_UNBOUNDED_QUERIES = "allowUnboundedQueries";

        /**
         * Parameter name: {@value #SIMILARITY} - Use the Lucene FuzzyQuery. Values range from 0.0
         * to 1.0 and defaults to 0.8
         */
        public static final String SIMILARITY = "similarity";

        /**
         * Parameter name: {@value #OUTPUT} - Display results as text only {@value Output#TEXT} or with
         * graphic overviews {@value Output#FULL} (default)
         */
        public static final String OUTPUT = "output";

        /**
         * Parameter name: {@value #SORT_BY} - Order results by {@value SortBy#RELEVANCE} (default),
         * {@value SortBy#RATING}, {@value SortBy#POPULARITY} or by {@value SortBy#DATE}
         */
        public static final String SORT_BY = "sortBy";

        /**
         * Parameter name: {@value #SORT_ORDER} - Order results in reverse order or not false
         * (default)
         */
        public static final String SORT_ORDER = "sortOrder";

        /**
         * Parameter name: {@value #INTERMAP} - Boolean that indicates if GUI shows the embedded
         * InterMap (on) or defaults to the old GUI (off). Values are {@value
         * org.fao.geonet.constants.Geonet.Text#ON} (default) or {@value
         * org.fao.geonet.constants.Geonet.Text#OFF}
         */
        public static final String INTERMAP = "intermap";

        /**
         * Parameter name: {@value #RESTORELASTSEARCH} - Text field that specified whether the last
         * search result should be restored
         */
        public static final String RESTORELASTSEARCH = "restorelastsearch";

        /**
         * Parameter name: {@value #GEOMETRY} - Used to filter results of query based on geometry
         * Currently intersection is used to do the filtering
         *
         * The geometry values a geometry expressed in WKT
         */
        public static final String GEOMETRY = "geometry";

        /**
         * Parameter name: {@value #UUID} - Text field that search for specific uuid given
         */
        public static final String UUID = "uuid";
        /**
         * Attrset used in Z39.50 search
         */
        public static final String ATTRSET = "attrset";
        /**
         * Parameter name: {@value #ZQUERY} - A Z3950 query as specified in the Z3950 harvester
         */
        public static final String ZQUERY = "zquery";


        public static final String RESULT_TYPE = "resultType";

        public static final String FAST = "fast";
        public static final String INDEX = "index";
        public static final String BUILD_SUMMARY = "buildSummary";
        public static final String SUMMARY_ONLY = "summaryOnly";
        public static final String REQUESTED_LANGUAGE = "requestedLanguage";
        public static final String SUMMARY_ITEMS = "summaryItems";
        public static final java.lang.String EXTRA_DUMP_FIELDS = "extraDumpFields";

        /**
         * TODO javadoc.
         */
        public static final class ResultType {
            public static final String RESULTS = "results";
            public static final String HITS = "hits";
            public static final String VALIDATE = "validate";
            public static final String SUGGESTIONS = "suggestions";
        }

        /**
         * TODO java.
         */
        public static final class Relation {
            public static final String EQUAL = "equal";
            public static final String OVERLAPS = "overlaps";
            public static final String ENCLOSES = "encloses";
            public static final String OUTSIDEOF = "fullyOutsideOf";
            public static final String ENCLOSEDWITHIN = "fullyEnclosedWithin";
            public static final String INTERSECTION = "intersection";
            public static final String CROSSES = "crosses";
            public static final String TOUCHES = "touches";
            public static final String WITHIN = "within";
            public static final String WITHIN_BBOX = "within_bbox";
            public static final String OVERLAPS_BBOX = "overlaps_bbox";
        }

        /**
         * TODO javadoc.
         */
        public static final class Output {
            public static final String FULL = "full";
            public static final String TEXT = "text";
        }

        /**
         * TODO javadoc.
         */
        public static final class SortBy {
            public static final String RELEVANCE = "relevance";
            public static final String RATING = "rating";
            public static final String POPULARITY = "popularity";
            public static final String DATE = "changeDate";

            /**
             * Parameter name: {@value #TITLE} - Title not tokenized mainly used for sorting
             * purpose
             */
            public static final String TITLE = "title";
            public static final String SCALE_DENOMINATOR = "denominator";
        }
    }

    /**
     * Container for config elements that are inside the configuration file.
     */
    public static final class Config {
        public static final String HTMLCACHE_DIR = "htmlCacheDir";
        public static final String INDEX_CONFIG_DIR = "indexConfigDir";
        /**
         * Profiles of languages for autodetection using https://code.google.com/p/language-detection/.
         */
        public static final String LANGUAGE_PROFILES_DIR = "languageProfilesDir";
        public static final String MAX_SUMMARY_KEYS = "maxSummaryKeys";
        public static final String SCHEMA_MAPPINGS = "schemaMappings";
        public static final String LICENSE_DIR = "licenseDir";
        public static final String DATA_DIR = "dataDir";
        public static final String BACKUP_DIR = "backupDir";
        public static final String SCHEMAPLUGINS_DIR = "schemaPluginsDir";
        public static final String CODELIST_DIR = "codeListDir";
        public static final String NODE_LESS_DIR = "node_less_files";
        public static final String DIR = "dir";
        public static final String SUMMARY_CONFIG = "summaryConfig";
        public static final String LUCENE_CONFIG = "luceneConfig";
        public static final String GUI_CONFIG = "guiConfig";
        public static final String PREFERRED_SCHEMA = "preferredSchema";
        public static final String MAX_WRITES_IN_TRANSACTION = "maxWritesInTransaction";
        public static final String SUBVERSION_PATH = "subversionPath";
        public static final String STATUS_ACTIONS_CLASS = "statusActionsClass";
        public static final String CONFIG_DIR = "configDir";
        public static final String UPLOAD_DIR = "uploadDir";
        public static final String FORMATTER_PATH = "formatterPath";
        public static final String RESOURCES_DIR = "resources";
        public static final String SYSTEM_DATA_DIR = "geonetworkDataDir";
        public static final String HIDE_WITHHELD_ELEMENTS = "hidewithheldelements";
        public static final String DB_HEARTBEAT_ENABLED = "DBHeartBeatEnabled";
        public static final String DB_HEARTBEAT_INITIALDELAYSECONDS = "DBHeartBeatInitialDelaySeconds";
        public static final String DB_HEARTBEAT_FIXEDDELAYSECONDS = "DBHeartBeatFixedDelaySeconds";
        public static final String SCHEMA_PLUGINS_CATALOG_UPDATE = "createOrUpdateSchemaCatalog";
    }

    /**
     * Container for element values.
     */
    public static final class Text {
        public static final String ON = "on";
        public static final String OFF = "off";
        public static final String DOWN = "down";
        public static final String GRAPH_OVER = "graphOver";
    }

    /**
     * Codelist directories.
     */
    public static final class CodeList {
        public static final String LOCAL = "local";
        public static final String EXTERNAL = "external";
        public static final String REGISTER = "register";

        public static final String THESAURUS = "thesauri";
        public static final String CONTACT = "contactDirectories";
        public static final String CRS = "crs";
    }

    /**
     * Services.
     */
    public static final class Service {
        public static final String XML_LOGIN = "xml.user.login";
        public static final String XML_LOGOUT = "xml.user.logout";
        public static final String XML_INFO = "xml.info";
        public static final String XML_SEARCH = "xml.search";
        public static final String XML_METADATA_GET = "xml.metadata.get";
        public static final String XML_METADATA_RATE = "xml.metadata.rate";
        public static final String MEF_IMPORT = "mef.import";
        public static final String MEF_EXPORT = "mef.export";
    }

    public static final class Namespaces {
        public static final Namespace GCO = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
        public static final Namespace GEONET = Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork");
        public static final Namespace GMX = Namespace.getNamespace("gmx", "http://www.isotc211.org/2005/gmx");
        public static final Namespace GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        public static final Namespace OASIS_CATALOG = Namespace.getNamespace("urn:oasis:names:tc:entity:xmlns:xml:catalog");
        public static final Namespace SRV = Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
        public static final Namespace XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        public static final Namespace XSL = Namespace.getNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
        public static final Namespace XSD = Namespace.getNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        public static final Namespace XSI = Namespace.getNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        public static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");
        public static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");
        public static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        public static final Namespace GML32 = Namespace.getNamespace("gml", "http://www.opengis.net/gml/3.2");
        public static final Namespace SVRL = Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
        public static final Namespace SLD = Namespace.getNamespace("sld", "http://www.opengis.net/sld");
        public static final Namespace SE = Namespace.getNamespace("se", "http://www.opengis.net/se");
        public static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        public static final Namespace ATOM = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
    }

    public static class IndexFieldNames {
        public static final String HASXLINKS = "_hasxlinks";
        public static final String XLINK = "_xlink";
        public static final String ROOT = "_root";
        public static final String SCHEMA = "documentStandard";
        public static final String DATABASE_CREATE_DATE = "createDate";
        public static final String DATABASE_CHANGE_DATE = "changeDate";
        public static final String SOURCE = "_source";
        public static final String HARVESTUUID = "harvesterUuid";
        public static final String IS_TEMPLATE = "isTemplate";
        public static final String UUID = "uuid";
        public static final String IS_HARVESTED = "isHarvested";
        public static final String OWNER = "owner";
        public static final String OWNERNAME = "recordOwner";
        public static final String POPULARITY = "_popularity";
        public static final String RATING = "_rating";
        public static final String DISPLAY_ORDER = "_displayOrder";
        public static final String EXTRA = "_extra";
        public static final String USERINFO = "_userinfo";
        public static final String GROUP_OWNER = "groupOwner";
        public static final String GROUP_WEBSITE = "_groupWebsite";
        public static final String LOGO = "_logo";
        public static final String OP_PREFIX = "op";
        public static final String GROUP_PUBLISHED = "groupPublished";
        public static final String CAT = "_cat";
        public static final String STATUS = "mdStatus";
        public static final String STATUS_CHANGE_DATE = "mdStatusChangeDate";
        public static final String VALID = "_valid";
        public static final String ID = "id";
        public static final String VALID_INSPIRE = "_valid_inspire";
        public static final String ANY = "any";
        public static final String LOCALE = "locale";
        public static final String IS_PUBLISHED_TO_ALL = "isPublishedToAll";
        public static final String FEEDBACKCOUNT = "feedbackCount";
        public static final String DRAFT = "draft";
        public static final String DRAFT_ID = "draftId";
        public static final String RESOURCETITLE = "resourceTitle";
        public static final String RESOURCEABSTRACT = "resourceAbstract";
        public static final String PARENTUUID = "parentUuid";
        public static final String RECORDOPERATESON = "recordOperateOn";
        public static final String FEATUREOFRECORD = "featureOfRecord";
        public static final String RECORDLINKFLAG = "record";
        public static final String RECORDLINK = "recordLink";
        public static final String INSPIRE_REPORT_URL = "_inspireReportUrl";
        public static final String INSPIRE_VALIDATION_DATE = "_inspireValidationDate";
        public static final String STATUS_WORKFLOW = "statusWorkflow";
        public static final String USER_SAVED_COUNT = "userSavedCount";

        public static class RecordLink {
            public static final String ORIGIN = "origin";
            public static final String TO = "to";
            public static final String TYPE = "type";
            public static final String TITLE = "title";
            public static final String URL = "url";
        }
    }

    public static class SearchConfig {
        public static final String SEARCH_IGNORE_PORTAL_FILTER_OPTION = "ignorePortalFilter";
    }

    public static final class HttpProtocol {
        public static final String HTTP = "http";
        public static final String HTTPS = "https";
    }

    public static final class DefaultHttpPort {
        public static final int HTTP = 80;
        public static final int HTTPS = 443;
    }

}
