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

package org.fao.geonet.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.fao.geonet.kernel.setting.SettingManager;

/**
 * Utility class to send mails. Supports both html and plain text. It usually takes the settings
 * from the database, but you can also indicate all params.
 *
 * @author delawen
 */
public class MailUtil {

    /**
     * Send an html mail. Will look on the settings directly to know the remitent
     */
    public static Boolean sendHtmlMail(List<String> toAddress, String subject,
                                       String htmlMessage, SettingManager settings) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();
        configureBasics(settings, email);

        email.setSubject(subject);
        try {
            email.setHtmlMsg(htmlMessage);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                e.printStackTrace();
                return false;
            }
        }

        return send(email);
    }

    /**
     * Send a plain text mail. Will look on the settings directly to know the remitent
     */
    public static Boolean sendMail(List<String> toAddress, String subject,
                                   String message, SettingManager settings) {
        // Create data information to compose the mail
        Email email = new SimpleEmail();
        configureBasics(settings, email);

        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }

        return send(email);
    }

    /**
     * Send a plain text mail. Will look on the settings directly to know the remitent
     */
    public static Boolean sendMail(List<String> toAddress, String subject,
                                   String message, SettingManager settings, String replyTo,
                                   String replyToDesc) {
        // Create data information to compose the mail
        Email email = new SimpleEmail();
        configureBasics(settings, email);

        List<InternetAddress> addressColl = new ArrayList<InternetAddress>();
        try {
            addressColl.add(new InternetAddress(replyTo, replyToDesc));
            email.setReplyTo(addressColl);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return false;
        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }

        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }

        return send(email);
    }

    /**
     * Send an html mail with atachments
     */
    public static Boolean sendHtmlMailWithAttachment(List<String> toAddress,
                                                     String hostName, Integer smtpPort, String from, String username,
                                                     String password, String subject, String htmlMessage,
                                                     List<EmailAttachment> attachment) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();

        configureBasics(hostName, smtpPort, from, username, password, email, false, false);

        for (EmailAttachment attach : attachment) {
            try {
                email.attach(attach);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }

        email.setSubject(subject);
        try {
            email.setHtmlMsg(htmlMessage);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                e.printStackTrace();
                return false;
            }
        }

        return send(email);
    }

    private static Boolean send(final Email email) {
        try {
            email.send();

        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void sendWithThread(final Email email) {
        try {
            Thread t = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        email.send();
                    } catch (EmailException e) {
                        e.printStackTrace();
                    }
                }
            };

            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a plain text mail
     */
    public static Boolean sendMail(List<String> toAddress, String hostName,
                                   Integer smtpPort, String from, String username, String password,
                                   String subject, String message) {

        Email email = new SimpleEmail();
        configureBasics(hostName, smtpPort, from, username, password, email, false, false);

        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                e.printStackTrace();
                return false;
            }
        }

        return send(email);
    }

    /**
     * Create data information to compose the mail
     */
    private static void configureBasics(String hostName, Integer smtpPort,
                                        String from, String username, String password, Email email, Boolean ssl, Boolean tls) {
        if (hostName != null) {
            email.setHostName(hostName);
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
        if (smtpPort != null) {
            email.setSmtpPort(smtpPort);
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
        if (username != null) {
            email.setAuthenticator(new DefaultAuthenticator(username, password));
        }


        if (tls != null) {
            email.setStartTLSEnabled(tls);
            email.setStartTLSRequired(tls);
        }

        if (ssl != null) {
            email.setSSLOnConnect(ssl);
            if (ssl) email.setSslSmtpPort(smtpPort + "");
        }

        if (from != null && !from.isEmpty()) {
            try {
                email.setFrom(from);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
    }

    /**
     * Configure the basics (hostname, port, username, password,...)
     */
    private static void configureBasics(SettingManager settings, Email email) {
        String username = settings
            .getValue("system/feedback/mailServer/username");
        String password = settings
            .getValue("system/feedback/mailServer/password");
        Boolean ssl = settings
            .getValueAsBool("system/feedback/mailServer/ssl");
        Boolean tls = settings
            .getValueAsBool("system/feedback/mailServer/tls");

        String hostName = settings.getValue("system/feedback/mailServer/host");
        Integer smtpPort = Integer.valueOf(settings
            .getValue("system/feedback/mailServer/port"));

        String from = settings.getValue("system/feedback/email");

        configureBasics(hostName, smtpPort, from, username, password, email, ssl, tls);
    }

    public static Boolean sendMail(String email, String subject,
                                   String message, SettingManager sm) {
        List<String> to = new ArrayList<String>(1);
        to.add(email);
        return sendMail(to, subject, message, sm);
    }


    public static Boolean sendMail(String to, String subject, String message,
                                   SettingManager sm, String replyTo, String replyToDescr) {
        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);
        return sendMail(to_, subject, message, sm, replyTo, replyToDescr);
    }

    public static void testSendMail(String to, String subject, String message,
                                    SettingManager sm, String replyTo, String replyToDescr) throws Exception {
        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);
        testSendMail(to_, subject, message, sm, replyTo, replyToDescr);
    }

    public static void testSendMail(List<String> toAddress, String subject,
                                    String message, SettingManager settings, String replyTo,
                                    String replyToDesc) throws Exception {
        // Create data information to compose the mail
        Email email = new SimpleEmail();
        configureBasics(settings, email);

        List<InternetAddress> addressColl = new ArrayList<InternetAddress>();

        addressColl.add(new InternetAddress(replyTo, replyToDesc));
        email.setReplyTo(addressColl);

        email.setSubject(subject);
        email.setMsg(message);

        // send to all mails extracted from settings
        for (String add : toAddress) {
            email.addBcc(add);
        }

        email.send();
    }
}
