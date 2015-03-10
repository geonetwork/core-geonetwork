package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkExtension;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * An extension point called to create files to export as part of the MEF export.
 *
 * User: Jesse
 * Date: 11/8/13
 * Time: 3:21 PM
 */
public abstract class ExportFormat implements GeonetworkExtension {
    /**
     * Return a list of &lt;filename, fileContents>.
     *
     *
     * @param context
     * @param metadata the metadata to convert to files.
     *
     * @return
     */
    public abstract Iterable<Pair<String, String>> getFormats(ServiceContext context, Metadata metadata) throws Exception;


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
