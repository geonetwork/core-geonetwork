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

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ListBundleFilesTest {

    @Test
    public void testIsEditibleFileType() {
        Path p = Mockito.mock(Path.class);
        Mockito.when(p.getFileName()).thenReturn(p);
        // extensions = {"properties", "xml", "xsl", "css", "js"};
        Mockito.when(p.toString()).thenReturn("file.properties", "manifest.xml", "view.xsl", "custom.css", "custom.js", "README");

        ListBundleFiles lst = new ListBundleFiles();
        Method prvMeth = ReflectionUtils.findMethod(lst.getClass(), "isEditibleFileType", Path.class);
        prvMeth.setAccessible(true);

        Boolean[] rets = new Boolean[6];
        for (int i = 0; i < 6; ++i) {
            rets[i] = (Boolean) ReflectionUtils.invokeMethod(prvMeth, lst, p);

        }
        assertTrue("isEditibleFileType(\"file.properties\"): Expected true, false returned", rets[0]);
        assertTrue("isEditibleFileType(\"manifest.xml\"): Expected true, false returned", rets[1]);
        assertTrue("isEditibleFileType(\"view.xsl\"): Expected true, false returned", rets[2]);
        assertTrue("isEditibleFileType(\"custom.css\"): Expected true, false returned", rets[3]);
        assertTrue("isEditibleFileType(\"custom.js\"): Expected true, false returned", rets[4]);
        assertTrue("isEditibleFileType(\"README\"): Expected false, true returned", rets[5]);

    }

}
