//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.fao.geonet.api.exception.InputStreamLimitExceededException;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Tests enforcement of the configured byte limit while reading an input
 * stream.
 */
public class LimitedInputStreamTest {

    @Test
    public void exposesKnownFileSize() {
        LimitedInputStream input =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[5]),
                10,
                5
            );

        assertEquals(5, input.getFileSize());
        assertEquals(5, input.getKnownSize());
    }

    @Test
    public void reportsUnknownSizeWhenNotProvided() {
        LimitedInputStream input =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[5]),
                10
            );

        assertEquals(-1, input.getFileSize());
        assertEquals(-1, input.getKnownSize());
    }

    @Test
    public void permitsStreamAtConfiguredLimit() throws Exception {
        LimitedInputStream input =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[5]),
                5,
                5
            );

        byte[] data = new byte[5];

        assertEquals(5, input.read(data));
        assertEquals(-1, input.read());
    }

    @Test
    public void rejectsStreamThatExceedsConfiguredLimit() throws Exception {
        LimitedInputStream input =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[6]),
                5,
                -1
            );

        byte[] data = new byte[6];

        assertThrows(
            InputStreamLimitExceededException.class,
            () -> {
                while (input.read(data) != -1) {
                    // Consume until the limit is exceeded.
                }
            }
        );
    }
}
