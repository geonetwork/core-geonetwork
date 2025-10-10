/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.api.anonymousAccessLink;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping(value = {
        "/{portal}/api/anonymousAccessLink"
})
@Controller("anonymousAccessLink")
@Tag(name = "anonymous access links", description = "'permalinks to not published mds'")
public class AnonymousAccessLinkApi {

    @Autowired
    private AnonymousAccessLinkService anonymousAccessLinkService;

    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create anonymous access link",
            description = "")
    public AnonymousAccessLinkDto createAnonymousAccessLink(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "at least {\"metadataUuid\":\"...\"}")
            @RequestBody AnonymousAccessLinkDto anonymousAccessLinkDto) {
        return anonymousAccessLinkService.createAnonymousAccessLink(anonymousAccessLinkDto);
    }

    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "List all anonymous access links",
            description = "")
    public List<AnonymousAccessLinkDto> getAnonymousAccessLinks() {
        return anonymousAccessLinkService.getAllAnonymousAccessLinks();
    }

    @RequestMapping(
            method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an anonymous access link",
            description = "")
    public void deleteAccessLinks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "at least {\"metadataUuid\":\"...\"}")
            @RequestBody AnonymousAccessLinkDto anonymousAccessLinkDto) {
        anonymousAccessLinkService.deleteAnonymousAccessLink(anonymousAccessLinkDto);
    }
}
