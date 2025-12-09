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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail2.jakarta.*;
import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;

import jakarta.annotation.Nonnull;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to send mails. Supports both html and plain text. It usually
 * takes the settings from the database, but you can also indicate all params.
 *
 * @author delawen
 */
public class MailUtil {
    public static final Pattern metadataLuceneField = Pattern.compile("\\{\\{index:([^\\}]+)\\}\\}");

    public static final String LOG_MODULE_NAME = "geonetwork";

    /**
     * Send an html mail. Will look on the settings directly to know the
     * remitent
     *
     * @param toAddress
     * @param subject
     * @param htmlMessage
     * @param settings
     * @throws EmailException
     */
    public static Boolean sendHtmlMail(List<String> toAddress, String subject,
                                       String htmlMessage, SettingManager settings) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();
        configureBasics(settings, email);

        email.setSubject(subject);
        try {
            email.setCharset(EmailConstants.UTF_8);
            email.setHtmlMsg(htmlMessage);
        } catch (EmailException e1) {
            Log.error("Error setting email HTML content. Subject:" + subject, e1);
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email BCC address " + add, e);
                return false;
            }
        }

        return send(email);
    }

    /**
     * Send a plain text mail. Will look on the settings directly to know the
     * remitent
     *
     * @param toAddress
     * @param subject
     * @param message
     * @param settings
     * @throws EmailException
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
            Log.error(LOG_MODULE_NAME, "Error setting email message", e1);
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email BCC address " + add, e);
            }
        }

        return send(email);
    }

    /**
     * Send a plain text mail. Will look on the settings directly to know the
     * remitent
     *
     * @param toAddress
     * @param subject
     * @param message
     * @param htmlMessage
     * @param settings
     * @param replyTo
     * @param replyToDesc    @throws EmailException
     */
    public static Boolean sendMail(List<String> toAddress, String subject,
                                   String message, String htmlMessage,
                                   SettingManager settings, String replyTo,
                                   String replyToDesc) {
        // Create data information to compose the mail
        boolean isHtml = StringUtils.isNotBlank(htmlMessage);
        Email email = isHtml ? new HtmlEmail() : new SimpleEmail();
        configureBasics(settings, email);

        List<InternetAddress> addressColl = new ArrayList<InternetAddress>();
        if (StringUtils.isNotEmpty(replyTo)) {
            try {
                addressColl.add(new InternetAddress(replyTo, replyToDesc));
                email.setReplyTo(addressColl);
            } catch (UnsupportedEncodingException e2) {

                Log.error(LOG_MODULE_NAME, "Error setting email replyTo. Characters not supported in \"" + replyToDesc
                    + "\"", e2);
                return false;
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email replyTo. Invalid email address \"" + replyTo + "\"", e);
                return false;
            }
        }

        email.setSubject(subject);
        try {
            if (StringUtils.isNotBlank(message)) {
                email.setMsg(message);
            }
            if (isHtml) {
                ((HtmlEmail)email).setHtmlMsg(htmlMessage);
            }
        } catch (EmailException e1) {
            Log.error(LOG_MODULE_NAME, "Error setting email message", e1);
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email BCC address " + add, e);
            }
        }

        return send(email);
    }

    /**
     * Send an html mail with atachments
     *
     * @param toAddress
     * @param from
     * @param subject
     * @param htmlMessage
     * @param attachment
     * @throws EmailException
     */
    public static Boolean sendHtmlMailWithAttachment(List<String> toAddress,
                                                     String from, String subject, String htmlMessage,
                                                     List<EmailAttachment> attachment, SettingManager settings) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();
        String username = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_USERNAME);
        String password = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PASSWORD);
        Boolean ssl = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_SSL, false);
        Boolean tls = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_TLS, false);

        String hostName = settings.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
        Integer smtpPort = Integer.valueOf(settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT));
        Boolean ignoreSslCertificateErrors = settings.getValueAsBool
            (Settings.SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS, false);


        configureBasics(hostName, smtpPort, from, username, password, email, ssl, tls, ignoreSslCertificateErrors);

        for (EmailAttachment attach : attachment) {
            try {
                email.attach(attach);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error attaching attachment " + attach.getName(), e);
            }
        }

        email.setSubject(subject);
        try {
            email.setHtmlMsg(htmlMessage);
        } catch (EmailException e1) {
            Log.error(LOG_MODULE_NAME, "Error setting email HTML message", e1);
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email BCC address " + add, e);
                return false;
            }
        }

        return send(email);
    }

    private static Boolean send(final Email email) {
        try {
            email.send();

        } catch (EmailException e) {
            Log.error(LOG_MODULE_NAME, "Error sending email \"" + email.getSubject() + "\"", e);
            return false;
        }

        return true;
    }

    private static void sendWithThread(@Nonnull final Email email) {
        try {
            Thread t = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        email.send();
                    } catch (EmailException e) {
                        Log.error(LOG_MODULE_NAME, "Error sending email \"" + email.getSubject() + "\" unsing other " +
                            "thread", e);
                    }
                }
            };

            t.start();
        } catch (Exception e) {
            Log.error(LOG_MODULE_NAME, "Error sending email \"" + email.getSubject() + "\" unsing other " +
                "thread", e);
        }
    }

    /**
     * Send a plain text mail
     *
     * @param toAddress
     * @param from
     * @param subject
     * @param message
     * @throws EmailException
     */
    public static Boolean sendMail(List<String> toAddress, String from,
                                   String subject, String message, SettingManager settings) {

        Email email = new SimpleEmail();
        String username = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_USERNAME);
        String password = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PASSWORD);
        Boolean ssl = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_SSL, false);
        Boolean tls = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_TLS, false);

        String hostName = settings.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
        Integer smtpPort = Integer.valueOf(settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT));
        Boolean ignoreSslCertificateErrors = settings.getValueAsBool
            (Settings.SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS, false);
        configureBasics(hostName, smtpPort, from, username, password, email, ssl, tls, ignoreSslCertificateErrors);

        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException e1) {
            Log.error(LOG_MODULE_NAME, "Error setting email message", e1);
            return false;
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                Log.error(LOG_MODULE_NAME, "Error setting email BCC address " + add, e);
                return false;
            }
        }

        return send(email);
    }

    /**
     * Create data information to compose the mail
     *
     * @param hostName
     * @param smtpPort
     * @param from
     * @param username
     * @param password
     * @param email
     * @param ssl
     * @param tls
     * @param ignoreSslCertificateErrors
     */
    private static void configureBasics(String hostName, Integer smtpPort,
                                        String from, String username, String password, Email email, Boolean ssl,
                                        Boolean tls, Boolean ignoreSslCertificateErrors) {
        if (StringUtils.isNotEmpty(hostName)) {
            email.setHostName(hostName);
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
        if (StringUtils.isNotBlank(smtpPort + "")) {
            email.setSmtpPort(smtpPort);
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
        if (StringUtils.isNotEmpty(username)) {
            email.setAuthenticator(new DefaultAuthenticator(username, password));
        }

        if (tls != null && tls) {
            email.setStartTLSEnabled(tls);
            email.setStartTLSRequired(tls);
            email.setSSLCheckServerIdentity(true);
        }

        if (ssl != null && ssl) {
            email.setSSLOnConnect(ssl);
            email.setSSLCheckServerIdentity(true);
            if (StringUtils.isNotBlank(smtpPort + "")) {
                email.setSslSmtpPort(smtpPort + "");
            }
        }

        email.setCharset(EmailConstants.UTF_8);

        if (ignoreSslCertificateErrors != null && ignoreSslCertificateErrors) {
            try {
                email.setSSLCheckServerIdentity(false);

                Session mailSession = email.getMailSession();
                Properties p = mailSession.getProperties();
                p.setProperty("mail.smtp.ssl.trust", "*");

            } catch (EmailException e) {
                // Ignore the exception. Can't be reached because the host name is always set above or an
                // IllegalArgumentException is thrown.
            }
        }

        if (StringUtils.isNotBlank(from)) {
            try {
                email.setFrom(from);
            } catch (EmailException e) {
                throw new IllegalArgumentException(
                    "Invalid 'from' email setting in System Configuration (see Administration menu) - cannot send " +
                        "mail", e);
            }
        } else {
            throw new IllegalArgumentException(
                "Missing settings in System Configuration (see Administration menu) - cannot send mail");
        }
    }

    /**
     * Configure the basics (hostname, port, username, password,...)
     *
     * @param settings
     * @param email
     */
    private static void configureBasics(SettingManager settings, Email email) {
        String username = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_USERNAME);
        String password = settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PASSWORD);
        Boolean ssl = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_SSL, false);
        Boolean tls = settings
            .getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_TLS, false);

        String hostName = settings.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
        Integer smtpPort = Integer.valueOf(settings
            .getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT));

        String from = settings.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);
        Boolean ignoreSslCertificateErrors =
            settings.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS, false);


        configureBasics(hostName, smtpPort, from, username, password, email, ssl, tls, ignoreSslCertificateErrors);
    }

    public static Boolean sendMail(String email, String subject,
                                   String message, String htmlMessage, SettingManager sm) {
        List<String> to = new ArrayList<String>(1);
        to.add(email);
        return sendMail(to, subject, message, htmlMessage, sm, null, null);
    }


    public static Boolean sendMail(String to, String subject,
                                   String message, String htmlMessage,
                                   SettingManager sm, String replyTo, String replyToDescr) {
        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);
        return sendMail(to_, subject, message, htmlMessage, sm, replyTo, replyToDescr);
    }

    public static void testSendMail(String to, String subject, String message,
                                    String htmlMessage, SettingManager sm, String replyTo, String replyToDescr) throws Exception {
        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);
        testSendMail(to_, subject, message, htmlMessage, sm, replyTo, replyToDescr);
    }

    public static void testSendMail(List<String> toAddress, String subject,
                                    String message, String htmlMessage, SettingManager settings, String replyTo,
                                    String replyToDesc) throws Exception {
        // Create data information to compose the mail
        boolean isHtml = StringUtils.isNotBlank(htmlMessage);
        Email email = isHtml ? new HtmlEmail() : new SimpleEmail();
        configureBasics(settings, email);

        List<InternetAddress> addressColl = new ArrayList<InternetAddress>();

        addressColl.add(new InternetAddress(replyTo, replyToDesc));
        email.setReplyTo(addressColl);
        email.setSubject(subject);

        if (StringUtils.isNotBlank(message)) {
            email.setMsg(message);
        }
        if (isHtml) {
            ((HtmlEmail)email).setHtmlMsg(htmlMessage);
        }

        // send to all mails extracted from settings
        for (String add : toAddress) {
            email.addBcc(add);
        }

        email.send();
    }


    /**
     *
     * @param message  The message to work on
     * @param uuid     The record UUID
     * @param language The language (define the index to look into)
     * @return The message with field substituted by values
     */
    public static String compileMessageWithIndexFields(String message, String uuid, String language) {
        // Search lucene field to replace
        Matcher m = metadataLuceneField.matcher(message);
        ArrayList<String> fields = new ArrayList<String>();
        while (m.find()) {
            fields.add(m.group(1));
        }

        // First substitution for variables not stored in the index
        for (String f : fields) {
            String mdf = XslUtil.getIndexField(null, uuid, f, language);
            message = message.replace("{{index:" + f + "}}", mdf);
        }
        return message;
    }

}
