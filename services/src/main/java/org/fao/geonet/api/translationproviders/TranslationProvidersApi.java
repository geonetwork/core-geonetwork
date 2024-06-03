/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.api.translationproviders;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.fao.geonet.translations.ITranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RequestMapping(value = {
    "/{portal}/api/translationproviders"
})
@Tag(name = "translationproviders",
    description = "Translation providers")
@RestController("translationproviders")
public class TranslationProvidersApi {
    private List<ITranslationService> translationServiceList;

    public TranslationProvidersApi(List<ITranslationService> translationServiceList) {
        this.translationServiceList = translationServiceList;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Retrieve the list of translation provider.",
        description = "")
    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of translation provider names.")
    })
    @PreAuthorize("hasAuthority('Administrator')")
    public List<String> getTranslationProviderNames(
    )  {
        return translationServiceList.stream().map(t -> t.name()).collect(Collectors.toList());
    }
}
