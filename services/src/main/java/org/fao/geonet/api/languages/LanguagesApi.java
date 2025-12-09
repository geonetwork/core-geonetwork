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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Language;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.DbLib;
import org.fao.geonet.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping(value = {
    "/{portal}/api/languages"
})
@Tag(name = "languages",
    description = "Languages operations")
@RestController
public class LanguagesApi {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    private String defaultLanguage;

    @Resource(name="defaultLanguage")
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get languages available in the application",
        description = "Languages available in this version of the application. Those that you can add using PUT operation and which have SQL script to initialize the language.")
    @RequestMapping(
        value = "/application",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public List<Language> getApplicationLanguages() throws Exception {
        Set<String> applicationLanguages =
            (Set<String>) ApplicationContextHolder.get().getBean("languages");
        List<Language> list = applicationLanguages.stream().map(l -> {
            Language language = new Language();
            language.setId(l);
            return language;
        }).collect(Collectors.toList());
        return list;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get languages",
        description = "Languages for the application having translations in the database. " +
            "All tables with 'Desc' suffix contains translation for some domain objects " +
            "like groups, tags, ...")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Language> getLanguages() throws Exception {
        return languageRepository.findAll();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a language",
        description = "Add all default translations from all *Desc tables in the database. " +
            "This operation will only add translations for a default catalog installation. " +
            "Defaults can be customized in SQL scripts located in " +
            "WEB-INF/classes/setup/sql/data/*."
//        authorizations = {
//          @Authorization(value = "basicAuth")
//        }
    )
    @RequestMapping(
        value = "/{langCode}",
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Language translations added."),
        @ApiResponse(responseCode = "404", description = "Resource not found. eg. No SQL file available for that langugae."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Administrator can access it.")
    })
    public void addLanguages(
        @Parameter(description = ApiParams.API_PARAM_ISO_3_LETTER_CODE,
            required = true)
        @PathVariable
            String langCode,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws IOException, ResourceNotFoundException {

        Optional<Language> lang = languageRepository.findById(langCode);
        if (!lang.isPresent()) {
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
                    DbLib.runSQL(context, data);
                    return;
                }
            }
            throw new ResourceNotFoundException(String.format(
                "Language data file '%s' not found in classes/setup/sql/data.", languageDataFile
            ));
        } else {
            throw new RuntimeException(String.format(
                "Language '%s' already available.", lang.get().getId()
            ));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a language",
        description = "Delete all translations from all *Desc tables in the database. " +
            "Warning: This will also remove all translations you may have done " +
            "to those objects (eg. custom groups)."
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/{langCode}",
        method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Language translations removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Resource not found."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Administrator can access it.")
    })
    public void deleteLanguage(
        @Parameter(
            description = ApiParams.API_PARAM_ISO_3_LETTER_CODE,
            required = true)
        @PathVariable
            String langCode,
        HttpServletRequest request
    ) throws IOException, ResourceNotFoundException {
        Optional<Language> lang = languageRepository.findById(langCode);
        if (!lang.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                "Language '%s' not found.", langCode
            ));
        } else {
            long count = languageRepository.count();
            if (count == 1) {
                throw new NotAllowedException(String.format(
                    "You can't delete the last language. Add another one before removing %s.",
                    langCode
                ));
            }
            final String LANGUAGE_DELETE_SQL = "language-delete.sql";

            Path templateFile = dataDirectory.getWebappDir().resolve("WEB-INF")
                .resolve("classes").resolve("setup").resolve("sql").resolve("template")
                .resolve(LANGUAGE_DELETE_SQL);
            if (Files.exists(templateFile)) {
                List<String> data = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(templateFile.toFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        data.add(String.format(line, lang.get().getId()));
                    }
                }
                if (data.size() > 0) {
                    ServiceContext context = ApiUtils.createServiceContext(request);
                    DbLib.runSQL(context, data);
                    return;
                }
            }
            throw new ResourceNotFoundException(String.format(
                "Template file '%s' not found in classes/setup/sql/template.", LANGUAGE_DELETE_SQL
            ));
        }
    }
}
