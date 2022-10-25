/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.utils;

import org.apache.logging.log4j.LogManager;
import org.fao.geonet.Logger;
import org.junit.Test;

public class LogTest {

    @Test
    public final void modules() throws Exception {
        Logger jeevesLogger = Log.createLogger(Log.JEEVES);
        Logger engineLogger = Log.createLogger(Log.ENGINE);

        jeevesLogger.info("jeeves is ready");
        jeevesLogger.error("jeeves is troubled");
        engineLogger.info("engine is ready");
        engineLogger.error("engine is troubled");
    }
    @Test
    public final void inferred() throws Exception {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(LogTest.class);

        Logger geonetworkLogger = Log.createLogger("geonetwork");
        Logger harvesterLogger = Log.createLogger("geonetwork.harvester");

        logger.info("test starting");
        geonetworkLogger.info("geonetwork is ready");
        geonetworkLogger.error("geonetwork is troubled");
        harvesterLogger.info("harvester is ready");
        harvesterLogger.error("harvester is troubled");
    }
    @Test
    public final void markers() throws Exception {
        Logger jeevesLogger = Log.createLogger(LogTest.class,Log.JEEVES_MARKER);
        Logger engineLogger = Log.createLogger(LogTest.class,Log.ENGINE_MARKER);

        jeevesLogger.info("jeeves is ready");
        jeevesLogger.error("jeeves is troubled");
        engineLogger.info("engine is ready");
        engineLogger.error("engine is troubled");
    }
}
