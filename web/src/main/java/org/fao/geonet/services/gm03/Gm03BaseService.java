package org.fao.geonet.services.gm03;

import java.io.File;

import javax.xml.transform.TransformerConfigurationException;

import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Logger;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class Gm03BaseService implements Service {
    protected File xsl;
    private File xsd;

    public void init(String appPath, ServiceConfig params) throws Exception {
        final String xslTxt = params.getValue("xsl");
        xsl = new File(xslTxt);
        if (!xsl.isAbsolute())
            xsl = new File(appPath + xslTxt);

        final String xsdTxt = params.getValue("xsd");
        xsd = new File(xsdTxt);
        if (!xsd.isAbsolute())
            xsd = new File(appPath + xsdTxt);
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        boolean validate = Util.getParam(params, "validate", false);
        final File xsdFile;
        if (validate) {
            xsdFile = xsd;
        } else {
            xsdFile = null;
        }

        UserSession session = context.getUserSession();

        //-----------------------------------------------------------------------
        //--- handle current tab

        Element elCurrTab = params.getChild(Params.CURRTAB);

        if (elCurrTab != null)
            session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());

        //-----------------------------------------------------------------------
        //--- check access

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        // the metadata ID
        String id;

        // does the request contain a UUID ?
        try {
            String uuid = Util.getParam(params, Params.UUID);
            // lookup ID by UUID
            id = dm.getMetadataId(dbms, uuid);
        }
        catch (MissingParameterEx x) {
            // request does not contain UUID; use ID from request
            try {
                id = Util.getParam(params, Params.ID);
            }
            // request does not contain ID
            catch (MissingParameterEx xx) {
                // give up
                throw new Exception("Request must contain a UUID or an ID");
            }
        }

        Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW);

        //-----------------------------------------------------------------------
        //--- get metadata

        Element elMd = dm.getMetadata(context, id, false, true, true);

        if (elMd == null)
            throw new MetadataNotFoundEx(id);

        Logger logger = context.getLogger();
        try {
            logger.info("1");
            DOMOutputter outputter = new DOMOutputter();
            logger.info("2");
            Document domIn = outputter.output(new org.jdom.Document(elMd));
            logger.info("3");

            ISO19139CHEtoGM03Base toGm03 = createConverter(xsdFile);
            logger.info("4");
            Document domOut = toGm03.convert(domIn);
            logger.info("5");

            DOMBuilder builder = new DOMBuilder();
            logger.info("6");
            return builder.build(domOut).getRootElement();
        } catch (RuntimeException e) {
            logger.error(e.toString());
            throw e;
        } catch (Exception e) {
            logger.error(e.toString());
            throw e;
        }
    }

    protected abstract ISO19139CHEtoGM03Base createConverter(File xsdFile) throws SAXException, TransformerConfigurationException;
}
