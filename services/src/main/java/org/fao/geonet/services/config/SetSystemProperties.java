/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.services.config;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.util.ThreadPool;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Configure geonetwork's thread pool to not add tasks to thread pool but instead execute them in
 * calling thread.
 *
 * Required for integration tests
 *
 * User: jeichar Date: 1/19/12 Time: 9:01 PM
 */
public class SetSystemProperties implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
        // empty
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        final String propertyName = Util.getParam(params, "name");
        final boolean newValue = Boolean.parseBoolean(Util.getParam(params, "value"));
        if (propertyName.equalsIgnoreCase(ThreadPool.SEQUENTIAL_EXECUTION)) {
            System.setProperty(ThreadPool.SEQUENTIAL_EXECUTION, Boolean.toString(newValue));
        } else if (propertyName.equalsIgnoreCase(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD)) {
            System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(newValue));
        } else {
            throw new IllegalArgumentException("system property: " + propertyName + " is not permitted to be set via web API");
        }
        return new Element("response").setText("ok");
    }
}
