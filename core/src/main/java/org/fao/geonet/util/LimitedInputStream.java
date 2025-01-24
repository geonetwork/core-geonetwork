//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.fao.geonet.api.exception.InputStreamLimitExceededException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link org.apache.commons.fileupload.util.LimitedInputStream} that throws a
 * {@link InputStreamLimitExceededException} when the configured limit is exceeded.
 */
public class LimitedInputStream extends org.apache.commons.fileupload.util.LimitedInputStream {

    /**
     * The size of the file being uploaded if known.
     */
    long fileSize = -1;


    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax    The limit; no more than this number of bytes
     *                    shall be returned by the source stream.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream, pSizeMax);
    }

    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax    The limit; no more than this number of bytes
     *                    shall be returned by the source stream.
     * @param fileSize    The size of the file being uploaded.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax, long fileSize) {
        super(inputStream, pSizeMax);
        this.fileSize = fileSize;
    }

    @Override
    protected void raiseError(long pSizeMax, long pCount) throws IOException {
        throw new InputStreamLimitExceededException(pSizeMax);
    }

    public long getFileSize() {
        return fileSize;
    }
}
