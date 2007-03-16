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

	//--------------------------------------------------------------------------
	//--- container for file names

	public class File
	{
		public static final String SCHEMA              = "schema.xsd";
		public static final String SEARCH_LUCENE       = "lucene.xsl";
		public static final String SEARCH_Z3950_CLIENT = "z3950Client.xsl";
		public static final String SEARCH_Z3950_SERVER = "z3950Server.xsl";
		public static final String UPDATE_FIXED_INFO   = "update-fixed-info.xsl";
		public static final String EXTRACT_UUID        = "extract-uuid.xsl";
		public static final String EXTRACT_THUMBNAILS  = "extract-thumbnails.xsl";
		public static final String SET_THUMBNAIL       = "set-thumbnail.xsl";
		public static final String UNSET_THUMBNAIL     = "unset-thumbnail.xsl";
		public static final String SCHEMA_SUGGESTIONS  = "schema-suggestions.xml";
		public static final String CQL_TO_FILTER       = "cql-to-filter.xsl";
		public static final String FILTER_TO_LUCENE    = "filter-to-lucene.xsl";
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
		public static final String SUMMARY    = "summary";
		public static final String SITE_URL   = "siteURL";
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
		public static final String IMPORT_STYLESHEETS = "/xsl/conversion/import";
		public static final String LOGOS              = "/images/logos/";
	}

	//--------------------------------------------------------------------------
	//--- Session constants

	public class Session
	{
		public static final String MAIN_SEARCH   = "main.search";
		public static final String SEARCH_RESULT = "search.result";
		public static final String METADATA_SHOW = "metadata.show";
	}

	//--------------------------------------------------------------------------
	//--- resource names

	public class Res
	{
		public static final String MAIN_DB = "main-db";
	}

	//--------------------------------------------------------------------------
	//--- container for search elements

	public class SearchResult
	{
		public static final String TITLE         = "title";
		public static final String ABSTRACT      = "abstract";
		public static final String ANY           = "any";
		public static final String REGION        = "region";
		public static final String SOUTH_BL      = "southBL";
		public static final String NORTH_BL      = "northBL";
		public static final String EAST_BL       = "eastBL";
		public static final String WEST_BL       = "westBL";
		public static final String RELATION      = "relation";
		public static final String FROM          = "from";
		public static final String TO            = "to";
		public static final String GROUP         = "group";
		public static final String PROFILE       = "profile";
		public static final String SERVERS       = "servers";
		public static final String TIMEOUT       = "timeout";
		public static final String KEYWORDS      = "keywords";
		public static final String THEME_KEY     = "themekey";
		public static final String DOWNLOAD      = "download";
		public static final String ONLINE        = "online";
		public static final String DIGITAL       = "digital";
		public static final String PAPER         = "paper";
		public static final String CATEGORY      = "category";
		public static final String SITE_ID       = "siteId";
		public static final String TEMPLATE      = "template";
		public static final String EXTENDED      = "extended";
		public static final String HELP          = "help";
		public static final String REMOTE        = "remote";
		public static final String KEYWORD       = "keyword";
		public static final String SERVER        = "server";
		public static final String HITS_PER_PAGE = "hitsPerPage";

		//-----------------------------------------------------------------------

		public class Relation
		{
			public static final String EQUAL     = "equal";
			public static final String OVERLAPS  = "overlaps";
			public static final String ENCLOSES  = "encloses";
			public static final String OUTSIDEOF = "fullyOutsideOf";
		}
	}

	//--------------------------------------------------------------------------
	//--- container for profile names

	public class Profile
	{
		public static final String ADMINISTRATOR = "Administrator";
		public static final String GUEST         = "Guest";
	}

	//--------------------------------------------------------------------------
	//--- container for config elements that are inside the configuration file

	public class Config
	{
		public static final String LUCENE_DIR       = "luceneDir";
		public static final String MAX_SUMMARY_KEYS = "maxSummaryKeys";
		public static final String SCHEMA_MAPPINGS  = "schemaMappings";
		public static final String DATA_DIR         = "dataDir";
		public static final String DIR              = "dir";
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
	//--- logging

	public static final String GEONETWORK   = "geonetwork";
	public static final String HARVEST_MAN  = GEONETWORK + ".harvest-man";
	public static final String HARVESTER    = GEONETWORK + ".harvester";
	public static final String SETTINGS     = GEONETWORK + ".settings";
	public static final String DATA_MANAGER = GEONETWORK + ".datamanager";
	public static final String SEARCH_ENGINE= GEONETWORK + ".search";
	public static final String Z3950_SERVER = GEONETWORK + ".z3950server";
	public static final String INDEX_ENGINE = GEONETWORK + ".index";
	public static final String MEF          = GEONETWORK + ".mef";
	public static final String CSW          = GEONETWORK + ".csw";

	public static final String CSW_SEARCH   = CSW + ".search";

	//--------------------------------------------------------------------------
	//--- services

	public class Service
	{
		public static final String XML_LOGIN  = "xml.user.login";
		public static final String XML_LOGOUT = "xml.user.logout";
		public static final String XML_INFO   = "xml.info";
		public static final String XML_SEARCH = "xml.search";
		public static final String MEF_IMPORT = "mef.import";
		public static final String MEF_EXPORT = "mef.export";
	}
}

//=============================================================================

