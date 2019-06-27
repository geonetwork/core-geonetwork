//=============================================================================
//===   Copyright (C) 2010 Food and Agriculture Organization of the
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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Properties;

/**
 * Mail utility that use standard Java classes, unlike org.fao.geonet.util.MailSender which uses
 * Apache Commons mail.
 *
 * Another difference is that this class does not send an email in a separate thread which makes it
 * easier to handle exceptions.
 *
 * Another difference is that this class is capable of sending multipart email with text, html,
 * images and attachments.
 *
 * @author heikki doeleman
 */
public class JavaMailer {

    private Properties props = null;
    private Session mailSession = null;
    private Transport transport = null;
    private String smtpHost = null;
    private int smtpPort = 0;
    private boolean authentication;
    private String username = null;
    private String password = null;

    /**
     * Constructor establishes a connection to your smtp server. If that fails,
     * ExceptionInInitializerError is thrown; make sure to catch it if you want to handle it.
     */
    public JavaMailer(String smtpHost, String smtpPort, boolean authentication, String username, String password) {
        try {
            props = new Properties();
            // If true, attempt to authenticate the user using the AUTH command. Defaults to false.
            props.put("mail.smtps.auth", String.valueOf(authentication));
            this.authentication = authentication;
            // The SMTP server
            props.put("mail.smtp.host", smtpHost);
            // The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25.
            props.put("mail.smtp.port", smtpPort);
            // If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login
            // commands. Note that an appropriate trust store must configured so that the client will trust the server's certificate. Defaults to false.
            props.put("mail.smtp.starttls.enable", "true");
            // Socket connection timeout value in milliseconds. Default is infinite timeout.
            props.put("mail.smtp.connectiontimeout", "10000");
            // Socket I/O timeout value in milliseconds. Default is infinite timeout.
            props.put("mail.smtp.timeout", "10000");
            // If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException. If
            // set to false (the default), the message is not sent to any of the recipients if there is an invalid recipient address.
            props.put("mail.smtp.sendpartial", "true");
            mailSession = Session.getDefaultInstance(props, null);
            // for SSL
            // transport = mailSession.getTransport("smtps");
            transport = mailSession.getTransport("smtp");
            this.smtpHost = smtpHost;
            this.smtpPort = Integer.parseInt(smtpPort);
            this.username = username;
            this.password = password;
        } catch (NoSuchProviderException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    /**
     * Sends multipart email.
     *
     * @param subject    the email subject
     * @param text       the content of the body for text mail
     * @param html       the content of the body for html mail
     * @param sender     the email sender
     * @param recipients the email recipients
     */
    public void send(String subject, String text, String html, String sender, String... recipients) throws MessagingException {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setSubject(subject);
            message.setFrom(new InternetAddress(sender));
            if (recipients == null) {
                throw new MessagingException("No recipients specified to send e-mail, aborting.");
            }
            for (int i = 0; i < recipients.length; i++) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients[i]));
            }

            // create the mail root multipart
            MimeMultipart mpRoot = new MimeMultipart("mixed");

            // Create the content multipart (for text and HTML)
            MimeMultipart mpContent = new MimeMultipart("alternative");

            // Create a body part to house the multipart/alternative Part
            MimeBodyPart contentPartRoot = new MimeBodyPart();
            contentPartRoot.setContent(mpContent);

            // Add the root body part to the root multipart
            mpRoot.addBodyPart(contentPartRoot);

            // Add text
            if (text != null && text.length() > 0) {
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(text);
                mpContent.addBodyPart(mbp1);
            }

            // Add html
            if (html != null && html.length() > 0) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(html, "text/html");
                mpContent.addBodyPart(messageBodyPart);
            }

            // Add an attachment (example)
    /*
            MimeBodyPart mbp3 = new MimeBodyPart();
            DataSource source = new FileDataSource("C:/tmp/images/header.png");
            mbp3.setDisposition(Part.ATTACHMENT);
            mbp3.setDataHandler(new DataHandler(source));
            mbp3.setFileName("myFile.png");

            // add the body part to the root
            // NOTE: ADDING TO ROOT, NOT CONTENT ROOT
            mpRoot.addBodyPart(mbp3);
    */

            message.setContent(mpRoot);
            message.saveChanges();

            String host = this.smtpHost;
            String username = this.username;
            String password = this.password;
            int smtpPort = this.smtpPort;
            if (this.authentication) {
                transport.connect(host, smtpPort, username, password);
            } else {
                transport.connect(host, smtpPort, null, null);
            }

            Address[] addresses = new Address[message.getRecipients(Message.RecipientType.TO).length];
            System.arraycopy(message.getRecipients(Message.RecipientType.TO), 0, addresses, 0, message.getRecipients(Message.RecipientType.TO).length);
            transport.sendMessage(message, addresses);
        } finally {
            transport.close();
        }
    }

}
