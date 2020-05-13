//==============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search parameters that can be provided by a search client.
 *
 * @author heikki doeleman
 * @author francois prunayre
 */
public class UserQueryInput {

    /**
     * List of fields which MUST not be control by user. Those fields are always removed from the
     * request.
     */
    public static final List<String> SECURITY_FIELDS = Arrays.asList(
        SearchParameter.OWNER,
        SearchParameter.ISADMIN,
        SearchParameter.ISREVIEWER,
        SearchParameter.ISUSERADMIN);

    /**
     * Don't take into account those field in search (those field are not indexed but are search
     * options).
     *
     * TODO : move to lucene-config.xml because those fields depends on the client
     */
    public static final List<String> RESERVED_FIELDS = Arrays.asList(
        SearchParameter.GROUP,
        SearchParameter.GROUPEDIT,
        Geonet.SearchResult.FAST,
        Geonet.SearchResult.SORT_BY,
        Geonet.SearchResult.SORT_ORDER,
        Geonet.SearchResult.EXTENDED,
        Geonet.SearchResult.INTERMAP,
        Geonet.SearchResult.HITS_PER_PAGE,
        Geonet.SearchResult.GEOMETRY,
        Geonet.SearchResult.OUTPUT,
        Geonet.SearchResult.SUMMARY_ONLY,
        Geonet.SearchResult.BUILD_SUMMARY,
        Geonet.SearchResult.REQUESTED_LANGUAGE,
        "region_simple", "attrset", "mode",
        "region", "from", "to", "hitsperpage", "georss"
    );
    /**
     * List of fields to exclude from language detection. TODO: should be lucene-config.xml
     */
    public static final List<String> NO_TEXT_FIELDS = Arrays.asList(
        SearchParameter.UUID,
        SearchParameter.PARENTUUID,
        SearchParameter.OPERATESON,
        SearchParameter._SCHEMA,
        SearchParameter.ROOT,
        SearchParameter.ISTEMPLATE,
        SearchParameter.RESULTTYPE,
        SearchParameter.RELATION,
        SearchParameter.SITEID,
        SearchParameter.TYPE,
        SearchParameter.HASFEATURECAT
    );
    // TODO Improve and move to config-lucene.xml in order to be able
    // to add range field from configuration
    public static final List<String> RANGE_QUERY_FIELDS = Arrays.asList(
        SearchParameter.DATEFROM,
        SearchParameter.DATETO,
        SearchParameter.REVISIONDATEFROM,
        SearchParameter.REVISIONDATETO,
        SearchParameter.PUBLICATIONDATEFROM,
        SearchParameter.PUBLICATIONDATETO,
        SearchParameter.DENOMINATORFROM,
        SearchParameter.DENOMINATORTO,
        SearchParameter.DENOMINATOR,
        SearchParameter.RATING,
        SearchParameter.RATINGFROM,
        SearchParameter.RATINGTO,
        SearchParameter.FEEDBACKCOUNT,
        SearchParameter.FEEDBACKCOUNTFROM,
        SearchParameter.FEEDBACKCOUNTTO,
        SearchParameter.CREATIONDATEFROM,
        SearchParameter.CREATIONDATETO);
    /**
     * TODO : use enum instead ?
     */
    private static final List<String> RANGE_FIELDS = Arrays.asList(
        LuceneIndexField.CHANGE_DATE,
        LuceneIndexField.REVISION_DATE,
        LuceneIndexField.PUBLICATION_DATE,
        LuceneIndexField.CREATE_DATE,
        LuceneIndexField.DENOMINATOR,
        SearchParameter.RATING,
        SearchParameter.FEEDBACKCOUNT);
    private static final List<String> RANGE_FIELDS_FROM = Arrays.asList(
        SearchParameter.DATEFROM,
        SearchParameter.REVISIONDATEFROM,
        SearchParameter.PUBLICATIONDATEFROM,
        SearchParameter.CREATIONDATEFROM,
        SearchParameter.DENOMINATORFROM,
        SearchParameter.RATINGFROM,
        SearchParameter.FEEDBACKCOUNTFROM
        );
    private static final List<String> RANGE_FIELDS_TO = Arrays.asList(
        SearchParameter.DATETO,
        SearchParameter.REVISIONDATETO,
        SearchParameter.PUBLICATIONDATETO,
        SearchParameter.CREATIONDATETO,
        SearchParameter.DENOMINATORTO,
        SearchParameter.RATINGTO,
        SearchParameter.FEEDBACKCOUNTTO);
    private static Map<String, String> searchParamToLuceneField = new LinkedHashMap<String, String>();

    static {
        // Populate map for search parameter to Lucene mapping
        searchParamToLuceneField.put(SearchParameter.SITEID, LuceneIndexField.SOURCE);
        searchParamToLuceneField.put(SearchParameter.INSPIRE, LuceneIndexField.INSPIRE_CAT);
        searchParamToLuceneField.put(SearchParameter.THEMEKEY, LuceneIndexField.KEYWORD);
        searchParamToLuceneField.put(SearchParameter.TOPICCATEGORY, LuceneIndexField.TOPIC_CATEGORY);
        searchParamToLuceneField.put(SearchParameter.CATEGORY, LuceneIndexField.CAT);
        searchParamToLuceneField.put(SearchParameter.OP_VIEW, LuceneIndexField._OP0);
        searchParamToLuceneField.put(SearchParameter.OP_DOWNLOAD, LuceneIndexField._OP1);
        searchParamToLuceneField.put(SearchParameter.OP_EDITING, LuceneIndexField._OP2);
        searchParamToLuceneField.put(SearchParameter.OP_NOTIFY, LuceneIndexField._OP3);
        searchParamToLuceneField.put(SearchParameter.OP_DYNAMIC, LuceneIndexField._OP5);
        searchParamToLuceneField.put(SearchParameter.OP_FEATURED, LuceneIndexField._OP6);
    }

    private String similarity;
    private String editable;
    private Map<String, Set<String>> searchCriteria = new LinkedHashMap<String, Set<String>>();
    private Map<String, Set<String>> searchPrivilegeCriteria = new LinkedHashMap<String, Set<String>>();
    private Map<String, String> searchOption = new LinkedHashMap<String, String>();
    private Set<String> facetQueries = new LinkedHashSet<String>();

    /**
     * Creates this from a JDOM element.
     *
     * @param jdom input
     */
    public UserQueryInput(Element jdom) {

        // Done in LuceneSearcher#computeQuery
        // protectRequest(jdom);

        for (Object e : jdom.getChildren()) {
            if (e instanceof Element) {
                Element node = (Element) e;
                String nodeName = node.getName();
                String nodeValue = StringUtils.trim(node.getText());
                if (SearchParameter.SIMILARITY.equals(nodeName)) {
                    setSimilarity(jdom.getChildText(SearchParameter.SIMILARITY));
                } else if (Geonet.SearchResult.EXTRA_DUMP_FIELDS.equals(nodeName)) {
                    // ignore this.  it is for presentation
                } else {
                    if (StringUtils.isNotBlank(nodeValue)) {
                        // Handles operation parameters. These parameters are safe, because
                        // the fields have been sanitized before (in LuceneSearcher.java:713)
                        if (nodeName.startsWith("_operation")) {
                            addValues(searchCriteria, searchParamToLuceneField.get(nodeName), nodeValue);
                        } else if (SECURITY_FIELDS.contains(nodeName) || nodeName.contains("_op")) {
                            addValues(searchPrivilegeCriteria, nodeName, nodeValue);
                        } else if (RESERVED_FIELDS.contains(nodeName)) {
                            searchOption.put(nodeName, nodeValue);
                        } else if (isFacetQuery(nodeName)) {
                            facetQueries.add(nodeValue);
                        } else {
                            // addValues(searchCriteria, nodeName, nodeValue);
                            // Rename search parameter to lucene index field
                            // when needed
                            addValues(
                                searchCriteria,
                                (searchParamToLuceneField
                                    .containsKey(nodeName) ? searchParamToLuceneField
                                    .get(nodeName) : nodeName),
                                nodeValue);

                        }
                    }
                }
            }
        }
    }

    /**
     * Return Lucene field name according to search parameter name.
     */
    public static String getRangeField(String searchFieldName) {
        // Do a range query for the search field itself (eg. denominator)
        if (RANGE_FIELDS.contains(searchFieldName)) {
            return searchFieldName;
        }
        if (RANGE_FIELDS_FROM.contains(searchFieldName)) {
            return RANGE_FIELDS.get(RANGE_FIELDS_FROM.indexOf(searchFieldName));
        }
        if (RANGE_FIELDS_TO.contains(searchFieldName)) {
            return RANGE_FIELDS.get(RANGE_FIELDS_TO.indexOf(searchFieldName));
        }
        return null;
    }

    public static String getTo(String fieldName) {
        return RANGE_FIELDS_TO.get(RANGE_FIELDS.indexOf(fieldName));
    }

    public static String getFrom(String fieldName) {
        return RANGE_FIELDS_FROM.get(RANGE_FIELDS.indexOf(fieldName));
    }

    /**
     * Return all search criteria.
     */
    public Map<String, Set<String>> getSearchCriteria() {
        return searchCriteria;
    }

    public Set<String> getFacetQueries() {
        return facetQueries;
    }

    /**
     * Return all search criteria except those which does not contains textual information like
     * identifiers or codelists (eg. UUID, PARENTUUID).
     *
     *
     * Those fields may be used for language detection.
     */
    public Map<String, Set<String>> getTextCriteria() {
        Map<String, Set<String>> textCriteria = new LinkedHashMap<String, Set<String>>();
        for (String criteria : searchCriteria.keySet()) {
            if (!NO_TEXT_FIELDS.contains(criteria)) {
                textCriteria.put(criteria, searchCriteria.get(criteria));
            }
        }
        return textCriteria;
    }

    public Map<String, Set<String>> getSearchPrivilegeCriteria() {
        return searchPrivilegeCriteria;
    }

    public Map<String, String> getSearchOption() {
        return searchOption;
    }

    /**
     * Is node a facet drilldown request
     */
    private boolean isFacetQuery(String nodeName) {
        return nodeName.equals(SearchParameter.FACET_QUERY);
    }

    /**
     * TODO javadoc.
     */
    private void addValues(Map<String, Set<String>> hash, String nodeName, String nodeValue) {
        Set<String> currentValues = searchCriteria.get(nodeName);

        try {
            if (currentValues == null) {
                Set<String> values = new LinkedHashSet<String>();
                String val = nodeValue;
                try {
                    val = URLDecoder.decode(nodeValue, "UTF-8");
                } catch (IllegalArgumentException iea) {
                    // keep val as the original nodeValue
                }
                values.add(val);
                hash.put(nodeName, values);
            } else {
                currentValues.add(URLDecoder.decode(nodeValue, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        addSearchCriteria(text);
        addFacetQueries(text);
        return text.toString();
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        if (editable.equals("true")) {
            this.editable = editable;
        } else {
            this.editable = "false";
        }
    }

    private void addSearchCriteria(StringBuilder text) {
        for (Map.Entry<String, Set<String>> entry : searchCriteria.entrySet()) {
            String fieldName = (String) entry.getKey();
            Set<String> fieldValue = (Set<String>) entry.getValue();
            text.append(fieldName).append(":").append(fieldValue).append(" ");
        }
    }

    private void addFacetQueries(StringBuilder text) {
        for (String facetQuery : facetQueries) {
            text.append("facetQuery:").append(facetQuery).append(" ");
        }
    }
}
