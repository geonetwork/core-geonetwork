package org.fao.geonet.guiservices.util;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

import java.nio.file.Path;

public class GetCsvFileName implements Service {
    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        return new Element("a").setText("metadata_{datetime}.csv");
    }
}
