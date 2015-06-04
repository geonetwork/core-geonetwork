package org.fao.geonet.services.mef;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.TransformerFactory;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.XSLTransformer;

public class ImportWmc extends NotInReadOnlyModeService {

    private String styleSheetWmc;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
        this.styleSheetWmc = appPath + Geonet.Path.IMPORT_STYLESHEETS + File.separator
                + "OGCWMC-to-ISO19139.xsl";
    }


    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context)  throws Exception {
        String wmcString = Util.getParam(params, "wmc_string");
        String wmcUrl = Util.getParam(params, "wmc_url");
        String viewerUrl = Util.getParam(params, "viewer_url");
        String groupId = Util.getParam(params, "group_id", "1");

        Map<String,String> xslParams = new HashMap<String,String>();
        xslParams.put("viewer_url", viewerUrl);
        xslParams.put("wmc_url", wmcUrl);

        // Should this be configurable ?

        xslParams.put("topic", "location");

        UserSession us = context.getUserSession();

        if (us != null) {
            xslParams.put("currentuser_name", us.getName() + " " + us.getSurname());
            xslParams.put("currentuser_phone", us.getPhone());
            xslParams.put("currentuser_mail", us.getEmailAddr());
            xslParams.put("currentuser_org", us.getOrganisation());
        }

        // 1. JDOMize the string
        Element wmcDoc = Xml.loadString(wmcString, false);
        // 2. Apply XSL (styleSheetWmc)
        Element transformedMd = Xml.transform(wmcDoc, styleSheetWmc, xslParams);

        // 4. Inserts the metadata (does basically the same as the metadata.insert.paste service (see Insert.java)

        String uuid = UUID.randomUUID().toString();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dm = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String uuidAction = Util.getParam(params, Params.UUID_ACTION, Params.NOTHING);

        String date = new ISODate().toString();

        final List<String> id = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();
        String localId = null;
        md.add(transformedMd);


        // Import record
        Importer.importRecord(uuid, localId , uuidAction, md, "iso19139", 0,
                gc.getSiteId(), gc.getSiteName(), context, id, date,
                date, groupId, "n", dbms);

        dm.indexInThreadPool(context, id.get(0), dbms);

        Element result = new Element("uuid");
        result.setText(uuid);

        return result;
    }
}