/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.XmlRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


/**
 * API utilities mainly to deal with parameters.
 */
public class ApiUtils {

    private ApiUtils() {

    }

    /**
     * Return a set of UUIDs based on the input UUIDs array or based on the current selection.
     */
    public static Set<String> getUuidsParameterOrSelection(String[] uuids, String bucket, UserSession session) {
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
        if (setOfUuidsToEdit.isEmpty()) {
            // TODO: i18n
            throw new IllegalArgumentException(
                "At least one record should be defined or selected for analysis.");
        }
        return setOfUuidsToEdit;
    }

    /**
     * Search if a record match the UUID on its UUID or an internal identifier
     */
    public static String getInternalId(String uuidOrInternalId, Boolean approved)
        throws Exception {

        IMetadataUtils metadataUtils = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        AbstractMetadata metadata = metadataUtils.findOneByUuid(uuidOrInternalId);
        String id = null;
        if (metadata != null) {
            id = String.valueOf(metadata.getId());
        }

        AbstractMetadata foundMetadata = null;
        if (!StringUtils.hasLength(id) && Lib.type.isInteger(uuidOrInternalId)) {
            //It wasn't a UUID so assume it is an internalId which should be a number
            foundMetadata = metadataUtils.findOne(uuidOrInternalId);
        } else if (Boolean.TRUE.equals(approved)) {
            //It was a UUID, check if draft or approved version
            foundMetadata = ApplicationContextHolder.get().getBean(MetadataRepository.class).findOneByUuid(uuidOrInternalId);
        }
        if (foundMetadata != null) {
            id = String.valueOf(foundMetadata.getId());
        }

        if (!StringUtils.hasLength(id)) {
            throw new ResourceNotFoundException(String.format(
                "Record with UUID '%s' not found in this catalog",
                uuidOrInternalId));
        }
        return id;
    }

    //fixes the uri fragment portion (that the part after the "#")
    // so it is properly encoded
    //http://www.thesaurus.gc.ca/concept/#Offshore area        -->   http://www.thesaurus.gc.ca/concept/#Offshore%20area
    //http://www.thesaurus.gc.ca/concept/#AIDS (disease)       -->   http://www.thesaurus.gc.ca/concept/#AIDS%20%28disease%29
    //http://www.thesaurus.gc.ca/concept/#Alzheimer's disease  -->   http://www.thesaurus.gc.ca/concept/#Alzheimer%27s%20disease
    //
    //Includes some special case handling for spaces and ":"
    //
    //TODO: there could be other special handling for special cases in the future
    public static String fixURIFragment(String uri) throws UnsupportedEncodingException {
        String[] parts = uri.split("#");
        if (parts.length >1) {
            parts[parts.length - 1] = parts[parts.length - 1].replace("+", " ");
            parts[parts.length - 1] = URLEncoder.encode(parts[parts.length - 1], "UTF-8");
            parts[parts.length - 1] = parts[parts.length - 1].replace("+", "%20");
            parts[parts.length - 1] = parts[parts.length - 1].replace("%3A", ":");
        }
        return String.join("#",parts);
    }


    public static AbstractMetadata getRecord(String uuidOrInternalId) throws ResourceNotFoundException {
        IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        AbstractMetadata metadata = null;

        try {
            metadata = metadataRepository.findOneByUuid(uuidOrInternalId);
            if (metadata != null) {
                Log.trace(Geonet.DATA_MANAGER, "ApiUtils.getRecord(" + uuidOrInternalId + ") -> " + metadata);
                return metadata;
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            Log.warning(Geonet.GEONETWORK, String.format(
                "More than one record found with UUID '%s'. Error is '%s'.",
                uuidOrInternalId, e.getMessage()));
        }

        try {
            Log.trace(Geonet.DATA_MANAGER, uuidOrInternalId + " not recognized as UUID. Trying ID.");
            metadata = metadataRepository.findOne(uuidOrInternalId);
            if (metadata != null) {
                if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
                    Log.trace(Geonet.DATA_MANAGER, "ApiUtils.getRecord(" + uuidOrInternalId + ") -> " + metadata);
                }
                return metadata;
            }
        } catch (NumberFormatException e) {
        } catch (InvalidDataAccessApiUsageException e) {
        }

        Log.trace(Geonet.DATA_MANAGER, "Record identified by " + uuidOrInternalId + " not found.");
        throw new ResourceNotFoundException(String.format("Record with UUID '%s' not found in this catalog", uuidOrInternalId));
    }

    /**
     * Return the Jeeves user session.
     * <p>
     * If session is null, it's probably a bot due to {@link AllRequestsInterceptor#createSessionForAllButNotCrawlers(HttpServletRequest)}.
     * In such case return an exception.
     */
    public static UserSession getUserSession(HttpSession httpSession) {
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
    public static ServiceContext createServiceContext(HttpServletRequest request) {
        String iso3langCode = ApplicationContextHolder.get().getBean(LanguageUtils.class)
            .getIso3langCode(request.getLocales());
        return createServiceContext(request, iso3langCode);
    }

    public static ServiceContext createServiceContext(HttpServletRequest request, String iso3langCode) {
        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        ServiceContext serviceContext = serviceManager.createServiceContext("Api", iso3langCode, request);
        serviceContext.setAsThreadLocal();
        return serviceContext;
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
     * Check if the current user can edit this approved record
     */
    public static AbstractMetadata canEditRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        return canEditRecord(metadataUuid, true, request);
    }

    /**
     * Check if the current user can edit this record.
     */
    public static AbstractMetadata canEditRecord(String metadataUuid, boolean approved, HttpServletRequest request) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        String metadataId = getInternalId(metadataUuid, approved);
        AbstractMetadata metadata = getRecord(metadataId);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        if (!accessManager.canEdit(createServiceContext(request), String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "You can't edit record with UUID %s", metadataUuid));
        }
        return metadata;
    }

    /**
     * Check if the current user can change status of this record.
     */
    public static AbstractMetadata canChangeStatusRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        AbstractMetadata metadata = getRecord(metadataUuid);
        AccessManager accessManager = appContext.getBean(AccessManager.class);
        if (!accessManager.canChangeStatus(createServiceContext(request), String.valueOf(metadata.getId()))) {
            throw new SecurityException(String.format(
                "You can't change status of record with UUID %s", metadataUuid));
        }
        return metadata;
    }

    /**
     * Check if the current user can view this approved record
     */
    public static AbstractMetadata canViewRecord(String metadataUuid, HttpServletRequest request) throws Exception {
        return canViewRecord(metadataUuid, true, request);
    }

    /**
     * Check if the current user can view this record.
     */
    public static AbstractMetadata canViewRecord(String metadataUuid, boolean approved, HttpServletRequest request) throws Exception {
        String metadataId = getInternalId(metadataUuid, approved);

        AbstractMetadata metadata = getRecord(metadataId);
        try {
            Lib.resource.checkPrivilege(createServiceContext(request), String.valueOf(metadata.getId()), ReservedOperation.view);
        } catch (Exception e) {
            throw new SecurityException(String.format(
                "You can't view record with UUID %s", metadataUuid));
        }
        return metadata;
    }

    /**
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

    /**
     * Avoid browser cache issue when the same API Path serving various formats.
     *
     * eg. go to the API path serving HTML, then go to the main app
     * which is using the same API path but processing the JSON response.
     * Browser history back will return to the JSON output instead of the HTML one.
     *
     * Use the Vary header to indicate to the browser that the cache depends
     * on Accept header.
     */
    public static void setHeaderVaryOnAccept(HttpServletResponse response) {
        response.setHeader("Vary", "Accept");
    }

    /**
     * Process request validation, returning an string with the validation errors.
     *
     * @param bindingResult
     * @param messages
     */
    public static String processRequestValidation(BindingResult bindingResult, ResourceBundle messages) {
        if (bindingResult.hasErrors()) {
            java.util.List<ObjectError> errorList = bindingResult.getAllErrors();

            StringBuilder sb = new StringBuilder();
            Iterator<ObjectError> it = errorList.iterator();
            while (it.hasNext()) {
                ObjectError err = it.next();
                String msg = "";
                if (err.getCodes() != null) {
                    for(int i = 0; i < err.getCodes().length; i++) {
                        try {
                            msg = messages.getString(err.getCodes()[i]);

                            if (!StringUtils.isEmpty(msg)) {
                                break;
                            }
                        } catch (MissingResourceException ex) {
                            // Ignore
                        }
                    }
                }

                if (StringUtils.isEmpty(msg)) {
                    msg = err.getDefaultMessage();
                }

                sb.append(msg);
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static ResourceBundle getMessagesResourceBundle(Enumeration<Locale> locales) {
        LanguageUtils languageUtils = ApplicationContextHolder.get().getBean(LanguageUtils.class);
        Locale locale = languageUtils.parseAcceptLanguage(locales);
        return ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);
    }
}
