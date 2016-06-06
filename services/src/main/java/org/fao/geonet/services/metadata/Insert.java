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

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.ServletPathFinder;
import jeeves.services.ReadWriteController;

/**
 * Insert a metadata into the catalog. You have to be logged in as editor to use
 * this service.
 * 
 * <ul>
 * <li>String <strong>data</strong>: XML string of the metadata</li>
 * <li>Integer <strong>group</strong>: Id of the group to insert this metadata
 * into</li>
 * <li>[y|n]<strong>template</strong> (optional): Is it a template or a
 * metadata?</li>
 * <li>String <strong>styleSheet</strong> (optional): Stylesheet to apply to the
 * inserted metadata. Based on xsl/conversion/import</li>
 * <li>[on|off] <strong>validate</strong> (optional): Enable validation for this
 * metadata</li>
 * <li>String <strong>schema</strong> (optional): Schema of this metadata. If
 * not defined, it will try to extract it from the metadata structure</li>
 * <li>String <strong>category</strong> (optional): Category to assign to the
 * metadata</li>
 * <li>String <strong>extra</strong> (optional): Extra data</li>
 * <li>[generateUuid] <strong>uuidAction</strong> (optional): Action to do with
 * the uuid</li>
 * </ul>
 * 
 */

@Controller("md.insert")
@ReadWriteController
public class Insert {

    private Path stylePath = null;

    @RequestMapping(value = "/{lang}/md.insert", produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public @ResponseBody InsertResponse serviceSpecificExec(HttpSession session,
            @RequestParam(value = Params.DATA) String data,
            @RequestParam(value = Params.GROUP) String group,
            @RequestParam(value = Params.TEMPLATE, required = false, defaultValue = "n") String metadataType_st,
            @RequestParam(value = Params.STYLESHEET, required = false, defaultValue = "_none_") String style,
            @RequestParam(value = Params.VALIDATE, required = false, defaultValue = "off") String validate_st,
            @RequestParam(value = Params.SCHEMA, required = false) String schema,
            final @RequestParam(value = Params.CATEGORY, required = false, defaultValue = "") String category,
            final @RequestParam(value = "extra", required = false) String extra,
            final @RequestParam(value = Params.UUID_ACTION, required = false, defaultValue = Params.NOTHING) String uuidAction)
                    throws Exception {

        return process(data, group, metadataType_st, style, validate_st, schema,
                category, extra, uuidAction);
    }

    private InsertResponse process(String data, String group,
            String metadataType_st, String style, String validate_st,
            String schema, final String category, final String extra,
            final String uuidAction)
                    throws IOException, JDOMException, Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder
                .get();
        DataManager dataMan = appContext.getBean(DataManager.class);
        MetadataRepository metadataRepository = appContext
                .getBean(MetadataRepository.class);
        final ServiceContext context = ServiceContext.get();
        MetadataType metadataType = MetadataType.lookup(metadataType_st);
        boolean validate = validate_st.equals("on");

        Element xml = Xml.loadString(data, false);

        // Apply a stylesheet transformation if requested
        if (!style.equals("_none_")) {
            xml = Xml.transform(xml, getStylePath(appContext).resolve(style));
        }

        if (schema == null) {
            schema = dataMan.autodetectSchema(xml);
            if (schema == null) {
                throw new BadParameterEx(
                        "Can't detect schema for metadata automatically.",
                        "schema is unknown");
            }
        }
        if (validate)
            DataManager.validateMetadata(schema, xml, context);

        // if the uuid does not exist we generate it for metadata and templates
        String uuid;
        if (metadataType == MetadataType.SUB_TEMPLATE) {
            uuid = UUID.randomUUID().toString();
        } else {
            uuid = dataMan.extractUUID(schema, xml);
            if (uuid.length() == 0) {
                uuid = UUID.randomUUID().toString();
                xml = dataMan.setUUID(schema, uuid, xml);
            }
        }
        String date = new ISODate().toString();

        final List<String> id = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();
        md.add(xml);

        // Import record
        Map<String, String> sourceTranslations = Maps.newHashMap();
        Importer.importRecord(uuid, uuidAction, md, schema, 0,
                appContext.getBean(SettingManager.class).getSiteId(),
                appContext.getBean(SettingManager.class).getSiteName(),
                sourceTranslations, context, id, date, date, group,
                metadataType);

        int iId = Integer.parseInt(id.get(0));

        // Set template
        dataMan.setTemplate(iId, metadataType, null);

        dataMan.activateWorkflowIfConfigured(context, id.get(0), group);

        // Import category
        final boolean hasCategory = !category.equals("_none_")
                && !category.trim().isEmpty();

        if (hasCategory || extra != null) {
            metadataRepository.update(iId, new Updater<Metadata>() {
                @Override
                public void apply(@Nonnull Metadata metadata) {
                    if (hasCategory) {
                        Element categs = new Element("categories");
                        categs.addContent((new Element("category"))
                                .setAttribute("name", category));

                        Importer.addCategoriesToMetadata(metadata, categs,
                                context);
                    }

                    if (extra != null) {
                        metadata.getDataInfo().setExtra(extra);
                    }
                }
            });
        }

        // Index
        dataMan.indexMetadata(id.get(0), true);

        // Return response
        return new InsertResponse(String.valueOf(iId),
                String.valueOf(dataMan.getMetadataUuid(id.get(0))));
    }

    private Path getStylePath(ConfigurableApplicationContext appContext) {
        if (this.stylePath == null) {
            ServletPathFinder finder = new ServletPathFinder(
                    appContext.getBean(ServletContext.class));
            Path appPath = finder.getAppPath();
            this.stylePath = appPath.resolve(Geonet.Path.IMPORT_STYLESHEETS);
        }
        return this.stylePath;
    }

    @XmlRootElement(name = Jeeves.Elem.RESPONSE)
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class InsertResponse implements Serializable {
        private static final long serialVersionUID = 496130056479944137L;
        private String id;
        private String uuid;

        public String getId() {
            return id;
        }

        public String getUuid() {
            return uuid;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        InsertResponse() {
            super();
        }

        InsertResponse(String id, String uuid) {
            this();
            this.id = id;
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            return Jeeves.Elem.RESPONSE + "{" + "id=" + this.id + ", uuid="
                    + this.uuid + '}';
        }
    }

}
