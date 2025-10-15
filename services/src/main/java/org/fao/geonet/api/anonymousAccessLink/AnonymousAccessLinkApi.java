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
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
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
            method = RequestMethod.POST,
            value = "/{uuid}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create anonymous access link",
            description = "")
    public AnonymousAccessLinkDto createAnonymousAccessLink(
            @io.swagger.v3.oas.annotations.Parameter(description = "md uuid", required = true) @PathVariable(value = "uuid") String uuid) throws ResourceAlreadyExistException {
        return anonymousAccessLinkService.createAnonymousAccessLink(uuid);
    }

    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            value = "/{uuid}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "get one anonymous access link or empty",
            description = "")
    public AnonymousAccessLinkDto getAnonymousAccessLink(@io.swagger.v3.oas.annotations.Parameter(description = "md uuid", required = true) @PathVariable(value = "uuid") String uuid) {
        return  anonymousAccessLinkService.getAnonymousAccessLink(uuid);
    }

    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "List all anonymous access links with md infos",
            description = "")
    public List<AnonymousAccessLinkDto> getAnonymousAccessLinksWithMdInfos() throws IOException {
        return anonymousAccessLinkService.getAllAnonymousAccessLinksWithMdInfos();
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{uuid}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an anonymous access link",
            description = "")
    public void deleteAccessLinks(
            @io.swagger.v3.oas.annotations.Parameter(description = "md uuid", required = true) @PathVariable(value = "uuid") String uuid) {
        anonymousAccessLinkService.deleteAnonymousAccessLink(uuid);
    }
}
