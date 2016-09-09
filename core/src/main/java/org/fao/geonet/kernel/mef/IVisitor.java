//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.mef;

import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Register XML or binary file to process.
 */
public interface IVisitor {
    /**
     * Register XML or binary file to process.
     */
    public void visit(Path mefFile, IMEFVisitor v) throws Exception;

    /**
     * Register XML files.
     */
    public Element handleXml(Path mefFile, IMEFVisitor v) throws Exception;

    /**
     * Register binary file.
     *
     * @param mefFile a {@link File} to process
     * @param v       the visitor
     * @param info    the information file
     * @param index   the index to store the results
     */
    public void handleBin(Path mefFile, IMEFVisitor v, Element info, int index) throws Exception;

}

// =============================================================================

class InputStreamBridge extends InputStream {
    // --------------------------------------------------------------------------
    // ---
    // --- Constructor
    // ---
    // --------------------------------------------------------------------------

    private InputStream is;

    // --------------------------------------------------------------------------
    // ---
    // --- Bridging methods
    // ---
    // --------------------------------------------------------------------------

    public InputStreamBridge(InputStream is) {
        this.is = is;
    }

    public int read() throws IOException {
        return is.read();
    }

    public int available() throws IOException {
        return is.available();
    }

    // --- this *must* be empty to work with zip files
    public void close() throws IOException {
    }

    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        is.reset();
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Variables
    // ---
    // --------------------------------------------------------------------------

    public boolean markSupported() {
        return is.markSupported();
    }
}
// =============================================================================

