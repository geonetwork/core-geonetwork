package org.fao.geonet.services.harvesting;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import opendap.servlet.BadURLException;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

/**
 * Download a logfile from harvesting
 * 
 * @author delawen
 * 
 */
public class Log implements Service {
    public void init(Path appPath, ServiceConfig config) throws Exception {

    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String logfile = params.getChildText("file").trim();

        // Security checks, this is no free proxy!!
        if (logfile.startsWith("http") || logfile.startsWith("ftp")
                || logfile.startsWith("sftp")) {
            throw new BadURLException(
                    "This is no proxy. Stopping possible hacking attempt to url: "
                            + logfile);
        }

        if (!logfile.endsWith(".log")) {
            throw new BadURLException(
                    "Strange suffix for this log file. Stopping possible hacking attempt to uri: "
                            + logfile);
        }

        if (!logfile.contains("/harvester_")) {
            throw new BadURLException(
                    "This doesn't seem like a harvester log file. Stopping possible hacking attempt to uri: "
                            + logfile);
        }

        File file = new File(logfile);

        if (!file.exists() || !file.canRead()) {
            throw new NullPointerException(
                    "Couldn't find or read the logfile. Somebody moved it? " + file.getAbsolutePath());
        }

        return BinaryFile.encode(200, file.toPath().toAbsolutePath().normalize(), false).getElement();
    }

}
