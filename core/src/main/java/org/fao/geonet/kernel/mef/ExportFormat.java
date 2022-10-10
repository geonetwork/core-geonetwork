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

package org.fao.geonet.kernel.mef;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.API;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkExtension;
import org.fao.geonet.kernel.schema.ExportablePlugin;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

import static org.fao.geonet.constants.Geonet.MEF_MARKER;

/**
 * An extension point called to create files to export as part of the MEF export.
 *
 * User: Jesse Date: 11/8/13 Time: 3:21 PM
 */
public class ExportFormat implements GeonetworkExtension {

    private static Logger LOGGER = Log.createLogger(ExportFormat.class, MEF_MARKER);
    /**
     * Return a list of &lt;filename, fileContents>.
     *
     * @param metadata the metadata to convert to files.
     */
    public static Iterable<Pair<String, String>> getFormats(ServiceContext context, AbstractMetadata metadata) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        MetadataSchema metadataSchema = dm.getSchema(schema);
        SchemaPlugin schemaPlugin = metadataSchema.getSchemaPlugin();
        if (schemaPlugin instanceof ExportablePlugin) {
            Map<String, String> allFormats = ((ExportablePlugin) schemaPlugin).getExportFormats();

            Set<Pair<String, String>> allExports = new HashSet<>();
            for (Map.Entry<String, String> entry : allFormats.entrySet()) {
                String xslFileName = entry.getKey();
                String outputFileName = entry.getValue();
                Path path = metadataSchema.getSchemaDir().resolve(xslFileName);
                if (Files.isRegularFile(path)) {
                    String outputData = formatData(metadata, true, path);
                    allExports.add(Pair.read(outputFileName, outputData));
                } else {
                    // A conversion that does not exist
                    LOGGER.debug(MEF_MARKER, String.format("Exporting MEF file for '%s' schema plugin formats. File '%s' not found",
                        metadataSchema.getName(),
                        path.getFileName()));
                }
            }
            return allExports;
        }
        return Collections.emptyList();
    }

    ;


    /**
     * Format xml data
     *
     * @return ByteArrayInputStream
     */
    public static String formatData(AbstractMetadata metadata, boolean transform, Path stylePath) throws Exception {
        String xmlData = metadata.getData();

        Element md = Xml.loadString(xmlData, false);

        // Apply a stylesheet transformation when schema is ISO profil
        if (transform) {
            md = Xml.transform(md, stylePath);
        }

        String data = Xml.getString(md);

        if (!data.startsWith("<?xml")) {
            data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + data;
        }

        return data;
    }

}
