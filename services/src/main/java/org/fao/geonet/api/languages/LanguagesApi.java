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

package org.fao.geonet.api.languages;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
    "/{portal}/api/languages",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/languages"
})
@Api(value = "languages",
    tags = "languages",
    description = "Languages operations")
@Controller("languages")
public class LanguagesApi {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @ApiOperation(
        value = "Get languages",
        notes = "Languages for the application having translations in the database. " +
            "All tables with 'Desc' suffix contains translation for some domain objects " +
            "like groups, tags, ...",
        nickname = "getLanguages")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Language> getLanguages() throws Exception {
        return languageRepository.findAll();
    }


    @ApiOperation(
        value = "Add a language",
        notes = "Add all default translations from all *Desc tables in the database. " +
            "This operation will only add translations for a default catalog installation. " +
            "Defaults can be customized in SQL scripts located in " +
            "WEB-INF/classes/setup/sql/data/*.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "addLanguage")
    @RequestMapping(
        value = "/{langCode}",
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Language translations added.") ,
        @ApiResponse(code = 404, message = "Resource not found. eg. No SQL file available for that langugae.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Administrator can access it.")
    })
    public void addLanguages(
        @ApiParam(value = ApiParams.API_PARAM_ISO_3_LETTER_CODE,
            required = true)
        @PathVariable
            String langCode,
        @ApiIgnore
            HttpServletRequest request
    ) throws IOException, ResourceNotFoundException {

        Language lang = languageRepository.findOne(langCode);
        if (lang == null) {
            String languageDataFile = "loc-" + langCode + "-default.sql";
            Path templateFile = dataDirectory.getWebappDir().resolve("WEB-INF")
                .resolve("classes").resolve("setup").resolve("sql").resolve("data")
                .resolve(languageDataFile);
            if (Files.exists(templateFile)) {
                List<String> data = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(templateFile.toFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        data.add(line);
                    }
                }
                if (data.size() > 0) {
                    ServiceContext context = ApiUtils.createServiceContext(request);
                    Lib.db.runSQL(context, data);
                    return;
                }
            }
            throw new ResourceNotFoundException(String.format(
                "Language data file '%s' not found in classes/setup/sql/data.", languageDataFile
            ));
        } else {
            throw new RuntimeException(String.format(
                "Language '%s' already available.", lang.getId()
            ));
        }
    }

    @ApiOperation(
        value = "Remove a language",
        notes = "Delete all translations from all *Desc tables in the database. " +
            "Warning: This will also remove all translations you may have done " +
            "to those objects (eg. custom groups).",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteLanguage")
    @RequestMapping(
        value = "/{langCode}",
        method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Language translations removed.") ,
        @ApiResponse(code = 404, message = "Resource not found.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Administrator can access it.")
    })
    public void deleteLanguage(
        @ApiParam(
            value = ApiParams.API_PARAM_ISO_3_LETTER_CODE,
            required = true)
        @PathVariable
            String langCode,
            HttpServletRequest request
    ) throws IOException, ResourceNotFoundException {
        Language lang = languageRepository.findOne(langCode);
        if (lang == null) {
            throw new ResourceNotFoundException(String.format(
                "Language '%s' not found.", langCode
            ));
        } else {
            final String LANGUAGE_DELETE_SQL = "language-delete.sql";

            Path templateFile = dataDirectory.getWebappDir().resolve("WEB-INF")
                .resolve("classes").resolve("setup").resolve("sql").resolve("template")
                .resolve(LANGUAGE_DELETE_SQL);
            if (Files.exists(templateFile)) {
                List<String> data = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(templateFile.toFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        data.add(String.format(line, lang.getId()));
                    }
                }
                if (data.size() > 0) {
                    ServiceContext context = ApiUtils.createServiceContext(request);
                    Lib.db.runSQL(context, data);
                    return;
                }
            }
            throw new ResourceNotFoundException(String.format(
                "Template file '%s' not found in classes/setup/sql/template.", LANGUAGE_DELETE_SQL
            ));
        }
    }
}
