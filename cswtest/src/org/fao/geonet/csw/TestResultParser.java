package org.fao.geonet.csw;

import com.occamlab.te.TECore;

import net.sf.saxon.dom.DocumentBuilderImpl;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;

/**
 * Parses log file from TEAM ENGINE to get the number of tests failed, with warnings and passed
 *
 * @author josegar
 */
public class TestResultParser {
    private int failCount;
    private int warnCount;
    private int passCount;

    private Element test;

    public int getFailCount() {
        return failCount;
    }

    public int getWarnCount() {
        return warnCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public Element getTest() {
        return test;
    }

    public void processLog(File logdir,
                           String path) throws Exception {

        DocumentBuilderImpl DB;
        DB = new DocumentBuilderImpl();

        DocumentBuilder db3 = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder();
        Document owner = db3.newDocument();

        parse_log(DB, owner, logdir, path);
    }

    public Element parse_log(DocumentBuilderImpl db, Document owner, File logdir,
                             String path) throws Exception {


        File log = new File(new File(logdir, path), "log.xml");
        Document logdoc = TECore.read_log(log.getParent(), ".");
        Element test = owner.createElement("test");
        Element log_e = (Element) logdoc.getElementsByTagName("log").item(0);
        NodeList children = log_e.getChildNodes();
        boolean complete = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) children.item(i);
                if (e.getNodeName().equals("starttest")) {
                    NamedNodeMap atts = e.getAttributes();
                    for (int j = 0; j < atts.getLength(); j++) {
                        test.setAttribute(atts.item(j).getNodeName(), atts
                            .item(j).getNodeValue());
                    }
                } else if (e.getNodeName().equals("endtest")) {
                    complete = true;
                    if (Integer.parseInt(e.getAttribute("result")) == 3) {
                        failCount++;
                        test.setAttribute("failed", "yes");
                    }
                    if (Integer.parseInt(e.getAttribute("result")) == 1) {
                        warnCount++;
                        test.setAttribute("warning", "yes");
                    }
                    if (Integer.parseInt(e.getAttribute("result")) == 2) {
                        failCount++;
                    }
                    if (Integer.parseInt(e.getAttribute("result")) == 0) {
                        passCount++;
                    }
                } else if (e.getNodeName().equals("testcall")) {
                    test.appendChild(parse_log(db, owner, logdir, e
                        .getAttribute("path")));
                }
            }
        }
        test.setAttribute("complete", complete ? "yes" : "no");
        return test;
    }
}
