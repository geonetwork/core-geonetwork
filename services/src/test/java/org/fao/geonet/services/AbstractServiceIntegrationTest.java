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

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Adds extra bean required for services tests.
 *
 * User: Jesse Date: 10/17/13 Time: 9:53 AM
 */
@WebAppConfiguration(value = "/src/test/resources")
@ContextConfiguration(inheritLocations = true,
    locations = {"classpath:services-repository-test-context.xml", "classpath:services-web-test-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractServiceIntegrationTest extends AbstractCoreIntegrationTest {
    public static final String API_JSON_EXPECTED_ENCODING = "application/json";
    public static final String API_XML_EXPECTED_ENCODING = "application/xml";
    public static final String API_PNG_EXPECTED_ENCODING = "image/png";
}
