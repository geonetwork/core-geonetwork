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

/**
 * EntityResolver2 returning empty content for any external entity.
 */
public class NoOpEntityResolver implements EntityResolver2 {

    /**
     * Singleton instance of NoOpEntityResolver.
     */
    public static final NoOpEntityResolver INSTANCE = new NoOpEntityResolver();

    /**
     * Use the INSTANCE field to access.
     */
    protected NoOpEntityResolver(){
    }
    
    @Override
    public InputSource getExternalSubset(String name, String baseURI) {
        return null; // no external subset
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return emptyInputSource();
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) {
        return emptyInputSource();
    }

    /**
     * Empty InputSource, used to prevent download of external entities.
     *
     * @return an empty InputSource
     */
    private InputSource emptyInputSource() {
        return new InputSource(new StringReader(""));
    }

    @Override
    public String toString() {
        return "NoOpEntityResolver";
    }
}
