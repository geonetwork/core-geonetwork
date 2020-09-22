//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.formatters;

import static org.fao.geonet.api.records.formatters.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.utils.IO;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

/**
 * List all formatters
 *
 * @author jeichar
 */
@Controller("md.formatter.list")
public class ListFormatters extends AbstractFormatService {

    private void addFormatters(String schema, FormatterDataResponse response, Path root, Path file, boolean isSchemaPluginFormatter,
                               boolean publishedOnly)
        throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(file, IO.DIRECTORIES_FILTER)) {
            for (Path formatter : paths) {
                boolean add = true;
                if (FORMATTER_FILTER.accept(formatter)) {
                    ConfigFile config = new ConfigFile(formatter, true, null);
                    if (publishedOnly && !config.isPublished()) {
                        continue;
                    }

                    List<String> applicableSchemas = config.listOfApplicableSchemas();

                    if (!schema.equalsIgnoreCase("all") && !isSchemaPluginFormatter) {
                        if (!applicableSchemas.contains(schema)) {
                            add = false;
                        }
                    }

                    if (add) {
                        String path = root.relativize(formatter).toString().replace("\\", "/");
                        if (path.startsWith("/")) {
                            path = path.substring(1);
                        }
                        final FormatterData formatterData;
                        if (isSchemaPluginFormatter) {
                            formatterData = new FormatterData(schema, path);
                        } else {
                            formatterData = new FormatterData(null, path);
                        }
                        response.add(formatterData);
                    }
                } else {
                    addFormatters(schema, response, root, formatter, isSchemaPluginFormatter, publishedOnly);
                }
            }
        }
    }

    @RequestMapping(value = "/{portal}/{lang}/md.formatter.list", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public FormatterDataResponse exec(
        @RequestParam(required = false) final String id,
        @RequestParam(required = false) final String uuid,
        @RequestParam(defaultValue = "all") String schema,
        @RequestParam(defaultValue = "false") boolean pluginOnly,
        @RequestParam(defaultValue = "true") boolean publishedOnly
    ) throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        if (id != null || uuid != null) {
            try {
                loadMetadata(applicationContext.getBean(IMetadataUtils.class), Integer.parseInt(resolveId(id, uuid)));
            } catch (Throwable e) {
                // its ok.  just can't use metadata
            }
        }

        if (schema == null)
            schema = "all";

        schema = schema.trim();

        FormatterDataResponse response = new FormatterDataResponse();
        if (!pluginOnly) {
            Path userXslDir = applicationContext.getBean(GeonetworkDataDirectory.class).getFormatterDir();
            addFormatters(schema, response, userXslDir, userXslDir, false, publishedOnly);
        }

        final Set<String> schemas = applicationContext.getBean(SchemaManager.class).getSchemas();
        for (String schemaName : schemas) {
            if (schema.equals("all") || schema.equals(schemaName)) {
                final Path schemaDir = applicationContext.getBean(SchemaManager.class).getSchemaDir(schemaName);
                final Path formatterDir = schemaDir.resolve(SCHEMA_PLUGIN_FORMATTER_DIR);
                addFormatters(schemaName, response, formatterDir, formatterDir, true, publishedOnly);
            }
        }
        return response;
    }

    @XmlRootElement(name = "formatters")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormatterDataResponse implements Serializable {
        private static final long serialVersionUID = 8674269207113596010L;

        @XmlElement(name = "formatter")
        private List<FormatterData> formatters = Lists.newArrayList();

        public void add(FormatterData formatterData) {
            this.formatters.add(formatterData);
        }

        @Override
        public String toString() {
            return "FormatterDataResponse{" + formatters + '}';
        }

        public List<FormatterData> getFormatters() {
            return formatters;
        }
    }

    @XmlRootElement(name = "formatter")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormatterData implements Serializable {
        private static final long serialVersionUID = 2015204126746590712L;
        @XmlElement(name = "schema")
        private final String schema;
        private final String id;

        public FormatterData(String schema, String id) {
            this.schema = schema;
            this.id = id;
        }

        @Override
        public String toString() {
            return "FormatterData{" + "schema ='" + schema + '\'' + ", id='" + id + '\'' + '}';
        }


        public String getSchema() {
            return schema;
        }

        public String getId() {
            return id;
        }
    }

}
