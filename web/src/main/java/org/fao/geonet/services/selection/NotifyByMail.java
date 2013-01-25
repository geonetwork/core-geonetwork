package org.fao.geonet.services.selection;

import java.util.Iterator;
import java.util.List;

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
	
		private final String ADMIN_MAIL = "geocat@swisstopo.ch";
		
		//--------------------------------------------------------------------------
		//---
		//--- Init
		//---
		//--------------------------------------------------------------------------

		public void init(String appPath, ServiceConfig params) throws Exception {}

		//--------------------------------------------------------------------------
		//---
		//--- Service
		//---
		//--------------------------------------------------------------------------

		public Element exec(Element params, ServiceContext context) throws Exception {

		
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

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
	            	if("".equals(emailAddress)) {
	            		emailAddress = ADMIN_MAIL;
	            	}
	            	context.info("Send notification email to " + emailAddress + " for MD uuid : " + uuid);
	            	Element retchildserv = new Element("sendMail");
		            retchildserv.setAttribute("email", emailAddress);
		            retchildserv.setAttribute("uuid", uuid);

	            	ret.addContent(retchildserv);
	            	
	            	gc.getEmail().send("email", "Metadata not up to date : " + uuid, "Please keep the metadata up to date.", false);
	    		}
	        }
	    }

		return ret;
	}
}

// =============================================================================

