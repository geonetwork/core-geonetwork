package org.fao.geonet.services.metadata.format;

/**
 * Enumerates the support output types.
 *
 * @author Jesse on 10/26/2014.
 */
public enum FormatType {
    xml("application/xml"), html("text/html"), pdf("application/pdf"), txt("text/plain"), testpdf("application/test-pdf");
    public final String contentType;

    private FormatType(String contentType) {
        this.contentType = contentType;
    }
}
