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

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.search.ISearchManager;
import org.fao.geonet.kernel.search.SolrSearchManager;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jeeves.server.context.ServiceContext;


@Component(CatalogService.BEAN_PREFIX + GetDomain.NAME)
public class GetDomain extends AbstractOperation implements CatalogService {
    static final String NAME = "GetDomain";

    @Autowired
    private ApplicationContext springAppContext;
    @Autowired
    private CatalogConfiguration _catalogConfig;

    public static List<Element> handlePropertyName(CatalogConfiguration catalogConfig, String[] propertyNames,
                                                   ServiceContext context, boolean freq, int maxRecords,
                                                   String cswServiceSpecificConstraint) throws Exception {

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
            ISearchManager sm = gc.getBean(ISearchManager.class);

            String query = "";
            String groupsQuery = getGroupsSolrQuery(context);
            if (StringUtils.isNotEmpty(cswServiceSpecificConstraint)) {
                String constraintQuery = "";
                // TODO SOLR-MIGRATION
                query = String.format("+(%s %s)", constraintQuery, groupsQuery);
            } else {
                query = groupsQuery;
            }

            String indexField = catalogConfig.getFieldMapping().get(
                property.toLowerCase());

            List<FacetField.Count> fieldValues = ((SolrSearchManager) sm).getDocFieldValues(
                indexField, query, false, null, null);

            boolean isRange = false;
            if (catalogConfig.getGetRecordsRangeFields().contains(property)) {
                isRange = true;
            }

            if (isRange) {
                listOfValues = new Element("RangeOfValues", Csw.NAMESPACE_CSW);
            } else {
                listOfValues = new Element("ListOfValues", Csw.NAMESPACE_CSW);
            }

            if (isRange) {
                // TODO: SOLR-MIGRATION
//                    valuesList.add(new Element("MinValue",Csw.NAMESPACE_CSW).setText(sortedValues.first()));
//                    valuesList.add(new Element("MaxValue",Csw.NAMESPACE_CSW).setText(sortedValues.last()));
            } else {
                for (FacetField.Count count : fieldValues) {
                    Element value = new Element("Value", Csw.NAMESPACE_CSW);
                    value.setAttribute("count", count.getCount() + "");
                    value.setText(count.getName());
                    listOfValues.addContent(value);
                }
            }

            if (listOfValues != null && listOfValues.getChildren().size() != 0) {
                domainValues.addContent(listOfValues);
            }
            domainValuesList.add(domainValues);
        }
        return domainValuesList;

    }

    //---------------------------------------------------------------------------

    public static String getGroupsSolrQuery(ServiceContext context) throws Exception {
        AccessManager am = context.getBean(AccessManager.class);
        Set<Integer> hs = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);


        String q = hs.stream().map(Object::toString).collect(Collectors.joining("\" \"", "(\"", "\")"));
        ;

        // If user is authenticated, add the current user to the query because
        // if an editor unchecked all
        // visible options in privileges panel for all groups, then the metadata
        // records could not be found anymore, even by its editor.
        if (context.getUserSession().getUserId() != null) {
            q += " _owner:" + context.getUserSession().getUserId();
        }
        return q;
    }

    //---------------------------------------------------------------------------

    public String getName() {
        return NAME;
    }

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
                    cswServiceSpecificConstraint);
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

    public Element retrieveValues(String parameterName) throws CatalogException {
        return null;
    }

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
