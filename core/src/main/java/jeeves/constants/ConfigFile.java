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

/**
 * This class is simply a container for constants used in the config.xml file
 */

public class ConfigFile {
    public static final class Child {

        public static final String GENERAL = "general";
        public static final String DEFAULT = "default";
        public static final String RESOURCES = "resources";
        public static final String APP_HANDLER = "appHandler";
        public static final String SERVICES = "services";
        public static final String INCLUDE = "include";
        public static final String MONITORS = "monitors";
        /**
         * Default constructor. Builds a Child.
         */
        private Child() {
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- General elements
    //---
    //--------------------------------------------------------------------------

    public static class General {
        public static final class Child {
            public static final String PROFILES = "profiles";
            public static final String DEBUG = "debug";
            public static final String UPLOAD_DIR = "uploadDir";
            public static final String MAX_UPLOAD_SIZE = "maxUploadSize";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Default elements
    //---
    //--------------------------------------------------------------------------

    public static class Default {
        public static final class Child {
            public static final String SERVICE = "service";
            public static final String STARTUPERRORSERVICE = "startupErrorService";
            public static final String LANGUAGE = "language";
            public static final String LOCALIZED = "localized";
            public static final String CONTENT_TYPE = "contentType";
            public static final String ERROR = "error";
            public static final String GUI = "gui";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Application handler
    //---
    //--------------------------------------------------------------------------

    public static class AppHandler {
        public static final class Attr {
            public static final String CLASS = "class";

            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }
    }

    public static class Monitors {
        public static final class Attr {
            public static final String PACKAGE = "package";
            public static final String CLASS = "class";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String CRITICAL_SERVICE_CONTEXT_HEALTH_CHECK = "criticalHealthCheck";
            public static final String WARNING_SERVICE_CONTEXT_HEALTH_CHECK = "warningHealthCheck";
            public static final String EXPENSIVE_SERVICE_CONTEXT_HEALTH_CHECK = "expensiveHealthCheck";
            public static final String SERVICE_CONTEXT_GAUGE = "gauge";
            public static final String SERVICE_CONTEXT_TIMER = "timer";
            public static final String SERVICE_CONTEXT_METER = "meter";
            public static final String SERVICE_CONTEXT_HISTOGRAM = "histogram";
            public static final String SERVICE_CONTEXT_COUNTER = "counter";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Schedule {
        public static final class Attr {
            public static final String NAME = "name";
            public static final String CLASS = "class";
            public static final String WHEN = "when";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String PARAM = "param";

            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service elements
    //---
    //--------------------------------------------------------------------------

    public static class Services {
        public static final class Attr {
            public static final String PACKAGE = "package";

            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String SERVICE = "service";

            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Service {
        public static final class Attr {
            public static final String NAME = "name";
            public static final String TYPE = "type";
            public static final String MATCH = "match";
            public static final String SHEET = "sheet";
            public static final String CACHE = "cache";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }

            public static final class Type {
                public static final String HTML = "html";
                public static final String XML = "xml";
                /**
                 * Default constructor. Builds a Type.
                 */
                private Type() {
                }
            }
        }

        public static final class Child {
            public static final String CLASS = "class";
            public static final String OUTPUT = "output";
            public static final String ERROR = "error";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Class {
        public static final class Attr {
            public static final String NAME = "name";

            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String PARAM = "param";

            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Param {
        public static final class Attr {
            public static final String NAME = "name";
            public static final String VALUE = "value";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Output {
        public static final class Attr {
            public static final String TEST = "test";
            public static final String SHEET = "sheet";
            public static final String FORWARD = "forward";
            public static final String FILE = "file";
            public static final String BLOB = "blob";
            public static final String CONTENT_TYPE = "contentType";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String XML = "xml";
            public static final String CALL = "call";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Error {
        public static final class Attr {
            public static final String ID = "id";
            public static final String SHEET = "sheet";
            public static final String CONTENT_TYPE = "contentType";
            public static final String STATUS_CODE = "statusCode";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }

        public static final class Child {
            public static final String XML = "xml";
            public static final String CALL = "call";
            /**
             * Default constructor. Builds a Child.
             */
            private Child() {
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Gui services
    //---
    //--------------------------------------------------------------------------

    public static class Xml {
        public static final class Attr {
            public static final String NAME = "name";
            public static final String FILE = "file";
            public static final String BASE = "base";
            public static final String LANGUAGE = "language";
            public static final String LOCALIZED = "localized";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class Call {
        public static final class Attr {
            public static final String NAME = "name";
            public static final String CLASS = "class";
            /**
             * Default constructor. Builds a Attr.
             */
            private Attr() {
            }
        }
    }

}

//=============================================================================

