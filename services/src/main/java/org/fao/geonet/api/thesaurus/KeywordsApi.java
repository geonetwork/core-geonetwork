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

package org.fao.geonet.api.thesaurus;

import io.swagger.annotations.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.MetadataSearchAndReplace;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.ProcessingReport;
import org.fao.geonet.api.processing.report.registry.IProcessingReportRegistry;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@Api(value = "thesaurus",
    tags = "thesaurus",
    description = "Thesaurus operations")
@Controller("keywords")
public class KeywordsApi {

    @Autowired
    IsoLanguagesMapper mapper;

    @Autowired
    ThesaurusManager thesaurusManager;

    @ApiOperation(
        value = "Get keyword by id",
        nickname = "getKeywordById",
        notes = "Keywords are XML fragments that can be " +
                "inserted in metadata records using XLinks. XLinks can be remote or " +
                "local."
    )
    @RequestMapping(
        path = "/api/keyword",
        method = RequestMethod.GET,
        produces = {
                MediaType.APPLICATION_XML_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Keyword as XML."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Element getKeywordById(
        @ApiParam(
            value = "Identifier of the keyword.",
            required = true)
        @RequestParam (name = "id")
            String uri,
        @ApiParam(
            value = "Thesaurus of the keyword.",
            required = true)
        @RequestParam (name = "thesaurus")
            String sThesaurusName,
        @ApiParam(
            value = "Identifier of the keyword.",
            required = true)
        @RequestParam (name = "lang")
            String [] langs,
        @ApiParam(
            value = "Identifier of the keyword.",
            required = true)
        @RequestParam
            boolean multiple,
        @ApiParam(
            value = "Only print the keyword, no thesaurus information.",
            required = false)
        @RequestParam (required = false, defaultValue = "false")
            boolean keywordOnly,
        @ApiParam(
            value = "File to use for xsl transformation.",
            required = false)
        @RequestParam (required = false)
            String transformation,
        @ApiParam(
                value = "Al request params.")
        @RequestParam
                Map<String,String> allRequestParams,
        HttpServletRequest request

    ) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);

        for (int i = 0; i < langs.length; i++) {
            langs[i] = mapper.iso639_2_to_iso639_1(langs[i], langs[i].substring(2));
        }

        Element descKeys;

        if (uri == null) {
            descKeys = new Element("descKeys");
        } else {
            KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusManager);

            KeywordBean kb;
            if (!multiple) {
                kb = searcher.searchById(uri, sThesaurusName, langs);
                if (kb == null) {
                    descKeys = new Element("descKeys");
                } else {
                    descKeys = KeywordsSearcher.toRawElement(new Element("descKeys"), kb);
                }
            } else {
                String[] url = uri.split(",");
                List<KeywordBean> kbList = new ArrayList<>();
                for (String currentUri : url) {
                    kb = searcher.searchById(currentUri, sThesaurusName, langs);
                    if (kb != null) {
                        kbList.add(kb);
                    }
                }
                descKeys = new Element("descKeys");
                for (KeywordBean keywordBean : kbList) {
                    KeywordsSearcher.toRawElement(descKeys, keywordBean);
                }
            }
        }

        GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        Path convertXsl = dataDirectory.getWebappDir().resolve("xslt/services/thesaurus/convert.xsl");

        Element params = new Element("params");
        Element gui = new Element("gui");
        Element thesaurusEl = new Element("thesaurus");
        final Element root = new Element("root");

        gui.addContent(thesaurusEl);
        thesaurusEl.addContent(thesaurusManager.buildResultfromThTable(context));

        Element requestParams = new Element ("request");
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            requestParams.addContent(new Element(e.getKey()).setText(e.getValue()));
        }
        root.addContent(requestParams);
        root.addContent(descKeys);
        root.addContent(gui);
        final Element transform = Xml.transform(root, convertXsl);

        return transform;
    }

}
