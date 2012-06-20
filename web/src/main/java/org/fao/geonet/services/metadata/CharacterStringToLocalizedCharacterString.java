package org.fao.geonet.services.metadata;

import java.io.File;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

public class CharacterStringToLocalizedCharacterString implements Service {
    private static final String SEP = File.separator;

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String xsl = context.getAppPath() + SEP + "xsl" + SEP + "characterstring-to-localisedcharacterstring.xsl";
        
        String id = Util.getParam(params, "id", null);
        String uuid = Util.getParam(params, "uuid", null);
        
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager   dataMan = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        
        if(id==null && uuid!=null) {
            id = dataMan.getMetadataId(dbms, uuid); 
        }
        
        if(id!=null) {
            Element md = dataMan.getMetadata(context, id, false, false, true);
            Xml.transform(md,xsl);
            
            boolean validate = false;
            boolean updatefixedInfo = false;
            boolean index = true;
            String changeDate = null;
            boolean updateDateStamp = false;
            dataMan.updateMetadata(context, dbms, id, md, validate , updatefixedInfo, index, context.getLanguage(), changeDate, updateDateStamp, true);
            return new Element("response").setAttribute("success", "1");            
        } else {
            return new Element("response").setAttribute("processed", "0");
        }
    }

}
