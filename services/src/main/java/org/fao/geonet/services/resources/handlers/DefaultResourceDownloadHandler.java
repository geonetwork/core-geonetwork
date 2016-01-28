package org.fao.geonet.services.resources.handlers;


import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileDownload;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.repository.MetadataFileDownloadRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DefaultResourceDownloadHandler implements IResourceDownloadHandler {

    @Autowired
    private ThreadPool threadPool;
    public HttpEntity<byte[]> onDownload(ServiceContext context, NativeWebRequest request, int metadataId,
                              String fileName, Path file) throws ResourceHandlerException {


        try {
            String requesterName =  getParam(request, "name", "");
            String requesterMail =  getParam(request, "email",  "");
            String requesterOrg =  getParam(request, "org",  "");
            String requesterComments =  getParam(request, "comments",  "");


            // Store download request for statistics
            String downloadDate = new ISODate().toString();
            storeFileDownloadRequest(context, metadataId, fileName, requesterName, requesterMail, requesterOrg,
                    requesterComments, downloadDate);

            if (Files.exists(file) && request.checkNotModified(Files.getLastModifiedTime(file).toMillis())) {
                return null;
            }

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            headers.add("Cache-Control", "no-cache");
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                String ext = com.google.common.io.Files.getFileExtension(file.getFileName().toString()).toLowerCase();
                switch (ext) {
                    case "png":
                    case "gif":
                    case "bmp":
                    case "tif":
                    case "tiff":
                    case "jpg":
                    case "jpeg":
                        contentType = "image/" + ext;
                        break;
                    case "txt":
                    case "html":
                        contentType = "text/" + ext;
                        break;
                    default:
                        contentType = "application/" + ext;
                }
            }

            headers.add("Content-Type", contentType);

            return new HttpEntity<>(Files.readAllBytes(file), headers);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceDownloadHandler (onDownload): " + ex.getMessage(), ex);
            throw new ResourceHandlerException(ex);
        }
    }

    private String getParam(NativeWebRequest request, String paramName, String defaultVal) {
        String val = request.getParameter(paramName);
        if (val == null) {
            return defaultVal;
        }
        return val;
    }

    @Override
    public Element onDownloadMultiple(ServiceContext context, Element params, int metadataId, List<Element> files)
            throws ResourceHandlerException {

        try {
            String requesterName =  Util.getParam(params, "name", "");
            String requesterMail =  Util.getParam(params, "email",  "");
            String requesterOrg =  Util.getParam(params, "org",  "");
            String requesterComments =  Util.getParam(params, "comments",  "");

            String fileList = "";

            for (Object o : files) {
                Element elem = (Element)o;
                String fname = elem.getText();

                if (StringUtils.isEmpty(fileList)) {
                    fileList = fname;
                } else {
                    fileList = fileList + "~" + fname;
                }
            }


            //--- stores download stats for each file downloaded
            String downloadDate = new ISODate().toString();

            for (Object o : files) {
                Element elem = (Element)o;
                String fname = elem.getText();

                // Store download request for statistics
                storeFileDownloadRequest(context, metadataId, fname, requesterName, requesterMail, requesterOrg,
                        requesterComments, downloadDate);
            }


            return null;

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceDownloadHandler (onDownloadMultiple): " + ex.getMessage());
            ex.printStackTrace();
            throw new ResourceHandlerException(ex);
        }

    }

    /**
     * * Stores a file download request in the MetadataFileDownloads table.
     *
     * @param context
     * @param metadataId
     * @param fname
     * @param requesterName
     * @param requesterMail
     * @param requesterOrg
     * @param requesterComments
     * @param downloadDate
     */
    private void storeFileDownloadRequest(final ServiceContext context, final int metadataId, final String fname,
                                          final String requesterName, final String requesterMail,
                                          final String requesterOrg, final String requesterComments,
                                          final String downloadDate) {
        final MetadataFileUploadRepository uploadRepository = context.getBean(MetadataFileUploadRepository.class);
        final MetadataFileDownloadRepository repo = context.getBean(MetadataFileDownloadRepository.class);
        final String userName = context.getUserSession().getUsername();

        threadPool.runTask(new Runnable() {
            @Override
            public void run() {
                MetadataFileUpload metadataFileUpload;

                // Each download is related to a file upload record
                try {
                    metadataFileUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, fname);

                } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
                    // No related upload is found
                    metadataFileUpload = null;
                }

                if (metadataFileUpload != null) {
            MetadataFileDownload metadataFileDownload = new MetadataFileDownload();

                    metadataFileDownload.setMetadataId(metadataId);
                    metadataFileDownload.setFileName(fname);
                    metadataFileDownload.setRequesterName(requesterName);
                    metadataFileDownload.setRequesterMail(requesterMail);

                    metadataFileDownload.setRequesterOrg(requesterOrg);
                    metadataFileDownload.setRequesterComments(requesterComments);
                    metadataFileDownload.setDownloadDate(downloadDate);
                    metadataFileDownload.setUserName(userName);
                    metadataFileDownload.setFileUploadId(metadataFileUpload.getId());

                    repo.save(metadataFileDownload);
                }
            }
        });
    }
}
