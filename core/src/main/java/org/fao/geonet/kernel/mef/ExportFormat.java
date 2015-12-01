package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An extension point called to create files to export as part of the MEF export.
 *
 * User: Jesse
 * Date: 11/8/13
 * Time: 3:21 PM
 */
public class ExportFormat implements GeonetworkExtension {
    /**
     * Return a list of &lt;filename, fileContents>.
     *
     * @param context
     * @param metadata the metadata to convert to files.
     *
     * @return
     */
    public static Iterable<Pair<String, String>>  getFormats(ServiceContext context, Metadata metadata) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        MetadataSchema metadataSchema = dm.getSchema(schema);
        SchemaPlugin schemaPlugin = metadataSchema.getSchemaPlugin();
        if (schemaPlugin instanceof ExportablePlugin) {
            Map<String, String> allFormats = ((ExportablePlugin) schemaPlugin).getExportFormats();
            Iterator<String> allFiles = allFormats.keySet().iterator();
            Set<Pair<String, String>> allExports = new HashSet<>();
            while (allFiles.hasNext()) {
                String xslFileName = allFiles.next();
                String outputFileName = allFormats.get(xslFileName);
                Path path = metadataSchema.getSchemaDir().resolve(xslFileName);
                if (Files.isRegularFile(path)) {
                    String outputData = formatData(metadata, true, path);
                    allExports.add(Pair.read(outputFileName, outputData));
                } else {
                    // A conversion that does not exist
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, String.format("Exporting MEF file for '%s' schema plugin formats. File '%s' not found",
                                metadataSchema.getName(),
                                path.getFileName()));
                    }
                }
            }
            return allExports;
        }
        return Collections.emptyList();
    };


    /**
     * Format xml data
     *
     * @param metadata
     * @param transform
     * @return ByteArrayInputStream
     * @throws Exception
     */
    public static String formatData(Metadata metadata, boolean transform, Path stylePath) throws Exception {
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
