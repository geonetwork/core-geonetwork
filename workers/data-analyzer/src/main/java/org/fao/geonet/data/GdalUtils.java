package org.fao.geonet.data;

import org.fao.geonet.data.model.gdal.GdalDataset;
import org.fao.geonet.kernel.BatchEditParameter;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

public class GdalUtils {
    public static Element gdalSchemaToIso(GdalDataset dataset) {
        String xml = "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"/>";
        try {
            Element element = Xml.loadString(xml, false);

            BatchEditParameter batchEditParameter = new BatchEditParameter();


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
