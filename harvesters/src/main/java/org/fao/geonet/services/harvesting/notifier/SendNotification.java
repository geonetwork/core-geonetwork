/**
 * Copyright (C) 2013 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.services.harvesting.notifier;

import java.util.ArrayList;
import java.util.List;

import jeeves.server.context.ServiceContext;

import org.apache.commons.mail.EmailException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.MailUtil;
import org.jdom.Element;

/**
 * This class send an email after a harvester has been run
 * 
 * @author Maria Arias de Reyna
 */
public class SendNotification {

	/**
	 * Launches the notification manager
	 * 
	 * @param context
	 * @param abstractHarvester
	 * @param dbms
	 * @param catalogRequestId
	 *            Catalog request identifier to reject
	 * @param errors
	 * @throws EmailException
	 */
	public static void process(ServiceContext context, Element element,
			@SuppressWarnings("rawtypes") AbstractHarvester abstractHarvester)
			throws EmailException {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager settings = gc.getBean(SettingManager.class);
		notifyByMail(settings, element, abstractHarvester);
	}

	/**
	 * Send the mail
	 * 
	 * @param settings
	 * @param element
	 * @param ah
	 * @throws EmailException
	 */
	private static void notifyByMail(SettingManager settings, Element element,
			@SuppressWarnings("rawtypes") AbstractHarvester ah)
			throws EmailException {

		if (!settings.getValueAsBool("system/harvesting/mail/enabled")) {
			return;
		}

		String receiver = settings.getValue("system/harvesting/mail/recipient");
		List<String> toAddress = new ArrayList<String>();

		// If no email to send, take the email of the owner of the harvester
		if (receiver == null || receiver.trim().isEmpty()) {
			try {
				receiver = ah.getOwnerEmail();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		toAddress.add(receiver);

		String subject = settings.getValue("system/harvesting/mail/subject");

		String htmlMessage = settings
				.getValue("system/harvesting/mail/template");

		Element lastHarvest = (Element) element.getChildren().get(0);

		@SuppressWarnings("unchecked")
		List<Element> tmp = lastHarvest.getChildren();
		Element info = null;

		for (Element e : tmp) {
			if (e.getName().equalsIgnoreCase("info")) {
				info = e;
				break;
			}
		}
		
		// We should always get a info report BTW
		if (info != null) {
    		// Success, but with warnings or clean?
    		Element result = (Element) info.getChildren().get(0);
    
    		// switch between normal and error template
    		if (info.getChildren("error").size() > 0) {
    			// Error, Level 3, let's check it:
    			if (!settings.getValueAsBool("system/harvesting/mail/level3")) {
    				return;
    			}
    			htmlMessage = settings
    					.getValue("system/harvesting/mail/templateError");
    			Element error = (Element) info.getChildren("error").get(0);
    			String errorMsg = error.getChildText("message");
    			// do not convert it to html, dangerous!
    			errorMsg = errorMsg.replace("<", " ");
    
    			errorMsg += extractWarningsTrace(info);
    
    			htmlMessage = htmlMessage.replace("$$errorMsg$$", errorMsg);
    
    			String[] values = new String[] { "total", "added", "updated",
    					"removed", "unchanged", "unretrievable", "doesNotValidate" };
    
    			for (String value : values) {
    				htmlMessage = replace(result, htmlMessage, value);
    				subject = replace(result, subject, value);
    			}
    
    		} else {
    
    			if (result.getChildren("errors").size() > 0) {
    				// Success with warnings, Level 2, let's check it:
    				if (!settings.getValueAsBool("system/harvesting/mail/level2")) {
    					return;
    				}
    
    				htmlMessage = settings
    						.getValue("system/harvesting/mail/templateWarning");
    
    				String errorMsg = extractWarningsTrace(result);
    
    				htmlMessage = htmlMessage.replace("$$errorMsg$$", errorMsg);
    
    				String[] values = new String[] { "total", "added", "updated",
    						"removed", "unchanged", "unretrievable",
    						"doesNotValidate" };
    
    				for (String value : values) {
    					htmlMessage = replace(result, htmlMessage, value);
    					subject = replace(result, subject, value);
    				}
    			} else {
    				// Success!! Level 1, let's check it:
    				if (!settings.getValueAsBool("system/harvesting/mail/level1")) {
    					return;
    				}
    
    				String[] values = new String[] { "total", "added", "updated",
    						"removed", "unchanged", "unretrievable",
    						"doesNotValidate" };
    
    				for (String value : values) {
    					htmlMessage = replace(result, htmlMessage, value);
    					subject = replace(result, subject, value);
    				}
    			}
    		}
		}

		htmlMessage = htmlMessage.replace("$$harvesterName$$",
                ah.getParams().getName());
		subject = subject.replace("$$harvesterName$$", ah.getParams().getName());

		htmlMessage = htmlMessage.replace("$$harvesterType$$", ah.getType());
		subject = subject.replace("$$harvesterType$$", ah.getType());

		MailUtil.sendHtmlMail(toAddress, subject, htmlMessage, settings);
	}

	/**
	 * @param result
	 * @return
	 */
	private static String extractWarningsTrace(Element result) {
		StringBuffer errorMsg = new StringBuffer("");

		for (Object o : result.getChildren("errors")) {
			Element errores = ((Element) o);
			for (Object a : errores.getChildren("error")) {
				Element error = ((Element) a);
				String desc = error.getChildText("description");
				String hint = error.getChildText("hint");
				String trace = getErrorTrace(error);

				// do not convert it to html, dangerous!
				errorMsg = errorMsg.append("<li><p><strong>'")
				        .append(desc)
				        .append("'</strong>:")
				        .append(hint)
				        .append("</p><p>")
				        .append(trace)
				        .append("</p></li>");
			}
		}
		errorMsg.append("<ul>").append(errorMsg).append("</ul>");
		return errorMsg.toString();
	}

	/**
	 * @param error
	 * @return
	 */
	private static String getErrorTrace(Element error) {
		String trace = "";
		for (Object e : error.getChildren("error")) {
			Element error_ = ((Element) e);
			trace = error_.getChildText("message");
		}
		return trace.replace("<", " ");
	}

	/**
	 * Helps in the transformation of template to message
	 * 
	 * @param element
	 * @param htmlMessage
	 * @param value
	 * @return
	 */
	private static String replace(Element element, String htmlMessage,
			String value) {
		String tmp = element.getChildText(value);
		if (tmp == null) {
			return htmlMessage.replace("$$" + value + "$$", "0");
		} else {
			return htmlMessage.replace("$$" + value + "$$", tmp);
		}
	}
}