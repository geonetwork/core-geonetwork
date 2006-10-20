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

