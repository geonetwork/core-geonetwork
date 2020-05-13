//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.thumbnail;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import lizard.tiff.Tiff;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Controller
@ReadWriteController
@Deprecated
public class Set {
    private static final ImageObserver IMAGE_OBSERVER = (img, infoflags, x, y, width, height) -> false;

    private static final String IMAGE_TYPE = "png";
    private static final String SMALL_SUFFIX = "_s";
    private static final String FNAME_PARAM = "fname=";


    @RequestMapping(value = {"/{portal}/{lang}/md.thumbnail.upload"}, produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Response serviceSpecificExec(HttpServletRequest request,
                                        @PathVariable String lang,
                                        @RequestParam(Params.ID) String id,
                                        @RequestParam(Params.TYPE) String type,
                                        @RequestParam(Params.VERSION) String version,
                                        @RequestParam(Params.FNAME) MultipartFile file,
                                        @RequestParam(Params.SCALING_DIR) String scalingDir,
                                        @RequestParam(value = Params.SCALING, defaultValue = "false") boolean scaling,
                                        @RequestParam(Params.SCALING_FACTOR) int scalingFactor,
                                        @RequestParam(value = Params.CREATE_SMALL, defaultValue = "false") boolean createSmall,
                                        @RequestParam(value = Params.SMALL_SCALING_DIR, defaultValue = "") String smallScalingDir,
                                        @RequestParam(value = Params.SMALL_SCALING_FACTOR, defaultValue = "0") int smallScalingFactor
    ) throws Exception {

        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        ServiceContext context = serviceManager.createServiceContext("md.thumbnail.upload", lang, request);

        Lib.resource.checkEditPrivilege(context, id);

        //-----------------------------------------------------------------------
        //--- environment vars

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        //--- check if the metadata has been modified from last time

        if (version != null && !StringUtils.isEmpty(version) && !dataMan.getVersion(id).equals(version))
            throw new ConcurrentUpdateEx(id);

        //-----------------------------------------------------------------------
        //--- create the small thumbnail, removing the old one

        if (createSmall) {
            Path smallFile = getFileName(file.getOriginalFilename(), true);

            removeOldThumbnail(context, id, "small", false);
            createThumbnail(context, file, id, smallScalingFactor, smallScalingDir, smallFile);
            dataMan.setThumbnail(context, id, true, smallFile.toString(), false);
        }

        //-----------------------------------------------------------------------
        //--- create the requested thumbnail, removing the old one

        removeOldThumbnail(context, id, type, false);

        if (scaling) {
            Path newFile = getFileName(file.getOriginalFilename(), type.equals("small"));

            createThumbnail(context, file, id, scalingFactor, scalingDir, newFile);

            dataMan.setThumbnail(context, id, type.equals("small"), newFile.toString(), false);
        } else {
            //--- move uploaded file to destination directory
            final Store store = context.getBean("resourceStore", Store.class);
            final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
            final String metadataUuid = metadataUtils.getMetadataUuid(id);
            store.putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC);

            dataMan.setThumbnail(context, id, type.equals("small"), file.getOriginalFilename(), false);
        }

        dataMan.indexMetadata(id, true, null);

        return new Response(id, dataMan.getNewVersion(id));
    }

    // FIXME : not elegant
    public Element execOnHarvest(
        Element params,
        ServiceContext context,
        DataManager dataMan) throws Exception {

        String id = Util.getParam(params, Params.ID);

        //-----------------------------------------------------------------------
        //--- create the small thumbnail, removing the old one
        boolean createSmall = Util.getParam(params, Params.CREATE_SMALL, false);
        final String file = Util.getParam(params, Params.FNAME);
        String scalingDir = Util.getParam(params, Params.SCALING_DIR, "width");
        boolean scaling = Util.getParam(params, Params.SCALING, false);
        int scalingFactor = Util.getParam(params, Params.SCALING_FACTOR, 1);
        String type = Util.getParam(params, Params.TYPE);


        if (createSmall) {
            Path smallFile = getFileName(file, true);
            final Path inFile = context.getUploadDir().resolve(file);
            MultipartFile multipartFile = new FileWrappingMultipartFile(inFile, file);

            String smallScalingDir = Util.getParam(params, Params.SMALL_SCALING_DIR, "");
            int smallScalingFactor = Util.getParam(params, Params.SMALL_SCALING_FACTOR, 0);
            // FIXME should be done before removeOldThumbnail(context, dbms, id, "small");
            createThumbnail(context, multipartFile, id, smallScalingFactor, smallScalingDir, smallFile);
            dataMan.setThumbnail(context, id, true, smallFile.toString(), false);
        }

        //-----------------------------------------------------------------------
        //--- create the requested thumbnail, removing the old one
        removeOldThumbnail(context, id, type, false);
        saveThumbnail(scaling, file, type, scalingDir, scalingFactor, dataMan, id, context);

        //-----------------------------------------------------------------------
        dataMan.indexMetadata(id, true, null);
        Element response = new Element("Response");
        response.addContent(new Element("id").setText(id));

        return response;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private void saveThumbnail(boolean scaling, String file, String type, String scalingDir, int scalingFactor,
                               DataManager dataMan, String id, ServiceContext context) throws Exception {
        if (scaling) {
            Path newFile = getFileName(file, type.equals("small"));
            Path inFile = context.getUploadDir().resolve(file);

            MultipartFile multipartFile = new FileWrappingMultipartFile(inFile, file);
            createThumbnail(context, multipartFile, id, scalingFactor, scalingDir, newFile);

            try {
                Files.delete(inFile);
            } catch (IOException e) {
                context.error("Error while deleting thumbnail : " + inFile);
            }

            dataMan.setThumbnail(context, id, type.equals("small"), newFile.toString(), false);
        } else {
            //--- move uploaded file to destination directory
            Path inFile = context.getUploadDir().resolve(file);
            try {
                final Store store = context.getBean("resourceStore", Store.class);
                final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
                final String metadataUuid = metadataUtils.getMetadataUuid(id);
                store.putResource(context, metadataUuid, inFile, MetadataResourceVisibility.PUBLIC);
            } finally {
                IO.deleteFileOrDirectory(inFile);
            }

            dataMan.setThumbnail(context, id, type.equals("small"), file, false);
        }
    }

    private void removeOldThumbnail(ServiceContext context, String id, String type, boolean indexAfterChange) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        Element result = dataMan.getThumbnails(context, id);

        if (result == null)
            throw new IllegalArgumentException("Metadata not found --> " + id);

        result = result.getChild(type);

        //--- if there is no thumbnail, we return

        if (result == null)
            return;

        //-----------------------------------------------------------------------
        //--- remove thumbnail

        dataMan.unsetThumbnail(context, id, type.equals("small"), indexAfterChange);

        //--- remove file

        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String metadataUuid = metadataUtils.getMetadataUuid(id);
        store.delResource(context, metadataUuid, getFileName(result.getText()));
    }

    //--------------------------------------------------------------------------

    private void createThumbnail(ServiceContext context, MultipartFile inFile, String metadataId, int scalingFactor,
                                 String scalingDir, Path filename) throws Exception {
        BufferedImage origImg = getImage(inFile);

        int imgWidth = origImg.getWidth();
        int imgHeight = origImg.getHeight();

        int width;
        int height;

        if (scalingDir.equals("width")) {
            width = scalingFactor;
            height = width * imgHeight / imgWidth;
        } else {
            height = scalingFactor;
            width = height * imgWidth / imgHeight;
        }

        Image thumb = origImg.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);

        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);

        Graphics2D g = bimg.createGraphics();
        g.drawImage(thumb, 0, 0, null);
        g.dispose();

        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String metadataUuid = metadataUtils.getMetadataUuid(metadataId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bimg, IMAGE_TYPE, os);
        store.putResource(context, metadataUuid, filename.toString(), new ByteArrayInputStream(os.toByteArray()), null,
                MetadataResourceVisibility.PUBLIC, true);
    }

    //--------------------------------------------------------------------------

    private Path getFileName(String file, boolean small) {
        int pos = file.lastIndexOf('.');

        if (pos != -1) {
            file = file.substring(0, pos);
        }

        final String path;
        if (small) {
            path = file + SMALL_SUFFIX + "." + IMAGE_TYPE;
        } else {
            path = file + "." + IMAGE_TYPE;
        }

        return IO.toPath(path);
    }

    //--------------------------------------------------------------------------
    @Nonnull
    public BufferedImage getImage(MultipartFile inFile) throws IOException {
        String lcFile = inFile.getName().toLowerCase();

        if (lcFile.endsWith(".tif") || lcFile.endsWith(".tiff")) {
            //--- load the TIFF/GEOTIFF file format

            Image image = getTiffImage(inFile);

            int width = image.getWidth(IMAGE_OBSERVER);
            int height = image.getHeight(IMAGE_OBSERVER);

            BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bimg.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bimg;
        }
        try (InputStream in = inFile.getInputStream()) {
            return ImageIO.read(in);
        }
    }

    //--------------------------------------------------------------------------

    private Image getTiffImage(MultipartFile inFile) throws IOException {
        Tiff t = new Tiff();
        t.read(inFile.getBytes());

        if (t.getPageCount() == 0)
            throw new IOException("No images inside TIFF file");

        return t.getImage(0);
    }

    /**
     * Return file name from full url thumbnail formated as http://wwwmyCatalogue.com:8080/srv/eng/resources.get?uuid=34baff6e-3880-4589-a5e9-4aa376ecd2a5&fname=snapshot3.png
     */
    private String getFileName(String file) {
        if (!file.contains(FNAME_PARAM)) {
            return file;
        } else {
            return file.substring(file.lastIndexOf(FNAME_PARAM) + FNAME_PARAM.length());
        }
    }

    @XmlRootElement(name = "response")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static final class Response {

        private final String id;
        private final String version;

        public Response(String id, String newVersion) {
            this.id = id;
            this.version = newVersion;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    private static class FileWrappingMultipartFile implements MultipartFile {
        private final Path inFile;

        public FileWrappingMultipartFile(Path inFile, String file) {
            this.inFile = inFile;
        }

        @Override
        public String getName() {
            return inFile.getFileName().toString();
        }

        @Override
        public String getOriginalFilename() {
            return inFile.getFileName().toString();
        }

        @Override
        public String getContentType() {
            return "image/" + com.google.common.io.Files.getFileExtension(getName());
        }

        @Override
        public boolean isEmpty() {
            return getSize() == 0;
        }

        @Override
        public long getSize() {
            try {
                return Files.size(inFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(inFile);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return IO.newInputStream(inFile);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.copy(inFile, dest.toPath());
        }
    }
}
