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

package org.fao.geonet.api.related;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.records.MetadataUtils;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.api.records.model.related.RelatedResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.fao.geonet.api.records.attachments.AbstractStore.getAndCheckMetadataId;

@RequestMapping(value = {
    "/{portal}/api/related"
})
@Tag(name = "related",
    description = "Related records")
@Controller("related")
@ReadWriteController
public class Related implements ApplicationContextAware {

    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    GeonetworkDataDirectory dataDirectory;
    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record related resources for all requested metadatas",
        description = "Retrieve related services, datasets, onlines, thumbnails, sources, ... " +
            "to all requested records.<br/>" +
            "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html'>More info</a>")
    @RequestMapping(value = "",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the associated resources."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Map<String, RelatedResponse> getAssociatedResourcesForRecords(
        @Parameter(description = "Type of related resource. If none, all resources are returned.",
            required = false
        )
        @RequestParam(defaultValue = "", name = "type")
            RelatedItemType[] types,
        @Parameter(description = "Uuids of the metadatas you request the relations from.",
            required = false
        )
        @RequestParam(defaultValue = "", name = "uuid")
            String[] uuids,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(required = false, defaultValue = "true")
        Boolean approved,
        HttpServletRequest request) throws Exception {

        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Path relatedXsl = dataDirectory.getWebappDir().resolve("xslt/services/metadata/relation.xsl");

        AbstractMetadata md;
        Map<String, RelatedResponse> res = new HashMap<String, RelatedResponse>();

        for (String uuid : uuids) {
            try {
                md = ApiUtils.canViewRecord(uuid, approved, request);
                Element raw = new Element("root").addContent(Arrays.asList(
                    new Element("gui").addContent(Arrays.asList(
                        new Element("language").setText(language.getISO3Language()),
                        new Element("url").setText(context.getBaseUrl())
                    )),
                    MetadataUtils.getRelated(context, md.getId(), md.getUuid(), types, 0, 100)
                ));
                final Element transform = Xml.transform(raw, relatedXsl);
                RelatedResponse response = (RelatedResponse) Xml.unmarshall(transform, RelatedResponse.class);
                res.put(uuid, response);
            } catch (SecurityException e) {
                Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
                throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
            } catch (Exception exception) {
                Log.debug(API.LOG_MODULE_NAME, exception.getMessage(), exception);
            }

        }
        return res;
    }
}
