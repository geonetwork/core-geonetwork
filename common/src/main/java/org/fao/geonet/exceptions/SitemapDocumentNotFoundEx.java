package org.fao.geonet.exceptions;

/**
 * Exception class issued when requesting a non existing Sitemap document.
 *
 * @author Jose García
 */
public class SitemapDocumentNotFoundEx extends NotFoundEx {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    private static final long serialVersionUID = -1237422298790255281L;

    public SitemapDocumentNotFoundEx(int page)
    {
        super("Sitemap document not found", page);

        id = "sitemap-document-not-found";
    }
}
