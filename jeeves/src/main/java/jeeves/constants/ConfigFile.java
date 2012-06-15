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

/** This class is simply a container for constants used in the config.xml file
  */

public class ConfigFile
{
	public final class Child
	{

        /**
		 * Default constructor.
		 * Builds a Child.
		 */
		private Child() {}
		
	   public static final String GENERAL     = "general";
		public static final String DEFAULT     = "default";
		public static final String RESOURCES   = "resources";
		public static final String APP_HANDLER = "appHandler";
		public static final String SERVICES    = "services";
		public static final String SCHEDULES   = "schedules";
		public static final String INCLUDE     = "include";
        public static final String MONITORS    = "monitors";
	}

	//--------------------------------------------------------------------------
	//---
	//--- General elements
	//---
	//--------------------------------------------------------------------------

	public class General
	{
		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String PROFILES   = "profiles";
			public static final String DEBUG      = "debug";
			public static final String UPLOAD_DIR = "uploadDir";
			public static final String MAX_UPLOAD_SIZE = "maxUploadSize";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Default elements
	//---
	//--------------------------------------------------------------------------

	public class Default
	{
		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		  public static final String SERVICE      = "service";
		  public static final String STARTUPERRORSERVICE = "startupErrorService";
			public static final String LANGUAGE     = "language";
			public static final String LOCALIZED    = "localized";
			public static final String CONTENT_TYPE = "contentType";
			public static final String ERROR        = "error";
			public static final String GUI          = "gui";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Resources elements
	//---
	//--------------------------------------------------------------------------

	public class Resources
	{
		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String RESOURCE = "resource";
		}
	}

	//--------------------------------------------------------------------------

	public class Resource
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String ENABLED = "enabled";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String NAME     = "name";
			public static final String PROVIDER = "provider";
			public static final String CONFIG   = "config";
			public static final String ACTIVATOR= "activator";
		}
	}

	//--------------------------------------------------------------------------

	public class Activator
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String CLASS = "class";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Application handler
	//---
	//--------------------------------------------------------------------------

	public class AppHandler
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String CLASS = "class";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Schedule elements
	//---
	//--------------------------------------------------------------------------

	public class Schedules
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
			public static final String PACKAGE = "package";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String SCHEDULE = "schedule";
		}
	}
    public class Monitors {
        public final class Attr
        {
            /**
             * Default constructor.
             * Builds a Attr.
             */
            private Attr() {}

            public static final String PACKAGE = "package";
            public static final String CLASS = "class";
        }

        public final class Child
        {
            /**
             * Default constructor.
             * Builds a Child.
             */
            private Child() {}

            public static final String CRITICAL_SERVICE_CONTEXT_HEALTH_CHECK = "criticalHealthCheck";
            public static final String WARNING_SERVICE_CONTEXT_HEALTH_CHECK = "warningHealthCheck";
            public static final String EXPENSIVE_SERVICE_CONTEXT_HEALTH_CHECK = "expensiveHealthCheck";
            public static final String SERVICE_CONTEXT_GAUGE = "gauge";
            public static final String SERVICE_CONTEXT_TIMER = "timer";
            public static final String SERVICE_CONTEXT_METER = "meter";
            public static final String SERVICE_CONTEXT_HISTOGRAM = "histogram";
            public static final String SERVICE_CONTEXT_COUNTER = "counter";
        }
    }

	//--------------------------------------------------------------------------

	public class Schedule
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME  = "name";
			public static final String CLASS = "class";
			public static final String WHEN  = "when";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String PARAM = "param";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service elements
	//---
	//--------------------------------------------------------------------------

	public class Services
	{
		public final class Attr
		{
		   /**
		    * Default constructor.
		    * Builds a Attr.
		    */
		   private Attr() {}
		   
		   public static final String PACKAGE = "package";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String SERVICE = "service";
		}
	}

	//--------------------------------------------------------------------------

	public class Service
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME  = "name";
			public static final String TYPE  = "type";
			public static final String MATCH = "match";
			public static final String SHEET = "sheet";
			public static final String CACHE = "cache";

			public final class Type
			{
				/**
				 * Default constructor.
				 * Builds a Type.
				 */
				private Type() {}
				
			   public static final String HTML = "html";
				public static final String XML  = "xml";
			}
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String CLASS  = "class";
			public static final String OUTPUT = "output";
			public static final String ERROR  = "error";
		}
	}

	//--------------------------------------------------------------------------

	public class Class
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME = "name";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String PARAM = "param";
		}
	}

	//--------------------------------------------------------------------------

	public class Param
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME  = "name";
			public static final String VALUE = "value";
		}
	}

	//--------------------------------------------------------------------------

	public class Output
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String TEST         = "test";
			public static final String SHEET        = "sheet";
			public static final String FORWARD      = "forward";
			public static final String FILE         = "file";
			public static final String BLOB         = "blob";
			public static final String CONTENT_TYPE = "contentType";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String XML  = "xml";
		   public static final String CALL = "call";
           public static final String PRE_SHEET = "preSheet";
		}
	}

	//--------------------------------------------------------------------------

	public class Error
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String ID           = "id";
			public static final String SHEET        = "sheet";
			public static final String CONTENT_TYPE = "contentType";
			public static final String STATUS_CODE  = "statusCode";
		}

		public final class Child
		{
			/**
			 * Default constructor.
			 * Builds a Child.
			 */
			private Child() {}
			
		   public static final String XML  = "xml";
			public static final String CALL = "call";
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Gui services
	//---
	//--------------------------------------------------------------------------

	public class Xml
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME      = "name";
			public static final String FILE      = "file";
			public static final String BASE      = "base";
			public static final String LANGUAGE  = "language";
			public static final String LOCALIZED = "localized";
		}
	}

	//--------------------------------------------------------------------------

	public class Call
	{
		public final class Attr
		{
			/**
			 * Default constructor.
			 * Builds a Attr.
			 */
			private Attr() {}
			
		   public static final String NAME     = "name";
			public static final String CLASS    = "class";
		}
	}

}

//=============================================================================

