package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataFileUpload_;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.io.PrintWriter;
import java.util.*;

import static org.fao.geonet.api.reports.ReportUtils.CSV_FORMAT;

/**
 * Creates a report for metadata file uploads.
 *
 * @author Jose García
 */
public class ReportUploads implements IReport {
    /**
     * Report filter.
     */
    private final ReportFilter reportFilter;

    @Autowired
    MetadataRepository metadataRepository;

    /**
     * Creates a report for metadata file uploads instance.
     *
     * @param filter report filter.
     */
    public ReportUploads(final ReportFilter filter) {
        this.reportFilter = filter;
    }


    /**
     * Creates a metadata file uploads report and streams to a PrintWriter.
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

            // Retrieve metadata file uploads
            final MetadataFileUploadRepository uploadRepository =
                context.getBean(MetadataFileUploadRepository.class);
            final Sort sort = Sort.by(Sort.Direction.ASC,
                SortUtils.createPath(MetadataFileUpload_.uploadDate));
            final List<MetadataFileUpload> records = uploadRepository.findAll(
                MetadataFileUploadSpecs.uploadDateBetweenAndByGroups(
                    reportFilter.getBeginDate(),
                    reportFilter.getEndDate(),
                    reportFilter.getGroups()),
                sort);

            // Write header
            csvFilePrinter.printRecord("Metadata file uploads");
            csvFilePrinter.println();

            String[] entries = ("Metadata ID#Metadata Title#File download#"
                + "File download date#Requester name#Requester mail#"
                + "Requester organisation#Requester comments#"
                + "Username#Surname#Name#Email#Profile#Delete date")
                .split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            List<User> users = context.getBean(UserRepository.class).findAll();

            for (MetadataFileUpload fileUpload : records) {
                String username = fileUpload.getUserName();
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
                }

                String fileName = fileUpload.getFileName();

                Optional<Metadata> metadata = metadataRepository.findById(fileUpload.getMetadataId());
                String metadataUuid = metadata.get().getUuid();
                String metadataTitle = ReportUtils.retrieveMetadataIndex(
                    metadataUuid, "resourceTitleObject", "default");


                // Online resource description from the index ...
                String uploadDescription = "";
                Set<String> fields = new HashSet<String>();
                fields.add("linkage_name_des");

// TODOES
//                throw new NotImplementedException("Not implemented in ES");
//                Map<String, Map<String, String>> fieldValues =
//                        LuceneSearcher.getAllMetadataFromIndexFor(
//                                context.getLanguage(), "_id",
//                                fileUpload.getMetadataId() + "",
//                                fields, false);
//
//                if (!fieldValues.isEmpty()) {
//                    uploadDescription =
//                            fieldValues.get("0").get("linkage_name_des");
//                }
//
                // Build the record element with the information for the report
                List<String> record = new ArrayList<>();
                record.add(metadataUuid);
                record.add(metadataTitle);
                record.add(fileName);
                record.add(uploadDescription);
                record.add(fileUpload.getUploadDate());
                record.add(username);
                record.add(surname);
                record.add(name);
                record.add(email);
                record.add(profile);
                record.add(fileUpload.getDeletedDate());

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
