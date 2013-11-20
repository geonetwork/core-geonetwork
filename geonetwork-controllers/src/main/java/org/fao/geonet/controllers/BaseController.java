package org.fao.geonet.controllers;

import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.server.sources.http.ServletPathFinder;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

@Controller
public abstract class BaseController {

    private String FS = File.separator;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected ServletContext servletContext;

    @Autowired
    private XmlCacheManager cacheManager;

    @Autowired
    private SchemaManager schemaManager;


    /**
     * Creates the html page to return from the model (response) and xsl sheet.
     *
     * @param response
     * @return
     */
    protected String buildHtmlPage(Element response, String xslSheet) throws Exception {
        final ServletPathFinder pathFinder = new ServletPathFinder(servletContext);
        String styleSheet = pathFinder.getAppPath() + xslSheet;

        // Adds translations, etc. to the model as required for xslt.
        Element response2 = postProcessResponse(response);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Xml.transform(response2, styleSheet, baos);

        return baos.toString();
    }

    /**
     * Adds translations, etc. required by xslt to create html pages from xslt.
     *
     * TODO: Remove harcoded values and create dynamically from the request.
     *
     * @param response
     * @return
     */
    protected Element postProcessResponse(Element response) {
        Element root = new Element("root");
        Element gui = new Element("gui");


        try {
            Element xml = cacheManager.get(servletContext, true, "loc", "xml/strings.xml", "eng",  "eng");
            gui.addContent(xml);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Element xml = cacheManager.get(servletContext, true, "loc", "xml/countries.xml", "eng",  "eng");
            gui.addContent(xml);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        gui.addContent(getIsoLanguages());

        Element session = new Element("session");
        session.addContent(new Element("userId").setText("1"));
        session.addContent(new Element("username").setText("admin"));
        session.addContent(new Element("name").setText("admin"));
        session.addContent(new Element("surname").setText("admin"));
        session.addContent(new Element("profile").setText("Administrator"));

        gui.addContent(session);
        gui.addContent(getEnv());
        gui.addContent(getSystemConfig());
        gui.addContent(getSchemaList());
        gui.addContent(getSchemas());

        gui.addContent(new Element("language").setText("eng"));
        gui.addContent(new Element("reqService").setText("group.list"));
        gui.addContent(new Element("url").setText("/geonetwork"));
        gui.addContent(new Element("locUrl").setText("/geonetwork/loc/eng"));
        gui.addContent(new Element("service").setText("/geonetwork/srv"));
        gui.addContent(new Element("locService").setText("/geonetwork/srv/eng"));

        try {
            Element xml = cacheManager.get(servletContext, false, "", "WEB-INF/config-gui.xml", "eng",  "eng");
            gui.addContent(xml);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        root.addContent(gui);
        root.addContent(response);

        return root;
    }

    private Element getIsoLanguages() {
        return context.getBean(IsoLanguageRepository.class).findAllAsXml();
    }

    private Element getSchemaList() {
        SchemaManager schemaMan = context.getBean(SchemaManager.class);

        Element schemas = new Element("schemalist");

        for (String schema : schemaMan.getSchemas()) {
            Element elem = new Element("name").setText(schema);
            elem.setAttribute("plugin","true"); // all schemas are plugins
            elem.setAttribute("schemaConvertDirectory",schemaMan.getSchemaDir(schema)+"convert"+FS);
            elem.setAttribute("namespaces",schemaMan.getNamespaceString(schema));
            // is it editable?
            if (schemaMan.getSchema(schema).canEdit()) {
                elem.setAttribute("edit","true");
            } else {
                elem.setAttribute("edit","false");
            }
            // get the conversion information and add it too
            try {
                List<Element> convElems = schemaMan.getConversionElements(schema);
                if (convElems.size() > 0) {
                    Element conv = new Element("conversions");
                    conv.addContent(convElems);
                    elem.addContent(conv);
                }
            } catch (Exception ex) {
                // TODO: Handle exception
            }
            schemas.addContent(elem);
        }

        return schemas;
    }


    private Element getSchemas()
    {
        Element schemas = new Element("schemas");

        for(String schema : schemaManager.getSchemas()) {
            try {
                Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);

                for (Map.Entry<String, XmlFile> entry : schemaInfo.entrySet()) {
                    XmlFile xf = entry.getValue();
                    String fname = entry.getKey();
                    Element response = xf.exec(new Element("junk"), cacheManager, servletContext);

                    response.setName(FilenameUtils.removeExtension(fname));
                    Element schemaElem = new Element(schema);
                    schemaElem.addContent(response);
                    schemas.addContent(schemaElem);
                }
            } catch (Exception e) {
                //context.error("Failed to load guiservices for schema "+schema+": "+e.getMessage());
                e.printStackTrace();
            }
        }

        //Log.info(Geonet.SCHEMA_MANAGER, "The response was "+Xml.getString(schemas));
        return schemas;
    }

    private Element getEnv() {
        // reset the thread local
        XmlSerializer.clearThreadLocal();

        Element response  = context.getBean(SettingManager.class).getAllAsXML(true);

        final String READ_ONLY = "readonly";
        Element readOnly = new Element(READ_ONLY);
        //readOnly.setText(Boolean.toString(gc.isReadOnly()));
        readOnly.setText(Boolean.toString(false));

        // Get the system node (which is for the time being the only child node
        // of settings
        Element system = response.getChild("system");
        system.addContent(readOnly);
        system.setName("env");
        return (Element) system.clone();
    }

    private Element getSystemConfig() {
        Element system  = context.getBean(SettingManager.class).getAllAsXML(false);
        return system;
    }



}