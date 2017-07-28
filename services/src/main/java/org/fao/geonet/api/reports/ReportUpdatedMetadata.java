package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jose on 31/01/17.
 */
public class ReportUpdatedMetadata implements IReport {
    /**
     * Report filter.
     */
    private ReportFilter reportFilter;

    /**
     *  Creates a report for metadata file uploads instance.
     *
     * @param filter report filter.
     */
    public ReportUpdatedMetadata(final ReportFilter filter) {
        this.reportFilter = filter;
    }

    /**
     * Creates the users report and streams to a PrintWriter.
     *
     * @param context Service context.
     * @param writer Writer.
     * @throws Exception  Exception creating a report.
     */
    public void create(final ServiceContext context,
                       final PrintWriter writer) throws Exception {
        CSVPrinter csvFilePrinter = null;

        try {
            //initialize CSVPrinter object
            CSVFormat csvFileFormat =
                    CSVFormat.DEFAULT.withRecordSeparator("\n");
            csvFilePrinter = new CSVPrinter(writer, csvFileFormat);

            // Retrieve metadata
            final MetadataRepository metadataRepository =
                    context.getBean(MetadataRepository.class);
            final List<Metadata> records =
                    metadataRepository.getMetadataReports().
                            getUpdatedMetadata(
                                    reportFilter.getBeginDate(),
                                    reportFilter.getEndDate(),
                                    reportFilter.getGroups());

            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(writer, csvFileFormat);

            // Write header
            csvFilePrinter.printRecord("Metadata Updated");
            csvFilePrinter.println();

            String[] entries = (
                    "Metadata ID#Metadata Title#Metadata Create date#"
                    + "Owner Username#Owner Surname#Owner Name#Owner Email#"
                    + "Group Owner#Group Owner Email").split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            List<User> users =
                    context.getBean(UserRepository.class).findAll();
            List<Group> groups =
                    context.getBean(GroupRepository.class).findAll();

            // Process the records
            for (Metadata metadata : records) {
                String userOwnerUsername = "";
                String userOwnerName = "";
                String userOwnerSurname = "";
                String userOwnerMail = "";
                String groupOwnerName = "";
                String groupOwnerMail = "";

                Integer mdUserOwner =
                        metadata.getSourceInfo().getOwner();
                if (mdUserOwner != null) {
                    Optional<User> userOwnerFilter = users.stream().filter(
                            u -> u.getId() == mdUserOwner)
                            .findFirst();

                    if (userOwnerFilter.isPresent()) {
                        User userOwner = userOwnerFilter.get();

                        userOwnerUsername = Optional.ofNullable(
                                userOwner.getUsername()).orElse("");
                        userOwnerName = Optional.ofNullable(
                                userOwner.getName()).orElse("");
                        userOwnerSurname = Optional.ofNullable(
                                userOwner.getSurname()).orElse("");
                        userOwnerMail = Optional.ofNullable(
                                userOwner.getEmail()).orElse("");
                    }
                }

                Integer mdGroupOwner =
                        metadata.getSourceInfo().getGroupOwner();
                if (mdGroupOwner != null) {
                    Optional<Group> groupOwnerFilter = groups.stream().filter(
                            g -> g.getId() == mdGroupOwner)
                            .findFirst();
                    if (groupOwnerFilter.isPresent()) {
                        Group groupOwner = groupOwnerFilter.get();

                        String groupNameTranslation =
                                groupOwner.getLabelTranslations().get(
                                        context.getLanguage());
                        groupOwnerName =
                                Optional.ofNullable(groupNameTranslation)
                                        .orElse(groupOwner.getName());

                        groupOwnerMail = Optional.ofNullable(
                                groupOwner.getEmail()).orElse("");
                    }
                }

                String mdTitle = ReportUtils.retrieveMetadataTitle(
                        context, metadata.getId());

                // Build the record element with the information for the report
                List<String> record = new ArrayList<>();
                record.add(metadata.getUuid());
                record.add("" + mdTitle);
                record.add("" + metadata.getDataInfo().getCreateDate());
                record.add(userOwnerUsername);
                record.add(userOwnerSurname);
                record.add(userOwnerName);
                record.add(userOwnerMail);
                record.add(groupOwnerName);
                record.add(groupOwnerMail);

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
