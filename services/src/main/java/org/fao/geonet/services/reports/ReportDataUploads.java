/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.services.reports;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service to return the uploaded files to metadata records during a period.
 *
 * Service parameters: dateFrom (mandatory) dateTo   (mandatory) groups   (optional)
 *
 * Service output:
 * <p/>
 * <response> <record> <username></username>                    Upload user username
 * <surname></surname>                      Upload user surname <name></name>
 *     Upload user Owner name <email></email>                          Upload user  mail
 * <profile></profile>                      Upload user profile <recordname></recordname>
 *     Metadata title <uuid></uuid>                            Metadata UUID <filename></filename>
 *                  File name <uploaddesc></uploaddesc>                File description: from online
 * resource descrption <uploaddate></uploaddate>                File upload date
 * <expiry_datetime></expiry_datetime>      File delete date or metadata delete (logical deletes are
 * used for uploads) </record> </response>
 */
@Deprecated
public class ReportDataUploads implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        // Process parameters
        String beginDate = Util.getParam(params, "dateFrom");
        String endDate = Util.getParam(params, "dateTo");

        beginDate = beginDate + "T00:00:00";
        endDate = endDate + "T23:59:59";

        ISODate beginDateIso = new ISODate(beginDate);
        ISODate endDateIso = new ISODate(endDate);
        Set<Integer> groupList = ReportUtils.groupsForFilter(context, params);

        // Retrieve metadata file uploads
        final Sort sort = new Sort(Sort.Direction.ASC, SortUtils.createPath(MetadataFileUpload_.uploadDate));
        final List<MetadataFileUpload> records = context.getBean(MetadataFileUploadRepository.class).findAll(
            MetadataFileUploadSpecs.uploadDateBetweenAndByGroups(beginDateIso, endDateIso, groupList), sort);

        // Process metadata results for the report
        Element response = new Element(Jeeves.Elem.RESPONSE);

        for (MetadataFileUpload fileUpload : records) {
            String username = fileUpload.getUserName();

            User user = context.getBean(UserRepository.class).findOneByUsername(username);

            String name = (user.getName() != null ? user.getName() : "");
            String surname = (user.getSurname() != null ? user.getSurname() : "");
            String email = (user.getEmail() != null ? user.getEmail() : "");
            String profile = user.getProfile().name();

            String fileName = fileUpload.getFileName();

            // Get metadata title from index
            String metadataTitle = ReportUtils.retrieveMetadataTitle(context, fileUpload.getMetadataId());
            String metadataUuid = ReportUtils.retrieveMetadataUuid(context, fileUpload.getMetadataId());


            // Online resource description from the index ...
            String uploadDescription = "";
            Set<String> fields = new HashSet<String>();
            fields.add("linkage_name_des");

            Map<String, Map<String, String>> fieldValues = LuceneSearcher.getAllMetadataFromIndexFor(context.getLanguage(), "_id",
                fileUpload.getMetadataId() + "", fields, false);

            if (!fieldValues.isEmpty()) {
                uploadDescription = fieldValues.get("0").get("linkage_name_des");
            }

            // Build the record element with the information for the report
            Element metadataEl = new Element("record");
            metadataEl.addContent(new Element("uuid").setText(metadataUuid))
                .addContent(new Element("recordname").setText(metadataTitle))
                .addContent(new Element("filename").setText(fileName))
                .addContent(new Element("uploaddesc").setText(uploadDescription))
                .addContent(new Element("uploaddate").setText(fileUpload.getUploadDate()))
                .addContent(new Element("username").setText(username))
                .addContent(new Element("surname").setText(surname))
                .addContent(new Element("name").setText(name))
                .addContent(new Element("email").setText(email))
                .addContent(new Element("profile").setText(profile))
                .addContent(new Element("expiry_datetime").setText(fileUpload.getDeletedDate()));

            response.addContent(metadataEl);
        }
        return response;
    }
}
