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

package org.fao.geonet.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;

import java.nio.file.Path;

/**
 * @author heikki doeleman
 */
public abstract class ShowViewBaseService implements Service {
    protected boolean skipPopularity;
    protected boolean skipInfo;
    protected boolean addRefs;

    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        String skip;

        skip = params.getValue("skipPopularity", "n");
        skipPopularity = skip.equals("y");

        skip = params.getValue("skipInfo", "n");
        skipInfo = skip.equals("y");

        skip = params.getValue("addRefs", "n");
        addRefs = skip.equals("y");
    }

}
