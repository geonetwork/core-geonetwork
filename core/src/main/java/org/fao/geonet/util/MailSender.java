//=============================================================================
//===   Copyright (C) 2001-2022 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import jeeves.server.context.ServiceContext;
import org.apache.commons.mail2.jakarta.SimpleEmail;
import org.apache.commons.mail2.core.EmailException;
import org.fao.geonet.Logger;
import org.fao.geonet.Util;

import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * see {@link MailUtil}
 */
@Deprecated
public class MailSender extends Thread {
    Logger _logger;
    SimpleEmail email;

    public MailSender(ServiceContext context) {
        _logger = context.getLogger();
    }

    /**
     * @param username TODO
     * @param password TODO
     * @param useSSL   TODO
     */
    private void setUp(String server, int port, String username, String password, boolean useSSL, boolean useTLS,
                       boolean ignoreSslCertificateErrors,
                       String from, String fromDescr, String to, String subject, String message) throws EmailException {
        email.setHostName(server);
        email.setSmtpPort(port);
        email.setFrom(from, fromDescr);
        if (!"".equals(username)) {
            email.setAuthentication(username, password);
        }
        if (useTLS) {
            email.setStartTLSEnabled(true);
            email.setStartTLSRequired(true);
            email.setSSLCheckServerIdentity(true);

        }
        if (useSSL) {
            email.setSSLOnConnect(true);
            email.setSslSmtpPort(port + "");
            email.setSSLCheckServerIdentity(true);
        }
        email.addTo(to);
        email.setSubject(subject);
        if ((message == null) || (message.length() == 0)) {
            throw new EmailException("Invalid message supplied");
        }
        email.setContent(message, "text/plain; charset=UTF-8");
        if (ignoreSslCertificateErrors) {
            email.getMailSession().getProperties().setProperty("mail.smtp.ssl.trust", "*");
        }
    }

    /**
     * Better use through MailUtil, as it takes the settings directly from the BBDD.
     *
     * @param username TODO
     * @param password TODO
     * @param useSSL   TODO
     */
    public void send(String server, int port, String username, String password, boolean useSSL, boolean useTLS,
                     boolean ignoreSslCertificateErrors, String from, String fromDescr, String to, String toDescr, String
                         subject, String message) {
        email = new SimpleEmail();
        try {
            setUp(server, port, username, password, useSSL, useTLS, ignoreSslCertificateErrors, from, fromDescr, to,
                subject, message);
            start();
        } catch (EmailException e) {
            logEx(e);
        }
    }

    /**
     * Better use through MailUtil, as it takes the settings directly from the BBDD.
     */
    public void sendWithReplyTo(String server, int port, String username, String password, boolean useSSL, boolean useTLS,
                                boolean ignoreSslCertificateErrors, String from, String fromDescr, String to,
                                String toDescr, String replyTo, String replyToDesc, String subject, String message) {
        email = new SimpleEmail();
        try {
            setUp(server, port, username, password, useSSL, useTLS, ignoreSslCertificateErrors, from, fromDescr, to,
                subject, message);
            List<InternetAddress> addressColl = new ArrayList<InternetAddress>();
            addressColl.add(new InternetAddress(replyTo, replyToDesc));
            email.setReplyTo(addressColl);

            start();
        } catch (Exception e) {
            logEx(e);
        }
    }

    public void run() {
        try {
            email.send();

            _logger.info("Mail sent");
        } catch (EmailException e) {
            logEx(e);
        }
    }

    private void logEx(Exception e) {
        if (_logger != null) {
            _logger.error("Unable to mail feedback");
            _logger.error("  Exception : " + e);
            _logger.error("  Message   : " + e.getMessage());
            _logger.error("  Stack     : " + Util.getStackTrace(e));
        }
    }
};

