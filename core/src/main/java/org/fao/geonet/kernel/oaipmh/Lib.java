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

package org.fao.geonet.kernel.oaipmh;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.EsSearchManager;
import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_CORE;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.jdom.Element;

public class Lib {
    public static final String SESSION_OBJECT = "oai-list-records-result";

    private Lib() {

    }
    public static boolean existsConverter(Path schemaDir, String prefix) {
        Path f = schemaDir.resolve("formatter").resolve(prefix).resolve("view.xsl");
        return Files.exists(f);
    }

    /**
     * Return all XSL file basename available for the given schema directory.
     */
    public static List<String> availableConverters(Path schemaDir) {
        List<String> result = new ArrayList<>();
        Path convertDir = schemaDir.resolve("formatter");
        if (Files.exists(convertDir) && Files.isDirectory(convertDir)) {
            try {
                Files.list(convertDir).forEach(f -> {
                    if (Files.isDirectory(f)) {
                        String folderName = f.getFileName().toString();
                        result.add(folderName);
                    }
                });
            } catch (Exception e) {
                Log.warning(Geonet.OAI_HARVESTER, "OAI / Exception while retrieving converters " + e.getMessage());
            }
        }
        return result;
    }


    public static Element prepareTransformEnv(String uuid, String changeDate, String baseUrl, String siteUrl, String siteName) {

        //--- setup environment

        Element env = new Element("env");

        env.addContent(new Element("uuid").setText(uuid));
        env.addContent(new Element("changeDate").setText(changeDate));
        env.addContent(new Element("baseURL").setText(baseUrl));
        env.addContent(new Element("siteURL").setText(siteUrl));
        env.addContent(new Element("siteName").setText(siteName));

        return env;
    }

    public static Element transform(Path schemaDir, Element env, Element md, String targetFormat) throws Exception {

        //--- setup root element

        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        //--- do an XSL transformation

        Path styleSheet = schemaDir.resolve("formatter").resolve(targetFormat).resolve("view.xsl");

        return Xml.transform(root, styleSheet);
    }

    public static List<Integer> search(ServiceContext context, Element params) throws Exception {
        EsSearchManager searchMan = context.getBean(EsSearchManager.class);

        JsonNode esJsonQuery = createSearchQuery(params);

        // Get results count
        SearchResponse queryResult = searchMan.query(
            esJsonQuery,
            FIELDLIST_CORE,
            0, 1);

        long total = queryResult.hits().total().value();

        queryResult = searchMan.query(
            esJsonQuery,
            FIELDLIST_CORE,
            0, (int) total);

        List<Integer> result;

        ObjectMapper objectMapper = new ObjectMapper();
        result = ((List<Hit>) queryResult.hits().hits())
            .stream()
            .map(h -> Integer.parseInt(objectMapper.convertValue(h.source(), Map.class)
                .get(Geonet.IndexFieldNames.ID).toString())).collect(Collectors.toList());
        return result;
    }

    public static Element toJeevesException(OaiPmhException e) {
        String msg = e.getMessage();
        String cls = e.getClass().getSimpleName();
        String id = e.getCode();
        Element res = e.getResponse();

        Element error = new Element(Jeeves.Elem.ERROR)
            .addContent(new Element("message").setText(msg))
            .addContent(new Element("class").setText(cls));

        error.setAttribute("id", id);

        if (res != null) {
            Element elObj = new Element("object");
            elObj.addContent(res.detach());

            error.addContent(elObj);
        }

        return error;
    }

    static JsonNode createSearchQuery(Element params) throws JsonProcessingException {
        final String PARAM_SET = "category";
        final String PARAM_FROM = "extFrom";
        final String PARAM_UNTIL = "extTo";
        final String PARAM_METADATAPREFIX = "_schema";

        String jsonQuery = "{" +
            "    \"bool\": {" +
            "      \"must\": [" +
            "        {" +
            "          \"terms\": {" +
            "            \"isTemplate\": [\"n\"]" +
            "          }" +
            "        }%s%s" +
            "      ]" +
            "    }" +
            "}";

        String categoryQuery = "";
        if (params.getChild(PARAM_SET) != null) {
            categoryQuery = String.format("        ,{" +
                "          \"term\": {" +
                "            \"cat\": {" +
                "              \"value\": \"%s\"" +
                "            }" +
                "          }" +
                "        }", params.getChild(PARAM_SET).getValue());
        }

        String temporalExtentQuery = "";
        if ((params.getChild(PARAM_FROM) != null) ||
            (params.getChild("extTo") != null)) {

            String fromQuery = "";
            if (params.getChild(PARAM_FROM) != null) {
                fromQuery = String.format("                \"gte\": \"%s\",", params.getChild(PARAM_FROM).getValue());
            }

            String untilQuery = "";
            if (params.getChild(PARAM_UNTIL) != null) {
                untilQuery = String.format("                \"lte\": \"%s\",", params.getChild(PARAM_UNTIL).getValue()) ;
            }

            temporalExtentQuery = String.format("  ,{\"range\": {\"resourceTemporalDateRange\": {" +
                "                %s" +
                "                %s" +
                "                \"relation\": \"intersects\"" +
                "            }}}", fromQuery, untilQuery);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(String.format(jsonQuery, categoryQuery, temporalExtentQuery));
    }
}
