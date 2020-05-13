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

package org.fao.geonet.services.feedback;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.util.JavaMailer;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * NGR implementation of feedback. The feedback form in the GUI is a draggable div, that submits to
 * this service. The contents are assembled in an email that is sent to the feedback email address
 * configured in the System Configuration page, and if requested, also to an email address taken
 * from the metadata contents. The feedback message is not stored in the database.
 *
 * The server response is unfortunately specifically constructed so that a client Ext Js Form knows
 * it was a success, converted to json format by extjs-feedback-response.xsl.
 *
 * TODO i18n
 *
 * @author heikki doeleman
 */
@Deprecated
public class Receptor implements Service {

    //private static final String NOREPLY = "noreply@nationaalgeoregister.nl";

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, final ServiceContext context) throws Exception {
        try {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            SettingManager sm = gc.getBean(SettingManager.class);
            String NOREPLY = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

            //
            // user-supplied params
            //
            String phone = Util.getParam(params, Params.PHONE, "");
            String sex = Util.getParam(params, Params.SEX, "");
            String name = Util.getParam(params, Params.NAME, "");
            String remarks = Util.getParam(params, Params.REMARKS, "");
            String category = Util.getParam(params, Params.CATEGORY, "");
            String function = Util.getParam(params, Params.FUNCTION, "");
            String organization = Util.getParam(params, Params.ORGANIZATION, "");
            String feedbackFunction = Util.getParam(params, Params.FEEDBACK_FUNCTION, "");
            String email = Util.getParam(params, Params.EMAIL, "");

            //
            // automatic hidden params
            //
            String metadataOrganization = Util.getParam(params, Params.METADATA_ORGANIZATION, "");
            String metadataEmail = Util.getParam(params, Params.METADATA_EMAIL, "");
            String uuid = Util.getParam(params, Params.UUID, "");
            String date = Util.getParam(params, Params.DATE, "");
            String title = Util.getParam(params, Params.TITLE, "");

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy 'at' hh:mm:ss z");
            String currentDateTime = sdf.format(cal.getTime());
            String body = "Date and time: " + currentDateTime + "\n";
            if (StringUtils.isEmpty(uuid)) {
                body += "Subject:\tGeneral feedback GeoNetwork catalog\n";
            } else {
                body += "Subject:\tMetadata feedback GeoNetwork catalog\n";
                body += "Metadata UUID: " + uuid + "\n";
                body = "Metadata title: " + title + "\n";
                body += "Metadata date: " + date + "\n";
                body += "Metadata organization: " + metadataOrganization + "\n";
            }
            body = "Name:\t" + name + "\n";
            body += "Sex:\t" + sex + "\n";
            body += "Function:\t" + function + "\n";
            body += "Organization:\t" + organization + "\n";
            body += "Phone:\t" + phone + "\n";
            body += "Email:\t" + email + "\n";
            body += "Feedback function:\t" + feedbackFunction + "\n";
            body += "Feedback category:\t" + category + "\n";
            body += "Remarks:\n" + remarks;

            Log.debug(Geonet.FEEDBACK, "created feedback message:\n" + body);

            String subject;
            if (StringUtils.isEmpty(uuid)) {
                subject = "General feedback GeoNetwork catalog: " + feedbackFunction;
            } else {
                subject = "Metadata feedback GeoNetwork catalog: " + feedbackFunction;
            }

            String host = sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
            String port = sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT);
            String to = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

            Log.debug(Geonet.FEEDBACK, "email settings.. host: " + host + " port: " + port + " email: " + to);

            JavaMailer mailer = new JavaMailer(host, port, false, null, null);

            if (StringUtils.isEmpty(NOREPLY)) {
                NOREPLY = "noreply@geonetwork-opensource.org";
            }
            if (StringUtils.isNotEmpty(metadataEmail)) {
                if (StringUtils.isNotEmpty(to)) {
                    mailer.send(subject, body, null, NOREPLY, to, metadataEmail);
                } else {
                    mailer.send(subject, body, null, NOREPLY, metadataEmail);
                }
            } else {
                if (StringUtils.isNotEmpty(to)) {
                    mailer.send(subject, body, null, NOREPLY, to);
                } else {
                    Element response = new Element("response");
                    response.setText("Feedback email not sent, because both the catalog system email and the metadata email were empty.");
                }
            }
            Element response = new Element("response");
            Element success = new Element("feedbackreceptorsuccess");
            response.addContent(success);
            Element sexMarker = new Element("sex");
            sexMarker.setText(sex);
            response.addContent(sexMarker);
            Element nameMarker = new Element("name");
            nameMarker.setText(name);
            response.addContent(nameMarker);
            Log.debug(Geonet.FEEDBACK, Xml.getString(response));
            return response;
        } catch (Throwable x) {
            Log.error(Geonet.FEEDBACK, x.getMessage() + "\n" + Util.getStackTrace(x));
            //throw x;
            // because Jeeves does not return HTTP error codes in case of error, the onFailure callback in the client
            // does not work. So we return an error structure that is expected in the client's onSuccess.
            Element error = new Element("feedbackreceptorerror");
            error.addContent(x.getMessage());
            error.setAttribute("id", "feedbackreceptorerror");
            return new Element("response").addContent(error);
        }
    }
}
