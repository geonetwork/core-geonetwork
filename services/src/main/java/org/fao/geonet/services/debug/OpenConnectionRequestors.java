package org.fao.geonet.services.debug;

import com.google.common.collect.Multimap;
import jeeves.exceptions.JeevesException;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.jdom.Element;

/**
 * Returns an xml report containing the StackTraces of each thread that accessed
 * a currently open resource.
 *
 * User: jeichar Date: 4/5/12 Time: 4:38 PM
 */
public class OpenConnectionRequestors implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {
        // do nothing
    }

    public Element exec(Element params, ServiceContext context) throws Exception {

        Element report;

        if (Log.isDebugEnabled(Log.Dbms.RESOURCE_TRACKING)) {
            Multimap<Object, Exception> directOpeners = context.getResourceManager().getDirectOpenResourceAccessTracker();
            Multimap<Object, Exception> openers = context.getResourceManager().getResourceAccessTracker();

            Element directOpenReport = new Element("directOpen");
            Element openReport = new Element("open");
            report = new Element("report").addContent(openReport).addContent(directOpenReport);

            addToReport(directOpeners, directOpenReport);
            addToReport(openers, openReport);
        } else {
            report = new Element("report").setText(Log.Dbms.RESOURCE_TRACKING+" is not in debug mode, there for there is resource tracking data available.");
        }
        return report;
    }

    private void addToReport(Multimap<Object, Exception> openers, Element subreport) {
        for (Object key : openers.keySet()) {
            Element resource = new Element("resource").setAttribute("id", key.toString());
            subreport.addContent(resource);
            for (Exception accessorException : openers.get(key)) {
                if (accessorException == null) {
                    resource.addContent(new Element("NoTrace").setText("No logged stack trace"));
                } else {
                    resource.addContent(JeevesException.toElement(accessorException));
                }
            }
        }
    }
}
