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
import jeeves.server.overrides.ConfigurationOverrides;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.*;

import javax.servlet.ServletContext;

public class CatalogConfiguration {

    // GetRecords variables
    private final HashMap<String, String> _fieldMapping = new HashMap<String, String>();
    private final HashMap<String, HashMap<String, String>> _fieldMappingXPath = new HashMap<String, HashMap<String, String>>();
    private final Set<String> _isoQueryables = new HashSet<String>();
    private final Set<String> _additionalQueryables = new HashSet<String>();
    private final Set<String> _getRecordsConstraintLanguage = new HashSet<String>();
    private final Set<String> _getRecordsOutputFormat = new HashSet<String>();
    private final Set<String> _getRecordsOutputSchema = new HashSet<String>();
    private final Set<String> _getRecordsTypenames = new HashSet<String>();
    private final Set<String> _getRecordsRangeFields = new HashSet<String>();
    // DescribeRecord variables
    private final HashMap<String, String> _describeRecordTypenames = new HashMap<String, String>();
    private final Set<Namespace> _describeRecordNamespaces = new HashSet<Namespace>();
    private final Set<String> _describeRecordOutputFormat = new HashSet<String>();
    // GetCapabilities variables
    private int _numberOfKeywords = 10;
    private int _maxNumberOfRecordsForKeywords = Integer.MAX_VALUE;
    // GetDomain variables
    private int _maxNumberOfRecordsForPropertyNames = Integer.MAX_VALUE;
    // GetRecordById variables
    private boolean _increasePopularity = false;

    @Autowired
    private GeonetworkDataDirectory _dataDir;
    @Autowired(required = false)
    private ServletContext _servletContext;
    @Autowired
    private SchemaManager _schemaManager;

    private volatile boolean initialized = false;


    public synchronized void init() {
        if (!initialized) {
            Log.info(Geonet.CSW, "  - Catalogue services for the web...");

            final Path webappDir = _dataDir.getWebappDir();
            try {
                loadCatalogConfig(webappDir, Csw.CONFIG_FILE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            initialized = true;
        }
    }

    private void loadCatalogConfig(Path path, String configFileName)
        throws Exception {
        Path configFile = path.resolve("WEB-INF").resolve(configFileName);

        Log.info(Geonet.CSW, "Loading : " + configFile);

        Element configRoot = Xml.loadFile(configFile);
        ConfigurationOverrides.DEFAULT.updateWithOverrides(configFile.toString(), _servletContext, _dataDir.getWebappDir(), configRoot);

        @SuppressWarnings("unchecked")
        List<Element> operationsList = configRoot.getChildren(Csw.ConfigFile.Child.OPERATIONS);

        for (Element element : operationsList) {
            initOperations(element);
        }

        // --- recurse on includes
        @SuppressWarnings("unchecked")
        List<Element> includes = configRoot.getChildren(ConfigFile.Child.INCLUDE);

        for (Element include : includes) {
            loadCatalogConfig(path, include.getText());
        }

    }

    private void initOperations(Element element) {
        @SuppressWarnings("unchecked")
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

    private void initGetRecordByIdConfig(Element operation) {
        Element increasePopularityConfig = operation.getChild(Csw.ConfigFile.Operation.Child.INCREASE_POPULARITY);
        if (increasePopularityConfig != null && "yes".equals(increasePopularityConfig.getText())) {
            _increasePopularity = true;
        }
    }

    /**
     * @param operation
     */
    private void initCapabilities(Element operation) {
        Element kn = operation.getChild(Csw.ConfigFile.Operation.Child.NUMBER_OF_KEYWORDS);
        if (kn != null && kn.getText() != null)
            _numberOfKeywords = Integer.parseInt(kn.getText());

        kn = operation.getChild(Csw.ConfigFile.Operation.Child.MAX_NUMBER_OF_RECORDS_FOR_KEYWORDS);
        if (kn != null && kn.getText() != null)
            _maxNumberOfRecordsForKeywords = Integer.parseInt(kn.getText());
    }

    /**
     * @param operation
     */
    private void initDomain(Element operation) {
        Element kn = operation.getChild(Csw.ConfigFile.Operation.Child.MAX_NUMBER_OF_RECORDS_FOR_PROPERTY_NAMES);
        if (kn != null && kn.getText() != null)
            _maxNumberOfRecordsForPropertyNames = Integer.parseInt(kn.getText());
    }

    /**
     * @param operation
     */
    private void initDescribeRecordConfig(Element operation) {
        // Handle typename parameter list value
        Map<String, Namespace> typenames = _schemaManager.getHmSchemasTypenames();

        for (Map.Entry<String, Namespace> entry : typenames.entrySet()) {
            String typeName = entry.getKey();
            Namespace ns = entry.getValue();
            String typename = typeName;
            // TODO: Schema plugin schema should be published in
            // /web/geonetwork/xml/validation/csw/2.0.2 for validation.
            String schema = ns.getPrefix().equals("csw") ? "record.xsd" : (
                ns.getPrefix().equals("gmd") ? "identification.xsd" : "unknown.xsd"
            );
            _describeRecordNamespaces.add(ns);
            _describeRecordTypenames.put(typename, schema);
        }
        // Handle outputFormat parameter
        _describeRecordOutputFormat.addAll(getOutputFormat(operation));

    }

    private void initGetRecordsConfig(Element operation) {
        // Only one child parameters
        Element params = operation
            .getChild(Csw.ConfigFile.Operation.Child.PARAMETERS);
        @SuppressWarnings("unchecked")
        List<Element> paramsList = params.getChildren(Csw.ConfigFile.Parameters.Child.PARAMETER);

        String name, field, type, range;
        for (Element param : paramsList) {
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
            @SuppressWarnings("unchecked")
            List<Element> xpathList = param.getChildren(Csw.ConfigFile.Parameter.Child.XPATH);
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

        @SuppressWarnings("unchecked")
        List<Element> constraintLanguageList = constraintLanguageElt.getChildren(Csw.ConfigFile.ConstraintLanguage.Child.VALUE);
        for (Element constraint : constraintLanguageList) {
            String value = constraint.getText();
            _getRecordsConstraintLanguage.add(value);
        }

        // Handle typenames parameter list value
        Map<String, Namespace> typenames = _schemaManager.getHmSchemasTypenames();
        for (Map.Entry<String, Namespace> entry : typenames.entrySet()) {
            String typeName = entry.getKey();
            Namespace ns = entry.getValue();
            String typename = ns.getPrefix() +
                ":" + typeName;
            _getRecordsOutputSchema.add(ns.getURI());
            _getRecordsTypenames.add(typename);
        }
    }

    /**
     * @param operation
     * @return
     */
    private Set<String> getOutputFormat(Element operation) {
        Set<String> outformatList = new HashSet<String>();
        Element outputFormat = operation
            .getChild(Csw.ConfigFile.Operation.Child.OUTPUTFORMAT);

        @SuppressWarnings("unchecked")
        List<Element> formatList = outputFormat.getChildren(Csw.ConfigFile.OutputFormat.Child.FORMAT);

        String format;
        for (Element currentFormat : formatList) {
            format = currentFormat.getText();
            outformatList.add(format);
        }
        return outformatList;
    }

    public HashMap<String, String> getFieldMapping() {
        init();
        return _fieldMapping;
    }

    public HashMap<String, HashMap<String, String>> getFieldMappingXPath() {
        init();
        return _fieldMappingXPath;
    }

    public Set<String> getTypeMapping(String type) {
        init();
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
    public int getNumberOfKeywords() {
        init();
        return _numberOfKeywords;
    }

    /**
     * @return the _maxNumberOfRecordsForKeywords
     */
    public int getMaxNumberOfRecordsForKeywords() {
        init();
        return _maxNumberOfRecordsForKeywords;
    }

    /**
     * @return the _maxNumberOfRecordsForPropertyNames
     */
    public int getMaxNumberOfRecordsForPropertyNames() {
        init();
        return _maxNumberOfRecordsForPropertyNames;
    }

    /**
     * @return the _describeRecordTypenames
     */
    public HashMap<String, String> getDescribeRecordTypename() {
        init();
        return _describeRecordTypenames;
    }

    /**
     * @return the _describeRecordNamespaces
     */
    public Set<Namespace> getDescribeRecordNamespaces() {
        init();
        return _describeRecordNamespaces;
    }

    /**
     * @return the _describeRecordOutputFormat
     */
    public Set<String> getDescribeRecordOutputFormat() {
        init();
        return _describeRecordOutputFormat;
    }

    /**
     * @return the _getRecordsOutputFormat
     */
    public Set<String> getGetRecordsOutputFormat() {
        init();
        return _getRecordsOutputFormat;
    }

    /**
     * @return the _getRecordsOutputSchema
     */
    public Set<String> getGetRecordsOutputSchema() {
        init();
        return _getRecordsOutputSchema;
    }

    /**
     * @return the _getRecordsTypenames
     */
    public Set<String> getGetRecordsTypenames() {
        init();
        return _getRecordsTypenames;
    }

    /**
     * @return the _getRecordsRangeFields
     */
    public Set<String> getGetRecordsRangeFields() {
        init();
        return _getRecordsRangeFields;
    }

    public boolean isIncreasePopularity() {
        init();
        return _increasePopularity;
    }

}
