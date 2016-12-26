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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * API utilities mainly to deal with parameters.
 */
public class ApiUtils {

    @Autowired
    static
    LanguageUtils languageUtils;

    /**
     * Return a set of UUIDs based on the input UUIDs array or based on the current selection.
     */
    static public Set<String> getUuidsParameterOrSelection(String[] uuids, String bucket, UserSession session) {
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            if (bucket == null) {
                bucket = SelectionManager.SELECTION_METADATA;
            }
            SelectionManager selectionManager =
                SelectionManager.getManager(session);
            synchronized (
                selectionManager.getSelection(bucket)) {
                final Set<String> selection = selectionManager.getSelection(bucket);
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
     * Search if a record match the UUID on its UUID or an internal identifier
     */
    public static String getInternalId(String uuidOrInternalId)
        throws Exception {
        String id;
        DataManager dm = ApplicationContextHolder.get().getBean(DataManager.class);

        id = dm.getMetadataId(uuidOrInternalId);
        if (id == null) {
            String checkingId = dm.getMetadataUuid(id);
            if (checkingId == null) {
                throw new ResourceNotFoundException(String.format(
                    "Record with UUID '%s' not found in this catalog",
                    uuidOrInternalId));
            }
        }
        return id;
    }
    public static Metadata getRecord(String uuidOrInternalId)
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOneByUuid(uuidOrInternalId);
        if (metadata == null) {
            try {
                metadata = metadataRepository.findOne(uuidOrInternalId);
            } catch (InvalidDataAccessApiUsageException e) {
                throw new ResourceNotFoundException(String.format(
                    "Record with UUID '%s' not found in this catalog",
                    uuidOrInternalId));
            }
            if (metadata == null) {
                throw new ResourceNotFoundException(String.format(
                    "Record with UUID '%s' not found in this catalog",
                    uuidOrInternalId));
            } else {
                return metadata;
            }
        } else {
            return metadata;
        }
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
        String iso3langCode = ApplicationContextHolder.get().getBean(LanguageUtils.class)
            .getIso3langCode(request.getLocales());
        return createServiceContext(request, iso3langCode);
    }

    static public ServiceContext createServiceContext(HttpServletRequest request, String iso3langCode) {
        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        return serviceManager.createServiceContext("Api", iso3langCode, request);
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

    public static Path downloadUrlInTemp(String url) throws IOException, URISyntaxException {

        URI uri = new URI(url);
        Path file = Files.createTempFile("file-from-url", ".xml");

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        XmlRequest httpReq = applicationContext
            .getBean(GeonetHttpRequestFactory.class)
            .createXmlRequest(uri.toURL());

        httpReq.setAddress(uri.getPath());

        Lib.net.setupProxy(applicationContext.getBean(SettingManager.class), httpReq);

        httpReq.executeLarge(file);
        return file;
    }

    /**
     * Check if the current user can edit this record.
     */
    static public Metadata canEditRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        Metadata metadata = getRecord(metadataUuid);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        if (!accessManager.canEdit(createServiceContext(request), String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "You can't edit record with UUID %s", metadataUuid));
        }
        return metadata;
    }

    /**
     * Check if the current user can view this record.
     */
    public static Metadata canViewRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        Metadata metadata = getRecord(metadataUuid);
        try {
            Lib.resource.checkPrivilege(createServiceContext(request), String.valueOf(metadata.getId()), ReservedOperation.view);
        } catch (Exception e) {
            throw new SecurityException(String.format(
                "You can't view record with UUID %s", metadataUuid));
        }
        return metadata;
    }

    /**
     *
     * @param img
     * @param outFile
     * @throws IOException
     */
    public static void createFavicon(Image img, Path outFile) throws IOException {
        int width = 32;
        int height = 32;
        String type = "png";

        Image thumb = img.getScaledInstance(width, height,
            BufferedImage.SCALE_SMOOTH);

        BufferedImage bimg = new BufferedImage(width, height,
            BufferedImage.TRANSLUCENT);

        Graphics2D g = bimg.createGraphics();
        g.drawImage(thumb, 0, 0, null);
        g.dispose();

        try (OutputStream out = Files.newOutputStream(outFile)) {
            ImageIO.write(bimg, type, out);
        }
    }
}
