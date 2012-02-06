package org.fao.geonet.kernel.csw.services;

import org.apache.commons.lang.StringUtils;

/**
 * Resource types supported for Harvest operation. Only iso19139 is supported at this moment.
 *
 * Example list from OGC 07-006 :
 *
 * Table 69 — URIs for well known metadata standards
 * URI                                                   Description
 * http://www.opengis.net/wms                            WMS capability document, all current versions
 * http://www.opengis.net/wfs                            WFS capability document, versions 1.0 and 1.1
 * http://www.opengis.net/wfs/1.2.0                      WFS capability document, version 1.2
 * http://www.opengis.net/wcs                            WCS capability document, version 1.0
 * http://www.opengis.net/wcs/1.1                        WCS capability document, version 1.1
 * http://www.opengis.net/cat/csw                        CSW capability document, versions 2.0.0 and 2.0.1
 * http://www.opengis.net/cat/csw/2.0.2                  CSW capability document, version 2.0.2
 * http://www.fgdc.gov/metadata/csdgm                    Content Standard for Digital Geospatial Metadata (CSDGM), Vers. 2 (FGDC-STD-001-1998)
 * http://www.auslig.gov.au/dtd/anzmeta-1.3.dtd          Australian Spatial Data Infrastructure Standard
 * http://www.isotc211.org/schemas/2005/gmd/             ISO19139 document
 * http://metadata.dod.mil/mdr/ns/DDMS/1.3/              DEPARTMENT OF DEFENSE DISCOVERY METADATA STANDARD (DDMS)
 *
 * @author heikki doeleman
 */
public enum SupportedResourceType {

    ISO19139 {
        public String toString() {
            return "http://www.isotc211.org/schemas/2005/gmd/";
        }
    };

    /**
     * Returns the enum value that has a toString equal to requested string, or null if not found.
     *
     * @param string - a string
     * @return SupportedResourceType if found, null otherwise
     */
    public static SupportedResourceType fromString(String string) {
        if (StringUtils.isNotEmpty(string)) {
            for (SupportedResourceType supportedResourceType : SupportedResourceType.values()) {
                if (string.equals(supportedResourceType.toString())) {
                    return supportedResourceType;
                }
            }
        }
        return null;
    }

}