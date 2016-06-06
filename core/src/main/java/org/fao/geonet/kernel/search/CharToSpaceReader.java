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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

import bak.pcj.set.CharOpenHashSet;

/**
 * Maps a set characters from one to a space.
 *
 * The purpose is to effectively ignore certain characters when analyzing input for lucene. Since
 * spaces are used to split tokens by Standard tokenizers, this reader will convert the set of input
 * characters to spaces so that they will be effectively ignored.
 *
 * @author jeichar
 */
class CharToSpaceReader extends FilterReader {

    char space = ' ';
    private CharOpenHashSet set;

    /**
     * @param charsToSetAsSpaces characters to convert to spaces
     */
    public CharToSpaceReader(Reader reader, char[] charsToSetAsSpaces) {
        super(reader);
        this.set = new CharOpenHashSet(charsToSetAsSpaces);
    }

    @Override
    public int read(char[] in, int start, int end) throws IOException {
        int read = super.read(in, start, end);
        for (int i = start; i < read; i++) {
            if (set.contains(in[i])) {
                in[i] = space;
            }
        }
        return read;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        char asChar = (char) read;
        if (asChar == read && set.contains(asChar)) return ' ';
        else return read;
    }

}
