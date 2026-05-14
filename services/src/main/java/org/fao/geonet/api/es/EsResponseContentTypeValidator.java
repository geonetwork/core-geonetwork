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

package org.fao.geonet.api.es;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class EsResponseContentTypeValidator {
    private static final Set<String> VALID_CONTENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "application/json", "text/plain"
    )));

    private static final String NOT_FOUND_MESSAGE = "Not Found";

    public void validateContentType(HttpURLConnection connectionWithFinalHost, HttpServletResponse response, String contentType) throws IOException {
        if (contentType == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Host url has been validated by proxy but content type given by remote host is null");
            return;
        }

        // content type has to be valid
        if (!isContentTypeValid(contentType)) {
            String responseMessage = connectionWithFinalHost.getResponseMessage();
            if (NOT_FOUND_MESSAGE.equalsIgnoreCase(responseMessage)) {
                // content type was not valid because it was a not found page (text/html)
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Remote host not found");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "The content type of the remote host's response \"" + contentType
                        + "\" is not allowed by the proxy rules");
            }
        }
    }


    /**
     * Check if the content type is accepted by the proxy
     *
     * @return true: valid; false: not valid
     */
    public boolean isContentTypeValid(final String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return false;
        }

        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0].trim().toLowerCase();
        return VALID_CONTENT_TYPES.contains(type);
    }
}
