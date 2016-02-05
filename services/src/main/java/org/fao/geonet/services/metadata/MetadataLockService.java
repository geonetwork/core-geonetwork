//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.repository.MetadataLockRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Check if a metadata is locked
 */
@Controller("metadata.lock")
public class MetadataLockService {
    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    @RequestMapping(value = "/{lang}/metadata.lock", produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Boolean exec(@RequestParam Integer id)
            throws Exception {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "User needs to log in");
        }

        ConfigurableApplicationContext appContext = ApplicationContextHolder
                .get();
        this.accessMan = appContext.getBean(AccessManager.class);
        User me = userRepository
                .findOneByUsername(context.getAuthentication().getName());

        if (me == null) {
            throw new AccessDeniedException(
                    SecurityContextHolder.class.getSimpleName()
                            + " has a user that is not in the database: "
                            + context.getAuthentication());
        }

        String md = Integer.toString(id);
        return accessMan.canEdit(ServiceContext.get(), md)
                && mdLockRepo.isLocked(md, me);
    }

    @Autowired
    private MetadataLockRepository mdLockRepo;
    @Autowired
    private UserRepository userRepository;

    protected AccessManager accessMan;

}

// =============================================================================
