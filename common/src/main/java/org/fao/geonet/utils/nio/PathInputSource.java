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

package org.fao.geonet.utils.nio;

import org.fao.geonet.Constants;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * An input source backed by a java nio path.
 *
 * @author Jesse on 1/20/2015.
 */
public class PathInputSource extends InputSource {
    private final PathSourceMixin pathSourceMixin;

    public PathInputSource(Path resource) {
        this.pathSourceMixin = new PathSourceMixin(resource);
    }

    @Override
    public InputStream getByteStream() {
        return this.pathSourceMixin.getInputStream();
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream() {
        final Charset cs;
        if (getEncoding() != null) {
            cs = Charset.forName(getEncoding());
        } else {
            cs = Constants.CHARSET;
        }
        return this.pathSourceMixin.getReader(cs);
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
        throw new UnsupportedOperationException();
    }
}
