package org.fao.geonet.services.debug;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.regex.Pattern;

/**
 * Returns a report of all of the files that are open by the
 * 
 * User: jeichar Date: 4/5/12 Time: 4:38 PM
 */
public class ListOpenFileHandles implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {
        // do nothing
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        int max = Util.getParam(params, "max", Integer.MAX_VALUE);
        Pattern filter = Pattern.compile(Util.getParam(params, "filter", ".*"));
        Element report = new Element("report");
        String osName = ManagementFactory.getOperatingSystemMXBean().getName().toLowerCase();
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            String[] name = ManagementFactory.getRuntimeMXBean().getName().split("@", 2);
            Integer pid = null;
            if (name.length == 2) {
                try {
                    pid = Integer.parseInt(name[0]);
                } catch (NumberFormatException e) {
                    try {
                        pid = Integer.parseInt(System.getProperty("sun.java.launcher.pid"));
                    } catch (NumberFormatException e2) {
                        try {
                            pid = Integer.parseInt(System.getProperty("pid"));
                        } catch (NumberFormatException e3) {
                            // ignore
                        }
                    }
                }
            }

            if (pid == null) {
                report.setText("Unable to determine the processId for this server.  Add following to start script: -Dpid=$$");
            } else {
                listFiles(report, pid, max, filter);
            }
        } else {
            report.setText("Windows version of this service is not yet implemented");
        }
        return report;
    }

    private void listFiles(Element report, Integer pid, int max, Pattern filter) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { "lsof", "-p", pid.toString() });
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(process.getInputStream(), Jeeves.ENCODING));
            
            String line;
            int total = 0;
            int filteredTotal = 0;
            int displayed = 0;
            while ((line = in.readLine()) != null) {
                total ++;
                if (filter.matcher(line).find()) {
                    filteredTotal ++;
                    max--;
                    if(max >= 0) {
                        displayed++;
                        report.addContent(new Element("line").setText(line));
                    }
                }
            }
            
            report.setAttribute("total", ""+total);
            report.setAttribute("filteredTotal", ""+filteredTotal);
            report.setAttribute("displayed", ""+Math.min(total, displayed));
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(process.getInputStream());
            IOUtils.closeQuietly(process.getOutputStream());
            IOUtils.closeQuietly(process.getErrorStream());
            process.destroy();
        }
    }

}
