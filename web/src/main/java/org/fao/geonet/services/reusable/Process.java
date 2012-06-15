package org.fao.geonet.services.reusable;

import java.util.List;

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

/**
 * Process xml and create shared objects from it
 */
public class Process implements Service
{

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {

        boolean addOnly = Boolean.parseBoolean(Util.getParam(params, "addOnly", "false").trim());
        String defaultLang = Util.getParam(params,"defaultLang","EN").trim();
        String xmlString = Util.getChild(params, "xml").getText();

        Element xml = Xml.loadString(xmlString, false);
        Element wrapped = new Element("wrapped").addContent(xml);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        ProcessParams processParams = new ProcessParams(dbms, ReusableObjectLogger.THREAD_SAFE_LOGGER, null, xml, wrapped, gc.getThesaurusManager(),gc.getExtentManager(),context.getBaseUrl(),gc.getSettingManager(),addOnly,defaultLang,context);
        List<Element> updated = gc.getReusableObjMan().process(processParams);

        return new Element("updated").addContent(updated);
    }
}
