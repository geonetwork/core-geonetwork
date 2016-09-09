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

package org.fao.geonet.csw.common;

import org.fao.geonet.constants.Geonet;
import org.jdom.Namespace;

/**
 * TODO javadoc.
 */
public final class Csw {
    //---------------------------------------------------------------------------
    //---
    //--- Namespaces
    //---
    //---------------------------------------------------------------------------

    public static final Namespace NAMESPACE_CSW = Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2");
    public static final Namespace NAMESPACE_CSW_OLD = Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw");
    public static final Namespace NAMESPACE_OGC = Geonet.Namespaces.OGC;
    public static final Namespace NAMESPACE_OWS = Geonet.Namespaces.OWS;
    public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://www.w3.org/2003/05/soap-envelope");
    public static final Namespace NAMESPACE_GMD = Geonet.Namespaces.GMD;
    public static final Namespace NAMESPACE_GFC = Namespace.getNamespace("gfc", "http://www.isotc211.org/2005/gfc");
    public static final Namespace NAMESPACE_GEONET = Geonet.Namespaces.GEONET;
    public static final Namespace NAMESPACE_DC = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
    public static final Namespace NAMESPACE_XSI = Geonet.Namespaces.XSI;
    public static final Namespace NAMESPACE_INSPIRE_DS = Namespace.getNamespace("inspire_ds", "http://inspire.ec.europa.eu/schemas/inspire_ds/1.0");
    public static final Namespace NAMESPACE_INSPIRE_COM = Namespace.getNamespace("inspire_common", "http://inspire.ec.europa.eu/schemas/common/1.0");
    public static final Namespace NAMESPACE_SRV = Geonet.Namespaces.SRV;

    //---------------------------------------------------------------------------
    //---
    //--- Strings
    //---
    //---------------------------------------------------------------------------
    public static final String OWS_SCHEMA_LOCATIONS = "http://schemas.opengis.net";
    public static final String SCHEMA_LANGUAGE = "http://www.w3.org/XML/Schema";
    public static final String SERVICE = "CSW";

    public static final String CSW_VERSION = "2.0.2";
    public static final String OWS_VERSION = "1.2.0";
    public static final String FILTER_VERSION_1_1 = "1.1.0";
    public static final String FILTER_VERSION_1_0 = "1.0.0";

    // Queryables
    public static final String ISO_QUERYABLES = "SupportedISOQueryables";
    public static final String ADDITIONAL_QUERYABLES = "AdditionalQueryables";

    // Sections
    public static final String SECTION_SI = "ServiceIdentification";
    public static final String SECTION_SP = "ServiceProvider";
    public static final String SECTION_OM = "OperationsMetadata";
    public static final String SECTION_FC = "Filter_Capabilities";

    public static final String OPERATION = "Operation";

    public static final String OUTPUT_FORMAT_APPLICATION_XML = "application/xml";

    //---------------------------------------------------------------------------
    //---
    //--- Configuration file
    //---
    //---------------------------------------------------------------------------

    public static final String CONFIG_FILE = "config-csw.xml";

    public static final class ConfigFile {

        public static final class Child {
            public static final String OPERATIONS = "operations";
        }

        // --------------------------------------------------------------------------
        // ---
        // --- Operations elements
        // ---
        // --------------------------------------------------------------------------

        public static final class Operations {
            public static final class Child {
                public static final String OPERATION = "operation";
            }
        }

        // --------------------------------------------------------------------------

        public static final class Operation {
            public static final class Attr {
                public static final String NAME = "name";

                public static final class Value {
                    public static final String GET_RECORDS = "GetRecords";
                    public static final String GET_CAPABILITIES = "GetCapabilities";
                    public static final String GET_DOMAIN = "GetDomain";
                    public static final String DESCRIBE_RECORD = "DescribeRecord";
                    public static final String GET_RECORD_BY_ID = "GetRecordById";
                    public static final String TRANSACTION = "Transaction";
                    public static final String HARVEST = "Harvest";
                }
            }

            public static final class Child {
                public static final String PARAMETERS = "parameters";
                public static final String TYPENAMES = "typenames";
                public static final String OUTPUTFORMAT = "outputformat";
                public static final String CONSTRAINT_LANGUAGE = "constraintLanguage";
                public static final String NUMBER_OF_KEYWORDS = "numberOfKeywords";
                public static final String MAX_NUMBER_OF_RECORDS_FOR_KEYWORDS = "maxNumberOfRecordsForKeywords";
                public static final String MAX_NUMBER_OF_RECORDS_FOR_PROPERTY_NAMES = "maxNumberOfRecordsForPropertyNames";
                public static final String INCREASE_POPULARITY = "increasePopularity";
            }
        }

        // --------------------------------------------------------------------------

        public static final class Parameters {
            public static final class Child {
                public static final String PARAMETER = "parameter";
            }
        }

        // --------------------------------------------------------------------------

        public static final class Parameter {
            public static final class Attr {
                public static final String NAME = "name";
                public static final String FIELD = "field";
                public static final String TYPE = "type";
                public static final String RANGE = "range";
                public static final String XPATH = "xpath";

            }

            public static final class Child {
                public static final String XPATH = "xpath";
            }
        }

        // --------------------------------------------------------------------------

        public static final class XPath {
            public static final class Attr {
                public static final String SCHEMA = "schema";
                public static final String PATH = "path";
            }
        }

        // --------------------------------------------------------------------------
        public static final class Typenames {
            public static final class Child {
                public static final String TYPENAME = "typename";
            }
        }

        // --------------------------------------------------------------------------

        public static final class Typename {
            public static final class Attr {
                public static final String NAME = "name";
                public static final String NAMESPACE = "namespace";
                public static final String PREFIX = "prefix";
                public static final String SCHEMA = "schema";
            }
        }

        // --------------------------------------------------------------------------

        public static final class OutputFormat {
            public static final class Child {
                public static final String FORMAT = "format";
            }
        }

        // --------------------------------------------------------------------------

        public static final class ConstraintLanguage {
            public static final class Child {
                public static final String VALUE = "value";
            }
        }
    }
}

//=============================================================================

