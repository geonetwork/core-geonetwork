//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.csw.common;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.jdom.Namespace;

//=============================================================================

public class Csw
{
	//---------------------------------------------------------------------------
	//---
	//--- Namespaces
	//---
	//---------------------------------------------------------------------------

	public static final Namespace NAMESPACE_CSW = Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw");
	public static final Namespace NAMESPACE_OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");
	public static final Namespace NAMESPACE_OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");
	public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://www.w3.org/2003/05/soap-envelope");

	//---------------------------------------------------------------------------
	//---
	//--- Strings
	//---
	//---------------------------------------------------------------------------

	public static final String SCHEMA_LANGUAGE = "http://www.w3.org/XML/Schema";
	public static final String SERVICE         = "http://www.opengis.net/cat/csw";

	public static final String CSW_VERSION    = "2.0.1";
	public static final String OWS_VERSION    = "1.0.0";
	public static final String FILTER_VERSION = "1.1.0";

	//---------------------------------------------------------------------------
	//---
	//--- Section
	//---
	//---------------------------------------------------------------------------

	public enum Section { ServiceIdentification, ServiceProvider, OperationsMetadata, Filter_Capabilities }

	//---------------------------------------------------------------------------
	//---
	//--- TypeName
	//---
	//---------------------------------------------------------------------------

	public enum TypeName
	{
		DATASET("dataset"), DATASET_COLLECTION("datasetcollection"), SERVICE("service"), APPLICATION("application");

		//------------------------------------------------------------------------

		private TypeName(String typeName) { this.typeName = typeName;}

		//------------------------------------------------------------------------

		public String toString() { return typeName; }

		//------------------------------------------------------------------------

		public static Set<TypeName> parse(String typeNames) throws InvalidParameterValueEx
		{
			HashSet<TypeName> hs = new HashSet<TypeName>();

			if (typeNames != null)
			{
				StringTokenizer st = new StringTokenizer(typeNames, " ");

				while (st.hasMoreTokens())
				{
					String typeName = st.nextToken();

					if (typeName.equals(DATASET.toString()))
						hs.add(DATASET);

					else if (typeName.equals(DATASET_COLLECTION.toString()))
						hs.add(DATASET_COLLECTION);

					else if (typeName.equals(SERVICE.toString()))
						hs.add(SERVICE);

					else if (typeName.equals(APPLICATION.toString()))
						hs.add(APPLICATION);

					else throw new InvalidParameterValueEx("typeName", typeName);
				}
			}

			return hs;
		}

		//------------------------------------------------------------------------

		private String typeName;
	}

	//---------------------------------------------------------------------------
	//---
	//--- ElementSetName
	//---
	//---------------------------------------------------------------------------

	public enum ElementSetName
	{
		BRIEF("brief"), SUMMARY("summary"), FULL("full");

		//------------------------------------------------------------------------

		private ElementSetName(String setName) { this.setName = setName;}

		//------------------------------------------------------------------------

		public String toString() { return setName; }

		//------------------------------------------------------------------------

		public static ElementSetName parse(String setName) throws InvalidParameterValueEx
		{
			if (setName == null)								return FULL;
			if (setName.equals(BRIEF  .toString()))	return BRIEF;
			if (setName.equals(SUMMARY.toString())) 	return SUMMARY;
			if (setName.equals(FULL   .toString()))	return FULL;

			throw new InvalidParameterValueEx("elementSetName", setName);
		}

		//------------------------------------------------------------------------

		private String setName;
	}

	//---------------------------------------------------------------------------
	//---
	//--- ResultType
	//---
	//---------------------------------------------------------------------------

	public enum ResultType
	{
		HITS("hits"), RESULTS("results"), VALIDATE("validate");

		//------------------------------------------------------------------------

		private ResultType(String type) { this.type = type;}

		//------------------------------------------------------------------------

		public String toString() { return type; }

		//------------------------------------------------------------------------

		public static ResultType parse(String type) throws InvalidParameterValueEx
		{
			if (type == null)								return HITS;
			if (type.equals(HITS.toString()))		return HITS;
			if (type.equals(RESULTS.toString())) 	return RESULTS;
			if (type.equals(VALIDATE.toString()))	return VALIDATE;

			throw new InvalidParameterValueEx("resultType", type);
		}

		//------------------------------------------------------------------------

		private String type;
	}

	//---------------------------------------------------------------------------
	//---
	//--- OutputSchema
	//---
	//---------------------------------------------------------------------------

	public enum OutputSchema
	{
		OGC_CORE("Record"), ISO_PROFILE("IsoRecord");

		//------------------------------------------------------------------------

		private OutputSchema(String schema) { this.schema = schema;}

		//------------------------------------------------------------------------

		public String toString() { return schema; }

		//------------------------------------------------------------------------

		public static OutputSchema parse(String schema) throws InvalidParameterValueEx
		{
			if (schema == null)						return OGC_CORE;
			if (schema.equals("csw:Record"))		return OGC_CORE;
			if (schema.equals("csw:IsoRecord")) return ISO_PROFILE;

			throw new InvalidParameterValueEx("outputSchema", schema);
		}

		//------------------------------------------------------------------------

		private String schema;
	}

	//---------------------------------------------------------------------------
	//---
	//--- ConstraintLanguage
	//---
	//---------------------------------------------------------------------------

	public enum ConstraintLanguage
	{
		CQL("CQL_TEXT"), FILTER("FILTER");

		//------------------------------------------------------------------------

		private ConstraintLanguage(String language) { this.language = language;}

		//------------------------------------------------------------------------

		public String toString() { return language; }

		//------------------------------------------------------------------------

		public static ConstraintLanguage parse(String language) throws MissingParameterValueEx,
																							InvalidParameterValueEx
		{
			if (language == null)
				throw new MissingParameterValueEx("constraintLanguage");

			if (language.equals(CQL.toString()))		return CQL;
			if (language.equals(FILTER.toString()))	return FILTER;

			throw new InvalidParameterValueEx("constraintLanguage", language);
		}

		//------------------------------------------------------------------------

		private String language;
	}
}

//=============================================================================

