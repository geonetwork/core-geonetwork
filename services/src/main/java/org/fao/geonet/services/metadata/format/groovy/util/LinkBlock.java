package org.fao.geonet.services.metadata.format.groovy.util;

import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * @author Jesse on 11/20/2014.
 */
public interface LinkBlock {
    String linksHtml(StringBuilder xml, Functions functions, Environment env) throws JDOMException, IOException;
}
