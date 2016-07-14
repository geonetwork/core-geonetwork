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

package org.fao.geonet.utils.debug;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A useful class for detecting unclosed input streams.  It keeps track of all the open input
 * streams and provides a way to write the stacktrace where each stream was created to a file to
 * allow debugging.
 *
 * @author Jesse on 1/17/2015.
 */
public class DebuggingInputStream extends FilterInputStream {
    private final OpenResourceTracker exception = new OpenResourceTracker();
    private final String descriptor;

    public DebuggingInputStream(String descriptor, InputStream in) throws IOException {
        super(in);
        this.descriptor = descriptor;
        OpenResourceTracker.open(descriptor, exception);
    }

    @Override
    public void close() throws IOException {
        super.close();
        OpenResourceTracker.close(descriptor, exception);
    }

}
