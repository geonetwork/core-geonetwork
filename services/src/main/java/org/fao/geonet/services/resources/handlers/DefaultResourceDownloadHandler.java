package org.fao.geonet.services.resources.handlers;


import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.io.*;
import java.util.List;

public class DefaultResourceDownloadHandler implements IResourceDownloadHandler {

    public Element onDownload(ServiceContext context, Element params, int metadataId,
                              String fileName, File file) throws ResourceHandlerException {


        try {
            String requesterName =  Util.getParam(params, "name", "");
            String requesterMail =  Util.getParam(params, "email",  "");
            String requesterOrg =  Util.getParam(params, "org",  "");
            String requesterComments =  Util.getParam(params, "comments",  "");


            // Store download request for statistics
            String downloadDate = new ISODate().toString();
            storeFileDownloadRequest(context, metadataId, fileName, requesterName, requesterMail, requesterOrg,
                    requesterComments, downloadDate);

            return BinaryFile.encode(200, file.getAbsolutePath());

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceDownloadHandler (onDownload): " + ex.getMessage());
            ex.printStackTrace();
            throw new ResourceHandlerException(ex);
        }
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
    private void storeFileDownloadRequest(ServiceContext context, int metadataId, String fname,
                                          String requesterName, String requesterMail,
                                          String requesterOrg, String requesterComments,
                                          String downloadDate) {
        MetadataFileUpload metadataFileUpload;

        // Each download is related to a file upload record
        try {
            metadataFileUpload = context.getBean(MetadataFileUploadRepository.class).
                    findByMetadataIdAndFileNameNotDeleted(metadataId, fname);

        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            Log.warning(Geonet.RESOURCES, "Store file download request: No upload request for (metadataid, file): (" + metadataId + "," + fname + ")");

            // No related upload is found
            metadataFileUpload = null;
        }

        if (metadataFileUpload != null) {
            MetadataFileDownloadRepository repo = context.getBean(MetadataFileDownloadRepository.class);

            MetadataFileDownload metadataFileDownload = new MetadataFileDownload();

            metadataFileDownload.setMetadataId(metadataId);
            metadataFileDownload.setFileName(fname);
            metadataFileDownload.setRequesterName(requesterName);
            metadataFileDownload.setRequesterMail(requesterMail);

            metadataFileDownload.setRequesterOrg(requesterOrg);
            metadataFileDownload.setRequesterComments(requesterComments);
            metadataFileDownload.setDownloadDate(downloadDate);
            metadataFileDownload.setUserName(context.getUserSession().getUsername());
            metadataFileDownload.setFileUploadId(metadataFileUpload.getId());

            repo.save(metadataFileDownload);
        }
    }
}
