/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.guiapi.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RequestMapping(value = {
    "/{portal}/search"
})
@Tag(name = "search",
    description = "Search operations")
@Controller("search")
public class SearchApi {
    public static final String APPLICATION_RDF_XML = "application/rdf+xml";

    @Operation(
        summary = "Get statistics about a field",
        description = "(experimental) This return facet info for the requested field and " +
            "provide a list of values.")
    @RequestMapping(
        produces = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            APPLICATION_RDF_XML
        },
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    public void getFieldInfo(
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response,
        @Parameter(hidden = true)
        @RequestHeader(
            value = "Accept",
            defaultValue = MediaType.TEXT_HTML_VALUE
        )
            String accept,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams
    ) throws Exception {
        boolean isRdf = APPLICATION_RDF_XML.equals(accept);
        boolean isXml = MediaType.APPLICATION_XML_VALUE.equals(accept);
        boolean isJson = MediaType.APPLICATION_JSON_VALUE.equals(accept);

        if (allRequestParams.get("resultType") == null) {
            allRequestParams.put("resultType", "details");
        }

        // We need the full XML to do the conversion to RDF
        allRequestParams.put("fast", isRdf ? "false" : "index");


        response.setHeader("Content-type", (isRdf || isXml || isJson ? accept : "text/html") + ";charset=utf-8");

        Element results = query(allRequestParams, request);
        if (isXml) {
            response.getWriter().write(Xml.getString(results));
        } else if (isJson) {
            response.getWriter().write(Xml.getJSON(results));
        } else {
            response.getWriter().write(
                new XsltResponseWriter(null, "search")
                    .withJson("catalog/locales/en-core.json")
                    .withJson("catalog/locales/en-search.json")
                    .withXml(results)
                    .withXsl(
                        isRdf ?
                            "xslt/services/dcat/rdf.xsl" : "xslt/ui-search/search-nojs.xsl")
                    .asHtml()
            );
            // FIXME: This use Xalan instead of Saxon
//        ModelAndView model = new ModelAndView("../xslt/ui-search/field-summary");
//        model.addObject("type", type);
//        model.addObject("xmlSource", query(queryFields, request));
//        return model;
        }
    }


    private Element query(Map<String, String> queryFields, HttpServletRequest request) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);
        ServiceContext context = ApiUtils.createServiceContext(request);

        // TODOES this is the proxy
        throw new NotImplementedException("Not implemented in ES");
//        Element params = new Element("params");
//        queryFields.forEach((k, v) -> params.addContent(new Element(k).setText(v)));
//
//        Element elData = SearchDefaults.getDefaultSearch(context, params);
//
//        LuceneSearcher searcher = null;
//        Element model = new Element("search");
//        model.addContent(params);
//        try {
//            searcher = (LuceneSearcher) searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
//
//            ServiceConfig config =  new ServiceConfig();
//            searcher.search(context, elData, config);
//            model.addContent(searcher.getSummary());
//            if (queryFields.get("summaryOnly") == null) {
//                model.addContent(searcher.present(context, params, config));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return model;
    }
}
