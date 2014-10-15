package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.jdom.Element;

import java.io.File;

/**
 * @author Jesse on 10/15/2014.
 */
public class FormatterParams {
    Format format;
    Element params;
    ServiceContext context;
    File formatDir;
    File viewFile;
    Element metadata;
    String schema;
    ConfigFile config;
}
