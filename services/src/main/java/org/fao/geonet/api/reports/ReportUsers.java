package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.User_;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.springframework.data.domain.Sort;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Creates a users report including last login date.
 *
 * @author Jose García.
 */
public class ReportUsers implements IReport {
    /**
     * Report filter.
     */
    private ReportFilter reportFilter;


    /**
     * Creates a report users instance.
     *
     * @param filter report filter.
     */
    public ReportUsers(final ReportFilter filter) {
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
            // Initialize CSVPrinter object
            CSVFormat csvFileFormat =
                    CSVFormat.DEFAULT.withRecordSeparator("\n");
            csvFilePrinter = new CSVPrinter(writer, csvFileFormat);

            // Retrieve users
            final UserRepository userRepository =
                    context.getBean(UserRepository.class);
            final Sort sort = new Sort(Sort.Direction.ASC,
                    SortUtils.createPath(User_.lastLoginDate));
            final List<User> records = userRepository.findAll(
                    UserSpecs.loginDateBetweenAndByGroups(
                            reportFilter.getBeginDate(),
                            reportFilter.getEndDate(),
                            reportFilter.getGroups()),
                    sort);

            // Write header
            csvFilePrinter.printRecord("Users");
            csvFilePrinter.println();

            String[] entries = ("Username#Surname#Name#"
                    + "Email#User groups#Last login date").split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            final UserGroupRepository userGroupRepository =
                    context.getBean(UserGroupRepository.class);

            for (User user : records) {
                String username = user.getUsername();
                String name = Optional.ofNullable(
                        user.getName()).orElse("");
                String surname = Optional.ofNullable(
                        user.getSurname()).orElse("");
                String email = Optional.ofNullable(
                        user.getEmail()).orElse("");
                String lastLoginDate = user.getLastLoginDate();
                StringBuilder userGroupsList = new StringBuilder();

                // Retrieve user groups
                List<UserGroup> userGroups = userGroupRepository.
                        findAll(UserGroupSpecs.hasUserId(user.getId()));

                int i = 0;
                for (UserGroup ug : userGroups) {
                    Group g = ug.getGroup();
                    String groupName = g.getLabelTranslations().get(
                            context.getLanguage());
                    if (groupName == null) {
                        groupName = g.getName();
                    }

                    String groupProfile = ug.getId().getProfile().name();

                    if (i++ > 0) {
                        userGroupsList.append("-");
                    }
                    userGroupsList.append(groupName + "/" + groupProfile);
                }

                // Build the record element with the information for the report
                List<String> record = new ArrayList<>();
                record.add(username);
                record.add(surname);
                record.add(name);
                record.add(email);
                record.add(userGroupsList.toString());
                record.add(lastLoginDate);

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
