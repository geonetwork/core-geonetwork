/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataFileDownloadSpecs;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fao.geonet.api.reports.ReportUtils.CSV_FORMAT;

/**
 * Creates a report for metadata file downloads.
 *
 * @author Jose García
 */
public class ReportDownloads implements IReport {
    /**
     * Report filter.
     */
    private final ReportFilter reportFilter;

    @Autowired
    MetadataRepository metadataRepository;

    /**
     * Creates a report for metadata file downloads instance.
     *
     * @param filter report filter.
     */
    public ReportDownloads(final ReportFilter filter) {
        this.reportFilter = filter;
    }

    /**
     * Creates a metadata file downloads report and streams to a PrintWriter.
     *
     * @param context Service context.
     * @param writer  Writer.
     * @throws Exception Exception creating a report.
     */
    public void create(final ServiceContext context,
                       final PrintWriter writer) throws Exception {
        CSVPrinter csvFilePrinter = null;

        try {
            // Initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(writer, CSV_FORMAT);

            // Retrieve metadata file downloads
            final MetadataFileDownloadRepository downloadRepository =
                context.getBean(MetadataFileDownloadRepository.class);

            final Sort sort = Sort.by(Sort.Direction.ASC,
                SortUtils.createPath(MetadataFileDownload_.downloadDate));
            final List<MetadataFileDownload> records =
                downloadRepository.findAll(
                    MetadataFileDownloadSpecs
                        .downloadDateBetweenAndByGroups(
                            reportFilter.getBeginDate(),
                            reportFilter.getEndDate(),
                            reportFilter.getGroups()),
                    sort);

            // Write header
            csvFilePrinter.printRecord("Metadata file downloads");
            csvFilePrinter.println();

            String[] entries = ("Metadata ID#Metadata Title#File download#"
                + "File download date#Requester name#Requester mail#"
                + "Requester organisation#Requester comments#"
                + "Username#Surname#Name#Email#Profile#Delete date")
                .split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            MetadataFileUploadRepository uploadRepo =
                context.getBean(MetadataFileUploadRepository.class);

            List<User> users = context.getBean(UserRepository.class).findAll();

            for (MetadataFileDownload fileDownload : records) {
                // User should be the user that uploaded the file
                int fileUploadId = fileDownload.getFileUploadId();
                Optional<MetadataFileUpload> metadataFileUpload =
                    uploadRepo.findOne(
                        MetadataFileUploadSpecs.hasId(fileUploadId));

                String username = metadataFileUpload.isPresent() ? metadataFileUpload.get().getUserName() : "";
                String name = "";
                String surname = "";
                String email = "";
                String profile = "";
                String requesterName = "";
                String requesterMail = "";

                Optional<User> userFilter = users.stream().filter(
                    u -> u.getUsername().equals(
                        username)).findFirst();

                if (userFilter.isPresent()) {
                    User user = userFilter.get();

                    name = Optional.ofNullable(
                        user.getName()).orElse("");
                    surname = Optional.ofNullable(
                        user.getSurname()).orElse("");
                    email = Optional.ofNullable(
                        user.getEmail()).orElse("");
                    profile = user.getProfile().name();

                    requesterName = fileDownload.getRequesterName();
                    requesterMail = fileDownload.getRequesterMail();
                    if (StringUtils.isEmpty(requesterName)
                        && StringUtils.isNotEmpty(
                        fileDownload.getUserName())) {

                        Optional<User> userDownloadFilter =
                            users.stream().filter(
                                    u -> u.getUsername().equals(
                                        fileDownload.getUserName()))
                                .findFirst();
                        if (userDownloadFilter.isPresent()) {
                            User userDownload = userDownloadFilter.get();

                            requesterName = userDownload.getName() + " "
                                + userDownload.getSurname();
                            requesterMail = userDownload.getEmail();
                        }
                    }
                }


                Optional<Metadata> metadata = metadataRepository.findById(fileDownload.getMetadataId());
                String metadataUuid = "NOT_FOUND";
                String metadataTitle = "NOT_FOUND";
                if (metadata.isPresent()) {
                    metadataUuid = metadata.get().getUuid();
                    metadataTitle = ReportUtils.retrieveMetadataIndex(
                        metadataUuid, "resourceTitleObject", "default");
                }

                List<String> recordDownloadInfo = new ArrayList<>();
                recordDownloadInfo.add(metadataUuid);
                recordDownloadInfo.add(metadataTitle);
                recordDownloadInfo.add(fileDownload.getFileName());
                recordDownloadInfo.add(fileDownload.getDownloadDate());
                recordDownloadInfo.add(requesterName);
                recordDownloadInfo.add(requesterMail);
                recordDownloadInfo.add(fileDownload.getRequesterOrg());
                recordDownloadInfo.add(fileDownload.getRequesterComments());
                recordDownloadInfo.add(username);
                recordDownloadInfo.add(surname);
                recordDownloadInfo.add(name);
                recordDownloadInfo.add(email);
                recordDownloadInfo.add(profile);
                recordDownloadInfo.add(metadataFileUpload.isPresent() ? metadataFileUpload.get().getDeletedDate() : "");

                csvFilePrinter.printRecord(recordDownloadInfo);
            }

        } finally {
            writer.flush();
            if (csvFilePrinter != null) {
                csvFilePrinter.flush();
            }
        }
    }
}
