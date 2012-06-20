package org.fao.geonet.services.reusable;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.reusable.ProcessParams;
import org.fao.geonet.kernel.reusable.log.ReusableObjectLogger;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Update a reusable object by passing the xml
 */
public class Update implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String defaultLang = Util.getParam(params, "defaultLang", "EN").trim();
        String xmlString = Util.getChild(params, "xml").getText();

        Element xml = Xml.loadString(xmlString, false);
        Element wrapped = new Element("wrapped").addContent(xml);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        ProcessParams processParams = new ProcessParams(dbms, ReusableObjectLogger.THREAD_SAFE_LOGGER, null, xml, wrapped, gc.getThesaurusManager(),gc.getExtentManager(),context.getBaseUrl(),gc.getSettingManager(),false,defaultLang,context);
        Collection<Element> newElements = gc.getReusableObjMan().updateXlink(xml, processParams);
        
        ArrayList<Element> updated = new ArrayList<Element>(newElements);
        updated.add(0,xml);
        xml.detach();
        
        return new Element("updated").addContent(updated);
    }
}
