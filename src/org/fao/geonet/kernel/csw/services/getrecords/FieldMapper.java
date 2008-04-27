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

package org.fao.geonet.kernel.csw.services.getrecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.jdom.Element;

//==============================================================================

class FieldMapper
{
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static String map(String field)
    {
	return hmMapping.get(getAbsolute(field));
    }

    //---------------------------------------------------------------------------

    public static Iterable<String> getMappedFields()
    {
	ArrayList<String> al = new ArrayList<String>();

	for(String[] couple : mapping)
	    al.add(couple[1]);

	return al;
    }

    //---------------------------------------------------------------------------

    public static boolean match(Element elem, Set<String> elemNames)
    {
	String name = elem.getQualifiedName();

	for (String field : elemNames)
	    if (getAbsolute(field).equals(name))
		return true;

	return false;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private static String getAbsolute(String field)
    {
	if (field.startsWith("./"))
	    return field.substring(2);

	return field;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    private static String[][] mapping =
    {
	{ "dc:identifier", "identifier" },
	{ "dc:title",      "title"      },
	{ "dct:abstract",  "abstract"   },
	{ "dct:modified",  "changeDate" },
	{ "dc:subject",    "keyword"    },
	{ "dc:type",       "type"       },
	{ "dct:spatial",   "crs"        },
	{ "csw:AnyText",   "any"        },
	{ "any",           "any"        },
	{ "dc:format",     "format"     },
	{ "dc:relation",   "relation"   },
	{ "dct:spatial",   "spatial"    },
	{ "dc:date",       "createDate" },

	{ "FileIdentifier",        "fileId"      },
	{ "Language",              "language"    },
	{ "AlternateTitle",        "altTitle"    },
	{ "CreationDate",          "createDate"  },
	{ "OrganisationName",      "orgName"     },
	{ "HasSecurityConstraints","secConstr"   },
	{ "HierarchyLevelName",    "levelName"   },
	{ "ParentIdentifier",      "parentId"    },
	{ "KeywordType",           "keywordType" },

	{ "TopicCategory",            "topicCat"        },
	{ "DatasetLanguage",          "datasetLang"     },
	{ "GeographicDescriptionCode","geoDescCode"     },
	{ "TempExtent_begin",         "tempExtentBegin" },
	{ "TempExtent_end",           "tempExtentEnd"   },
	{ "Denominator",              "denominator"     },
	{ "DistanceValue",            "distanceVal"     },
	{ "DistanceUOM",              "distanceUom"     },

	//--- these are needed just to avoid a warning when converting field names
	//--- from CSW names -> lucene names

	{ "northBL", "northBL" },
	{ "southBL", "southBL" },
	{ "eastBL",  "eastBL"  },
	{ "westBL",  "westBL"  }
    };

    //---------------------------------------------------------------------------

    private static HashMap<String, String> hmMapping = new HashMap<String, String>();

    //---------------------------------------------------------------------------

    static
    {
	for(String[] couple : mapping)
	    hmMapping.put(couple[0], couple[1]);
    }
}

//==============================================================================

