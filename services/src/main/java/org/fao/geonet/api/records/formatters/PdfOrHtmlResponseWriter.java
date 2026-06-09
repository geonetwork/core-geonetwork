package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
class PdfOrHtmlResponseWriter {

    void writeOutResponse(ServiceContext context, String metadataUuid, String lang, HttpServletResponse response, FormatType formatType, byte[] formattedMetadata) throws Exception {
        response.setContentType(formatType.contentType);
        String filename = "metadata-" + metadataUuid + "." + formatType;
        response.addHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        response.setStatus(HttpServletResponse.SC_OK);
        if (formatType == FormatType.pdf) {
            writerAsPDF(context, response, formattedMetadata, lang);
        } else {
            response.setCharacterEncoding(Constants.ENCODING);
            response.setContentType(formatType.contentType);
            response.setContentLength(formattedMetadata.length);
            response.setHeader("Cache-Control", "no-cache");
            response.getOutputStream().write(formattedMetadata);
        }
    }

    private void writerAsPDF(ServiceContext context, HttpServletResponse response, byte[] bytes, String lang) throws IOException, com.lowagie.text.DocumentException {
        final String htmlContent = new String(bytes, Constants.CHARSET);
        try {
            XslUtil.setNoScript();
            ITextRenderer renderer = new ITextRenderer();
            String siteUrl = context.getBean(SettingManager.class).getSiteURL(lang);
            MapRenderer mapRenderer = new MapRenderer(context);
            renderer.getSharedContext().setReplacedElementFactory(new ImageReplacedElementFactory(siteUrl.replace("/" + lang + "/", "/eng/"), renderer.getSharedContext()
                .getReplacedElementFactory(), mapRenderer));
            renderer.getSharedContext().setDotsPerPixel(13);
            renderer.setDocumentFromString(htmlContent, siteUrl);
            renderer.layout();
            renderer.createPDF(response.getOutputStream());
        } catch (final Exception e) {
            Log.error(Geonet.FORMATTER, "Error converting formatter output to a file: " + htmlContent, e);
            throw e;
        }
    }
}
