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

package org.fao.geonet.kernel.csw;

import jeeves.constants.ConfigFile;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CatalogConfiguration {

	// GetCapabilities variables
	private static int _numberOfKeywords = 10;
	private static int _maxNumberOfRecordsForKeywords = Integer.MAX_VALUE; 
	
	// GetDomain variables
	private static int _maxNumberOfRecordsForPropertyNames = Integer.MAX_VALUE;
	
	// GetRecords variables
	private static final HashMap<String, String> _fieldMapping = new HashMap<String, String>();
    private static final HashMap<String, HashMap<String, String>> _fieldMappingXPath = new HashMap<String, HashMap<String, String>>();
	private static final Set<String> _isoQueryables = new HashSet<String>();
	private static final Set<String> _additionalQueryables = new HashSet<String>();
	private static final Set<String> _getRecordsConstraintLanguage = new HashSet<String>();
	private static final Set<String> _getRecordsOutputFormat = new HashSet<String>();
	private static final Set<String> _getRecordsOutputSchema = new HashSet<String>();
	private static final Set<String> _getRecordsTypenames = new HashSet<String>();
	private static final Set<String> _getRecordsRangeFields = new HashSet<String>();
	
	// DescribeRecord variables
	private static final HashMap<String, String> _describeRecordTypenames = new HashMap<String, String>();
	private static final Set<Namespace> _describeRecordNamespaces = new HashSet<Namespace>();
	private static final Set<String> _describeRecordOutputFormat = new HashSet<String>();
	
	// GetRecordById variables
	private static boolean _increasePopularity = false;

	public static void loadCatalogConfig(String path, String configFile)
			throws Exception {
		configFile = path + File.separator + "WEB-INF" + File.separator + configFile;

		Log.info(Geonet.CSW, "Loading : " + configFile);

		Element configRoot = Xml.loadFile(configFile);

		List<Element> operationsList = configRoot.getChildren(Csw.ConfigFile.Child.OPERATIONS);

        for (Element element : operationsList) {
            initOperations(element);
        }

		// --- recurse on includes

		List includes = configRoot.getChildren(ConfigFile.Child.INCLUDE);

        for (Object include1 : includes) {
            Element include = (Element) include1;
            loadCatalogConfig(path, include.getText());
        }

	}

	private static void initOperations(Element element) {
		List<Element> operationLst = element.getChildren(Csw.ConfigFile.Operations.Child.OPERATION);

        for (Element operation : operationLst) {
            String operationName = operation.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME);

            if (operationName.equals(Csw.ConfigFile.Operation.Attr.Value.GET_CAPABILITIES)) {
                initCapabilities(operation);
                continue;
            }
            if (operationName.equals(Csw.ConfigFile.Operation.Attr.Value.GET_DOMAIN)) {
                initDomain(operation);
                continue;
            }
            if (operationName.equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORDS)) {
                initGetRecordsConfig(operation);
                continue;
            }
            if (operationName.equals(Csw.ConfigFile.Operation.Attr.Value.DESCRIBE_RECORD)) {
                initDescribeRecordConfig(operation);
            }
            
            if (operationName.equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORD_BY_ID)) {
                initGetRecordByIdConfig(operation);
            }
        }
	}

	private static void initGetRecordByIdConfig(Element operation) {
	    Element increasePopularityConfig = operation.getChild(Csw.ConfigFile.Operation.Child.INCREASE_POPULARITY);
        if (increasePopularityConfig != null && "yes".equals(increasePopularityConfig.getText())) {
            _increasePopularity = true;
        }
    }

    /**
	 * @param operation
	 */
	private static void initCapabilities(Element operation) {
		Element kn = operation.getChild(Csw.ConfigFile.Operation.Child.NUMBER_OF_KEYWORDS);
		if (kn != null && kn.getText()!= null)
			_numberOfKeywords = Integer.parseInt(kn.getText());

		kn = operation.getChild(Csw.ConfigFile.Operation.Child.MAX_NUMBER_OF_RECORDS_FOR_KEYWORDS);
		if (kn != null && kn.getText()!= null)
			_maxNumberOfRecordsForKeywords = Integer.parseInt(kn.getText());
	}

	/**
	 * @param operation
	 */
	private static void initDomain(Element operation) {
		Element kn = operation.getChild(Csw.ConfigFile.Operation.Child.MAX_NUMBER_OF_RECORDS_FOR_PROPERTY_NAMES);
		if (kn != null && kn.getText()!= null)
			_maxNumberOfRecordsForPropertyNames = Integer.parseInt(kn.getText());
	}

	/**
	 * @param operation
	 */
	private static void initDescribeRecordConfig(Element operation) {
		// Handle typename parameter list value
		List<Element> typenameList = getTypenamesConfig(operation);
		
		String name, prefix, uri, schema;
		Namespace namespace;

        for (Element typename : typenameList) {
            name = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.NAME);
            prefix = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.PREFIX);
            schema = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.SCHEMA);
            uri = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.NAMESPACE);
            namespace = Namespace.getNamespace(prefix, uri);
            _describeRecordNamespaces.add(namespace);
            _describeRecordTypenames.put(prefix + ":" + name, schema);
        }
		
		// Handle outputFormat parameter
		_describeRecordOutputFormat.addAll(getOutputFormat(operation));

	}

	private static List<Element> getTypenamesConfig(Element operation) {
		Element typenames = operation
				.getChild(Csw.ConfigFile.Operation.Child.TYPENAMES);

        return (List<Element>) typenames.getChildren(Csw.ConfigFile.Typenames.Child.TYPENAME);
	}

	private static void initGetRecordsConfig(Element operation) {
		// Only one child parameters
		Element params = operation
				.getChild(Csw.ConfigFile.Operation.Child.PARAMETERS);
		List<Element> paramsList = params.getChildren(Csw.ConfigFile.Parameters.Child.PARAMETER);
		Iterator<Element> it = paramsList.iterator();
		String name, field, type, range;
		while (it.hasNext()) {
			Element param = it.next();
			name = param
					.getAttributeValue(Csw.ConfigFile.Parameter.Attr.NAME);
			field = param
					.getAttributeValue(Csw.ConfigFile.Parameter.Attr.FIELD);
			type = param
					.getAttributeValue(Csw.ConfigFile.Parameter.Attr.TYPE);
			range = param
					.getAttributeValue(Csw.ConfigFile.Parameter.Attr.RANGE, "false");


            // TODO: OGC 07-45:
            // Case sensitivity is as follows: For the common queryables use the same case as defined in the base
            // specification (e.g. ‘apiso:title’), for the additional queryables use the cases as defined in this profile
            // (tables 9-14), e.g. ‘apiso:RevisionDate’.

			_fieldMapping.put(name.toLowerCase(), field);

			if (range.equals("true"))
				_getRecordsRangeFields.add(field);

			if (type.equals(Csw.ISO_QUERYABLES))
				_isoQueryables.add(name);
			else
				_additionalQueryables.add(name);

            // Load XPath mapping for queriables
            List<Element> xpathList =param.getChildren(Csw.ConfigFile.Parameter.Child.XPATH);
            Iterator<Element> itXPath = xpathList.iterator();
            String schema, path;
            HashMap<String, String> xpathMap = new HashMap<String, String>();
            while (itXPath.hasNext()) {
                Element xpath = itXPath.next();

                schema = xpath
                        .getAttributeValue(Csw.ConfigFile.XPath.Attr.SCHEMA);

                path = xpath
                        .getAttributeValue(Csw.ConfigFile.XPath.Attr.PATH);

                xpathMap.put(schema, path);

            }

            _fieldMappingXPath.put(name.toLowerCase(), xpathMap);
		}
		
		// OutputFormat parameter
		_getRecordsOutputFormat.addAll(getOutputFormat(operation));
		
		// ConstraintLanguage parameter
		Element constraintLanguageElt = operation.getChild(Csw.ConfigFile.Operation.Child.CONSTRAINT_LANGUAGE);
		List<Element> constraintLanguageList = constraintLanguageElt.getChildren(Csw.ConfigFile.ConstraintLanguage.Child.VALUE);
        for (Element constraint : constraintLanguageList) {
            String value = constraint.getText();
            _getRecordsConstraintLanguage.add(value);
        }
		
		// Handle typenames parameter list value
		List<Element> typenameList = getTypenamesConfig(operation);
		String tname, prefix, uri;
        for (Element typename : typenameList) {
            tname = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.NAME);
            prefix = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.PREFIX);
            uri = typename.getAttributeValue(Csw.ConfigFile.Typename.Attr.NAMESPACE);
            _getRecordsOutputSchema.add(uri);
            _getRecordsTypenames.add(prefix + ":" + tname);
        }
		
		
	}
	
	/**
	 * @param operation
	 * @return
	 */
	private static Set<String> getOutputFormat(Element operation) {
		Set<String> outformatList = new HashSet<String>();
		Element outputFormat = operation
				.getChild(Csw.ConfigFile.Operation.Child.OUTPUTFORMAT);

		List<Element> formatList = outputFormat.getChildren(Csw.ConfigFile.OutputFormat.Child.FORMAT);
		
		String format;
        for (Element currentFormat : formatList) {
            format = currentFormat.getText();
            outformatList.add(format);
        }
		return outformatList;
	}

	public static HashMap<String, String> getFieldMapping() {
		return _fieldMapping;
	}

    public static HashMap<String, HashMap<String, String>> getFieldMappingXPath() {
        return _fieldMappingXPath;
    }

	public static Set<String> getTypeMapping(String type) {
		// FIXME : handle not supported type throwing an exception
		if (type.equals(Csw.ISO_QUERYABLES))
			return _isoQueryables;
		else
			return _additionalQueryables;
	}
	
	
	
	// -------------
	//   Getters
	// -------------
	
	/**
	 * @return the _numberOfKeywords
	 */
	public static int getNumberOfKeywords() {
		return _numberOfKeywords;
	}

	/**
	 * @return the _maxNumberOfRecordsForKeywords
	 */
	public static int getMaxNumberOfRecordsForKeywords() {
		return _maxNumberOfRecordsForKeywords;
	}

	/**
	 * @return the _maxNumberOfRecordsForPropertyNames
	 */
	public static int getMaxNumberOfRecordsForPropertyNames() {
		return _maxNumberOfRecordsForPropertyNames;
	}

	/**
	 * @return the _describeRecordTypenames
	 */
	public static HashMap<String, String> getDescribeRecordTypename() {
		return _describeRecordTypenames;
	}
	
	/**
	 * @return the _describeRecordNamespaces
	 */
	public static Set<Namespace> getDescribeRecordNamespaces() {
		return _describeRecordNamespaces;
	}
	
	/**
	 * @return the _describeRecordOutputFormat
	 */
	public static Set<String> getDescribeRecordOutputFormat() {
		return _describeRecordOutputFormat;
	}

    /**
	 * @return the _getRecordsOutputFormat
	 */
	public static Set<String> getGetRecordsOutputFormat() {
		return _getRecordsOutputFormat;
	}

	/**
	 * @return the _getRecordsOutputSchema
	 */
	public static Set<String> getGetRecordsOutputSchema() {
		return _getRecordsOutputSchema;
	}

	/**
	 * @return the _getRecordsTypenames
	 */
	public static Set<String> getGetRecordsTypenames() {
		return _getRecordsTypenames;
	}

	/**
	 * @return the _getRecordsRangeFields
	 */
	public static Set<String> getGetRecordsRangeFields() {
		return _getRecordsRangeFields;
	}

    public static boolean is_increasePopularity() {
        return _increasePopularity;
    }

}
