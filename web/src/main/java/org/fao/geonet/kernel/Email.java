//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import jeeves.utils.Log;

import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ThreadPool;

public final class Email {
    final String feedbackAddress;
    final String smtpServer;
    final String portNo;
    private ThreadPool threadPool;

    public Email( SettingManager settings, ThreadPool threadPool ) {
        smtpServer = settings.getValue("system/feedback/mailServer/host");
        portNo = settings.getValue("system/feedback/mailServer/port");
        feedbackAddress = settings.getValue("system/feedback/email");
        this.threadPool = threadPool;
    }

    public void sendToAdmin( final String emailSubject, final String emailBody, boolean testing ) {
        send(feedbackAddress, emailSubject, emailBody, testing);
    }
    public void send( String emailTo, final String emailSubject, final String emailBody, boolean testing ) {
        EmailTask task = new EmailTask(this, emailTo, emailSubject, emailBody, testing);
        threadPool.runTask(task);
    }

    public static boolean isValidEmailAddress( String address ) {
        if (address == null) {
            return false;
        }

        boolean hasCharacters = address.trim().length() > 0;
        boolean hasAt = address.contains("@");

        if (!hasCharacters || !hasAt) return false;

        String[] parts = address.trim().split("@", 2);

        boolean mainPartNotEmpty = parts[0].trim().length() > 0;
        boolean hostPartNotEmpty = parts[1].trim().length() > 0;
        return mainPartNotEmpty && hostPartNotEmpty;
    }
}
final class EmailTask implements Runnable {
    final String emailTo;
    final String emailSubject;
    final String emailBody;
    final boolean testing;
    final Email mailer;

    public EmailTask( Email mailer, String emailTo, String emailSubject, String emailBody, boolean testing ) {
        this.emailTo = emailTo;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
        this.testing = testing;
        this.mailer = mailer;
    }

    public void run() {

        if (testing) return;

        try {
            String[] languages = {"de", "fr", "en", "it", "eng", "deu", "fra", "ita"};
            final Properties props = System.getProperties();
            props.put("mail.smtp.host", mailer.smtpServer);
            props.put("mail.protocol.port", mailer.portNo);
            final Session session = Session.getInstance(props, null);
            final MimeMessage message = new MimeMessage(session);
            if (isValidEmailAddress(mailer.feedbackAddress)) {
                message.setFrom(new InternetAddress(mailer.feedbackAddress));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
                message.setSubject(emailSubject, "UTF-8");

                Multipart multipart = new MimeMultipart();

                if (emailBody != null) {
                    MimeBodyPart bodyPart = new MimeBodyPart();
                    bodyPart.setText(emailBody, "UTF-8");
                    bodyPart.setContentLanguage(languages);
                    multipart.addBodyPart(bodyPart);
                }

                message.setContent(multipart);
                Transport.send(message);
            }
        } catch (AddressException e) {
            Log.error(Log.JEEVES, "Error sending email message: " + e);
            Log.error(Log.JEEVES, "Failed email is: \n" + emailBody);
        } catch (MessagingException e) {
            Log.error(Log.JEEVES, "Error sending email message: " + e);
            Log.error(Log.JEEVES, "Failed email is: \n" + emailBody);
        }
    }
    public static boolean isValidEmailAddress( String address ) {
        if (address == null) {
            return false;
        }

        boolean hasCharacters = address.trim().length() > 0;
        boolean hasAt = address.contains("@");

        if (!hasCharacters || !hasAt) return false;

        String[] parts = address.trim().split("@", 2);

        boolean mainPartNotEmpty = parts[0].trim().length() > 0;
        boolean hostPartNotEmpty = parts[1].trim().length() > 0;
        return mainPartNotEmpty && hostPartNotEmpty;
    }

}