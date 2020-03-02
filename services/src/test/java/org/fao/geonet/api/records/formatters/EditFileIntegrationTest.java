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

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

import jeeves.server.context.ServiceContext;

public class EditFileIntegrationTest extends AbstractServiceIntegrationTest {

    @Test(expected = BadParameterEx.class)
    public void testExecBadFnameParameter() throws Exception {
        EditFile ef = new EditFile();
        ServiceContext ctx = createServiceContext();

        Element params = Xml.loadString(
            "<request><id>test</id><_content_type>json</_content_type>"
                + "<fname>myfile</fname></request>", false);

        ef.exec(params, ctx);
    }

}
