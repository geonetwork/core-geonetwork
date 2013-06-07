package org.fao.geonet.services;

import jeeves.interfaces.Service;

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
     * @param host
     * @param port
     * @param subject
     * @param from
     * @param to
     * @param content
     * @param protocol
     * @return
     * @throws javax.mail.MessagingException
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
            e.printStackTrace();
        } catch (MessagingException e) {
            isSendout = false;
            e.printStackTrace();
        }
        return isSendout;
    }

}