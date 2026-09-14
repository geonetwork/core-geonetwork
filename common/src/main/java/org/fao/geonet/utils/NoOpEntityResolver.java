/*
 * Copyright (C) 2001-2018 Food and Agriculture Organization of the
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

import org.xml.sax.InputSource;
import org.xml.sax.ext.EntityResolver2;

import java.io.StringReader;

public class NoOpEntityResolver implements EntityResolver2 {
    @Override
    public InputSource getExternalSubset(String name, String baseURI) {
        // Return null (not an empty source) so a document without a DOCTYPE is
        // parsed exactly as before this class became an EntityResolver2: an empty
        // external subset would otherwise attach a synthetic DOCTYPE to every such
        // document. No external fetch happens either way.
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return emptyInputSource();
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) {
        return emptyInputSource();
    }

    private InputSource emptyInputSource() {
        return new InputSource(new StringReader(""));
    }
}
