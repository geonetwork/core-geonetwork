/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.itextpdf.text.Image;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.fao.geonet.api.records.extent.MetadataExtentApi;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImageReplacedElementFactory implements ReplacedElementFactory {
    private static Set<String> imgFormatExts = null;
    private final ReplacedElementFactory superFactory;
    private final MapRenderer mapRenderer;
    private String baseURL;

    public ImageReplacedElementFactory(String baseURL, ReplacedElementFactory superFactory) {
        this.superFactory = superFactory;
        this.baseURL = baseURL;
        this.mapRenderer = null;
    }

    public ImageReplacedElementFactory(String baseURL, ReplacedElementFactory superFactory, MapRenderer mapRenderer) {
        this.superFactory = superFactory;
        this.baseURL = baseURL;
        this.mapRenderer = mapRenderer;
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

    @Override
    public ReplacedElement createReplacedElement(LayoutContext layoutContext, BlockBox box,
                                                 UserAgentCallback userAgentCallback, int cssWidth, int cssHeight) {
        org.w3c.dom.Element element = box.getElement();
        if (element == null) {
            return null;
        }

        String nodeName = element.getNodeName();
        String src = element.getAttribute("src");
        if ("img".equals(nodeName)
            && (src.startsWith(baseURL + "region.getmap.png") || src.endsWith("/extents.png") || src.endsWith("/geom.png"))
            && mapRenderer != null) {
            BufferedImage image = null;
            try {
                Map<String, String> parameters = getParams(src);

                String id = parameters.get(Params.ID);
                String srs = parameters.get(MetadataExtentApi.MAP_SRS_PARAM) != null ? parameters.get(MetadataExtentApi.MAP_SRS_PARAM) : "EPSG:4326";
                Integer width = parameters.get(MetadataExtentApi.WIDTH_PARAM) != null ? Integer.parseInt(parameters.get(MetadataExtentApi.WIDTH_PARAM)) : null;
                Integer height = parameters.get(MetadataExtentApi.HEIGHT_PARAM) != null ? Integer.parseInt(parameters.get(MetadataExtentApi.HEIGHT_PARAM)) : null;
                String background = parameters.get(MetadataExtentApi.BACKGROUND_PARAM);
                String geomParam = parameters.get(MetadataExtentApi.GEOM_PARAM);
                String geomType = parameters.get(MetadataExtentApi.GEOM_TYPE_PARAM) != null ? parameters.get(MetadataExtentApi.GEOM_TYPE_PARAM) : "WKT";
                String geomSrs = parameters.get(MetadataExtentApi.GEOM_SRS_PARAM) != null ? parameters.get(MetadataExtentApi.GEOM_SRS_PARAM) : "EPSG:4326";

                image = mapRenderer.render(
                    id, srs, width, height, background, geomParam, geomType, geomSrs);
            } catch (Exception e) {
                Log.warning(Geonet.GEONETWORK, "Error writing metadata to PDF", e);
            }
            float factor = layoutContext.getDotsPerPixel();
            return loadImage(layoutContext, box, userAgentCallback, cssWidth, cssHeight, new BufferedImageLoader(image), factor);
        } else if ("img".equals(nodeName)
            && (src.startsWith(baseURL + "region.getmap.png") || src.endsWith("/extents.png") || src.endsWith("/geom.png"))) {
            StringBuilder builder = new StringBuilder(baseURL);
            try {
                if (StringUtils.startsWith(src, "http")) {
                    builder = new StringBuilder();
                }
                String[] parts = src.split("\\?|&");
                builder.append(parts[0]);
                builder.append('?');
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) {
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
            return loadImage(layoutContext, box, userAgentCallback, cssWidth, cssHeight, new UrlImageLoader(builder.toString()), factor);
        } else if ("img".equals(nodeName) && isSupportedImageFormat(src)) {
            float factor = layoutContext.getDotsPerPixel();
            return loadImage(layoutContext, box, userAgentCallback, cssWidth, cssHeight, new UrlImageLoader(src), factor);
        }

        try {
            return superFactory.createReplacedElement(layoutContext, box, userAgentCallback, cssWidth, cssHeight);
        } catch (Throwable e) {
            return new EmptyReplacedElement(cssWidth, cssHeight);
        }
    }

    private Map<String, String> getParams(String src) throws MalformedURLException {
        URL url = new URL(src);
        String query = url.getQuery();
        String[] keyValuePairs = query.split("&");
        Map<String, String> parameters = new HashMap<>();
        for (String keyValuePair : keyValuePairs) {
            String[] pair = keyValuePair.split("=");
            String key = pair[0];
            String value = pair.length > 1 ? pair[1] : null;
            parameters.put(key, value);
        }
        return parameters;
    }

    private boolean isSupportedImageFormat(String imgUrl) {
        String ext = Files.getFileExtension(imgUrl);
        return ext.trim().isEmpty() || getSupportedExts().contains(ext);
    }

    private ReplacedElement loadImage(LayoutContext layoutContext, BlockBox box, UserAgentCallback userAgentCallback,
                                      int cssWidth, int cssHeight, ImageLoader imageLoader, float scaleFactor) {
        try {
            Image image = imageLoader.loadImage();

            image.scaleAbsolute(image.getPlainWidth() * scaleFactor, image.getPlainHeight() * scaleFactor);
            FSImage fsImage = new ITextFSImage(image);

            if (cssHeight > -1 && cssWidth > -1) {
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

    /* Define API for loading an itext pdf image from a source */

    private interface ImageLoader {
        Image loadImage() throws Exception;
    }

    /* Define a url image loader  */

    private class UrlImageLoader implements ImageLoader {
        private final String url;

        public UrlImageLoader(String url) {
            this.url = url;
        }

        @Override
        public Image loadImage() throws Exception {
            Log.error(Geonet.GEONETWORK, "URL -> " + url);

            try (InputStream input = new URL(url).openStream()) {
                byte[] bytes = IOUtils.toByteArray(input);
                return Image.getInstance(bytes);
            }
        }
    }

    /* Define an AWT BufferedImage image loader */

    private class BufferedImageLoader implements ImageLoader {
        private final BufferedImage bufferedImage;

        public BufferedImageLoader(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }

        @Override
        public Image loadImage() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return Image.getInstance(baos.toByteArray());
        }
    }

}
