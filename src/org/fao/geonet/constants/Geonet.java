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

package org.fao.geonet.constants;

import jeeves.constants.*;

//=============================================================================

public class Geonet
{
	public static final String CONTEXT_NAME = "contextName";
	public static final String DEFAULT_LANGUAGE = "en";
	
	//--------------------------------------------------------------------------
	//--- container for file names

	public class File
	{
		public static final String SCHEMA              = "schema.xsd";
		public static final String SCHEMATRON          = "schematron.xsl";
		public static final String SCHEMATRON_XML      = "schematron_xml.xsl";
		public static final String SCHEMATRON_VERBID   = "schematron_verbid.xsl";
		public static final String SEARCH_LUCENE       = "lucene.xsl";
		public static final String SEARCH_Z3950_CLIENT = "z3950Client.xsl";
		public static final String SEARCH_Z3950_SERVER = "z3950Server.xsl";
		public static final String UPDATE_FIXED_INFO   = "update-fixed-info.xsl";
		public static final String EXTRACT_UUID        = "extract-uuid.xsl";
		public static final String SET_UUID            = "set-uuid.xsl";
		public static final String EXTRACT_THUMBNAILS  = "extract-thumbnails.xsl";
		public static final String SET_THUMBNAIL       = "set-thumbnail.xsl";
		public static final String UNSET_THUMBNAIL     = "unset-thumbnail.xsl";
		public static final String SCHEMA_SUGGESTIONS  = "schema-suggestions.xml";
		public static final String SCHEMA_SUBSTITUTES  = "schema-substitutes.xml";
		
		/**
		 * Stylesheet to convert a CQL parameter to a filter.
		 */
		public static final String CQL_TO_FILTER       = "cql-to-filter.xsl";
		public static final String FILTER_TO_LUCENE    = "filter-to-lucene.xsl";
		public static final String LICENSE_ANNEX       = "license-annex.html";
		public static final String LICENSE_ANNEX_XSL   = "metadata-license-annex.xsl";
		public static final String METADATA_BRIEF      = "metadata-brief.xsl";
	}

	public class SchemaPath
	{
		public static final String OAI_PMH = "xml/validation/oai/OAI-PMH.xsd";
	}

	//--------------------------------------------------------------------------
	//--- container for elements

	public class Elem
	{
		public static final String DOMAINS    = "domains";
		public static final String GROUPS     = "groups";
		public static final String GROUP      = "group";
		public static final String CATEGORIES = "categories";
		public static final String CATEGORY   = "category";
		public static final String REGIONS    = "regions";
		public static final String RECORD     = "record";
		public static final String ID         = "id";
		public static final String ON         = "on";
		public static final String NAME       = "name";
		public static final String SURNAME    = "surname";
		public static final String PROFILE    = "profile";
		public static final String OPERATIONS = "operations";
		public static final String OPER       = "oper";
		public static final String SHOWVALIDATIONERRORS   = "showvalidationerrors";
		public static final String SUMMARY    = "summary";
		public static final String SITE_URL   = "siteURL";
		public static final String APP_PATH   = "path";
	}

	//--------------------------------------------------------------------------
	//--- container for element attribs

	public class Attr
	{
	}

	//--------------------------------------------------------------------------
	//--- resource directory and search configuration file

	public class Path
	{
		public static final String SCHEMAS            = Jeeves.Path.XML + "schemas/";
		public static final String CSW                = Jeeves.Path.XML + "csw/";
		public static final String VALIDATION         = Jeeves.Path.XML + "validation/";
		public static final String STYLESHEETS        = "/xsl";
		public static final String CONV_STYLESHEETS   = STYLESHEETS + "/conversion";
		public static final String IMPORT_STYLESHEETS = CONV_STYLESHEETS + "/import";
		public static final String LOGOS              = "/images/logos/";
	}

	//--------------------------------------------------------------------------
	//--- Session constants

	public class Session
	{
		public static final String MAIN_SEARCH				= "main.search";
		public static final String SEARCH_RESULT			= "search.result";
		public static final String SEARCH_REQUEST_ID 		= "search_request_id";		
		public static final String METADATA_SHOW			= "metadata.show";
		public static final String METADATA_EDITING		= "metadata.editing";
		public static final String METADATA_POSITION  = "metadata.position";
		public static final String SEARCH_KEYWORDS_RESULT	= "search.keywords.result";
		public static final String SELECTED_RESULT          = "selected.result";
		public static final String VALIDATION_REPORT = "validation.report";
		public static final String METADATA_ISO19110		= "metadata.iso19110";
		public static final String FC_ISO19110		        = "fc.iso19110";
	}

	//--------------------------------------------------------------------------
	//--- resource names

	public class Res
	{
		public static final String MAIN_DB = "main-db";
	}

	/**
	 * Parameters that can be used in searches. 
	 * See the parameters for a more complete description.
	 * @see ../services.util.MainUtil.getDefaultSearch for
	 * default values.
	 */
	public class SearchResult
	{
        /** Parameter name: {@value #TITLE} - Free text field that searches
         * in the title */
		public static final String TITLE         = "title";
		
		/** Parameter name: {@value #ABSTRACT} - Free text field that searches
		 * in the abstract */
		public static final String ABSTRACT      = "abstract";
        
		/** Parameter name: {@value #ANY} - Free text field that searches
         * in all the text fields of a metadata record */
		public static final String ANY           = "any";
		public static final String PHRASE        = "phrase";
		public static final String OR            = "or";
		public static final String WITHOUT       = "without";
        
		/** Parameter name: {@value #REGION} - Index value of a region. 
		 * Used to retrieve the name and bounding box of the selected region */
        public static final String REGION        = "region";
        
        /** Parameter name: {@value #SOUTH_BL} - Lowest Latitude value in 
         * floating point format (geographic coordinate) 
         * Default value is {@code -90} */
		public static final String SOUTH_BL      = "southBL";
        
		/** Parameter name: {@value #NORTH_BL} - Highest Latitude value in 
         * floating point format (geographic coordinate) 
         * Default value is {@code 90} */
		public static final String NORTH_BL      = "northBL";
        
		/** Parameter name: {@value #EAST_BL} - Highest Longitude value in 
         * floating point format (geographic coordinate) 
         * Default value is {@code 180} */
		public static final String EAST_BL       = "eastBL";
        
		/** Parameter name: {@value #WEST_BL} - Lowest Longitude value in 
         * floating point format (geographic coordinate) 
         * Default value is {@code -180} */
		public static final String WEST_BL       = "westBL";
		
        /** Parameter name: {@value #RELATION} - Defines the type of spatial
         * query matching used
         * See {@link Relation} for possible values 
         * Default value is {@code {@value Relation#OVERLAPS}} */
		public static final String RELATION      = "relation";
		
        /** Parameter name: {@value #DATE_FROM} - Start date from when the 
         * referenced resource was updated. 
         * Formatted as <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>.
         * For example 1970-08-19T06:01:00 or 1970-08-19 */
		public static final String DATE_FROM     = "dateFrom";

        /** Parameter name: {@value #DATE_TO} - End date until when the 
         * referenced resource was updated. 
         * Formatted as <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>.
         * For example 2008-01-23T10:05:00 or 2008-01-23 */
		public static final String DATE_TO       = "dateTo";

        public static final String THEME_KEY     = "themekey";
        public static final String CATEGORY      = "category";

        /** Parameter name: {@value #TOPIC_CAT} - Restrict search to resources
         * that have the requested Topic Category set (ISO19115) */
        public static final String TOPIC_CAT     = "topicCat";
        
        /** Parameter name: {@value #PROTOCOL} - Searches the protocol field 
         * that's part of the online resources in ISO19115. Suggested values
         * are those listed in the localized strings.xml files at
         * /strings/protocolChoice/@value */
        public static final String PROTOCOL      = "protocol";
        
        /** Parameter name: {@value #DOWNLOAD} - Boolean that restricts results 
         * to those resources that have a files for download based on 
         * protocol values as defined in the Lucene indexing stylesheets. 
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
        public static final String DOWNLOAD      = "download";

        /** Parameter name: {@value #DYNAMIC} - Boolean that restricts results 
         * to those resources that have an interactive resource associated based
         * on protocol values as defined in the Lucene indexing stylesheets. 
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
		public static final String DYNAMIC       = "dynamic";
		
        /** Parameter name: {@value #DIGITAL} - Boolean that restricts results 
         * to those resources that describe digital data based on ISO19115
         * CI_PresentationFormCode codes
         * Exact values indexed are defined in the Lucene indexing stylesheets. 
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
		public static final String DIGITAL       = "digital";
		
        /** Parameter name: {@value #PAPER} - Boolean that restricts results 
         * to those resources that describe Hardcopy data based on ISO19115
         * CI_PresentationFormCode codes
         * Exact values indexed are defined in the Lucene indexing stylesheets. 
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
		public static final String PAPER         = "paper";
		
        /** Parameter name: {@value #SITE_ID} - Limit search results to resources 
         * that originate from the selected catalog. The Site's short name 
         * should be used as value */
		public static final String SITE_ID       = "siteId";
        
		/** Parameter name: {@value #GROUP} - Limit search results to resources 
		 * that are administered by the selected group. The group ID should be 
		 * used as value */
        public static final String GROUP         = "group";
        
        public static final String PROFILE       = "profile";
        public static final String SERVER        = "server";
        public static final String SERVERS       = "servers";
		
        /** Parameter name: {@value #TEMPLATE} - Boolean that defines if 
         * normal resources are searched or templates are searched
         * Values are {@code y} or {@code n} */
        public static final String TEMPLATE      = "template";
        
        /** Parameter name: {@value #EXTENDED} - Boolean that indicates if 
         * search is done in simple or Advanced mode.
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
        public static final String EXTENDED      = "extended";

        /** Parameter name: {@value #REMOTE} - Boolean that indicates if 
         * search is done on the local repository or using Z39.50 for on the 
         * fly searches in remote catalogs. Values are 
         * {@value org.fao.geonet.constants.Geonet.Text#ON} or 
         * {@value org.fao.geonet.constants.Geonet.Text#OFF} (default) */
        public static final String REMOTE        = "remote";
        
        /** Parameter name: {@value #TIMEOUT} - Time in seconds the Z39.50
         * search waits for responses from remote servers before timing out.
         * Default is 20 seconds */
        public static final String TIMEOUT       = "timeout";

	    /** Parameter name: {@value #HITS_PER_PAGE} - Number of results
	     * returned by the search engine. Default is 10 results */
		public static final String HITS_PER_PAGE = "hitsPerPage";
		
		/** Parameter name: {@value #SIMILARITY} - Use the Lucene FuzzyQuery.
		 * Values range from 0.0 to 1.0 and defaults to 0.8 */
        public static final String SIMILARITY    = "similarity";
		
		/** Parameter name: {@value #OUTPUT} - Display results as text only 
		 * {@value #TEXT} or with graphic overviews {@value #FULL} (default) */
        public static final String OUTPUT        = "output";

        /** Parameter name: {@value #SORT_BY} - Order results by 
         * {@value SortBy#RELEVANCE} (default), {@value SortBy#RATING}, 
         * {@value SortBy#POPULARITY} or by {@value SortBy#DATE} */
        public static final String SORT_BY       = "sortBy";

        /** Parameter name: {@value #SORT_ORDER} - Order results in reverse order or not 
         * false (default) */
        public static final String SORT_ORDER       = "sortOrder";

		/** Parameter name: {@value #INTERMAP} - Boolean that indicates if 
         * GUI shows the embedded InterMap (on) or defaults to the old GUI (off).
         * Values are {@value org.fao.geonet.constants.Geonet.Text#ON} (default)
         * or {@value org.fao.geonet.constants.Geonet.Text#OFF} */
        public static final String INTERMAP      = "intermap";
        
        /** Parameter name: {@value #GEOMETRY} - Used to filter results of query based on geometry
         * Currently intersection is used to do the filtering
         * 
         * The geometry values a geometry expressed in WKT*/
        public static final String GEOMETRY = "geometry";
        
        /** Parameter name: {@value #UUID} - Text field that search 
         * for specific uuid given */
        public static final String UUID = "uuid";

		public static final String RESULT_TYPE = "resultType";

		//-----------------------------------------------------------------------

        public class ResultType 
        {
            public static final String RESULTS                  = "results";
            public static final String HITS                     = "hits";
            public static final String VALIDATE                 = "validate";
            /**
             * Contains CSW results response with a GeoNetwork summary
             * of the current search. 
             */
            public static final String RESULTS_WITH_SUMMARY     = "results-with-summary";
        }

		//-----------------------------------------------------------------------

		public class Relation
		{
			public static final String EQUAL     = "equal";
			public static final String OVERLAPS  = "overlaps";
			public static final String ENCLOSES  = "encloses";
			public static final String OUTSIDEOF = "fullyOutsideOf";
			public static final String INTERSECTION = "intersection";
            public static final String CROSSES = "crosses";
            public static final String TOUCHES = "touches";
            public static final String WITHIN = "within";
		}

		//-----------------------------------------------------------------------

		public class Output
		{
			public static final String FULL = "full";
			public static final String TEXT = "text";
		}

		//-----------------------------------------------------------------------

		public class SortBy
		{
			public static final String RELEVANCE = "relevance";
			public static final String RATING    = "rating";
			public static final String POPULARITY= "popularity";
			public static final String DATE      = "changeDate";

			/** Parameter name: {@value #_TITLE} - Title not tokenized mainly
			 * used for sorting purpose */
			public static final String TITLE     = "title";
		}
	}

	//--------------------------------------------------------------------------
	//--- container for profile names

	public class Profile
	{
		public static final String ADMINISTRATOR = "Administrator";
		public static final String USER_ADMIN    = "UserAdmin";
		public static final String REVIEWER      = "Reviewer";
		public static final String EDITOR        = "Editor";
		public static final String GUEST         = "Guest";
	}

	//--------------------------------------------------------------------------
	//--- container for config elements that are inside the configuration file

	public class Config
	{
		public static final String HTMLCACHE_DIR    = "htmlCacheDir";
		public static final String LUCENE_DIR       = "luceneDir";
		public static final String MAX_SUMMARY_KEYS = "maxSummaryKeys";
		public static final String SCHEMA_MAPPINGS  = "schemaMappings";
		public static final String LICENSE_DIR      = "licenseDir";
		public static final String DATA_DIR         = "dataDir";
		public static final String CODELIST_DIR  	= "codeListDir";
		public static final String DIR              = "dir";
		public static final String SUMMARY_CONFIG   = "summaryConfig";
	}

	//--------------------------------------------------------------------------
	//--- container for element values

	public class Text
	{
		public static final String ON         = "on";
		public static final String OFF        = "off";
		public static final String DOWN       = "down";
		public static final String GRAPH_OVER = "graphOver";
	}

	//--------------------------------------------------------------------------
	//--- Codelist directories

	public class CodeList
	{
		public static final String LOCAL              = "local";
		public static final String EXTERNAL           = "external";

		public static final String THESAURUS          = "thesauri";
		public static final String CONTACT            = "contactDirectories";
		public static final String CRS                = "crs";
	}

	//--------------------------------------------------------------------------
	//--- logging

	public static final String GEONETWORK   = "geonetwork";
	public static final String HARVEST_MAN  = GEONETWORK + ".harvest-man";
	public static final String HARVESTER    = GEONETWORK + ".harvester";
	public static final String SETTINGS     = GEONETWORK + ".settings";
	public static final String DATA_MANAGER = GEONETWORK + ".datamanager";
	public static final String THESAURUS_MAN= GEONETWORK + ".thesaurus-man";
	public static final String SEARCH_ENGINE= GEONETWORK + ".search";
	public static final String Z3950_SERVER = GEONETWORK + ".z3950server";
	public static final String INDEX_ENGINE = GEONETWORK + ".index";
	public static final String MEF          = GEONETWORK + ".mef";
	public static final String CSW          = GEONETWORK + ".csw";
	public static final String LDAP         = GEONETWORK + ".ldap";
	public static final String EDITOR				= GEONETWORK + ".editor";
	public static final String EDITORADDELEMENT = GEONETWORK + ".editoraddelement";
	public static final String EDITOREXPANDELEMENT = GEONETWORK + ".editorexpandelement";
	public static final String SPATIAL      = GEONETWORK + ".spatial";
	public static final String CSW_SEARCH   = CSW + ".search";
	
	//--------------------------------------------------------------------------
	//--- services

	public class Service
	{
		public static final String XML_LOGIN         = "xml.user.login";
		public static final String XML_LOGOUT        = "xml.user.logout";
		public static final String XML_INFO          = "xml.info";
		public static final String XML_SEARCH        = "xml.search";
		public static final String XML_METADATA_GET  = "xml.metadata.get";
		public static final String XML_METADATA_RATE = "xml.metadata.rate";
		public static final String MEF_IMPORT        = "mef.import";
		public static final String MEF_EXPORT        = "mef.export";
	}
}

//=============================================================================

