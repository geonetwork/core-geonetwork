/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.records.attachments.AttachmentsApi;
import org.fao.geonet.resources.Resources;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public final class LogoUtils {
    public static final String API_GET_LOGO_NOTE = "If last-modified header "
        + "is present it is used to check if the logo has been modified since "
        + "the header date. If it hasn't been modified returns an empty 304 Not"
        + " Modified response. If modified returns the image. If there is "
        + "no logo then returns a transparent 1x1 px PNG image.";

    private static final int SIX_HOURS = 60 * 60 * 6;

    private static final String TRANSPARENT_1_X_1_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII";

    private static final byte[] TRANSPARENT_1_X_1_PNG = org.apache.commons.codec.binary.Base64.decodeBase64(TRANSPARENT_1_X_1_PNG_BASE64);

    private LogoUtils() {
    }

    public static Resources.ResourceHolder getImage(Resources resources,
                                                    ServiceContext serviceContext,
                                                    String logoRef) throws IOException {
        final Path logosDir = resources.locateLogosDir(serviceContext);
        final Path harvesterLogosDir = resources.locateHarvesterLogosDir(serviceContext);
        Resources.ResourceHolder image = null;
        if (isLocalLogoRef(logoRef)) {
            image = resources.getImage(serviceContext, logoRef, logosDir);
            if (image == null) {
                image = resources.getImage(serviceContext, logoRef, harvesterLogosDir);
            }
        }
        return image;
    }

    public static void writeImageOrTransparentLogo(WebRequest webRequest,
                                                   HttpServletResponse response,
                                                   Resources.ResourceHolder image) throws IOException {
        if (image != null) {
            FileTime lastModifiedTime = image.getLastModifiedTime();
            response.setDateHeader("Expires", System.currentTimeMillis() + SIX_HOURS * 1000L);
            if (webRequest.checkNotModified(lastModifiedTime.toMillis())) {
                return;
            }
            response.setContentType(AttachmentsApi.getFileContentType(image.getPath().getFileName().toString()));
            response.setContentLength((int) Files.size(image.getPath()));
            response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
            addLogoSecurityHeaders(response);
            FileUtils.copyFile(image.getPath().toFile(), response.getOutputStream());
            return;
        }

        if (webRequest.checkNotModified(0L)) {
            return;
        }
        response.setContentType("image/png");
        response.setContentLength(TRANSPARENT_1_X_1_PNG.length);
        response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
        addLogoSecurityHeaders(response);
        response.getOutputStream().write(TRANSPARENT_1_X_1_PNG);
    }

    /**
     * Logos can be uploaded in SVG format and are served from the catalog
     * origin. An SVG may embed scripts which would execute if the logo URL is
     * opened as a top level document, leading to stored XSS. These headers
     * neutralize any active content while keeping the logo usable in
     * {@code <img>} tags (where scripts never run anyway).
     */
    private static void addLogoSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy",
            "default-src 'none'; style-src 'unsafe-inline'; sandbox");
    }

    private static boolean isLocalLogoRef(String logoRef) {
        return StringUtils.isNotBlank(logoRef)
            && !logoRef.startsWith("http://")
            && !logoRef.startsWith("https://")
            && !logoRef.startsWith("https//");
    }
}
