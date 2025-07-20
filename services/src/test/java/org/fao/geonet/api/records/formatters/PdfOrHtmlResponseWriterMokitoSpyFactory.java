package org.fao.geonet.api.records.formatters;

import org.mockito.Mockito;

public class PdfOrHtmlResponseWriterMokitoSpyFactory {
    public PdfOrHtmlResponseWriter createSpy() {
        PdfOrHtmlResponseWriter pdfOrHtmlResponseWriter = new PdfOrHtmlResponseWriter();
        return Mockito.spy(pdfOrHtmlResponseWriter);
    }
}
