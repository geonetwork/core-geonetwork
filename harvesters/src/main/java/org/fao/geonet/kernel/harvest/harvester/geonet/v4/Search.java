//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseSearch;
import org.fao.geonet.utils.Log;
import org.jdom.Element;


//=============================================================================

class Search extends BaseSearch {

    public Search() {
        super();
    }

    public Search(Element search) throws BadParameterEx {
        super(search);
    }

    public static Search createEmptySearch(int from, int to) throws BadParameterEx {
        Search s = new Search(new Element("search"));
        s.setRange(from, to);
        return s;
    }

    public Search copy() {
        Search s = new Search();

        s.freeText = freeText;
        s.title = title;
        s.abstrac = abstrac;
        s.keywords = keywords;
        s.sourceUuid = sourceUuid;
        s.from = from;
        s.to = to;

        return s;
    }

    public String createElasticsearchQuery() {
        String sourceFilter = "";
        if (StringUtils.isNotEmpty(sourceUuid)) {
            sourceFilter = String.format(",{\"term\": {\"sourceCatalogue\": \"%s\"}}", sourceUuid);
        }

        String freeTextFilter = "";
        if (StringUtils.isNotEmpty(freeText)) {
            freeTextFilter = String.format(",{\"query_string\": {\"query\": \"(any.\\\\*:(%s) OR any.common:(%s))\", \"default_operator\": \"AND\"}}", freeText, freeText);
        }

        String titleFilter = "";
        if (StringUtils.isNotEmpty(title)) {
            titleFilter = String.format(",{\"query_string\": {\"query\": \"(resourceTitleObject.\\\\*:(%s))\", \"default_operator\": \"AND\"}}", title);
        }

        String abstractFilter = "";
        if (StringUtils.isNotEmpty(abstrac)) {
            abstractFilter = String.format(",{\"query_string\": {\"query\": \"(resourceAbstractObject.\\\\*:(%s))\", \"default_operator\": \"AND\"}}", abstrac);
        }

        String keywordFilter = "";
        if (StringUtils.isNotEmpty(keywords)) {
            abstractFilter = String.format(",{\"term\": {\"tag.default\": \"%s\"}}", keywords);
        }

        String queryBody = String.format("{\n" +
            "    \"from\": %d,\n" +
            "    \"size\": %d,\n" +
            "    \"sort\": [\"_score\"],\n" +
            "    \"query\": {\"bool\": {\"must\": [{\"terms\": {\"isTemplate\": [\"n\"]}}%s%s%s%s%s]}},\n" +
            "    \"_source\": {\"includes\": [\n" +
            "        \"uuid\",\n" +
            "        \"id\",\n" +
            "        \"isTemplate\",\n" +
            "        \"sourceCatalogue\",\n" +
            "        \"dateStamp\",\n" +
            "        \"documentStandard\"\n" +
            "    ]},\n" +
            "    \"track_total_hits\": true\n" +
            "}", from, to, sourceFilter, freeTextFilter, titleFilter, abstractFilter, keywordFilter);


        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Search request is " + queryBody);
        }

        return queryBody;
    }

    public void setRange(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("from", from)
            .append("to", to)
            .append("freeText", freeText)
            .append("title", title)
            .append("abstrac", abstrac)
            .append("keywords", keywords)
            .append("sourceUuid", sourceUuid)
            .toString();
    }
}



