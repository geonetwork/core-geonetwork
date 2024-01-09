/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.oaipmh;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping(value = {
    "/{portal}/api/oaipmh"
})
@Tag(name = "oaipmh",
    description = "OAIPMH server operations")
@Controller("oaipmh")
public class OaiPmhApi {

    @Autowired
    private LanguageUtils languageUtils;

    @Autowired
    private OaiPmhDispatcher oaiPmhDispatcher;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Oaiphm server",
        description = "")
    @GetMapping(
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Oaiphm server response.")
    })
    @ResponseBody
    public Element dispatch(
        @RequestParam(required = false) final String verb,
        @RequestParam(required = false) final String metadataPrefix,
        @RequestParam(required = false) final String set,
        @RequestParam(required = false) final String from,
        @RequestParam(required = false) final String until,
        @RequestParam(required = false) final String resumptionToken,
        final HttpServletRequest request
    ) {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        // Set the service name, used in OaiPmhDispatcher to build the oaiphm endpoint URL
        serviceContext.setService("api/oaipmh");

        Element params = new Element("params");
        if (StringUtils.isNotEmpty(verb)) {
            params.addContent(new Element("verb").setText(verb));
        }

        if (StringUtils.isNotEmpty(metadataPrefix)) {
            params.addContent(new Element("metadataPrefix").setText(metadataPrefix));
        }

        if (StringUtils.isNotEmpty(set)) {
            params.addContent(new Element("set").setText(set));
        }

        if (StringUtils.isNotEmpty(from)) {
            params.addContent(new Element("from").setText(from));
        }

        if (StringUtils.isNotEmpty(until)) {
            params.addContent(new Element("until").setText(until));
        }

        if (StringUtils.isNotEmpty(resumptionToken)) {
            params.addContent(new Element("resumptionToken").setText(resumptionToken));
        }

        return oaiPmhDispatcher.dispatch(params, serviceContext);
    }

}
