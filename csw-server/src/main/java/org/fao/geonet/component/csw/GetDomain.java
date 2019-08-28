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

package org.fao.geonet.component.csw;

import bak.pcj.map.ObjectKeyIntMapIterator;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.CatalogSearcher;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SummaryComparator;
import org.fao.geonet.kernel.search.SummaryComparator.SortOption;
import org.fao.geonet.kernel.search.SummaryComparator.Type;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.fao.geonet.kernel.search.LuceneSearcher.parseLuceneQuery;

//=============================================================================
@Component(CatalogService.BEAN_PREFIX + GetDomain.NAME)
public class GetDomain extends AbstractOperation implements CatalogService {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    static final String NAME = "GetDomain";
    @Autowired
    private LuceneConfig _luceneConfig;

    @Autowired
    private ApplicationContext springAppContext;
    @Autowired
    private CatalogConfiguration _catalogConfig;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static List<Element> handlePropertyName(CatalogConfiguration catalogConfig, String[] propertyNames,
                                                   ServiceContext context, boolean freq, int maxRecords,
                                                   String cswServiceSpecificConstraint,
                                                   LuceneConfig luceneConfig) throws Exception {

        List<Element> domainValuesList = new ArrayList<Element>();

        if (Log.isDebugEnabled(Geonet.CSW))
            Log.debug(Geonet.CSW, "Handling property names '" + Arrays.toString(propertyNames) + "' with max records of " + maxRecords);

        for (int i = 0; i < propertyNames.length; i++) {


            // Initialize list of values element.
            Element listOfValues = null;

            // Generate DomainValues element
            Element domainValues = new Element("DomainValues", Csw.NAMESPACE_CSW);

            // FIXME what should be the type ???
            domainValues.setAttribute("type", "csw:Record");

            String property = propertyNames[i].trim();

            // Set propertyName in any case.
            Element pn = new Element("PropertyName", Csw.NAMESPACE_CSW);
            domainValues.addContent(pn.setText(property));

            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            SearchManager sm = gc.getBean(SearchManager.class);


            IndexAndTaxonomy indexAndTaxonomy = sm.getNewIndexReader(null);
            try {
                GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
                BooleanQuery groupsQuery = (BooleanQuery) CatalogSearcher.getGroupsQuery(context);
                BooleanQuery query = null;

                // Apply CSW service specific constraint
                if (StringUtils.isNotEmpty(cswServiceSpecificConstraint)) {
                    Query constraintQuery = parseLuceneQuery(cswServiceSpecificConstraint, luceneConfig);

                    query = new BooleanQuery();

                    BooleanClause.Occur occur = LuceneUtils
                        .convertRequiredAndProhibitedToOccur(true, false);

                    query.add(groupsQuery, occur);
                    query.add(constraintQuery, occur);

                } else {
                    query = groupsQuery;
                }

                List<Pair<String, Boolean>> sortFields = Collections.singletonList(Pair.read(Geonet.SearchResult.SortBy.RELEVANCE, true));
                Sort sort = LuceneSearcher.makeSort(sortFields, context.getLanguage(), false);
                CachingWrapperFilter filter = null;

                Pair<TopDocs, Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(
                    maxRecords, 0, maxRecords, context.getLanguage(),
                    null, luceneConfig, reader,
                    query, filter, sort, null, false
                );
                TopDocs hits = searchResults.one();

                try {
                    // Get mapped lucene field in CSW configuration
                    String indexField = catalogConfig.getFieldMapping().get(
                        property.toLowerCase());
                    if (indexField != null)
                        property = indexField;

                    // check if params asked is in the index using getFieldNames ?
                    @SuppressWarnings("resource")
                    FieldInfos fi = SlowCompositeReaderWrapper.wrap(reader).getFieldInfos();
                    if (fi.fieldInfo(property) == null)
                        continue;

                    boolean isRange = false;
                    if (catalogConfig.getGetRecordsRangeFields().contains(
                        property))
                        isRange = true;

                    if (isRange)
                        listOfValues = new Element("RangeOfValues", Csw.NAMESPACE_CSW);
                    else
                        listOfValues = new Element("ListOfValues", Csw.NAMESPACE_CSW);

                    Set<String> fields = new HashSet<String>();
                    fields.add(property);
                    fields.add("_isTemplate");


                    // parse each document in the index
                    String[] fieldValues;
                    Collator stringCollator = Collator.getInstance();
                    stringCollator.setStrength(Collator.PRIMARY);
                    SortedSet<String> sortedValues = new TreeSet<String>(stringCollator);
                    ObjectKeyIntOpenHashMap duplicateValues = new ObjectKeyIntOpenHashMap();
                    for (int j = 0; j < hits.scoreDocs.length; j++) {
                        DocumentStoredFieldVisitor selector = new DocumentStoredFieldVisitor(fields);
                        reader.document(hits.scoreDocs[j].doc, selector);
                        Document doc = selector.getDocument();

                        // Skip templates and subTemplates
                        String[] isTemplate = doc.getValues("_isTemplate");
                        if (isTemplate[0] != null && !isTemplate[0].equals("n"))
                            continue;

                        // Get doc values for specified property
                        fieldValues = doc.getValues(property);
                        if (fieldValues == null)
                            continue;

                        addtoSortedSet(sortedValues, fieldValues, duplicateValues);
                    }

                    SummaryComparator valuesComparator = new SummaryComparator(SortOption.FREQUENCY, Type.STRING, context.getLanguage(), null);
                    TreeSet<SummaryComparator.SummaryElement> sortedValuesFrequency = new TreeSet<SummaryComparator.SummaryElement>(valuesComparator);
                    ObjectKeyIntMapIterator entries = duplicateValues.entries();

                    while (entries.hasNext()) {
                        entries.next();
                        sortedValuesFrequency.add(new SummaryComparator.SummaryElement(entries));
                    }

                    if (freq)
                        return createValuesByFrequency(sortedValuesFrequency);
                    else
                        listOfValues.addContent(createValuesElement(sortedValues, isRange));

                } finally {
                    // any children means that the catalog was unable to determine
                    // anything about the specified parameter
                    if (listOfValues != null && listOfValues.getChildren().size() != 0)
                        domainValues.addContent(listOfValues);

                    // Add current DomainValues to the list
                    domainValuesList.add(domainValues);
                }
            } finally {
                sm.releaseIndexReader(indexAndTaxonomy);
            }
        }
        return domainValuesList;

    }

    //---------------------------------------------------------------------------

    /**
     * @param sortedValues
     * @param fieldValues
     * @param duplicateValues
     */
    private static void addtoSortedSet(SortedSet<String> sortedValues,
                                       String[] fieldValues, ObjectKeyIntOpenHashMap duplicateValues) {
        for (String value : fieldValues) {
            sortedValues.add(value);
            if (duplicateValues.containsKey(value)) {
                int nb = duplicateValues.get(value);
                duplicateValues.remove(value);
                duplicateValues.put(value, nb + 1);
            } else
                duplicateValues.put(value, 1);
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Create value element for each item of the string array
     */
    private static List<Element> createValuesElement(SortedSet<String> sortedValues, boolean isRange) {
        List<Element> valuesList = new ArrayList<Element>();
        if (!isRange) {
            for (String value : sortedValues) {
                valuesList.add(new Element("Value", Csw.NAMESPACE_CSW).setText(value));
            }
        } else {
            valuesList.add(new Element("MinValue", Csw.NAMESPACE_CSW).setText(sortedValues.first()));
            valuesList.add(new Element("MaxValue", Csw.NAMESPACE_CSW).setText(sortedValues.last()));
        }
        return valuesList;
    }

    //---------------------------------------------------------------------------

    /**
     * @param sortedValuesFrequency
     * @return
     */
    private static List<Element> createValuesByFrequency(TreeSet<SummaryComparator.SummaryElement> sortedValuesFrequency) {

        List<Element> values = new ArrayList<Element>();
        Element value;

        for (SummaryComparator.SummaryElement element : sortedValuesFrequency) {
            value = new Element("Value", Csw.NAMESPACE_CSW);
            value.setAttribute("count", Integer.toString(element.count));
            value.setText(element.name);

            values.add(value);
        }
        return values;
    }

    //---------------------------------------------------------------------------

    public String getName() {
        return NAME;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public Element execute(Element request, ServiceContext context) throws CatalogException {
        checkService(request);
        checkVersion(request);

        Element response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);

        String[] propertyNames = getParameters(request, "PropertyName");
        String[] parameterNames = getParameters(request, "ParameterName");


        String cswServiceSpecificConstraint = request.getChildText(Geonet.Elem.FILTER);

        // PropertyName handled first.
        if (propertyNames != null) {
            List<Element> domainValues;
            try {
                final int maxNumberOfRecordsForPropertyNames = _catalogConfig.getMaxNumberOfRecordsForPropertyNames();
                domainValues = handlePropertyName(_catalogConfig, propertyNames, context, false, maxNumberOfRecordsForPropertyNames,
                    cswServiceSpecificConstraint, _luceneConfig);
            } catch (Exception e) {
                Log.error(Geonet.CSW, "Error getting domain value for specified PropertyName : " + e);
                throw new NoApplicableCodeEx(
                    "Raised exception while getting domain value for specified PropertyName  : " + e);
            }
            response.addContent(domainValues);
            return response;
        }

        if (parameterNames != null) {
            List<Element> domainValues = handleParameterName(parameterNames);
            response.addContent(domainValues);
        }

        return response;
    }

    //---------------------------------------------------------------------------

    public Element adaptGetRequest(Map<String, String> params) {
        String service = params.get("service");
        String version = params.get("version");
        String parameterName = params.get("parametername");
        String propertyName = params.get("propertyname");

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "version", version);

        //--- these 2 are in mutual exclusion.
        Element propName = new Element("PropertyName", Csw.NAMESPACE_CSW).setText(propertyName);
        Element paramName = new Element("ParameterName", Csw.NAMESPACE_CSW).setText(parameterName);

        // Property is handled first.
        if (propertyName != null && !propertyName.equals(""))
            request.addContent(propName);
        else if (parameterName != null && !parameterName.equals(""))
            request.addContent(paramName);

        return request;
    }

    //---------------------------------------------------------------------------

    public Element retrieveValues(String parameterName) throws CatalogException {
        return null;
    }

    //---------------------------------------------------------------------------

    private List<Element> handleParameterName(String[] parameterNames) throws CatalogException {
        Element values;
        List<Element> domainValuesList = null;

        for (int i = 0; i < parameterNames.length; i++) {

            if (i == 0) domainValuesList = new ArrayList<Element>();

            // Generate DomainValues element
            Element domainValues = new Element("DomainValues", Csw.NAMESPACE_CSW);

            // FIXME what should be the type ???
            domainValues.setAttribute("type", "csw:Record");

            String paramName = parameterNames[i];

            // Set parameterName in any case.
            Element pn = new Element("ParameterName", Csw.NAMESPACE_CSW);
            domainValues.addContent(pn.setText(paramName));

            String operationName = paramName.substring(0, paramName.indexOf('.'));
            String parameterName = paramName.substring(paramName.indexOf('.') + 1);

            CatalogService cs = checkOperation(operationName);
            values = cs.retrieveValues(parameterName);

            // values null mean that the catalog was unable to determine
            // anything about the specified parameter
            if (values != null)
                domainValues.addContent(values);

            // Add current DomainValues to the list
            domainValuesList.add(domainValues);

        }
        return domainValuesList;
    }

    //---------------------------------------------------------------------------

    private CatalogService checkOperation(String operationName)
        throws CatalogException {

        CatalogService cs = springAppContext.getBean(CatalogService.BEAN_PREFIX + operationName, CatalogService.class);

        if (cs == null)
            throw new OperationNotSupportedEx(operationName);

        return cs;
    }

    //---------------------------------------------------------------------------

    private String[] getParameters(Element request, String parameter) {
        if (request == null)
            return null;

        Element paramElt = request.getChild(parameter, Csw.NAMESPACE_CSW);

        if (paramElt == null)
            return null;

        String parameterName = paramElt.getText();

        return parameterName.split(",");
    }

}

//=============================================================================

