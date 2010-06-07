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
	public class Child
	{
		public static final String GENERAL     = "general";
		public static final String DEFAULT     = "default";
		public static final String RESOURCES   = "resources";
		public static final String APP_HANDLER = "appHandler";
		public static final String SERVICES    = "services";
		public static final String SCHEDULES   = "schedules";
		public static final String INCLUDE     = "include";
	}

	//--------------------------------------------------------------------------
	//---
	//--- General elements
	//---
	//--------------------------------------------------------------------------

	public class General
	{
		public class Child
		{
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
		public class Child
		{
			public static final String SERVICE      = "service";
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
		public class Child
		{
			public static final String RESOURCE = "resource";
		}
	}

	//--------------------------------------------------------------------------

	public class Resource
	{
		public class Attr
		{
			public static final String ENABLED = "enabled";
		}

		public class Child
		{
			public static final String NAME     = "name";
			public static final String PROVIDER = "provider";
			public static final String CONFIG   = "config";
			public static final String ACTIVATOR= "activator";
		}
	}

	//--------------------------------------------------------------------------

	public class Activator
	{
		public class Attr
		{
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
		public class Attr
		{
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
		public class Attr
		{
			public static final String PACKAGE = "package";
		}

		public class Child
		{
			public static final String SCHEDULE = "schedule";
		}
	}

	//--------------------------------------------------------------------------

	public class Schedule
	{
		public class Attr
		{
			public static final String NAME  = "name";
			public static final String CLASS = "class";
			public static final String WHEN  = "when";
		}

		public class Child
		{
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
		public class Attr
		{
			public static final String PACKAGE = "package";
		}

		public class Child
		{
			public static final String SERVICE = "service";
		}
	}

	//--------------------------------------------------------------------------

	public class Service
	{
		public class Attr
		{
			public static final String NAME  = "name";
			public static final String TYPE  = "type";
			public static final String MATCH = "match";
			public static final String SHEET = "sheet";
			public static final String CACHE = "cache";

			public class Type
			{
				public static final String HTML = "html";
				public static final String XML  = "xml";
			}
		}

		public class Child
		{
			public static final String CLASS  = "class";
			public static final String OUTPUT = "output";
			public static final String ERROR  = "error";
		}
	}

	//--------------------------------------------------------------------------

	public class Class
	{
		public class Attr
		{
			public static final String NAME = "name";
		}

		public class Child
		{
			public static final String PARAM = "param";
		}
	}

	//--------------------------------------------------------------------------

	public class Param
	{
		public class Attr
		{
			public static final String NAME  = "name";
			public static final String VALUE = "value";
		}
	}

	//--------------------------------------------------------------------------

	public class Output
	{
		public class Attr
		{
			public static final String TEST         = "test";
			public static final String SHEET        = "sheet";
			public static final String FORWARD      = "forward";
			public static final String FILE         = "file";
			public static final String BLOB         = "blob";
			public static final String CONTENT_TYPE = "contentType";
		}

		public class Child
		{
			public static final String XML  = "xml";
			public static final String CALL = "call";
		}
	}

	//--------------------------------------------------------------------------

	public class Error
	{
		public class Attr
		{
			public static final String ID           = "id";
			public static final String SHEET        = "sheet";
			public static final String CONTENT_TYPE = "contentType";
			public static final String STATUS_CODE  = "statusCode";
		}

		public class Child
		{
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
		public class Attr
		{
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
		public class Attr
		{
			public static final String NAME     = "name";
			public static final String CLASS    = "class";
		}
	}
}

//=============================================================================

