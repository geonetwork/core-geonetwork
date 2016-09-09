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

package org.fao.geonet.services;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.logging.LogFactory;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Base class for services that should not run their normal execution path if GeoNetwork is in
 * read-only mode.
 *
 * @author heikki doeleman
 */
public abstract class NotInReadOnlyModeService extends MailSendingService {
    private org.apache.commons.logging.Log log = LogFactory.getLog(NotInReadOnlyModeService.class);

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        // READONLYMODE
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        if (!gc.isReadOnly()) {
            return serviceSpecificExec(params, context);
        } else {
            log.debug("GeoNetwork is operating in read-only mode. Service execution skipped.");
            return null;
        }
    }

    /**
     * Contains the code for normal execution, when GeoNetwork is not in read-only mode.
     */
    public abstract Element serviceSpecificExec(Element params, ServiceContext context) throws Exception;
}
