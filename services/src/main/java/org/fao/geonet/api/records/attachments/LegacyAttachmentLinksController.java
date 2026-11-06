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
package org.fao.geonet.api.records.attachments;

import io.swagger.v3.oas.annotations.Hidden;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * Former download services (with disclaimer) whose URLs can be stored in records. They redirect to
 * the attachment download, which checks the download privilege.
 */
@Hidden
@Controller
public class LegacyAttachmentLinksController {

    @Autowired
    IMetadataUtils metadataUtils;

    @GetMapping({
        "/{portal}/{lang:[a-z]{2,3}}/file.disclaimer",
        "/{portal}/{lang:[a-z]{2,3}}/resources.get.archive"
    })
    public ResponseEntity<Void> redirectToAttachment(
        @PathVariable String portal,
        @RequestParam(required = false) String uuid,
        @RequestParam(required = false) String id,
        @RequestParam String fname,
        HttpServletRequest request) throws Exception {
        String metadataUuid = uuid;
        if (StringUtils.isEmpty(metadataUuid) && StringUtils.isNotEmpty(id)) {
            metadataUuid = metadataUtils.getMetadataUuid(id);
        }
        if (StringUtils.isEmpty(metadataUuid)) {
            throw new ResourceNotFoundException("Record not found. Set uuid or id.");
        }

        String location = request.getContextPath() + "/" + portal + "/api/records/"
            + UriUtils.encodePathSegment(metadataUuid, StandardCharsets.UTF_8)
            + "/attachments/" + UriUtils.encodePathSegment(fname, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, location).build();
    }
}
