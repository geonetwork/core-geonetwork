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

package org.fao.geonet.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.fao.geonet.Constants;

/**
 * Allows OutputStream to be mapped to StringBuffer.
 * <p/>
 * Smould IMHO be in Java SDK but somehow isn't.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class StringBufferOutputStream extends OutputStream {
    private StringBuffer strBuffer;
    private boolean closed = false;

    public StringBufferOutputStream(StringBuffer strBuffer) {
        super();
        this.strBuffer = strBuffer;
    }

    /**
     * method to write a char
     */
    public void write(int i) throws IOException {
        if (closed) {
            return;
        }

        strBuffer.append((char) i);
    }

    /**
     * write an array of bytes
     */
    public void write(byte[] b, int offset, int length)
        throws IOException {
        if (closed) {
            return;
        }

        if (b == null) {
            throw new NullPointerException("The byte array is null");
        }
        if (offset < 0 || length < 0 || (offset + length) > b.length) {
            throw new IndexOutOfBoundsException("offset and length are negative or extend outside array bounds");
        }

        String str = new String(b, offset, length, Charset.forName(Constants.ENCODING));
        strBuffer.append(str);
    }

    public void close() {
        strBuffer = null;
        closed = true;
    }

}
