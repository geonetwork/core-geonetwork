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

package org.fao.geonet.api;

import com.google.common.collect.Sets;

import org.apache.commons.beanutils.BeanUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

/**
 * API utilities mainly to deal with parameters.
 */
public class ApiUtils {
    /**
     * Return a set of UUIDs based on the input UUIDs array or based on the current selection.
     */
    static public Set<String> getUuidsParameterOrSelection(String[] uuids, UserSession session) {
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            SelectionManager selectionManager =
                SelectionManager.getManager(session);
            synchronized (
                selectionManager.getSelection(
                    SelectionManager.SELECTION_METADATA)) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                setOfUuidsToEdit = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToEdit = Sets.newHashSet(Arrays.asList(uuids));
        }
        if (setOfUuidsToEdit.size() == 0) {
            // TODO: i18n
            throw new IllegalArgumentException(
                "At least one record should be defined or selected for analysis.");
        }
        return setOfUuidsToEdit;
    }

    /**
     * Return the Jeeves user session.
     *
     * If session is null, it's probably a bot due to {@link AllRequestsInterceptor#createSessionForAllButNotCrawlers(HttpServletRequest)}.
     * In such case return an exception.
     */
    static public UserSession getUserSession(HttpSession httpSession) {
        if (httpSession == null) {
            throw new SecurityException("The service requested is not available for crawlers. HTTP session is not activated for bots.");
        }
        UserSession userSession = (UserSession) httpSession.getAttribute(Jeeves.Elem.SESSION);
        if (userSession == null) {
            throw new SecurityException("The service requested is not available for crawlers. Catalog session is null.");
        }
        return userSession;
    }

    /**
     * If you really need a ServiceContext use this. Try to avoid in order to reduce dependency on
     * Jeeves.
     */
    static public ServiceContext createServiceContext(HttpServletRequest request) {
        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        return serviceManager.createServiceContext("Api", "", request);
    }

    public static long sizeOfDirectory(Path lDir) throws IOException {
        final long[] size = new long[]{0};
        Files.walkFileTree(lDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size[0] += Files.size(file);
                return FileVisitResult.CONTINUE;
            }
        });

        return size[0] / 1024;
    }

    /**
     * Check if the current user can edit this record.
     */
    static public Metadata canEditRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        if (!accessManager.canEdit(createServiceContext(request), String.valueOf(metadata.getId()))) {
            throw new SecurityException("You can't edit this record");
        }
        return metadata;
    }

    /**
     * Check if the current user can view this record.
     */
    public static Metadata canViewRecord(String metadataUuid, HttpServletRequest request) {
        ApplicationContext appContext = ApplicationContextHolder.get();
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        try {
            Lib.resource.checkPrivilege(createServiceContext(request), String.valueOf(metadata.getId()), ReservedOperation.view);
        } catch (Exception e) {
            throw new SecurityException("You can't view this record");
        }
        return metadata;
    }
}
