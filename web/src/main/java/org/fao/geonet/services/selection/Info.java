package org.fao.geonet.services.selection;

import java.io.File;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

public class Info implements Service {

    private static final String EMAIL_TEMPLATES = "emailTemplates.xml";
    private String appPath;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        this.appPath = appPath;
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        synchronized (this) {
            File templates = new File(Resources.locateResourcesDir(context), EMAIL_TEMPLATES);
            if (!templates.exists()) {
                File base = new File(new File(appPath, "resources"), EMAIL_TEMPLATES);
                FileUtils.copyFile(base, templates);
            }
        }
        return context.getXmlCacheManager().get(context, false, Resources.locateResourcesDir(context), EMAIL_TEMPLATES, "eng", "eng", true);
    }

}
