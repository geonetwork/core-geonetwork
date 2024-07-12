/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fao.geonet.api.reports.ReportUtils.CSV_FORMAT;

/**
 * Creates a users report including last login date.
 *
 */
public class ReportUsers implements IReport {
    /**
     * Report filter.
     */
    private final ReportFilter reportFilter;


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
     * @param writer  Writer.
     * @throws Exception Exception creating a report.
     */
    public void create(final ServiceContext context,
                       final PrintWriter writer) throws Exception {
        // Initialize CSVPrinter object
        try(CSVPrinter csvFilePrinter = new CSVPrinter(writer, CSV_FORMAT)) {
            // Retrieve users
            final UserRepository userRepository =
                context.getBean(UserRepository.class);
            final Sort sort = Sort.by(Sort.Direction.ASC,
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
                + "Email#User groups/Profile#Last login date").split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            for (User user : records) {
                String username = user.getUsername();
                String name = Optional.ofNullable(
                    user.getName()).orElse("");
                String surname = Optional.ofNullable(
                    user.getSurname()).orElse("");
                String email = Optional.ofNullable(
                    user.getEmail()).orElse("");
                String lastLoginDate = user.getLastLoginDate();

                String userGroupsInfo = retrieveGroupsListInfo(context, user);

                if (!StringUtils.hasLength(userGroupsInfo)) {
                    // Add the user profile if not assigned to any group, usually Administrator / Guest profiles
                    userGroupsInfo = user.getProfile().name();
                }

                // Build the record element with the information for the report
                List<String> metadataRecord = new ArrayList<>();
                metadataRecord.add(username);
                metadataRecord.add(surname);
                metadataRecord.add(name);
                metadataRecord.add(email);
                metadataRecord.add(userGroupsInfo);
                metadataRecord.add(lastLoginDate);

                csvFilePrinter.printRecord(metadataRecord);
            }
        } finally {
            writer.flush();
        }
    }

    /**
     * Creates a string with the list of groups / profiles of a user:
     *
     *  group1/profileGroup1-group2/profileGroup2 ...
     *
     * @param context
     * @param user
     * @return
     */
    private String retrieveGroupsListInfo(final ServiceContext context, User user) {
        StringBuilder userGroupsList = new StringBuilder();

        final UserGroupRepository userGroupRepository =
            context.getBean(UserGroupRepository.class);

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

        return userGroupsList.toString();
    }
}
