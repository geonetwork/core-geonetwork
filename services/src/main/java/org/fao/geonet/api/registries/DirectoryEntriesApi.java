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

package org.fao.geonet.api.registries;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MultilingualSchemaPlugin;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.*;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/registries/entries"
})
@Tag(name = ApiParams.API_CLASS_REGISTRIES_TAG,
    description = ApiParams.API_CLASS_REGISTRIES_OPS)
public class DirectoryEntriesApi {

    private static final char SEPARATOR = '~';
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    IMetadataUtils metadataRepository;
    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a directory entry",
        description = "Directory entry (AKA subtemplates) are XML fragments that can be " +
            "inserted in metadata records using XLinks. XLinks can be remote or " +
            "local.")
    @RequestMapping(
        value = "/{uuid:.+}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Directory entry."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Element getEntry(
        @Parameter(
            description = "Directory entry UUID.",
            required = true)
        @PathVariable
            String uuid,
        @Parameter(
            description = "Process",
            required = false
        )
        @RequestParam(
            required = false
        )
            String[] process,
        @Parameter(
            description = "Transformation",
            required = false
        )
        @RequestParam(
            required = false
        )
            String transformation,
        @Parameter(
            description = "lang",
            required = false
        )
        @RequestParam(
            name = "lang",
            required = false
        )
            String[] langs,
        @Parameter(
            description = "schema",
            required = false
        )
        @RequestParam(
            required = false, defaultValue = "iso19139"
        )
            String schema,
        @Parameter(hidden = true)
            HttpServletRequest request
    )
        throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        final AbstractMetadata metadata = metadataRepository.findOneByUuid(uuid);

        if (metadata == null) {
            throw new ResourceNotFoundException(String.format(
                "Directory entry with UUID '%s' not found in this registry.",
                uuid
            ));
        }

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE) {
            throw new IllegalArgumentException(String.format(
                "The record found with UUID '%s' is not a subtemplate", uuid));
        }

        ServiceContext context = ApiUtils.createServiceContext(request);
        if (langs == null) {
            langs = context.getLanguage().split(",");
        }

        Element tpl = metadata.getXmlData(false);

        // Processing parameters process=xpath~value.
        // xpath must point to an Element or an Attribute.
        if (process != null) {
            String[] replaceList = process;
            for (String parameters : replaceList) {
                int endIndex = parameters.indexOf(SEPARATOR);
                if (endIndex == -1) {
                    continue;
                }
                String xpath = parameters.substring(0, endIndex);
                String value = parameters.substring(endIndex + 1);

                Set<Namespace> allNamespaces = Sets.newHashSet();
                final Iterator descendants = tpl.getDescendants();
                while (descendants.hasNext()) {
                    Object next = descendants.next();
                    if (next instanceof Element) {
                        Element element = (Element) next;
                        allNamespaces.add(element.getNamespace());
                        for (Object o : tpl.getAdditionalNamespaces()) {
                            if (o instanceof Namespace) {
                                Namespace namespace = (Namespace) o;
                                allNamespaces.add(namespace);
                            }
                        }
                    }
                }
                Object o = Xml.selectSingle(tpl, xpath, Lists.newArrayList(allNamespaces));
                if (o instanceof Element) {
                    ((Element) o).setText(value);
                } else if (o instanceof Attribute) {
                    ((Attribute) o).setValue(value);
                }
            }
        }
        // Multilingual record: Remove all localizedString from the subtemplate for langs that are not in the metadata and set the gco:CharacterString
        // Monolingual record: Remove all localized strings and extract main gco:CharacterString
        List<String> twoCharLangs = new ArrayList<String>();
        for (String l : langs) {
            twoCharLangs.add("#" + XslUtil.twoCharLangCode(l).toUpperCase());
        }
        MultilingualSchemaPlugin plugin = (MultilingualSchemaPlugin) schemaManager.getSchema(schema).getSchemaPlugin();
        if (plugin != null) {
            plugin.removeTranslationFromElement(tpl, twoCharLangs);
        }

        if (transformation != null) {
            Element root = new Element("root");
            Element requestElt = new Element("request");
            requestElt.addContent(new Element("transformation").setText(transformation));
            root.addContent(requestElt);
            root.addContent(tpl);

            Path xslt = dataDirectory.getWebappDir()
                .resolve("xslt/services/subtemplate/convert.xsl");
            return Xml.transform(root, xslt);
        } else {
            return tpl;
        }
    }
}
