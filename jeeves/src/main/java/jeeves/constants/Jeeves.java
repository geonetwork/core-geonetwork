//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.constants;


//=============================================================================

public final class Jeeves
{
	/**
    * Default constructor.
    * Builds a Jeeves.
    */
   private Jeeves() {}
   
   public static final int MAX_UPLOAD_SIZE = 50; // 50 MB

	public static final String CONFIG_FILE = "config.xml";

	public static final String XML_CATALOG_FILES = "jeeves.xml.catalog.files";
	public static final String XML_CATALOG_VERBOSITY = "jeeves.xml.catalog.verbosity";

    public static final String XML_CATALOG_BLANKXSLFILE = "jeeves.xml.catalog.blankxslfile";

	//--------------------------------------------------------------------------
	//---
	//--- Url prefixes
	//---
	//--------------------------------------------------------------------------

	public final class Prefix
	{
		/**
		 * Default constructor.
		 * Builds a Prefix.
		 */
		private Prefix() {}
		
	   public static final String SERVICE = "srv";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Element constants
	//---
	//--------------------------------------------------------------------------

	public final class Elem
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Elem.
       */
	   private Elem() {}
      
	   public static final String ROOT      = "root";
		public static final String RESPONSE  = "response";
		public static final String RECORD    = "record";
		public static final String OPERATION = "operation";
		public static final String GUI       = "gui";
		public static final String REQUEST   = "request";
		public static final String STRINGS   = "strings";
		public static final String SERVICES  = "services";
		public static final String SERVICE   = "service";
		public static final String PROFILES  = "profiles";
		public static final String FORWARD   = "forward";
		public static final String ERROR     = "error";
		public static final String SESSION   = "session";

		public static final String BASE_URL     = "url";
		public static final String LOC_URL      = "locUrl";
		public static final String BASE_SERVICE = "service";
		public static final String LOC_SERVICE  = "locService";
		public static final String LANGUAGE     = "language";
		public static final String REQ_SERVICE  = "reqService";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Container class for text inside elements / other strings
	//---
	//--------------------------------------------------------------------------

	public final class Text
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Text.
       */
	   private Text() {}
	   
	   public static final String TRUE    = "true";
		public static final String FALSE   = "false";
		public static final String ADDED   = "added";
		public static final String REMOVED = "removed";
		public static final String UPDATED = "updated";
		public static final String GUI_SERVICE  = "guiService";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Attribute constants
	//---
	//--------------------------------------------------------------------------

	public final class Attr
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Attr.
       */
	   private Attr() {}
	   
	   public static final String ID          = "id";
		public static final String PATH        = "path";
		public static final String MANDATORY   = "mandatory";
		public static final String FORWARD     = "forward";
		public static final String TYPE        = "type";
		public static final String FORMAT      = "format";
		public static final String NULL        = "null";
		public static final String VALUE       = "value";
		public static final String NAME        = "name";
		public static final String SERVICE     = "service";
		public static final String PATTERN     = "pattern";
		public static final String REPLACEMENT = "replacement";

		//--- values of the 'id' attribute in the error element

		public final class Id
		{
			/**
          * Default constructor.
          * Builds a Jeeves.Attr.Id.
          */
		   private Id() {}
         
		   public static final String ERROR   = "error";
		}

		//--- values of the type's attribute

		public final class Type
		{
			/**
          * Default constructor.
          * Builds a Jeeves.Attr.Type.
          */
		   private Type() {}
		   
		   public static final String STRING  = "string";
			public static final String INT     = "int";
			public static final String DOUBLE  = "double";
			public static final String DATE    = "date";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Directory and file names
	//---
	//--------------------------------------------------------------------------

	public final class Path
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Path.
       */
	   private Path() {}
      
	   public static final String XML = "xml/";
		public static final String XSL = "xsl/";
		public static final String WEBINF = "WEB-INF/";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Elements found in services configuration
	//---
	//--------------------------------------------------------------------------

	public final class Config
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Config.
       */
	   private Config() {}
      
	   public static final String FILE       = "file";
		public static final String SHEET      = "sheet";
		public static final String IN_FIELDS  = "inFields";
		public static final String OUT_FIELDS = "outFields";
		public static final String FIELD      = "field";
		public static final String DB         = "db";
		public static final String TABLE      = "table";
		public static final String KEY        = "key";
		public static final String WHERE      = "where";
		public static final String ORDER      = "order";
		public static final String QUERY      = "query";
		public static final String REQFIELDS  = "reqFields";
		public static final String FORMATS    = "formats";
		public static final String GROUP      = "group";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Elements found in resources configuration
	//---
	//--------------------------------------------------------------------------

	public class Res
	{
		public final class Pool
		{
			/**
          * Default constructor.
          * Builds a Jeeves.Res.Pool.
          */
		   private Pool() {}
		   
		   public static final String USER           = "user";
			public static final String PASSWORD       = "password";
			public static final String DRIVER         = "driver";
			public static final String URL            = "url";
			public static final String POOL_SIZE      = "poolSize";
			public static final String MAX_TRIES      = "maxTries";
			public static final String MAX_WAIT       = "maxWait";
			public static final String VALIDATION_QUERY = "validationQuery";
			public static final int DEF_POOL_SIZE      = 10;
			public static final int DEF_MAX_WAIT       = 200; // msecs between attempts
			public static final String MAX_IDLE = "maxIdle";
			public static final String MIN_IDLE = "minIdle";
			public static final String MAX_ACTIVE = "maxActive";
			public static final String TEST_WHILE_IDLE = "testWhileIdle";
			public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
			public static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
			public static final String NUM_TESTS_PER_EVICTION_RUN = "numTestsPerEvictionRun";
			public static final String CONTEXT = "context";
			public static final String RESOURCE_NAME = "resourceName";
			public static final String PROVIDE_DATA_STORE = "provideDataStore";
			public static final String MAX_OPEN_PREPARED_STATEMENTS = "maxOpenPreparedStatements";
			public static final String TRANSACTION_ISOLATION = "defaultTransactionIsolation";
			public static final String TRANSACTION_ISOLATION_READ_COMMITTED = "READ_COMMITTED";
			public static final String TRANSACTION_ISOLATION_SERIALIZABLE = "SERIALIZABLE";
			public static final String TRANSACTION_ISOLATION_REPEATABLE_READ = "REPEATABLE_READ";
		}
	}

	//--------------------------------------------------------------------------
	//--- xml parameters for modules

	public final class Param
	{
		/**
       * Default constructor.
       * Builds a Jeeves.Param.
       */
      private Param() {}
      
	   public static final String MODULE = "module";
	}
}

//=============================================================================

