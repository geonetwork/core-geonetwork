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

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.fao.geonet.Util;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Service to return the updated metadata during a period.
 *
 * Service parameters: dateFrom (mandatory) dateTo   (mandatory) groups   (optional)
 *
 * Service output:
 * <p/>
 * <response> <record> <username></username>                    Owner username <surname></surname>
 *                    Owner surname <name></name>                            Owner name
 * <email></email>                          Owner mail <groupName></groupName>
 * Group owner name <groupOwnerMail></groupOwnerMail>        Group owner mail
 * <recordName></recordName>                Metadata title <uuid></uuid>
 * Metadata UUID <changedate></changedate>                Metadata change date </record>
 * </response>
 *
 * @author Jose García
 */
@Deprecated
public class ReportUpdatedMetadata implements Service {
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

        // Retrieve metadata
        final List<? extends AbstractMetadata> records = context.getBean(IMetadataUtils.class).getMetadataReports().
            getUpdatedMetadata(beginDateIso, endDateIso, groupList);

        // Process metadata results for the report
        Element response = new Element(Jeeves.Elem.RESPONSE);

        // Process the records
        for (AbstractMetadata metadata : records) {
            User userOwner = context.getBean(UserRepository.class).findOne(metadata.getSourceInfo().getOwner());
            final Integer mdGroupOwner = metadata.getSourceInfo().getGroupOwner();
            String groupOwnerName = "";
            String groupOwnerMail = "";
            if (mdGroupOwner != null) {
                Group groupOwner = context.getBean(GroupRepository.class).findOne(mdGroupOwner);
                groupOwnerName = (groupOwner.getLabelTranslations().get(context.getLanguage()) != null ?
                    groupOwner.getLabelTranslations().get(context.getLanguage()) : groupOwner.getName());
                groupOwnerMail = (groupOwner.getEmail() != null ? groupOwner.getEmail() : "");
            }

            String userOwnerUsername = userOwner.getUsername();
            String userOwnerName = (userOwner.getName() != null ? userOwner.getName() : "");
            String userOwnerSurname = (userOwner.getSurname() != null ? userOwner.getSurname() : "");
            String userOwnerMail = (userOwner.getEmail() != null ? userOwner.getEmail() : "");

            String mdTitle = ReportUtils.retrieveMetadataTitle(context, metadata.getId());

            // Build the record element with the information for the report
            Element metadataEl = new Element("record");
            metadataEl.addContent(new Element("uuid").setText(metadata.getUuid()))
                .addContent(new Element("recordName").setText("" + mdTitle))
                .addContent(new Element("changedate").setText("" + metadata.getDataInfo().getChangeDate()))
                .addContent(new Element("username").setText(userOwnerUsername))
                .addContent(new Element("surname").setText(userOwnerSurname))
                .addContent(new Element("name").setText(userOwnerName))
                .addContent(new Element("email").setText(userOwnerMail))
                .addContent(new Element("groupName").setText(groupOwnerName))
                .addContent(new Element("groupOwnerMail").setText(groupOwnerMail));

            response.addContent(metadataEl);
        }
        return response;
    }
}
