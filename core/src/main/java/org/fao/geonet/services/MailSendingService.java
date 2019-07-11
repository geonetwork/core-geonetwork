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

package org.fao.geonet.services;

import jeeves.interfaces.Service;
import org.fao.geonet.utils.Log;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;

/**
 * @author heikki doeleman
 */
public abstract class MailSendingService implements Service {
    /**
     * Send the mail to the user.
     */
    protected boolean sendMail(String host, int port, String subject, String from, String to, String content, String protocol) throws MessagingException {
        boolean isSendout;

        Properties props = new Properties();

        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "false");

        Session mailSession = Session.getDefaultInstance(props);

        try {
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSentDate(new Date());
            msg.setSubject(subject);
            // Add content message
            msg.setText(content);
            Transport.send(msg);
            isSendout = true;
        } catch (AddressException e) {
            isSendout = false;
            Log.error(Log.SERVICE, e.getMessage(), e);
        } catch (MessagingException e) {
            isSendout = false;
            Log.error(Log.SERVICE, e.getMessage(), e);
        }
        return isSendout;
    }

}
