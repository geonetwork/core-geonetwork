//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.feedback;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.MailUtil;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Stores the feedback from a user into the database and sends an e-mail
 */

public class Insert implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, final ServiceContext context)
            throws Exception {
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        String name = Util.getParam(params, Params.NAME);
        String org = Util.getParam(params, Params.ORG);
        String email = Util.getParam(params, Params.EMAIL);
        String gender = Util.getParam(params, "gender", "-");
        String phone = Util.getParam(params, "phone", null);

        String comments = Util.getParam(params, Params.COMMENTS, "");
        String subject = Util.getParam(params, Params.SUBJECT, "New feedback");

        String function = Util.getParam(params, "function", "-");
        String type = Util.getParam(params, Params.TYPE, "-");
        String category = Util.getParam(params, Params.CATEGORY, "-");

        String uuid = Util.getParam(params, Params.UUID, null);
        String title = Util.getParam(params, "title", "-");
        String metadataEmail = Util.getParam(params, "metadataEmail", null);
        String metadataOrganization = Util.getParam(params,
                "metadataOrganization", "-");

        String to = sm.getValue("system/feedback/email");

        List<String> toAddress = new LinkedList<String>();
        toAddress.add(to);
        if (metadataEmail != null) {
            //Check metadata email belongs to metadata
            //security!!
            AbstractMetadata md = gc.getBean(MetadataRepository.class).findOneByUuid(uuid);
            if(md.getData().indexOf(metadataEmail) > 0) {
                toAddress.add(metadataEmail);
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(name).append(" (").append(org).append(")");
        message.append("<").append(email).append("> ");
        if (phone != null) {
            message.append(phone);
        }
        message.append("\n");

        message.append(function).append("\n");
        message.append(type).append("\n");
        message.append(category).append("\n");

        if (uuid != null) {
            message.append(title).append(" [").append(uuid).append("] - ")
                    .append(metadataOrganization).append(" \n");

        }

        message.append(comments);

        MailUtil.sendMail(toAddress, subject, message.toString(), sm);

        return new Element("response").addContent(params.cloneContent());
    }
}
