package org.fao.geonet.services.metadata.format;

import com.itextpdf.text.Image;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

public class ImageReplacedElementFactory implements ReplacedElementFactory {
    private final ReplacedElementFactory superFactory;
    private String baseURL;

    public ImageReplacedElementFactory(String baseURL, ReplacedElementFactory superFactory) {
        this.superFactory = superFactory;
        this.baseURL = baseURL;
    }

    @Override
    public ReplacedElement createReplacedElement(LayoutContext layoutContext, BlockBox box,
            UserAgentCallback userAgentCallback, int cssWidth, int cssHeight) {
        org.w3c.dom.Element element = box.getElement();
        if (element == null) {
            return null;
        }

        String nodeName = element.getNodeName();
        String src = element.getAttribute("src");
        if ("img".equals(nodeName) && src.contains("region.getmap.png")) {
            InputStream input = null;
            try {
                String[] parts = src.split("\\?|&");
                StringBuilder builder = new StringBuilder(baseURL);
                builder.append(parts[0]);
                builder.append('?');
                for (int i = 1; i < parts.length ; i++) {
                    if(i>1) {
                        builder.append('&');
                    }
                    String[] param = parts[i].split("=");
                    builder.append(param[0]);
                    builder.append('=');
                    builder.append(URLEncoder.encode(param[1], "UTF-8"));
                }
                input = new URL(builder.toString()).openStream();
                byte[] bytes = IOUtils.toByteArray(input);
                Image image = Image.getInstance(bytes);

                float factor = layoutContext.getDotsPerPixel();
                image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
                FSImage fsImage = new ITextFSImage(image);
                
                if(cssHeight > -1 && cssWidth > -1) {
                    fsImage.scale(cssWidth, cssHeight);
                }
                
                return new ITextImageElement(fsImage);
            } catch (Exception e) {
                Log.error(Geonet.GEONETWORK, "Error writing metadata to PDF", e);
            } finally {
                if (input != null) {
                    org.apache.commons.io.IOUtils.closeQuietly(input);
                }
            }
        }

        return superFactory.createReplacedElement(layoutContext, box, userAgentCallback, cssWidth, cssHeight);
    }

    @Override
    public void reset() {
        superFactory.reset();
    }

    @Override
    public void remove(org.w3c.dom.Element e) {
        superFactory.remove(e);
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener listener) {
        superFactory.setFormSubmissionListener(listener);
    }

}