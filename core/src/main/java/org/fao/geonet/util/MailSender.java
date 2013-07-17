//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import jeeves.interfaces.Logger;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

public class MailSender extends Thread
{
	Logger      _logger;
	SimpleEmail _mail;

	public MailSender(ServiceContext context)
	{
		_logger = context.getLogger();
	}

    /**
     *
     * @param server
     * @param port
     * @param username TODO
     * @param password TODO
     * @param useSSL TODO
     * @param from
     * @param fromDescr
     * @param to
     * @param subject
     * @param message
     * @throws EmailException
     */
    private void setUp(String server, int port, String username, String password, boolean useSSL, String from, String fromDescr, String to, String subject, String message) throws EmailException {
        _mail.setHostName(server);
        _mail.setSmtpPort(port);
        _mail.setFrom(from, fromDescr);
        if(!"".equals(username)) {
            _mail.setAuthentication(username, password);
        }
        if(useSSL) {
            _mail.setSSL(useSSL);
        }
        _mail.addTo(to);
        _mail.setSubject(subject);
        _mail.setMsg(message);
    }

    /**
     * TODO Javadoc.
     *
     * @param server
     * @param port
     * @param username TODO
     * @param password TODO
     * @param useSSL TODO
     * @param from
     * @param fromDescr
     * @param to
     * @param toDescr
     * @param subject
     * @param message
     */
	public void send(String server, int port, String username, String password, boolean useSSL, 
	        String from, String fromDescr, String to, String toDescr, String subject, String message) {
		_mail = new SimpleEmail();
		try {
            setUp(server, port, username, password, useSSL, from, fromDescr, to, subject, message);
            start();
		}
		catch(EmailException e) {
			logEx(e);
		}
	}

    /**
     * TODO Javadoc.
     *
     * @param server
     * @param port
     * @param from
     * @param fromDescr
     * @param to
     * @param toDescr
     * @param replyTo
     * @param replyToDesc
     * @param subject
     * @param message
     */
	public void sendWithReplyTo(String server, int port, String username, String password, boolean useSSL, 
	        String from, String fromDescr, String to, String toDescr, 
	        String replyTo, String replyToDesc, String subject, String message) {
		_mail = new SimpleEmail();
		try {
            setUp(server, port, username, password, useSSL, from, fromDescr, to, subject, message);
            List<InternetAddress> addressColl = new ArrayList<InternetAddress>();
			addressColl.add(new InternetAddress(replyTo, replyToDesc));
			_mail.setReplyTo(addressColl);

			start();
		}
		catch(Exception e) {
			logEx(e);
		}
	}

	public void run()
	{
		try
		{
			_mail.send();

			_logger.info("Mail sent");
		}
		catch(EmailException e)
		{
			logEx(e);
		}
	}

	private void logEx(Exception e)
	{
		_logger.error("Unable to mail feedback");
		_logger.error("  Exception : " + e);
		_logger.error("  Message   : " + e.getMessage());
		_logger.error("  Stack     : " + Util.getStackTrace(e));
	}
};

