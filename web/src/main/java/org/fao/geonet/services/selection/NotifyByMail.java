package org.fao.geonet.services.selection;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

/**
 * Select a list of elements stored in session
 * For all the MD stored in the selectionManager, the service will send an email to the owner of the MD.
 * If the owner hasn't any mail addresse, the email will be sent to ADMIN_MAIL
 * Returns status 
 * 
 * @author fgravin
 */

public class NotifyByMail implements Service {
	
		private ServiceConfig _config;
		
		private final String ADMIN_MAIL = "geocat@swisstopo.ch";
		
		private static final Pattern rfc2822 = Pattern.compile(
		        "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
		);

		//--------------------------------------------------------------------------
		//---
		//--- Init
		//---
		//--------------------------------------------------------------------------

		public void init(String appPath, ServiceConfig config) throws Exception {
			_config = config;
		}

		//--------------------------------------------------------------------------
		//---
		//--- Service
		//---
		//--------------------------------------------------------------------------

		public Element exec(Element params, ServiceContext context) throws Exception {
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		String messageBody       = _config.getValue("body");
		String messageBodyError = _config.getValue("bodyError");
		String messageSubject   = _config.getValue("subject");
		
		UserSession us = context.getUserSession();
		
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		
		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(us);

		Element ret = new Element("response");

	    synchronized(sm.getSelection("metadata")) {
	        for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
	            String uuid = (String) iter.next();
	            String emailAddress;
	            
	            List<Element> uuidQuery = dbms.select("SELECT u.email FROM Metadata m, Users u where m.owner = u.id AND m.uuid ='" + uuid +"'" ).getChildren();
	            for (Element uuidElement : uuidQuery) {
	            	emailAddress = uuidElement.getChildText("email");
	            	if(emailAddress == null || "".equals(emailAddress) || !rfc2822.matcher(emailAddress).matches()) {
	            		emailAddress = ADMIN_MAIL;
	            	}
	            	
	            	context.info("Send notification email to " + emailAddress + " for MD uuid : " + uuid);
	            	
	            	Element retchildserv = new Element("sendMail");
		            retchildserv.setAttribute("email", emailAddress);
		            retchildserv.setAttribute("uuid", uuid);
		            
	            	String body = MessageFormat.format(messageBody, uuid);
	            	
	            	try {
	            		gc.getEmail().send(emailAddress, messageSubject, body, false);
	            	} catch(Exception e) {
	            		if(!emailAddress.equals(ADMIN_MAIL)) {
	            			gc.getEmail().sendToAdmin(messageSubject, MessageFormat.format(messageBodyError, body), false);
	            			retchildserv.setText("error");
	            		}
	            	} finally {
	            		ret.addContent(retchildserv);
	            	}
	    		}
	        }
	    }

		return ret;
	}
}

// =============================================================================

