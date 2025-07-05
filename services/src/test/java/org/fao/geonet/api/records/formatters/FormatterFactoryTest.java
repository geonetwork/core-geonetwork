/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FormatterFactoryTest extends AbstractServiceIntegrationTest {
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    @Test
    public void getExistentDcatFormatter() {
        File file = Mockito.mock(File.class);
        Mockito.when(file.exists())
            .thenReturn(true);

        FormatterImpl dcatFormatter = FormatterFactory.getFormatter(context, file, "dcat");
        assertTrue(dcatFormatter instanceof DcatFormatter);
    }

    @Test
    public void getExistentXsltFormatter() {
        File file = Mockito.mock(File.class);
        Mockito.when(file.exists())
            .thenReturn(true);

        FormatterImpl xsltFormatter = FormatterFactory.getFormatter(context, file, "fullview");
        assertTrue(xsltFormatter instanceof XsltFormatter);
    }

    @Test
    public void getNonExistentFormatter() {
        File file = Mockito.mock(File.class);
        Mockito.when(file.exists())
            .thenReturn(false);

        try {
            FormatterFactory.getFormatter(context, file, "fullview");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
