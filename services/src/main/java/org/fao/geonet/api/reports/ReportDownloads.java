package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.csv.CSVFormat;
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
                        if (userFilter.isPresent()) {
                            User userDownload = userDownloadFilter.get();

                            requesterName = userDownload.getName() + " "
                                + userDownload.getSurname();
                            requesterMail = userDownload.getEmail();
                        }
                    }
                }


                Optional<Metadata> metadata = metadataRepository.findById(fileDownload.getMetadataId());
                String metadataUuid = metadata.get().getUuid();
                String metadataTitle = ReportUtils.retrieveMetadataIndex(
                    metadataUuid, "resourceTitleObject", "default");

                List<String> record = new ArrayList<>();
                record.add(metadataUuid);
                record.add(metadataTitle);
                record.add(fileDownload.getFileName());
                record.add(fileDownload.getDownloadDate());
                record.add(requesterName);
                record.add(requesterMail);
                record.add(fileDownload.getRequesterOrg());
                record.add(fileDownload.getRequesterComments());
                record.add(username);
                record.add(surname);
                record.add(name);
                record.add(email);
                record.add(profile);
                record.add(metadataFileUpload.isPresent() ? metadataFileUpload.get().getDeletedDate() : "");

                csvFilePrinter.printRecord(record);
            }

        } finally {
            writer.flush();
            if (csvFilePrinter != null) {
                csvFilePrinter.flush();
            }
        }
    }
}
