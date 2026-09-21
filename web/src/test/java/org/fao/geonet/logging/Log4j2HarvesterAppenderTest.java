/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.logging;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Every shipped log4j2 configuration must route harvester messages to a per-run log file
 * stored in the same directory as {@code geonetwork.log}.
 *
 * <p>{@code HarvestHistory.checkInfoXml()} locates a harvest run's log file relative to
 * {@code Log.getLogfile()}, which resolves the {@code File} appender of the {@code geonetwork}
 * logger. A configuration that omits the {@code Harvester} routing appender, omits the
 * {@code geonetwork} logger, or writes harvester logs to a different directory makes the
 * "log file" button silently disappear from the harvester history in the admin UI.
 */
public class Log4j2HarvesterAppenderTest {

    private static final File CONFIG_DIR = new File("src/main/webapp/WEB-INF/classes");

    @Test
    public void everyConfigurationRoutesHarvesterLogsNextToTheCatalogueLog() throws Exception {
        File[] configs = CONFIG_DIR.listFiles((dir, name) -> name.matches("log4j2(-.*)?\\.xml"));
        assertNotNull("No log4j2 configuration found in " + CONFIG_DIR.getAbsolutePath(), configs);
        assertTrue("No log4j2 configuration found in " + CONFIG_DIR.getAbsolutePath(), configs.length > 0);

        for (File config : configs) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(config);
            String name = config.getName();

            Element file = appenderNamed(document, "RollingFile", "File");
            assertNotNull(name + ": missing <RollingFile name=\"File\"> appender", file);

            Element routing = appenderNamed(document, "Routing", "Harvester");
            assertNotNull(name + ": missing <Routing name=\"Harvester\"> appender, "
                + "harvester runs would not get their own log file", routing);

            NodeList routed = routing.getElementsByTagName("File");
            assertEquals(name + ": expected a single routed <File> appender", 1, routed.getLength());

            assertEquals(name + ": harvester logs must be written to the same directory as geonetwork.log",
                directoryOf(textOf(file, "filename")),
                directoryOf(((Element) routed.item(0)).getAttribute("fileName")));

            assertTrue(name + ": logger 'geonetwork' must reference the 'File' appender, "
                    + "Log.getLogfile() uses it to locate the catalogue log directory",
                appenderRefs(loggerNamed(document, "geonetwork"), name, "geonetwork").contains("File"));

            assertTrue(name + ": logger 'geonetwork.harvester' must reference the 'Harvester' appender",
                appenderRefs(loggerNamed(document, "geonetwork.harvester"), name, "geonetwork.harvester")
                    .contains("Harvester"));
        }
    }

    private static Element appenderNamed(Document document, String tag, String name) {
        NodeList candidates = document.getElementsByTagName(tag);
        for (int i = 0; i < candidates.getLength(); i++) {
            Element candidate = (Element) candidates.item(i);
            if (name.equals(candidate.getAttribute("name"))) {
                return candidate;
            }
        }
        return null;
    }

    private static Element loggerNamed(Document document, String name) {
        NodeList loggers = document.getElementsByTagName("Logger");
        for (int i = 0; i < loggers.getLength(); i++) {
            Element logger = (Element) loggers.item(i);
            if (name.equals(logger.getAttribute("name"))) {
                return logger;
            }
        }
        return null;
    }

    private static List<String> appenderRefs(Element logger, String config, String loggerName) {
        assertNotNull(config + ": missing <Logger name=\"" + loggerName + "\">", logger);
        List<String> refs = new ArrayList<>();
        NodeList nodes = logger.getElementsByTagName("AppenderRef");
        for (int i = 0; i < nodes.getLength(); i++) {
            refs.add(((Element) nodes.item(i)).getAttribute("ref"));
        }
        return refs;
    }

    private static String textOf(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        assertTrue("missing <" + tag + ">", nodes.getLength() > 0);
        return nodes.item(0).getTextContent().trim();
    }

    /**
     * The directory part of a log4j2 file name, keeping property lookups such as
     * {@code ${sys:log_dir:-${logs_dir}}} verbatim so that two appenders are only considered
     * co-located when they resolve identically at runtime.
     */
    private static String directoryOf(String fileName) {
        int separator = fileName.lastIndexOf('/');
        return separator < 0 ? "" : fileName.substring(0, separator);
    }
}
