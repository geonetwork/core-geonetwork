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

import org.apache.commons.mail.*;

import jeeves.interfaces.Logger;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

public class MailSender extends Thread
{
	Logger      _logger;
	SimpleEmail _mail;

	public MailSender(ServiceContext context)
	{
		_logger = context.getLogger();
	}

	public void send(String server, int port, String from, String fromDescr, String to, String toDescr, String subject, String message)
	{
		_mail = new SimpleEmail();

		try
		{
			_mail.setHostName(server);
			_mail.setSmtpPort(port);
			_mail.setFrom(from, fromDescr);
			_mail.addTo(to);
			_mail.setSubject(subject);
			_mail.setMsg(message);

			start();
		}
		catch(EmailException e)
		{
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

