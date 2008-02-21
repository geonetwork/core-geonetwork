//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.wfp.vam.intermap.services.wmc;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;
import jeeves.exceptions.ResourceNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.GlobalTempFiles;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.GeoRSSCodec;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.WmcCodec;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.util.XmlTransformer;

/**
 * @author ETj
 */
public class MailWmcContext implements Service
{
	static private File _xslfile;
	static private String _mailserver;
	static private int _smtpport;
	static private String _gnURL;

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		_gnURL 		= config.getMandatoryValue("gnurl");
		_xslfile 	= new File(config.getMandatoryValue("customstylesheet"));
		_mailserver = config.getValue("mailserver"); // we want to issue a warning ourselves
		if(_mailserver == null)
		{
			Logger.getLogger("MailWmcContext").warning("*** The mailserver config property has not been set. WMC mail will not be available.");
		}

		try
		{
			_smtpport =Integer.parseInt(config.getValue("smtpport"));
		}
		catch (NumberFormatException e)
		{
			Logger.getLogger("MailWmcContect").warning("*** The mail server smtpport property has not been set. Using default port 25");
			_smtpport = 25;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		if(_mailserver == null)
			throw new ResourceNotFoundEx("'mailserver' property is missing from configuration file.");

		String title = params.getChildText("wmc_title");
		String comment = params.getChildText("wmc_comment");
		String mailfrom = params.getChildText("wmc_mailfrom");
		String mailto = params.getChildText("wmc_mailto");
		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));

		MapMerger mm = MapUtil.getMapMerger(context);
		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);

		WMCViewContext viewContext = WmcCodec.createViewContext(mm, ms, title, width, height);
		Element eViewContext = viewContext.toElement();

		XMLOutputter xcomp = new XMLOutputter(Format.getCompactFormat());
		String comp = xcomp.outputString(eViewContext);
		String enc1 = URLEncoder.encode(comp, "UTF-8");
		String enc2 = URLEncoder.encode(enc1, "UTF-8");

		System.out.println("Sending WMC context mail");
		System.out.println("   from: " + mailfrom);
		System.out.println("   to:   " + mailto);
		System.out.println("   title:   " + title);
		System.out.println("   comment: " + comment);
		System.out.println("   by: "   + _mailserver);
		System.out.println("   on port: "   + _smtpport);

		 // Create the email message
		HtmlEmail email = new HtmlEmail();
		email.setHostName(_mailserver);
		email.setSmtpPort(_smtpport);
		email.addTo(mailto);
		email.setFrom(mailfrom);
		email.setSubject(title);

		// create the image file
		String imagename = mm.merge(300,200);
		File imagepath = new File(mm.getImageLocalPath(), imagename);
		// embed the image and get the content id
		URL imageurl = imagepath.toURL();
		String cid = email.embed(imageurl, "Map");

		// set the html and the text messages
		String fullurl = _gnURL+"?wmc="+enc2;

		Element maildata = new Element("maildata")
			.addContent(new Element("gnurl").setText(_gnURL))
			.addContent(new Element("url").setText(fullurl))
			.addContent(new Element("title").setText(title))
			.addContent(new Element("mailfrom").setText(mailfrom))
			.addContent(new Element("comment").setText(comment))
			.addContent(new Element("imgsrc").setText("cid:"+cid));

		Element stylesheet = (Element)Xml.loadFile(_xslfile).clone();
		Element tmail = XmlTransformer.transform(maildata, stylesheet);

		Element ehtml = tmail.getChild("html");
		Element etext = tmail.getChild("text");

		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String html = xo.outputString(ehtml);
		String text = etext.getText();

		email.setHtmlMsg(html);
		email.setTextMsg(text); // set the alternative message

		// Create the context file as attachment
		File tempContextFile = GlobalTempFiles.getInstance().getFile("cml");
		FileWriter fw = new FileWriter(tempContextFile);
		xo.output(eViewContext, fw);
		fw.flush();
		fw.close();

		// Attach the context
		EmailAttachment cmlAttachment = new EmailAttachment();
		cmlAttachment.setPath(tempContextFile.getAbsolutePath());
		cmlAttachment.setDisposition(EmailAttachment.ATTACHMENT);
		cmlAttachment.setDescription("Interactive map");
		cmlAttachment.setName("InteractiveMap.cml");
		email.attach(cmlAttachment);

		if( ms != null && ! ms.isEmpty())
		{
			Element erss = GeoRSSCodec.getGeoRSS(ms, title);
			Document drss = new Document(erss);

			// Create the rss file as attachment
			File tempRssFile = GlobalTempFiles.getInstance().getFile("rss");
			fw = new FileWriter(tempRssFile);
			xo.output(drss, fw);
			fw.flush();
			fw.close();

			// Attach the rss
			EmailAttachment rssAttachment = new EmailAttachment();
			rssAttachment.setPath(tempRssFile.getAbsolutePath());
			rssAttachment.setDisposition(EmailAttachment.ATTACHMENT);
			rssAttachment.setDescription("MarkerSet");
			rssAttachment.setName("AtomMarkerSet.rss");
			email.attach(rssAttachment);
		}

		// send the email
		email.send();

		return new Element("response");
	}

}



//=============================================================================

