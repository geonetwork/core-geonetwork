package org.fao.geonet.kernel.schema;

import java.util.Map;

/**
 * Created by francois on 01/12/15.
 */
public interface ExportablePlugin {
    /**
     * Return the list of format to export to.
     * The key are the path to the XSL transformation to apply eg. convert/to19139.xsl
     * and the value is the file name.
     * @return
     */
    Map<String, String> getExportFormats();
}
