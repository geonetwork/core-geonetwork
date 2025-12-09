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
package org.fao.geonet.api.selections;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.kernel.SelectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS;

/**
 * Select a list of elements stored in session.
 */
@RequestMapping(value = {
    "/{portal}/api/selections"
})
@Tag(name = "selections",
    description = "Selection related operations")
@Controller("selections")
public class SelectionsApi {

    @io.swagger.v3.oas.annotations.Operation(summary = "Get current selections")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Map<String, Integer> getSelectionsAndSize(
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        SelectionManager selectionManager =
            SelectionManager.getManager(ApiUtils.getUserSession(httpSession));

        return selectionManager.getSelectionsAndSize();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get current selection")
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/{bucket}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Set<String> get(
        @Parameter(description = "Bucket name",
            required = true,
            example = "metadata")
        @PathVariable
            String bucket,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        SelectionManager selectionManager =
            SelectionManager.getManager(ApiUtils.getUserSession(httpSession));

        synchronized (selectionManager.getSelection(bucket)) {
            return selectionManager.getSelection(bucket);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Select one or more items")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{bucket}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    ResponseEntity<Integer> add(
        @Parameter(description = "Bucket name",
            required = true,
            example = "metadata")
        @PathVariable
            String bucket,
        @Parameter(description = "One or more record UUIDs. If null, select all in current search if bucket name is 'metadata' (TODO: remove this limitation?).",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request
    )
        throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        int nbSelected = SelectionManager.updateSelection(bucket,
            session,
            uuid != null ?
                SelectionManager.ADD_SELECTED :
                SelectionManager.ADD_ALL_SELECTED,
            uuid != null ?
                Arrays.asList(uuid) : null,
            ApiUtils.createServiceContext(request));

        return new ResponseEntity<>(nbSelected, HttpStatus.CREATED);
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Clear selection or remove items")
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{bucket}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    ResponseEntity<Integer> clear(
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = true,
            example = "metadata")
        @PathVariable
            String bucket,
        @Parameter(
            description = API_PARAM_RECORD_UUIDS,
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request
    )
        throws Exception {

        int nbSelected = SelectionManager.updateSelection(bucket,
            ApiUtils.getUserSession(httpSession),
            uuid != null ?
                SelectionManager.REMOVE_SELECTED :
                SelectionManager.REMOVE_ALL_SELECTED,
            uuid != null ?
                Arrays.asList(uuid) : null,
            ApiUtils.createServiceContext(request));

        return new ResponseEntity<>(nbSelected, HttpStatus.OK);
    }
}
