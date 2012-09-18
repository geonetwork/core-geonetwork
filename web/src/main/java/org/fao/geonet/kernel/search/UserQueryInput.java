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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Search parameters that can be provided by a search client.
 * 
 * @author heikki doeleman
 * @author francois prunayre
 */
public class UserQueryInput {

    /**
     * List of fields which MUST not be control by user. Those fields are always
     * removed from the request.
     */
    public static final List<String> SECURITY_FIELDS = Arrays.asList(
            SearchParameter.OWNER, 
            SearchParameter.ISADMIN, 
            SearchParameter.ISREVIEWER, 
            SearchParameter.ISUSERADMIN);
            //***
            // LuceneIndexField.GROUP_OWNER);

    /**
     * Don't take into account those field in search (those field are not 
     * indexed but are search options).
     * 
     * TODO : move to lucene-config.xml because those fields depends on the
     * client
     */
    public static final List<String> RESERVED_FIELDS = Arrays.asList(
            SearchParameter.GROUP, 
            Geonet.SearchResult.FAST, 
            Geonet.SearchResult.SORT_BY,
            Geonet.SearchResult.SORT_ORDER,
            Geonet.SearchResult.REMOTE, 
            Geonet.SearchResult.EXTENDED, 
            Geonet.SearchResult.INTERMAP, 
            Geonet.SearchResult.HITS_PER_PAGE, 
            Geonet.SearchResult.GEOMETRY,
            Geonet.SearchResult.TIMEOUT, 
            Geonet.SearchResult.OUTPUT, 
            Geonet.SearchResult.SUMMARY_ONLY, 
            Geonet.SearchResult.BUILD_SUMMARY,
            Geonet.SearchResult.REQUESTED_LANGUAGE,
            "region_simple", "attrset", "mode", 
            "region", "from", "to", "hitsperpage" 
            );

    private String similarity;
    private String editable;
    private static Map<String, String> searchParamToLuceneField = new HashMap<String, String>();
    static {
        // Populate map for search parameter to Lucene mapping
        searchParamToLuceneField.put(SearchParameter.SITEID, LuceneIndexField._SOURCE);
        searchParamToLuceneField.put(SearchParameter.INSPIRE, LuceneIndexField.INSPIRE_CAT);
        searchParamToLuceneField.put(SearchParameter.THEMEKEY, LuceneIndexField.KEYWORD);
        searchParamToLuceneField.put(SearchParameter.TOPICCATEGORY, LuceneIndexField.TOPIC_CATEGORY);
        searchParamToLuceneField.put(SearchParameter.CATEGORY, LuceneIndexField._CAT);
    }
    private Map<String, Set<String>> searchCriteria = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> searchPrivilegeCriteria = new HashMap<String, Set<String>>();
    private Map<String, String> searchOption = new HashMap<String, String>();

    public Map<String, Set<String>> getSearchCriteria() {
        return searchCriteria;
    }

    public Map<String, Set<String>> getSearchPrivilegeCriteria() {
        return searchPrivilegeCriteria;
    }

    public Map<String, String> getSearchOption() {
        return searchOption;
    }

    /**
     * Creates this from a JDOM element.
     * 
     * @param jdom
     *            input
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
                } else {
                    if (StringUtils.isNotBlank(nodeValue)) {

                        if (SECURITY_FIELDS.contains(nodeName)
                                || nodeName.contains("_op")) {
                            addValues(searchPrivilegeCriteria, nodeName, nodeValue);
                        } else if (RESERVED_FIELDS.contains(nodeName)) {
                            searchOption.put(nodeName, nodeValue);
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
     * TODO javadoc.
     *
     * @param hash
     * @param nodeName
     * @param nodeValue
     */
    private void addValues(Map<String, Set<String>> hash, String nodeName, String nodeValue) {
        Set<String> currentValues = searchCriteria.get(nodeName);

        if (currentValues == null) {
            HashSet<String> values = new HashSet<String>();
            values.add(nodeValue);
            hash.put(nodeName, values);
        } else {
            currentValues.add(nodeValue);
        }
    }

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
            LuceneIndexField.DENOMINATOR);
    private static final List<String> RANGE_FIELDS_FROM = Arrays.asList(
            SearchParameter.DATEFROM, 
            SearchParameter.REVISIONDATEFROM,
            SearchParameter.PUBLICATIONDATEFROM,
            SearchParameter.CREATIONDATEFROM, 
            SearchParameter.DENOMINATORFROM);
    private static final List<String> RANGE_FIELDS_TO = Arrays.asList(
            SearchParameter.DATETO, 
            SearchParameter.REVISIONDATETO,
            SearchParameter.PUBLICATIONDATETO, 
            SearchParameter.CREATIONDATETO,
            SearchParameter.DENOMINATORTO);

    /**
     * Return Lucene field name according to search parameter name.
     * 
     * @param searchFieldName
     * @return
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
     * All @See {@value #SECURITY_FIELDS} are removed from a user request.
     * 
     * @param jdom
     */
    @SuppressWarnings({"JavadocReference"})
    private void protectRequest(Element jdom) {
        for (String fieldName : SECURITY_FIELDS) {
            jdom.removeChildren(fieldName);
        }
    }

    /**
     * 
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        Map<String, Set<String>> searchCriteria = getSearchCriteria();
        for (Iterator iter = searchCriteria.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            String fieldName = (String) entry.getKey();
            Set<String> fieldValue = (Set<String>) entry.getValue();
            text.append(fieldName).append(":").append(fieldValue).append(" ");
        }
        return text.toString();
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setEditable(String editable) {
        if(editable.equals("true")) {
            this.editable = editable;
        }
        else {
            this.editable = "false";
        }
    }

    public String getEditable() {
        return editable;
    }
}
