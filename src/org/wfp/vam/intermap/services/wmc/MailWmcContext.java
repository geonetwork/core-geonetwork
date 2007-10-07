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
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wfp.vam.intermap.kernel.GlobalTempFiles;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.WmcCodec;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.util.XmlTransformer;

/**
 * @author ETj
 */
public class MailWmcContext implements Service
{
	static private File   _xslfile;
	static private String _mailserver;
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
		String mailfrom = params.getChildText("wmc_mailfrom");
		String mailto = params.getChildText("wmc_mailto");
		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));

		MapMerger mm = MapUtil.getMapMerger(context);

		WMCViewContext viewContext = WmcCodec.createViewContext(mm, title, width, height);
		Element eViewContext = viewContext.toElement();

		XMLOutputter xcomp = new XMLOutputter(Format.getCompactFormat());
		String comp = xcomp.outputString(eViewContext);
		String enc1 = URLEncoder.encode(comp, "UTF-8");
		String enc2 = URLEncoder.encode(enc1, "UTF-8");

		System.out.println("Sending WMC context mail");
		System.out.println("   from: " + mailfrom);
		System.out.println("   to:   " + mailto);
		System.out.println("   by: "   + _mailserver);

		 // Create the email message
		HtmlEmail email = new HtmlEmail();
		email.setHostName(_mailserver);
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
			.addContent(new Element("url").setText(fullurl))
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

		// Attach it
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(tempContextFile.getAbsolutePath());
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("WMC context");
		attachment.setName("WMContext.cml");

		email.attach(attachment);

		// send the email
		email.send();

		return new Element("response");
	}

}



//=============================================================================
