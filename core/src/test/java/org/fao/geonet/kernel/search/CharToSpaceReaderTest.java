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

package org.fao.geonet.kernel.search;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;

public class CharToSpaceReaderTest {

    String source = "h-w how/are ( you?";
    String expected = "h w how are   you?";
    char[] chars = "-/(".toCharArray();

    @Test
    public void testRead() throws IOException {

        Reader reader = new StringReader(source);
        CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
        try {
            for (int i = 0; i < expected.length(); i++) {
                char c = expected.charAt(i);
                char read = (char) mapReader.read();
                assertEquals("Expected to get " + c + " at position " + i + " but got " + read, c, read);
            }

            assertEquals(-1, mapReader.read());
        } finally {
            mapReader.close();
        }
    }

    @Test
    public void testReadCharArrayIntInt() throws IOException {
        Reader reader = new StringReader(source);
        CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
        try {
            char[] read = new char[expected.length() * 2];
            int numRead = mapReader.read(read, 2, expected.length() + 2);

            assertEquals(expected.length(), numRead);

            for (int i = 2; i < expected.length(); i++) {
                assertEquals(expected.charAt(i - 2), read[i]);
            }
        } finally {
            mapReader.close();
        }

    }

    @Test
    public void testReadCharBuffer() throws IOException {
        Reader reader = new StringReader(source);
        CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
        try {
            CharBuffer buffer = CharBuffer.allocate(expected.length());
            int numRead = mapReader.read(buffer);

            assertEquals(expected.length(), numRead);

            for (int i = 0; i < expected.toCharArray().length; i++) {
                assertEquals(expected.charAt(i), buffer.get(i));
            }
        } finally {
            mapReader.close();
        }

    }

    @Test
    public void testReadCharArray() throws IOException {
        Reader reader = new StringReader(source);
        CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
        try {
            char[] buffer = new char[expected.length()];
            int numRead = mapReader.read(buffer);

            assertEquals(expected.length(), numRead);

            for (int i = 0; i < expected.length(); i++) {
                assertEquals(expected.charAt(i), buffer[i]);
            }
        } finally {
            mapReader.close();
        }

    }

}
