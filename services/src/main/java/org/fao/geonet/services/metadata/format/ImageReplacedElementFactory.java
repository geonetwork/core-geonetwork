package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.itextpdf.text.Image;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.EmptyReplacedElement;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import javax.imageio.ImageIO;

public class ImageReplacedElementFactory implements ReplacedElementFactory {
    private static Set<String> imgFormatExts = null;
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
            StringBuilder builder = new StringBuilder(baseURL);
            try {
                String[] parts = src.split("\\?|&");
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
            } catch (Exception e) {
                Log.warning(Geonet.GEONETWORK, "Error writing metadata to PDF", e);
            }
            float factor = layoutContext.getDotsPerPixel();
            return loadImage(layoutContext, box, userAgentCallback, cssWidth, cssHeight, builder.toString(), factor);
        } else if ("img".equals(nodeName) && isSupportedImageFormat(src)) {
            float factor = layoutContext.getDotsPerPixel();
            return loadImage(layoutContext, box, userAgentCallback, cssWidth, cssHeight, src, factor);
        }

        try {
            return superFactory.createReplacedElement(layoutContext, box, userAgentCallback, cssWidth, cssHeight);
        } catch (Throwable e) {
            return new EmptyReplacedElement(cssWidth, cssHeight);
        }
    }

    private boolean isSupportedImageFormat(String imgUrl) {
        String ext = Files.getFileExtension(imgUrl);
        return ext.trim().isEmpty() || getSupportedExts().contains(ext);
    }

    private static Set<String> getSupportedExts() {
        if (imgFormatExts == null) {
            synchronized (ImageReplacedElementFactory.class) {
                if (imgFormatExts == null) {
                    imgFormatExts = Sets.newHashSet();
                    for (String ext : ImageIO.getReaderFileSuffixes()) {
                        imgFormatExts.add(ext.toLowerCase());
                    }
                }
            }
        }

        return imgFormatExts;
    }

    private ReplacedElement loadImage(LayoutContext layoutContext, BlockBox box, UserAgentCallback userAgentCallback,
                                      int cssWidth, int cssHeight, String url, float scaleFactor) {
        InputStream input = null;
        try {
            input = new URL(url).openStream();
            byte[] bytes = IOUtils.toByteArray(input);
            Image image = Image.getInstance(bytes);

            image.scaleAbsolute(image.getPlainWidth() * scaleFactor, image.getPlainHeight() * scaleFactor);
            FSImage fsImage = new ITextFSImage(image);

            if(cssHeight > -1 && cssWidth > -1) {
                fsImage.scale(cssWidth, cssHeight);
            }
            int maxWidth = (int) (900 * scaleFactor);
            if (fsImage.getWidth() > maxWidth) {
                int ratio = fsImage.getWidth() / maxWidth;
                fsImage.scale(maxWidth, fsImage.getHeight() / ratio);
            }

            return new ITextImageElement(fsImage);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error writing metadata to PDF", e);

            try {
                return superFactory.createReplacedElement(layoutContext, box, userAgentCallback, cssWidth, cssHeight);
            } catch (Throwable e2) {
                return new EmptyReplacedElement(cssWidth, cssHeight);
            }
        } finally {
            if (input != null) {
                IOUtils.closeQuietly(input);
            }
        }
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
