//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseSearch;
import org.fao.geonet.utils.Log;
import org.jdom.Element;


/**
 * The {@code Search} class extends the functionality of {@link BaseSearch}
 * and provides additional methods for handling search operations, such as
 * constructing custom Elasticsearch queries and managing search ranges.
 */
class Search extends BaseSearch {

    public int size;
    public String categories;
    public String schemes;
    public String groupOwners;

    public Search() {
        super();
    }

    public Search(Element search) throws BadParameterEx {
        super(search);
        categories = Util.getParam(search, "categories", "");
        schemes = Util.getParam(search, "schemes", "");
        groupOwners = Util.getParam(search, "groupOwners", "");
    }

    public static Search createEmptySearch(int from, int size) throws BadParameterEx {
        Search s = new Search(new Element("search"));
        s.setRange(from, size);
        return s;
    }

    public Search copy() {
        Search s = new Search();

        s.freeText = freeText;
        s.title = title;
        s.abstractText = abstractText;
        s.keywords = keywords;
        s.sourceUuid = sourceUuid;
        s.from = from;
        s.size = size;
        s.categories = categories;
        s.groupOwners = groupOwners;
        s.schemes = schemes;

        return s;
    }

    /**
     * Generates an Elasticsearch query string based on the defined search parameters and filters.
     * The query includes various filters such as source UUID filter, free text filter,
     * title filter, abstract filter, and keyword filter. It also specifies pagination parameters
     * and the list of fields to be included in the response.
     *
     * @return A string representation of the Elasticsearch query formatted as a JSON object.
     */
    public String createElasticsearchQuery() {

        List<String> filters = new ArrayList<>();

        if (StringUtils.isNotBlank(sourceUuid)) {
            filters.add(String.format("{\"term\": {\"sourceCatalogue\": \"%s\"}}", sourceUuid));
        }

        if (StringUtils.isNotBlank(freeText)) {
            filters.add(String.format("{\"query_string\": {\"query\": \"(any.\\\\*:(%s) OR any.common:(%s))\", \"default_operator\": \"AND\"}}", freeText, freeText));
        }

        if (StringUtils.isNotBlank(title)) {
            filters.add(String.format("{\"query_string\": {\"query\": \"(resourceTitleObject.\\\\*:(%s))\", \"default_operator\": \"AND\"}}", title));
        }

        if (StringUtils.isNotBlank(abstractText)) {
            filters.add(String.format("{\"query_string\": {\"query\": \"(resourceAbstractObject.\\\\*:(%s))\", \"default_operator\": \"AND\"}}", abstractText));
        }

        if (StringUtils.isNotBlank(keywords)) {
            filters.add(String.format("{\"term\": {\"tag.default\": \"%s\"}}", keywords));
        }

        if (StringUtils.isNotBlank(categories)) {
            filters.add(String.format("{\"query_string\": {\"query\": \"cat:(%s)\"}}", categories));
        }

        if (StringUtils.isNotBlank(schemes)) {
            filters.add(String.format("{\"query_string\": {\"query\": \"documentStandard:(%s)\", \"default_operator\": \"OR\"}}", schemes));
        }

        if (StringUtils.isNotBlank(groupOwners)) {
            try {
                List<String> groupOwners = Arrays.stream(this.groupOwners.split(","))
                    .map(String::trim)  // Remove extra spaces
                    .collect(Collectors.toList());
                String groupOwnersJson = new ObjectMapper().writeValueAsString(groupOwners); // Outputs: ["group1","group2"]
                filters.add(String.format("{\"terms\": {\"groupOwner\": %s}}", groupOwnersJson));

            } catch (JsonProcessingException e) {
                Log.debug(Geonet.HARVEST_MAN, "Error creating criteria for ownerGroup. Ignoring this filter.");
            }
        }

        String queryFilter = String.join(",", filters);
        if (StringUtils.isNotBlank(queryFilter)) {
            queryFilter = "," + queryFilter;
        }

        String queryBody = String.format("{\n" +
                "    \"from\": %d,\n" +
                "    \"size\": %d,\n" +
                "    \"sort\": [\"_score\"],\n" +
                "    \"query\": {\"bool\": {\"must\": [{\"terms\": {\"isTemplate\": [\"n\"]}}%s]}},\n" +
                "    \"_source\": {\"includes\": [\n" +
                "        \"uuid\",\n" +
                "        \"id\",\n" +
                "        \"isTemplate\",\n" +
                "        \"sourceCatalogue\",\n" +
                "        \"dateStamp\",\n" +
                "        \"documentStandard\"\n" +
                "    ]},\n" +
                "    \"track_total_hits\": true\n" +
                "}",
            from, size, queryFilter);


        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Search request is " + queryBody);
        }

        return queryBody;
    }

    /**
     * Sets the pagination parameters for the Elasticsearch query.
     *
     * @param from the starting offset (0-based index of the first record to return)
     * @param size the number of records to return per page
     */
    public void setRange(int from, int size) {
        this.from = from;
        this.size = size;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("from", from)
            .append("size", size)
            .append("freeText", freeText)
            .append("title", title)
            .append("abstrac", abstractText)
            .append("keywords", keywords)
            .append("categories", categories)
            .append("schemes", schemes)
            .append("groupOwners", groupOwners)
            .append("sourceUuid", sourceUuid)
            .toString();
    }
}



