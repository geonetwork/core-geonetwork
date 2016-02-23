package org.fao.geonet.services.api.schema;
//==============================================================================
//===	Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.api.API;
import org.fao.geonet.services.api.exception.ResourceNotFoundException;
import org.fao.geonet.services.api.tools.i18n.LanguageUtils;
import org.fao.geonet.services.schema.Info;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */

@RequestMapping(value = {
        "/api/standard",
        "/api/" + API.VERSION_0_1 +
                "/standard"
})
@Api(value = "standard",
        tags= "standard",
        description = "Standard related operations")
@Controller("standard")
public class StandardApi implements ApplicationContextAware {

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    LanguageUtils languageUtils;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @ApiOperation(value = "List a codelist entries",
                  nickname = "getSchemaTranslations")
    @RequestMapping(value = "/{schema}/codelists/{codelist}",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    @ResponseBody
    public Map<String, String> getSchemaTranslations(
            @PathVariable String schema,
            @PathVariable String codelist,
            @RequestParam(required = false) String parent,
            @RequestParam(required = false) String xpath,
            @RequestParam(required = false) String isoType,
            ServletRequest request
    ) throws Exception {
        Map<String, String> response = new LinkedHashMap<String, String>();
        final ServiceContext context = ServiceContext.get();
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        String elementName = Info.findNamespace(codelist, schemaManager, schema);
        Element e = Info.getHelp(schemaManager, "codelists.xml",
                schema, elementName, parent, xpath, isoType, context);
        if (e == null) {
            throw new ResourceNotFoundException(String.format(
                    "'%s' not found.", codelist));
        }
        List<Element> listOfEntry = e.getChildren("entry");
        for (Element entry : listOfEntry) {
            response.put(entry.getChildText("code"), entry.getChildText("label"));
        }
        return response;
    }
}