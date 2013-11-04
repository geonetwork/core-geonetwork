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
 * Utility class to send mails. Supports both html and plain text. It usually
 * takes the settings from the database, but you can also indicate all params.
 * 
 * @author delawen
 * 
 */
public class MailUtil {

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
     * Send a plain text mail. Will look on the settings directly to know the
     * remitent
     * 
     * @param toAddress
     * @param subject
     * @param htmlMessage
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
            e1.printStackTrace();
            return false;
        }
        email.setSSL(true);

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
     * Send a plain text mail. Will look on the settings directly to know the
     * remitent
     * 
     * @param toAddress
     * @param subject
     * @param htmlMessage
     * @param settings
     * @throws EmailException
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
        email.setSSL(true);

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
     * Send an html mail
     * 
     * @param toAddress
     * @param hostName
     * @param smtpPort
     * @param mail
     * @param username
     * @param password
     * @param subject
     * @param htmlMessage
     * @throws EmailException
     */
    public static Boolean sendHtmlMail(List<String> toAddress, String hostName,
            Integer smtpPort, String from, String username, String password,
            String subject, String htmlMessage) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();

        configureBasics(hostName, smtpPort, from, username, password, email, false);

        email.setSubject(subject);
        try {
            email.setHtmlMsg(htmlMessage);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }
        email.setSSL(true);

        // send to all mails extracted from settings
        for (String add : toAddress) {
            try {
                email.addBcc(add);
            } catch (EmailException e) {
                return false;
            }
        }

        return send(email);
    }

    /**
     * Send an html mail with atachments
     * 
     * @param toAddress
     * @param hostName
     * @param smtpPort
     * @param mail
     * @param username
     * @param password
     * @param subject
     * @param htmlMessage
     * @param attachment
     * @throws EmailException
     */
    public static Boolean sendHtmlMailWithAttachment(List<String> toAddress,
            String hostName, Integer smtpPort, String from, String username,
            String password, String subject, String htmlMessage,
            List<EmailAttachment> attachment) {
        // Create data information to compose the mail
        HtmlEmail email = new HtmlEmail();

        configureBasics(hostName, smtpPort, from, username, password, email, false);

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
        email.setSSL(true);

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
            return false;
        }
        return true;
    }

    /**
     * Send a plain text mail
     * 
     * @param toAddress
     * @param hostName
     * @param smtpPort
     * @param mail
     * @param username
     * @param password
     * @param subject
     * @param htmlMessage
     * @throws EmailException
     */
    public static Boolean sendMail(List<String> toAddress, String hostName,
            Integer smtpPort, String from, String username, String password,
            String subject, String message) {

        Email email = new SimpleEmail();
        configureBasics(hostName, smtpPort, from, username, password, email, false);

        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException e1) {
            e1.printStackTrace();
            return false;
        }
        email.setSSL(true);

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
     * 
     * @param hostName
     * @param smtpPort
     * @param mail
     * @param username
     * @param password
     * @param email
     * @param ssl 
     */
    private static void configureBasics(String hostName, Integer smtpPort,
            String from, String username, String password, Email email, Boolean ssl) {
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
        
        if(ssl != null) {
            email.setSSL(ssl);
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
     * 
     * @param settings
     * @param email
     */
    private static void configureBasics(SettingManager settings, Email email) {
        String username = settings
                .getValue("system/feedback/mailServer/username");
        String password = settings
                .getValue("system/feedback/mailServer/password");
        Boolean ssl = settings
                .getValueAsBool("system/feedback/mailServer/ssl");

        String hostName = settings.getValue("system/feedback/mailServer/host");
        Integer smtpPort = Integer.valueOf(settings
                .getValue("system/feedback/mailServer/port"));

        String from = settings.getValue("system/feedback/email");

        configureBasics(hostName, smtpPort, from, username, password, email, ssl);
    }

    public static Boolean sendMail(String email, String subject,
            String message, SettingManager sm) {
        List<String> to = new ArrayList<String>(1);
        to.add(email);
        return sendMail(to, subject, message, sm);
    }

    public static Boolean sendHtmlMail(String email, String host, Integer port,
            String from, String username, String password, String subject,
            String content) {
        List<String> to = new ArrayList<String>(1);
        to.add(email);
        return sendHtmlMail(to, host, port, from, username, password, subject,
                content);

    }

    public static Boolean sendMail(String to, String subject, String message,
            SettingManager sm, String replyTo, String replyToDescr) {
        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);
        return sendMail(to_, subject, message, sm, replyTo, replyToDescr);
    }

    public static Boolean sendMail(String to, String hostName,
            Integer smtpPort, String from, String username, String password,
            String subject, String message) {

        List<String> to_ = new ArrayList<String>(1);
        to_.add(to);

        return sendMail(to_, hostName, smtpPort, from, username, password,
                subject, message);
    }

}
