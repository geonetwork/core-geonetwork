/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fao.geonet.api.reports.ReportUtils.CSV_FORMAT;

/**
 * Created by jose on 31/01/17.
 */
public class ReportInternalMetadata implements IReport {
    /**
     * Report filter.
     */
    private final ReportFilter reportFilter;

    /**
     * Creates a report users instance.
     *
     * @param filter report filter.
     */
    public ReportInternalMetadata(final ReportFilter filter) {
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
        CSVPrinter csvFilePrinter = null;

        try {
            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(writer, CSV_FORMAT);

            // Retrieve metadata
            final IMetadataUtils metadataRepository =
                context.getBean(IMetadataUtils.class);
            final List<? extends AbstractMetadata> records =
                metadataRepository.getMetadataReports().
                    getInternalMetadata(
                        reportFilter.getBeginDate(),
                        reportFilter.getEndDate(),
                        reportFilter.getGroups(),
                        OperationAllowedSpecs.isPublic(
                            ReservedOperation.view));

            // Write header
            csvFilePrinter.printRecord("Metadata Internal");
            csvFilePrinter.println();

            String[] entries = (
                "Metadata ID#Metadata Title#Metadata Create date#Metadata Update date#"
                    + "Owner Username#Owner Surname#Owner Name#Owner Email#"
                    + "Group Owner#Group Owner Email").split("#");
            csvFilePrinter.printRecord(Arrays.asList(entries));

            List<User> users =
                context.getBean(UserRepository.class).findAll();
            List<Group> groups =
                context.getBean(GroupRepository.class).findAll();

            // Process the records
            for (AbstractMetadata metadata : records) {
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


                String mdTitle = ReportUtils.retrieveMetadataIndex(
                    metadata.getUuid(), "resourceTitleObject", "default");

                // Build the record element with the information for the report
                List<String> record = new ArrayList<>();
                record.add(metadata.getUuid());
                record.add("" + mdTitle);
                record.add("" + metadata.getDataInfo().getCreateDate());
                record.add("" + metadata.getDataInfo().getChangeDate());
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
