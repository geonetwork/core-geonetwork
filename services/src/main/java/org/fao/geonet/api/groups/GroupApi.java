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
package org.fao.geonet.api.groups;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.attachments.AttachmentsActionsApi;
import org.fao.geonet.api.records.attachments.AttachmentsApi;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Created by juanluisrp on 24/05/2016.
 */
@EnableWebMvc
@Service
@RequestMapping(value = {
    "/api/groups",
    "/api/" + API.VERSION_0_1
        + "/groups"
})
@Api(value = "groups",
    tags = "groups",
    description = "Group related operations")
@Controller("groups")
public class GroupApi {
    /** API logo note.*/
    private static final String API_GET_LOGO_NOTE = "If last-modified header "
        + "is present it is used to check if the logo has been modified since "
        + "the header date. If it hasn't been modified returns an empty 304 Not"
        + " Modified response. If modified returns the image. If the group has "
        + "no logo then returns a transparent 1x1 px PNG image.";

    /** Logger name. */
    public static final String LOGGER = Geonet.GEONETWORK + ".api.groups";
    /** Six hours in seconds. */
    private static final int SIX_HOURS = 60 * 60 * 6;
    /** Transparent 1x1 px PNG encoded in Base64. */
    private static final String TRANSPARENT_1_X_1_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR"
        + "42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
    /** Transparent 1x1 px PNG. */
    private static final byte[] TRANSPARENT_1_X_1_PNG = Base64.decodeBase64(TRANSPARENT_1_X_1_PNG_BASE64);

    /**
     * Message source.
     */
    @Autowired
    @Qualifier("apiMessages")
    private ResourceBundleMessageSource messages;
    /** Language utils used to detect the requested language. */
    @Autowired
    private LanguageUtils languageUtils;


    /**
     * Writes the group logo image to the response. If no image is found it
     * writes a 1x1 transparent PNG. If the request contain cache related
     * headers it checks if the resource has changed and return a 304 Not
     * Modified response if not changed.
     * @param groupId the group identifier.
     * @param webRequest the web request.
     * @param request the native HTTP Request.
     * @param response the servlet response.
     * @throws ResourceNotFoundException if no group exists with groupId.
     */
    @ApiOperation(value = "Get the group logo image.",
        nickname = "get",
        notes = API_GET_LOGO_NOTE
    )
    @RequestMapping(value = "/{groupId}/logo", method = RequestMethod.GET)
    public final void getGroupLogo(
        @ApiParam(value = "Group identifier", required = true) @PathVariable(value = "groupId") final Integer groupId,
        final WebRequest webRequest,
        HttpServletRequest request,
        HttpServletResponse response) throws ResourceNotFoundException {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        ServiceContext context = ServiceContext.get();
        if (context == null) {
            throw new RuntimeException("ServiceContext not available");
        }

        GroupRepository groupRepository = context.getBean(GroupRepository.class);


        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException(messages.getMessage("api.groups.group_not_found", new
                Object[]{groupId}, locale));
        }
        try {
            final Path logosDir = Resources.locateLogosDir(context);
            final String logoUUID = group.getLogo();
            Path imagePath = null;
            FileTime lastModifiedTime = null;
            if (StringUtils.isNotBlank(logoUUID) && !logoUUID.startsWith("http://") && !logoUUID.startsWith("https//")) {
                imagePath = Resources.findImagePath(logoUUID,
                    logosDir);
                if (imagePath != null) {
                    lastModifiedTime = Files.getLastModifiedTime(imagePath);
                    if (webRequest.checkNotModified(lastModifiedTime.toMillis())) {
                        // webRequest.checkNotModified sets the right HTTP headers
                        response.setDateHeader("Expires", System.currentTimeMillis() + SIX_HOURS *  1000L);

                        return;
                    }
                    response.setContentType(AttachmentsApi.getFileContentType(imagePath));
                    response.setContentLength((int) Files.size(imagePath));
                    response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                    response.setDateHeader("Expires", System.currentTimeMillis() + SIX_HOURS *  1000L);
                    FileUtils.copyFile(imagePath.toFile(), response.getOutputStream());
                }
            }

            if (imagePath == null) {
                // no logo image found. Return a transparent 1x1 png
                lastModifiedTime = FileTime.fromMillis(0);
                if (webRequest.checkNotModified(lastModifiedTime.toMillis())) {
                    return;
                }
                response.setContentType("image/png");
                response.setContentLength(TRANSPARENT_1_X_1_PNG.length);
                response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                response.getOutputStream().write(TRANSPARENT_1_X_1_PNG);
            }

        } catch (IOException e) {
            Log.error(LOGGER, String.format("There was an error accessing the logo of the group with id '%d'",
                groupId));
            throw new RuntimeException(e);
        }
    }
}
