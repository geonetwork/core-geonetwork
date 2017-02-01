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

package org.fao.geonet.api.records.lock;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_ID;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.MetadataLock;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.metadata.IMetadataManager;
import org.fao.geonet.repository.MetadataLockRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

@RequestMapping(value = { "/api/records",
        "/api/" + API.VERSION_0_1 + "/records" })
@Api(value = API_CLASS_RECORD_TAG, tags = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordLock")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class MetadataLockApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    private MetadataLockRepository mdLockRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IMetadataManager mdManager;

    protected AccessManager accessMan;

    @ApiOperation(value = "Check if a record is locked for editing.", notes = "Returns the record is locked.", nickname = "editor")
    @RequestMapping(value = "/{id}/checkLock", method = RequestMethod.GET, consumes = {
            MediaType.ALL_VALUE }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "If the record is locked."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseBody
    public Boolean startEditing(
            @ApiParam(value = API_PARAM_RECORD_ID, required = true) @PathVariable Integer id,
            HttpServletRequest request) throws Exception {
        final SecurityContext context = SecurityContextHolder.getContext();

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        this.accessMan = applicationContext.getBean(AccessManager.class);

        String md = Integer.toString(id);
        if (!accessMan.canEdit(ServiceContext.get(), md)) {
            throw new SecurityException("The user cannot edit this metadata.");
        }

        User me = userRepository
                .findOneByUsername(context.getAuthentication().getName());

        return mdLockRepo.isLocked(md, me);
    }

    @ApiOperation(value = "Release the lock over a record.", nickname = "editor")
    @RequestMapping(value = "/{id}/releaseLock", method = RequestMethod.DELETE, consumes = {
            MediaType.ALL_VALUE }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "If the record is unlocked."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseBody
    public Boolean releaseLock(
            @ApiParam(value = API_PARAM_RECORD_ID, required = true) @PathVariable Integer id,
            HttpServletRequest request) throws Exception {
        final SecurityContext context = SecurityContextHolder.getContext();

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        this.accessMan = applicationContext.getBean(AccessManager.class);

        String md = Integer.toString(id);
        if (!accessMan.canEdit(ServiceContext.get(), md)) {
            throw new SecurityException("The user cannot edit this metadata.");
        }

        User me = userRepository
                .findOneByUsername(context.getAuthentication().getName());

        return mdLockRepo.unlock(Integer.toString(id), me);
    }

    @ApiOperation(value = "Get all locks on metadata.", nickname = "editor")
    @RequestMapping(value = "/all/locks", method = RequestMethod.GET, consumes = {
            MediaType.ALL_VALUE }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the list of locks."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseBody
    public List<Lock> getAllLocks(HttpServletRequest request) throws Exception {

        List<MetadataLock> locks = mdLockRepo.findAll();
        List<Lock> response = new LinkedList<Lock>();

        for (MetadataLock mdLock : locks) {
            IMetadata md = mdManager
                    .getMetadataObject(mdLock.getMetadata());
            if (md != null) {
                Lock lock = new Lock();
                lock.setDate(mdLock.getTimestamp().toString());
                lock.setUsername(mdLock.getUser().getUsername());
                lock.setUuid(md.getUuid());
                lock.setId(md.getId());
                response.add(lock);
            }
        }

        return response;
    }
}
